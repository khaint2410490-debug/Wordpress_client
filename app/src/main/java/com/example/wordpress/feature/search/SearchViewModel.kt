package com.example.wordpressclient.feature.search

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.wordpressclient.core.common.AppError
import com.example.wordpressclient.domain.repository.PostRepository
import com.example.wordpressclient.domain.repository.SavedPostRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

/**
 * SearchViewModel = xử lý logic tìm kiếm bài viết.
 *
 * Tìm kiếm dùng lại PostRepository.getPosts(search = ...), nên không cần repository mới.
 * Có "debounce": đợi người dùng ngừng gõ một chút rồi mới gọi server, để không gửi
 * một request cho mỗi ký tự gõ.
 */
@HiltViewModel
class SearchViewModel @Inject constructor(
    private val repository: PostRepository,
    private val savedPostRepository: SavedPostRepository
) : ViewModel() {

    // Trạng thái có thể thay đổi, chỉ ViewModel được sửa.
    private val _uiState = MutableStateFlow(SearchUiState())

    // Bản chỉ đọc mà màn hình quan sát.
    val uiState: StateFlow<SearchUiState> = _uiState.asStateFlow()

    // Giữ tham chiếu lần tìm đang chờ/đang chạy để hủy khi người dùng gõ tiếp.
    private var searchJob: Job? = null

    init {
        viewModelScope.launch {
            savedPostRepository.observeSavedPosts().collect { posts ->
                _uiState.update { it.copy(savedPosts = posts) }
            }
        }
    }

    fun toggleSavedOnly() {
        searchJob?.cancel()
        val showSavedOnly = !_uiState.value.showSavedOnly
        _uiState.update { state ->
            state.copy(
                showSavedOnly = showSavedOnly,
                isLoading = false,
                errorMessage = null
            )
        }
        if (!showSavedOnly && _uiState.value.query.isNotBlank()) {
            searchNow()
        }
    }

    fun toggleSaved(post: com.example.wordpressclient.domain.model.Post) {
        viewModelScope.launch {
            if (_uiState.value.savedPosts.any { it.id == post.id }) {
                savedPostRepository.removeSavedPost(post.id)
            } else {
                savedPostRepository.savePost(post)
            }
        }
    }

    /**
     * Gọi mỗi khi người dùng đổi nội dung ô tìm kiếm.
     */
    fun onQueryChange(newQuery: String) {
        _uiState.update { it.copy(query = newQuery) }

        // Người dùng gõ tiếp thì hủy lần tìm cũ (đang đợi hoặc đang gọi mạng).
        searchJob?.cancel()

        if (_uiState.value.showSavedOnly) return

        // Ô tìm kiếm trống: xóa kết quả và quay về trạng thái ban đầu.
        if (newQuery.isBlank()) {
            _uiState.update {
                it.copy(
                    results = emptyList(),
                    isLoading = false,
                    errorMessage = null,
                    hasSearched = false
                )
            }
            return
        }

        // Đợi DEBOUNCE_MS; nếu trong thời gian đó có chữ mới thì job này bị hủy và không gọi mạng.
        searchJob = viewModelScope.launch {
            delay(DEBOUNCE_MS)
            performSearch(newQuery.trim())
        }
    }

    /**
     * Tìm ngay, không đợi. Dùng khi người dùng bấm nút tìm trên bàn phím hoặc bấm "Thử lại".
     */
    fun searchNow() {
        if (_uiState.value.showSavedOnly) return
        val query = _uiState.value.query.trim()
        if (query.isEmpty()) return

        searchJob?.cancel()
        searchJob = viewModelScope.launch { performSearch(query) }
    }

    /** Gọi repository và cập nhật state theo kết quả. */
    private suspend fun performSearch(query: String) {
        _uiState.update { it.copy(isLoading = true, errorMessage = null) }

        repository.getPosts(page = 1, perPage = RESULTS_PER_PAGE, search = query)
            .onSuccess { posts ->
                _uiState.update {
                    it.copy(results = posts, isLoading = false, hasSearched = true)
                }
            }
            .onFailure { error ->
                // Nếu lỗi chưa phải AppError (repository chưa đổi lỗi) thì coi là lỗi không xác định.
                val appError = error as? AppError ?: AppError.Unknown(error)
                _uiState.update {
                    it.copy(isLoading = false, errorMessage = appError.message)
                }
            }
    }

    private companion object {
        // Thời gian đợi sau lần gõ cuối trước khi gọi server (mili giây).
        const val DEBOUNCE_MS = 500L

        // Số kết quả tối đa mỗi lần tìm (chỉ lấy trang đầu).
        const val RESULTS_PER_PAGE = 20
    }
}