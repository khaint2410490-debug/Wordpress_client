package com.example.wordpressclient.domain.model

/**
 * Post = model bài viết của ứng dụng (tầng domain).
 *
 * Đây là dạng "sạch" mà phần giao diện (Screen, ViewModel) sẽ dùng.
 * Nó không biết gì về JSON hay WordPress API. PostMapper sẽ chuyển
 * PostDto (dữ liệu thô từ server) thành Post.
 *
 * Quy ước: class này chỉ chứa dữ liệu đã xử lý xong, không chứa logic mạng.
 */
data class Post(

    // ID duy nhất của bài viết. Dùng làm khóa khi mở màn hình chi tiết
    // và làm `key` trong LazyColumn.
    val id: Int,

    // Tiêu đề dạng văn bản thường. PostMapper cần giải mã các ký tự HTML
    // như &amp; hoặc &#8211; trước khi gán vào đây.
    val title: String,

    // Đoạn tóm tắt dạng văn bản thường (đã bỏ thẻ HTML như <p>),
    // dùng để hiển thị trong danh sách bài viết.
    val excerpt: String,

    // Nội dung đầy đủ vẫn giữ nguyên ở dạng HTML,
    // để màn hình chi tiết quyết định cách hiển thị (AndroidView, WebView...).
    val contentHtml: String,

    // Ngày đăng, hiện để dạng chuỗi ISO 8601, ví dụ "2024-05-01T10:30:00".
    // Việc định dạng cho dễ đọc (ví dụ "01/05/2024") làm ở tầng UI.
    val date: String,

    // Link bài viết trên web, dùng cho chức năng chia sẻ hoặc mở bằng trình duyệt.
    val link: String = "",

    // URL ảnh đại diện. Là null nếu bài không có ảnh,
    // hoặc nếu chưa lấy dữ liệu ảnh (cần tham số _embed, làm sau).
    val featuredImageUrl: String? = null
)