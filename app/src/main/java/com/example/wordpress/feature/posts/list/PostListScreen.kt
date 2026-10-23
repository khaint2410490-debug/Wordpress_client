package com.example.wordpressclient.feature.posts.list


import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.List
import androidx.compose.material3.Card
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.pulltorefresh.PullToRefreshBox
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.layout.ContentScale
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import coil.compose.AsyncImage
import com.example.wordpressclient.core.ui.BookmarkButton
import com.example.wordpressclient.core.ui.ErrorRetryMessage
import com.example.wordpressclient.core.ui.toVietnameseDate
import com.example.wordpressclient.domain.model.Post


/**
 * PostListScreen = màn hình danh sách bài viết (bản "có trạng thái").
 *
 * Việc của nó: lấy ViewModel từ Hilt, đọc state, rồi giao cho PostListContent vẽ.
 *
 * @param onPostClick Gọi khi người dùng bấm vào một bài, truyền ID bài đó.
 *                    Việc chuyển sang màn hình chi tiết sẽ do AppNavHost quyết định.
 * @param onSearchClick Gọi khi người dùng mở màn hình tìm kiếm.
 */
@Composable
fun PostListScreen(
    onPostClick: (Int) -> Unit,
    onSearchClick: () -> Unit,
    title: String = "Bài viết",
    onCategoriesClick: (() -> Unit)? = null,
    onBackClick: (() -> Unit)? = null,
    viewModel: PostListViewModel = hiltViewModel()
) {
    // collectAsStateWithLifecycle: biến StateFlow thành state của Compose
    // và tự dừng thu thập khi màn hình không còn hiển thị.
    val state by viewModel.uiState.collectAsStateWithLifecycle()

    PostListContent(
        state = state,
        onPostClick = onPostClick,
        onSearchClick = onSearchClick,
        onLoadMore = viewModel::loadNextPage,
        onRetry = viewModel::retry,
        onRefresh = viewModel::refresh,
        onBookmarkClick = viewModel::toggleSaved
    )
}

/**
 * PostListContent = phần giao diện thuần, chỉ nhận state và vẽ ra.
 * Tách riêng để dễ xem trước (Preview) và kiểm thử, vì không phụ thuộc ViewModel hay Hilt.
 */
@Composable
@OptIn(ExperimentalMaterial3Api::class)
private fun PostListContent(
    state: PostListUiState,
    onPostClick: (Int) -> Unit,
    onSearchClick: () -> Unit,
    onLoadMore: () -> Unit,
    title: String = "Bài viết",
    onCategoriesClick: (() -> Unit)? = null,
    onBackClick: (() -> Unit)? = null,
    onRetry: () -> Unit,
    onRefresh: () -> Unit,
    onBookmarkClick: (Post) -> Unit
) {
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(title) },
                // Chỉ có nút quay lại khi được truyền vào (màn hình bài theo danh mục).
                navigationIcon = {
                    if (onBackClick != null) {
                        IconButton(onClick = onBackClick) {
                            Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Quay lại")
                        }
                    }
                },
                actions = {
                    // Chỉ có nút danh mục ở danh sách chung.
                    if (onCategoriesClick != null) {
                        IconButton(onClick = onCategoriesClick) {
                            Icon(Icons.AutoMirrored.Filled.List, contentDescription = "Danh mục")
                        }
                    }
                    IconButton(onClick = onSearchClick) {
                        Icon(Icons.Default.Search, contentDescription = "Tìm kiếm")
                    }
                }
            )
        }
    ) { innerPadding ->
        PullToRefreshBox(
            isRefreshing = state.isRefreshing,
            onRefresh = onRefresh,
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
                    ErrorRetryMessage(
                        message = state.errorMessage,
                        onRetry = onRetry,
                        modifier = Modifier.align(Alignment.Center)
                    )
                }

                // Tải xong mà không có bài nào (ví dụ danh mục chưa có bài).
                state.posts.isEmpty() && !state.isLoading && !state.isRefreshing -> {
                    Text(
                        text = "Chưa có bài viết nào.",
                        modifier = Modifier.align(Alignment.Center)
                    )
                }

                // 3. Có dữ liệu: hiện danh sách.
                else -> PostList(
                    state = state,
                    onPostClick = onPostClick,
                    onLoadMore = onLoadMore,
                    onRetry = onRetry,
                    onBookmarkClick = onBookmarkClick
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
    onRetry: () -> Unit,
    onBookmarkClick: (Post) -> Unit
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
            PostItem(
                post = post,
                isSaved = post.id in state.savedPostIds,
                onClick = { onPostClick(post.id) },
                onBookmarkClick = { onBookmarkClick(post) }
            )
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
                    ErrorRetryMessage(
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
private fun PostItem(
    post: Post,
    isSaved: Boolean,
    onClick: () -> Unit,
    onBookmarkClick: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
    ) {
        Row(
            modifier = Modifier.padding(16.dp),
            horizontalArrangement = Arrangement.spacedBy(12.dp),
            verticalAlignment = Alignment.Top
        ) {
            Column(Modifier.weight(1f)) {
                Text(
                    text = post.title,
                    style = MaterialTheme.typography.titleMedium,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis,
                )

                if (post.authorName != null) {
                    Row(
                        modifier = Modifier.padding(top = 8.dp),
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        post.authorAvatarUrl?.let { avatarUrl ->
                            AsyncImage(
                                model = avatarUrl,
                                contentDescription = "Ảnh đại diện ${post.authorName}",
                                contentScale = ContentScale.Crop,
                                modifier = Modifier
                                    .size(28.dp)
                                    .clip(CircleShape)
                            )
                        }
                        Text(
                            text = post.authorName,
                            style = MaterialTheme.typography.labelMedium,
                            color = MaterialTheme.colorScheme.secondary,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                    }
                }

                Text(
                    text = post.date.toVietnameseDate(),
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.secondary,
                    modifier = Modifier.padding(top = 4.dp)
                )

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

            post.featuredImageUrl?.let { imageUrl ->
                AsyncImage(
                    model = imageUrl,
                    contentDescription = "Ảnh đại diện bài viết ${post.title}",
                    contentScale = ContentScale.Crop,
                    modifier = Modifier
                        .size(width = 96.dp, height = 112.dp)
                        .clip(RoundedCornerShape(8.dp))
                )
            }
            BookmarkButton(isSaved = isSaved, onClick = onBookmarkClick)
        }
    }
}
