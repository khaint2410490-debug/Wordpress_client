package com.example.wordpressclient.feature.search

import com.example.wordpressclient.domain.model.Post

/**
 * SearchUiState = toàn bộ trạng thái của màn hình tìm kiếm.
 *
 * Giữ từ khóa và cờ hasSearched để phân biệt trạng thái chưa tìm với kết quả rỗng.
 */
data class SearchUiState(
    val query: String = "",
    val results: List<Post> = emptyList(),
    val isLoading: Boolean = false,
    val errorMessage: String? = null,
    val hasSearched: Boolean = false
)
