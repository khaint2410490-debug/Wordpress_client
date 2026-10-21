package com.example.wordpressclient.feature.search

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.Button
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.ListItem
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.wordpressclient.domain.model.Post

/**
 * SearchScreen = màn hình tìm kiếm bài viết (bản "có trạng thái").
 *
 * Lấy ViewModel từ Hilt, đọc state rồi giao cho SearchContent vẽ.
 *
 * @param onPostClick Gọi khi người dùng bấm vào một kết quả, truyền ID bài đó.
 */
@Composable
fun SearchScreen(
    onPostClick: (Int) -> Unit,
    viewModel: SearchViewModel = hiltViewModel()
) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()

    SearchContent(
        state = state,
        onQueryChange = viewModel::onQueryChange,
        onSearch = viewModel::searchNow,
        onPostClick = onPostClick
    )
}

/** Phần giao diện thuần: chỉ nhận state và vẽ ra, không phụ thuộc ViewModel hay Hilt. */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun SearchContent(
    state: SearchUiState,
    onQueryChange: (String) -> Unit,
    onSearch: () -> Unit,
    onPostClick: (Int) -> Unit
) {
    Scaffold(
        topBar = { TopAppBar(title = { Text("Tìm kiếm") }) }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            // Ô nhập từ khóa.
            OutlinedTextField(
                value = state.query,
                onValueChange = onQueryChange,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 8.dp),
                placeholder = { Text("Tìm bài viết...") },
                leadingIcon = { Icon(Icons.Default.Search, contentDescription = null) },
                // Nút xóa nhanh, chỉ hiện khi đã có chữ.
                trailingIcon = {
                    if (state.query.isNotEmpty()) {
                        IconButton(onClick = { onQueryChange("") }) {
                            Icon(Icons.Default.Close, contentDescription = "Xóa từ khóa")
                        }
                    }
                },
                singleLine = true,
                // Đổi phím Enter của bàn phím thành nút "Tìm", bấm vào sẽ tìm ngay.
                keyboardOptions = KeyboardOptions(imeAction = ImeAction.Search),
                keyboardActions = KeyboardActions(onSearch = { onSearch() })
            )

            // Thanh tiến trình mỏng khi đang tìm. Giữ chỗ cố định để nội dung không bị nhảy.
            if (state.isLoading) {
                LinearProgressIndicator(Modifier.fillMaxWidth())
            }

            Box(Modifier.fillMaxSize()) {
                when {
                    // Lỗi: thông báo kèm nút thử lại.
                    state.errorMessage != null -> {
                        Column(
                            modifier = Modifier
                                .align(Alignment.Center)
                                .padding(16.dp),
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            Text(text = state.errorMessage, color = MaterialTheme.colorScheme.error)
                            Button(onClick = onSearch) { Text("Thử lại") }
                        }
                    }

                    // Có kết quả: hiện danh sách.
                    state.results.isNotEmpty() -> {
                        LazyColumn(Modifier.fillMaxSize()) {
                            // key = id giúp Compose nhận ra đúng từng mục khi danh sách thay đổi.
                            items(items = state.results, key = { it.id }) { post ->
                                SearchResultItem(post = post, onClick = { onPostClick(post.id) })
                            }
                        }
                    }

                    // Đang tìm mà chưa có kết quả nào: để trống, thanh tiến trình ở trên đã báo.
                    state.isLoading -> Unit

                    // Đã tìm xong nhưng không có bài nào khớp.
                    state.hasSearched -> {
                        Text(
                            text = "Không tìm thấy bài viết nào cho \"${state.query.trim()}\".",
                            modifier = Modifier
                                .align(Alignment.Center)
                                .padding(16.dp)
                        )
                    }

                    // Chưa tìm gì: gợi ý nhập từ khóa.
                    else -> {
                        Text(
                            text = "Nhập từ khóa để tìm bài viết.",
                            modifier = Modifier
                                .align(Alignment.Center)
                                .padding(16.dp)
                        )
                    }
                }
            }
        }
    }
}

/** Một dòng kết quả: tiêu đề và đoạn tóm tắt ngắn. */
@Composable
private fun SearchResultItem(post: Post, onClick: () -> Unit) {
    ListItem(
        headlineContent = {
            Text(text = post.title, maxLines = 2, overflow = TextOverflow.Ellipsis)
        },
        supportingContent = {
            // Chỉ hiện tóm tắt nếu bài có.
            if (post.excerpt.isNotBlank()) {
                Text(text = post.excerpt, maxLines = 2, overflow = TextOverflow.Ellipsis)
            }
        },
        modifier = Modifier.clickable(onClick = onClick)
    )
}