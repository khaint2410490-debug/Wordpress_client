package com.example.wordpressclient.feature.categories

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.ListItem
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.wordpressclient.core.common.UiState
import com.example.wordpressclient.core.ui.ErrorRetryMessage
import com.example.wordpressclient.domain.model.Category

/**
 * CategoryScreen = màn hình danh sách danh mục (bản "có trạng thái").
 *
 * Lấy ViewModel từ Hilt, đọc state rồi giao cho CategoryContent vẽ.
 *
 * @param onCategoryClick Gọi khi người dùng bấm vào một danh mục, truyền cả danh mục đó.
 *                        Chuyển tới đâu (ví dụ danh sách bài của danh mục) do AppNavHost quyết định.
 */
@Composable
fun CategoryScreen(
    onCategoryClick: (Category) -> Unit,
    onBackClick: () -> Unit,
    viewModel: CategoryViewModel = hiltViewModel()
) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()

    CategoryContent(
        state = state,
        onCategoryClick = onCategoryClick,
        onRetry = viewModel::loadCategories,
        onBackClick = onBackClick
    )
}

/** Phần giao diện thuần: chỉ nhận state và vẽ ra, không phụ thuộc ViewModel hay Hilt. */
@Composable
@OptIn(ExperimentalMaterial3Api::class)
private fun CategoryContent(
    state: UiState<List<Category>>,
    onCategoryClick: (Category) -> Unit,
    onRetry: () -> Unit,
    onBackClick: () -> Unit
) {
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Danh mục") },
                navigationIcon = {
                    IconButton(onClick = onBackClick) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Quay lại"
                        )
                    }
                }
            )
        }
    ) { innerPadding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            when (state) {
                // Đang tải: vòng xoay giữa màn hình.
                is UiState.Loading -> {
                    CircularProgressIndicator(Modifier.align(Alignment.Center))
                }

                // Lỗi: thông báo kèm nút thử lại.
                is UiState.Error -> {
                    ErrorRetryMessage(
                        message = state.message,
                        onRetry = onRetry,
                        modifier = Modifier.align(Alignment.Center)
                    )
                }

                // Có dữ liệu: hiện danh sách, hoặc thông báo nếu rỗng.
                is UiState.Success -> {
                    if (state.data.isEmpty()) {
                        Text(
                            text = "Chưa có danh mục nào.",
                            modifier = Modifier.align(Alignment.Center)
                        )
                    } else {
                        LazyColumn(Modifier.fillMaxSize()) {
                            // key = id giúp Compose nhận ra đúng từng mục khi danh sách thay đổi.
                            items(items = state.data, key = { it.id }) { category ->
                                ListItem(
                                    headlineContent = { Text(category.name) },
                                    supportingContent = { Text("${category.count} bài viết") },
                                    modifier = Modifier.clickable { onCategoryClick(category) }
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}