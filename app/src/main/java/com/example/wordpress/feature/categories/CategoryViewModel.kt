package com.example.wordpressclient.feature.categories

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.wordpressclient.core.common.AppError
import com.example.wordpressclient.core.common.UiState
import com.example.wordpressclient.domain.model.Category
import com.example.wordpressclient.domain.repository.CategoryRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

/**
 * CategoryViewModel = tải và giữ danh sách danh mục cho màn hình danh mục.
 *
 * Danh mục không phân trang (số lượng ít), nên dùng UiState<List<Category>> chung
 * với ba trạng thái Loading / Success / Error là đủ.
 */
@HiltViewModel
class CategoryViewModel @Inject constructor(
    private val repository: CategoryRepository
) : ViewModel() {

    // Trạng thái có thể thay đổi, chỉ ViewModel được sửa. Bắt đầu bằng Loading.
    private val _uiState = MutableStateFlow<UiState<List<Category>>>(UiState.Loading)

    // Bản chỉ đọc mà màn hình quan sát.
    val uiState: StateFlow<UiState<List<Category>>> = _uiState.asStateFlow()

    // Giữ tham chiếu lần tải đang chạy để hủy khi bấm "Thử lại".
    private var loadJob: Job? = null

    init {
        // Vừa mở màn hình là tải danh mục.
        loadCategories()
    }

    /**
     * Tải danh sách danh mục. Dùng cho lần tải đầu tiên và khi bấm "Thử lại".
     */
    fun loadCategories() {
        // Hủy lần tải cũ (nếu còn chạy) để tránh hai request chồng nhau.
        loadJob?.cancel()
        _uiState.value = UiState.Loading

        loadJob = viewModelScope.launch {
            repository.getCategories()
                .onSuccess { categories ->
                    _uiState.value = UiState.Success(categories)
                }
                .onFailure { error ->
                    // Nếu lỗi chưa phải AppError (repository chưa đổi lỗi) thì coi là lỗi không xác định.
                    val appError = error as? AppError ?: AppError.Unknown(error)
                    _uiState.value = UiState.Error(appError.message)
                }
        }
    }
}