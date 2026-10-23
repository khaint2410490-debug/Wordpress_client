package com.example.wordpressclient.feature.posts.list

import com.example.wordpressclient.domain.model.Post

/**
 * PostListUiState = toàn bộ trạng thái của màn hình danh sách bài viết.
 *
 * Tôi dùng data class thay vì UiState<List<Post>> chung, vì danh sách có phân trang cần
 * thể hiện đồng thời nhiều thứ: danh sách đang hiện, đang tải thêm, lỗi, đã hết bài.
 *
 * Ví dụ: đang xem 20 bài và tải trang 3 bị lỗi thì
 * posts vẫn còn 20 bài, errorMessage có nội dung, màn hình không bị xóa trắng.
 */
data class PostListUiState(

    // Các bài viết đã tải được, nối dần theo từng trang.
    val posts: List<Post> = emptyList(),

    // true khi đang tải trang đầu tiên (hiện vòng xoay giữa màn hình).
    // Mặc định false; loadNextPage() sẽ tự bật lên true khi bắt đầu tải.
    val isLoading: Boolean = false,

    // true khi đang tải các trang sau (hiện vòng xoay nhỏ ở cuối danh sách).
    val isLoadingMore: Boolean = false,

    // Thông báo lỗi gần nhất, null nếu không có lỗi.
    val errorMessage: String? = null,

    // true khi server không còn bài nào để tải thêm.
    val endReached: Boolean = false,

    // true khi người dùng đang kéo để làm mới (hiện vòng xoay ở đầu danh sách).
    val isRefreshing: Boolean = false
)