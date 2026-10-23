package com.example.wordpressclient.feature.posts.list

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.SavedStateHandle
import com.example.wordpressclient.core.common.AppError
import com.example.wordpressclient.domain.repository.PostRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

/**
 * PostListViewModel = giữ trạng thái và xử lý logic cho màn hình danh sách bài viết.
 *
 * - @HiltViewModel + @Inject constructor: Hilt tự tạo ViewModel và truyền PostRepository vào
 *   (đã nối với WordPressPostRepository trong RepositoryModule).
 * - ViewModel chỉ biết interface PostRepository, không biết gì về Retrofit.
 */
@HiltViewModel
class PostListViewModel @Inject constructor(
    private val repository: PostRepository,
    savedStateHandle: SavedStateHandle
) : ViewModel() {

    // ID danh mục để lọc bài. Là null khi mở danh sách chung (route không có tham số này).
    private val categoryId: Int? = savedStateHandle.get<Int>(CATEGORY_ID_ARG)

    // Trạng thái có thể thay đổi, chỉ ViewModel được phép sửa.
    private val _uiState = MutableStateFlow(PostListUiState())

    // Bản chỉ đọc mà màn hình quan sát. Mỗi khi giá trị đổi, giao diện tự vẽ lại.
    val uiState: StateFlow<PostListUiState> = _uiState.asStateFlow()

    // Trang kế tiếp cần tải (WordPress đánh số trang từ 1).
    private var nextPage = 1

    init {
        // Vừa mở màn hình là tải trang đầu tiên.
        loadNextPage()
    }

    /**
     * Tải trang kế tiếp. Dùng cho cả lần tải đầu tiên lẫn cuộn xuống tải thêm.
     */
    fun loadNextPage() {
        val current = _uiState.value

        // Bỏ qua nếu đang tải dở (trang đầu hoặc các trang sau) hoặc đã hết bài.
        // Nhờ vậy cuộn nhanh cũng không gọi trùng nhiều request cùng lúc.
        if (current.isLoading || current.isLoadingMore || current.isRefreshing || current.endReached) return

        // nextPage == 1 nghĩa là chưa có trang nào: đây là lần tải đầu tiên.
        val isFirstPage = nextPage == 1

        // Bật cờ tải tương ứng và xóa lỗi cũ.
        _uiState.update {
            it.copy(
                isLoading = isFirstPage,
                isLoadingMore = !isFirstPage,
                errorMessage = null
            )
        }

        viewModelScope.launch {
            repository.getPosts(page = nextPage, perPage = PAGE_SIZE, categoryId = categoryId)
                .onSuccess { newPosts ->
                    nextPage++
                    _uiState.update {
                        it.copy(
                            posts = it.posts + newPosts,
                            isLoading = false,
                            isLoadingMore = false,
                            // Trang trả về ít hơn PAGE_SIZE nghĩa là không còn bài để tải thêm.
                            endReached = newPosts.size < PAGE_SIZE
                        )
                    }
                }
                .onFailure { error ->
                    // Nếu lỗi chưa phải AppError (repository chưa đổi lỗi) thì coi là lỗi không xác định.
                    val appError = error as? AppError ?: AppError.Unknown(error)
                    _uiState.update {
                        it.copy(
                            isLoading = false,
                            isLoadingMore = false,
                            errorMessage = appError.message
                        )
                    }
                }
        }
    }

    /**
     * Gọi khi người dùng bấm "Thử lại" sau khi gặp lỗi.
     * Vì lúc lỗi nextPage chưa tăng, gọi lại sẽ tải đúng trang vừa thất bại.
     */
    fun retry() = loadNextPage()

    companion object {
        // Tên tham số trong route, phải khớp với Routes.CATEGORY_POSTS.
        const val CATEGORY_ID_ARG = "categoryId"

        // Số bài mỗi trang. WordPress mặc định là 10.
        private const val PAGE_SIZE = 10
    }

    /**
     * Làm mới: tải lại trang 1 và thay toàn bộ danh sách.
     * Nếu lỗi thì giữ nguyên danh sách cũ, chỉ hiện thông báo lỗi.
     */
    fun refresh() {
        val current = _uiState.value
        // Đang tải gì đó thì bỏ qua để không gọi trùng.
        if (current.isLoading || current.isLoadingMore || current.isRefreshing) return

        _uiState.update { it.copy(isRefreshing = true, errorMessage = null) }

        viewModelScope.launch {
            repository.getPosts(page = 1, perPage = PAGE_SIZE, categoryId = categoryId)
                .onSuccess { freshPosts ->
                    // Trang 1 đã có, lần cuộn tiếp theo sẽ tải trang 2.
                    nextPage = 2
                    _uiState.update {
                        it.copy(
                            posts = freshPosts,
                            isRefreshing = false,
                            endReached = freshPosts.size < PAGE_SIZE
                        )
                    }
                }
                .onFailure { error ->
                    val appError = error as? AppError ?: AppError.Unknown(error)
                    _uiState.update {
                        it.copy(isRefreshing = false, errorMessage = appError.message)
                    }
                }
        }
    }
}