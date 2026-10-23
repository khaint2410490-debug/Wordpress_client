package com.example.wordpressclient.feature.posts.detail

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.wordpressclient.core.common.AppError
import com.example.wordpressclient.core.common.UiState
import com.example.wordpressclient.domain.repository.PostRepository
import com.example.wordpressclient.domain.repository.SavedPostRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

/**
 * PostDetailViewModel = tải và giữ dữ liệu một bài viết cho màn hình chi tiết.
 *
 * - SavedStateHandle: Hilt tự truyền vào. Navigation đặt các tham số của route
 *   (ở đây là ID bài viết) vào đây, nên ViewModel biết cần tải bài nào.
 * - PostRepository: nguồn dữ liệu, đã nối trong RepositoryModule.
 */
@HiltViewModel
class PostDetailViewModel @Inject constructor(
    savedStateHandle: SavedStateHandle,
    private val repository: PostRepository,
    private val savedPostRepository: SavedPostRepository
) : ViewModel() {

    // ID bài viết lấy từ tham số điều hướng. Nếu thiếu thì báo lỗi ngay (lỗi lập trình, không phải lỗi người dùng).
    private val postId: Int = checkNotNull(savedStateHandle.get<Int>(POST_ID_ARG))

    // Trạng thái có thể thay đổi, chỉ ViewModel được sửa. Bắt đầu bằng Loading.
    private val _uiState = MutableStateFlow<PostDetailUiState>(UiState.Loading)

    // Bản chỉ đọc mà màn hình quan sát.
    val uiState: StateFlow<PostDetailUiState> = _uiState.asStateFlow()
    private val _savedPostIds = MutableStateFlow<Set<Int>>(emptySet())
    val savedPostIds: StateFlow<Set<Int>> = _savedPostIds.asStateFlow()

    // Giữ tham chiếu tới lần tải đang chạy để có thể hủy khi bấm "Thử lại".
    private var loadJob: Job? = null

    init {
        viewModelScope.launch {
            savedPostRepository.observeSavedPostIds().collect { ids ->
                _savedPostIds.value = ids
            }
        }
        // Vừa mở màn hình là tải bài viết.
        loadPost()
    }

    fun toggleSaved() {
        val post = (_uiState.value as? UiState.Success)?.data ?: return
        viewModelScope.launch {
            if (post.id in _savedPostIds.value) {
                savedPostRepository.removeSavedPost(post.id)
            } else {
                savedPostRepository.savePost(post)
            }
        }
    }

    /**
     * Tải bài viết theo ID. Dùng cho lần tải đầu tiên và cả khi bấm "Thử lại".
     */
    fun loadPost() {
        // Hủy lần tải cũ (nếu còn chạy) để tránh hai request chồng nhau.
        loadJob?.cancel()
        _uiState.value = UiState.Loading

        loadJob = viewModelScope.launch {
            repository.getPostById(postId)
                .onSuccess { post ->
                    _uiState.value = UiState.Success(post)
                }
                .onFailure { error ->
                    // Nếu lỗi chưa phải AppError (repository chưa đổi lỗi) thì coi là lỗi không xác định.
                    val appError = error as? AppError ?: AppError.Unknown(error)
                    _uiState.value = UiState.Error(appError.message)
                }
        }
    }

    companion object {
        /**
         * Tên khóa của tham số ID trong route. Để public để Routes.kt và AppNavHost
         * dùng đúng cùng một tên, tránh gõ sai chuỗi ở hai nơi.
         */
        const val POST_ID_ARG = "postId"
    }
}