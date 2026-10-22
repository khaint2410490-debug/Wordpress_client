package com.example.wordpressclient.data.remote.dto

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

/**
 * PostDto = "Data Transfer Object" của một bài viết.
 *
 * Class này mô tả đúng cấu trúc JSON mà WordPress REST API trả về
 * (endpoint posts của WordPress REST API). Nó chỉ dùng ở tầng data.
 * Sau đó PostMapper sẽ chuyển nó thành model Post sạch hơn cho phần UI.
 *
 * Quy ước: tên thuộc tính theo kiểu camelCase của Kotlin,
 * còn @SerialName chỉ định tên khóa thật trong JSON.
 */
@Serializable
data class PostDto(

    // ID duy nhất của bài viết trên WordPress.
    val id: Int,

    // Ngày đăng, theo múi giờ của site, dạng ISO 8601, ví dụ "2024-05-01T10:30:00".
    val date: String,

    // Ngày đăng theo giờ GMT/UTC, nên dùng khi cần so sánh hoặc sắp xếp chính xác.
    // Có giá trị mặc định null để app không bị lỗi nếu thiếu trường này.
    @SerialName("date_gmt")
    val dateGmt: String? = null,

    // Đường dẫn thân thiện (slug) của bài, ví dụ "bai-viet-dau-tien".
    val slug: String = "",

    // Đường link đầy đủ tới bài viết trên web, dùng cho chức năng chia sẻ
    // hoặc mở bài viết bằng trình duyệt.
    val link: String = "",

    // Tiêu đề bài viết. WordPress không trả về chuỗi thường mà bọc trong object
    // { "rendered": "..." }, nên ta cần class lồng nhau RenderedDto.
    val title: RenderedDto,

    // Nội dung đầy đủ của bài viết, ở dạng HTML.
    val content: RenderedDto,

    // Đoạn tóm tắt ngắn, thường dùng để hiển thị trong danh sách bài viết.
    // Cũng ở dạng HTML nên có thể chứa thẻ như <p>.
    val excerpt: RenderedDto? = null,

    // ID của ảnh đại diện (featured image). Trường này chỉ là số ID.
    // Bài không có ảnh đại diện thì giá trị thường là 0.
    @SerialName("featured_media")
    val featuredMedia: Int = 0,

    // ID tác giả. Chỉ là số ID, muốn có tên tác giả cần thêm tham số _embed (làm sau).
    val author: Int = 0,

    // Danh sách ID các danh mục mà bài viết thuộc về.
    val categories: List<Int> = emptyList()
)

/**
 * WordPress bọc các trường văn bản (title, content, excerpt) trong dạng:
 *   "title": { "rendered": "Xin chào" }
 *
 * "rendered" là nội dung đã được WordPress xử lý, thường chứa HTML
 * và các ký tự đặc biệt đã được mã hóa (ví dụ &amp; hoặc &#8211;).
 * Vì vậy khi hiển thị, bạn cần giải mã HTML (sẽ xử lý trong PostMapper).
 */
@Serializable
data class RenderedDto(
    val rendered: String = ""
)