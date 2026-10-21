package com.example.wordpressclient.data.repository

import com.example.wordpressclient.data.mapper.toDomain
import com.example.wordpressclient.domain.model.Post
import com.example.wordpressclient.domain.repository.PostRepository
import kotlinx.coroutines.CancellationException
import retrofit2.HttpException
import javax.inject.Inject

/**
 * WordPressPostRepository = phần cài đặt thật của PostRepository.
 *
 * Việc của lớp này: gọi WordPressApi, nhận PostDto, đổi sang Post bằng PostMapper,
 * rồi gói kết quả vào Result để ViewModel dùng.
 *
 * - @Inject constructor: Hilt tự tạo lớp này và truyền WordPressApi vào
 *   (WordPressApi được cung cấp bởi NetworkModule).
 * - Retrofit với hàm suspend tự chạy ở luồng nền, nên ở đây không cần đổi luồng thủ công.
 */
class WordPressPostRepository @Inject constructor(
    private val api: `WordPressApi.kt`
) : PostRepository {

    /**
     * Lấy danh sách bài viết, rồi chuyển từng PostDto thành Post.
     */
    override suspend fun getPosts(
        page: Int,
        perPage: Int,
        search: String?,
        categoryId: Int?
    ): Result<List<Post>> = safeCall {
        val response = api.getPosts(
            page = page,
            perPage = perPage,
            search = search,
            // Domain gọi là categoryId, còn API gọi là categories, ở đây ánh xạ lại.
            categories = categoryId
        )

        when {
            // Thành công: lấy body (nếu null thì coi như danh sách rỗng) rồi chuyển sang Post.
            response.isSuccessful -> response.body().orEmpty().toDomain()

            // Theo hiểu biết của tôi, WordPress trả lỗi 400 khi xin trang vượt quá số trang
            // hiện có. Với các trang sau trang 1, ta coi đó là "hết bài" thay vì lỗi,
            // để cuộn tải thêm không hiện thông báo lỗi sai.
            response.code() == 400 && page > 1 -> emptyList()

            // Các trường hợp còn lại: ném lỗi HTTP để safeCall gói vào Result.failure.
            else -> throw HttpException(response)
        }
    }

    /**
     * Lấy một bài viết theo ID rồi chuyển sang Post.
     * Nếu ID không tồn tại, Retrofit ném HttpException (404) và safeCall sẽ gói lại.
     */
    override suspend fun getPostById(id: Int): Result<Post> = safeCall {
        api.getPostById(id).toDomain()
    }

    /**
     * Hàm hỗ trợ: chạy một đoạn code và gói kết quả vào Result.
     *
     * Ta không dùng runCatching trực tiếp vì nó cũng bắt cả CancellationException.
     * Khi coroutine bị hủy (ví dụ người dùng thoát màn hình), lỗi này phải được ném lại
     * để coroutine dừng đúng cách, nếu không có thể gây hành vi khó hiểu.
     */
    private inline fun <T> safeCall(block: () -> T): Result<T> =
        try {
            Result.success(block())
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            Result.failure(e)
        }
}