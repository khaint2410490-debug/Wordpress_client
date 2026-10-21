package com.example.wordpressclient.data.remote

import com.example.wordpressclient.data.remote.dto.PostDto
import retrofit2.Response
import retrofit2.http.GET
import retrofit2.http.Path
import retrofit2.http.Query

/**
 * WordPressApi = nơi khai báo các endpoint của WordPress REST API.
 *
 * Đây chỉ là interface: Retrofit sẽ tự tạo phần cài đặt thật khi bạn gọi
 * RetrofitClient.createService<WordPressApi>().
 *
 * Các đường dẫn bên dưới là đường dẫn tương đối, được nối vào BASE_URL
 * (ví dụ "https://your-site.com/wp-json/wp/v2/" + "posts").
 *
 * `suspend` nghĩa là hàm chạy bất đồng bộ trong coroutine,
 * nên ViewModel gọi được mà không làm đơ giao diện.
 */
interface `WordPressApi.kt` {

    /**
     * Lấy danh sách bài viết.
     * Endpoint: GET /posts
     *
     * Kiểu trả về là Response<List<PostDto>> thay vì List<PostDto> trực tiếp,
     * vì thông tin phân trang của WordPress nằm ở header (X-WP-Total, X-WP-TotalPages)
     * chứ không nằm trong body. Bọc trong Response giúp ta đọc được header này sau.
     *
     * @param page    Trang cần lấy, bắt đầu từ 1.
     * @param perPage Số bài mỗi trang. WordPress mặc định là 10 và giới hạn tối đa 100.
     * @param search  Từ khóa tìm kiếm, null nếu không tìm. Dùng cho SearchScreen.
     * @param categories ID danh mục để lọc, null nếu lấy tất cả. Dùng cho CategoryScreen.
     */
    @GET("posts")
    suspend fun getPosts(
        @Query("page") page: Int = 1,
        @Query("per_page") perPage: Int = 10,
        @Query("search") search: String? = null,
        @Query("categories") categories: Int? = null
    ): Response<List<PostDto>>

    /**
     * Lấy một bài viết theo ID.
     * Endpoint: GET /posts/{id}
     *
     * Trả thẳng PostDto. Nếu ID không tồn tại, server trả lỗi 404
     * và Retrofit sẽ ném HttpException (ta xử lý trong repository, làm sau).
     *
     * @param id ID bài viết, nằm ngay trong đường dẫn.
     */
    @GET("posts/{id}")
    suspend fun getPostById(
        @Path("id") id: Int
    ): PostDto
}