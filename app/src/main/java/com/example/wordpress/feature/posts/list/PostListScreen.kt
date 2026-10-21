package com.example.wordpressclient.feature.posts.list

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.wordpressclient.domain.model.Post

/**
 * PostListScreen = màn hình danh sách bài viết (bản "có trạng thái").
 *
 * Việc của nó: lấy ViewModel từ Hilt, đọc state, rồi giao cho PostListContent vẽ.
 *
 * @param onPostClick Gọi khi người dùng bấm vào một bài, truyền ID bài đó.
 *                    Việc chuyển sang màn hình chi tiết sẽ do AppNavHost quyết định.
 */
@Composable
fun PostListScreen(
    onPostClick: (Int) -> Unit,
    viewModel: PostListViewModel = hiltViewModel()
) {
    // collectAsStateWithLifecycle: biến StateFlow thành state của Compose
    // và tự dừng thu thập khi màn hình không còn hiển thị.
    val state by viewModel.uiState.collectAsStateWithLifecycle()

    PostListContent(
        state = state,
        onPostClick = onPostClick,
        onLoadMore = viewModel::loadNextPage,
        onRetry = viewModel::retry
    )
}

/**
 * PostListContent = phần giao diện thuần, chỉ nhận state và vẽ ra.
 * Tách riêng để dễ xem trước (Preview) và kiểm thử, vì không phụ thuộc ViewModel hay Hilt.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun PostListContent(
    state: PostListUiState,
    onPostClick: (Int) -> Unit,
    onLoadMore: () -> Unit,
    onRetry: () -> Unit
) {
    Scaffold(
        topBar = { TopAppBar(title = { Text("Bài viết") }) }
    ) { innerPadding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            when {
                // 1. Đang tải trang đầu: vòng xoay giữa màn hình.
                state.isLoading && state.posts.isEmpty() -> {
                    CircularProgressIndicator(Modifier.align(Alignment.Center))
                }

                // 2. Lỗi khi chưa có bài nào: thông báo giữa màn hình kèm nút thử lại.
                state.errorMessage != null && state.posts.isEmpty() -> {
                    ErrorMessage(
                        message = state.errorMessage,
                        onRetry = onRetry,
                        modifier = Modifier.align(Alignment.Center)
                    )
                }

                // 3. Có dữ liệu: hiện danh sách.
                else -> PostList(
                    state = state,
                    onPostClick = onPostClick,
                    onLoadMore = onLoadMore,
                    onRetry = onRetry
                )
            }
        }
    }
}

/** Danh sách cuộn, tự tải thêm khi gần chạm cuối. */
@Composable
private fun PostList(
    state: PostListUiState,
    onPostClick: (Int) -> Unit,
    onLoadMore: () -> Unit,
    onRetry: () -> Unit
) {
    val listState = rememberLazyListState()

    // Kiểm tra mỗi khi vị trí cuộn đổi: true nếu đã gần cuối danh sách (còn 3 mục).
    val nearEnd by remember {
        derivedStateOf {
            val info = listState.layoutInfo
            val lastVisible = info.visibleItemsInfo.lastOrNull()?.index ?: 0
            info.totalItemsCount > 0 && lastVisible >= info.totalItemsCount - 3
        }
    }

    // Khi nearEnd chuyển sang true thì yêu cầu ViewModel tải trang tiếp theo.
    // ViewModel tự bỏ qua nếu đang tải hoặc đã hết bài.
    LaunchedEffect(nearEnd) {
        if (nearEnd) onLoadMore()
    }

    LazyColumn(
        state = listState,
        modifier = Modifier.fillMaxSize(),
        contentPadding = androidx.compose.foundation.layout.PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        // key = id giúp Compose nhận ra đúng từng mục khi danh sách thay đổi.
        items(items = state.posts, key = { it.id }) { post ->
            PostItem(post = post, onClick = { onPostClick(post.id) })
        }

        // Chân danh sách: vòng xoay khi đang tải thêm, hoặc lỗi kèm nút thử lại.
        item {
            when {
                state.isLoadingMore -> {
                    Box(Modifier.fillMaxWidth().padding(16.dp), Alignment.Center) {
                        CircularProgressIndicator()
                    }
                }
                state.errorMessage != null -> {
                    ErrorMessage(
                        message = state.errorMessage,
                        onRetry = onRetry,
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            }
        }
    }
}

/** Một thẻ bài viết trong danh sách. */
@Composable
private fun PostItem(post: Post, onClick: () -> Unit) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
    ) {
        Column(Modifier.padding(16.dp)) {
            // Tiêu đề: tối đa 2 dòng, dài hơn thì hiện "...".
            Text(
                text = post.title,
                style = MaterialTheme.typography.titleMedium,
                maxLines = 2,
                overflow = TextOverflow.Ellipsis
            )

            // Ngày đăng: tạm cắt 10 ký tự đầu của chuỗi ISO, ví dụ "2024-05-01".
            Text(
                text = post.date.take(10),
                style = MaterialTheme.typography.labelMedium,
                color = MaterialTheme.colorScheme.secondary,
                modifier = Modifier.padding(top = 4.dp)
            )

            // Đoạn tóm tắt: tối đa 3 dòng. Bỏ qua nếu bài không có tóm tắt.
            if (post.excerpt.isNotBlank()) {
                Text(
                    text = post.excerpt,
                    style = MaterialTheme.typography.bodyLarge,
                    maxLines = 3,
                    overflow = TextOverflow.Ellipsis,
                    modifier = Modifier.padding(top = 8.dp)
                )
            }
        }
    }
}

/** Thông báo lỗi kèm nút thử lại, dùng cho cả lỗi toàn màn hình lẫn lỗi ở chân danh sách. */
@Composable
private fun ErrorMessage(
    message: String,
    onRetry: () -> Unit,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier.padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        Text(text = message, color = MaterialTheme.colorScheme.error)
        Button(onClick = onRetry) { Text("Thử lại") }
    }
}