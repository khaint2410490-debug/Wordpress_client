package com.example.wordpressclient.domain.repository

import com.example.wordpressclient.domain.model.Post

/**
 * PostRepository = "hợp đồng" mô tả những gì app cần làm với bài viết.
 *
 * Đây chỉ là interface (khai báo hàm, chưa có phần xử lý). Phần cài đặt thật
 * nằm ở WordPressPostRepository (tầng data). Nhờ vậy ViewModel chỉ biết tới interface này
 * và không cần biết dữ liệu đến từ Retrofit hay nơi nào khác.
 *
 * Quy ước của file này:
 * - Chỉ dùng model Post của domain, không xuất hiện PostDto hay Retrofit.
 * - Mọi hàm trả về Result<...> (kiểu có sẵn của Kotlin): thành công thì chứa dữ liệu,
 *   thất bại thì chứa lỗi. ViewModel dùng .onSuccess / .onFailure
 *   (hoặc .fold) để cập nhật UI mà không cần try/catch.
 * - Hàm có `suspend` vì phải chờ mạng, nên gọi trong coroutine.
 */
interface PostRepository {

    /**
     * Lấy danh sách bài viết.
     *
     * @param page       Trang cần lấy, bắt đầu từ 1 (dùng cho cuộn tải thêm).
     * @param perPage    Số bài mỗi trang.
     * @param search     Từ khóa tìm kiếm, null nếu không tìm (dùng cho SearchScreen).
     * @param categoryId ID danh mục để lọc, null nếu lấy tất cả (dùng cho CategoryScreen).
     * @return Result chứa danh sách Post của trang đó. Danh sách rỗng
     *         nghĩa là không còn bài nào.
     */
    suspend fun getPosts(
        page: Int = 1,
        perPage: Int = 10,
        search: String? = null,
        categoryId: Int? = null
    ): Result<List<Post>>

    /**
     * Lấy một bài viết theo ID (dùng cho PostDetailScreen).
     *
     * @param id ID bài viết.
     * @return Result chứa Post, hoặc chứa lỗi nếu không tìm thấy hay mất mạng.
     */
    suspend fun getPostById(id: Int): Result<Post>
}