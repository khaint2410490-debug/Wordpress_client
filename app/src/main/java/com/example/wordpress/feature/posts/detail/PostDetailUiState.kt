package com.example.wordpressclient.feature.posts.detail

import com.example.wordpressclient.core.common.UiState
import com.example.wordpressclient.domain.model.Post

/**
 * PostDetailUiState = trạng thái của màn hình chi tiết bài viết.
 *
 * Màn hình này chỉ tải đúng một bài, không phân trang, nên ba trạng thái chung
 * Loading / Success / Error của UiState là đủ (đúng như tôi đã nêu ở UiState.kt).
 * Ta dùng typealias: PostDetailUiState chỉ là tên gọi khác, ngắn gọn hơn, của UiState<Post>.
 */
typealias PostDetailUiState = UiState<Post>