package com.example.wordpressclient.data.mapper

import com.example.wordpressclient.data.remote.dto.PostDto
import com.example.wordpressclient.domain.model.Post

/**
 * PostMapper = nơi chuyển PostDto (dữ liệu thô từ WordPress) thành Post (model của app).
 *
 * Tôi viết dưới dạng extension function để dùng gọn: dto.toDomain().
 * Cách này không cần tạo đối tượng hay inject bằng Hilt.
 */

/**
 * Chuyển một PostDto thành một Post.
 */
fun PostDto.toDomain(): Post = Post(
    id = id,

    // Tiêu đề từ WordPress có thể chứa ký tự đã mã hóa HTML (ví dụ &amp;, &#8211;),
    // nên cần giải mã thành văn bản thường trước khi hiển thị.
    title = title.rendered.htmlToPlainText(),

    // Excerpt có thể là null (nếu server không trả về),
    // khi đó dùng chuỗi rỗng. Đồng thời bỏ thẻ HTML như <p>.
    excerpt = excerpt?.rendered.orEmpty().htmlToPlainText(),

    // Nội dung đầy đủ giữ nguyên dạng HTML, màn hình chi tiết sẽ tự quyết định cách hiển thị.
    contentHtml = content.rendered,

    // Giữ nguyên chuỗi ngày ISO 8601, việc định dạng cho dễ đọc làm ở tầng UI.
    date = date,

    // Link bài viết trên web (có giá trị mặc định "" trong DTO nên không bao giờ null).
    link = link,

    featuredImageUrl = embedded?.featuredMedia?.firstOrNull()?.sourceUrl?.takeIf(String::isNotBlank),
    authorName = embedded?.author?.firstOrNull()?.name?.takeIf(String::isNotBlank),
    authorAvatarUrl = embedded?.author?.firstOrNull()?.avatarUrls?.get("96")
        ?: embedded?.author?.firstOrNull()?.avatarUrls?.values?.firstOrNull()
)

/**
 * Chuyển cả danh sách PostDto thành danh sách Post.
 * Dùng khi gọi API lấy nhiều bài viết cùng lúc.
 */
fun List<PostDto>.toDomain(): List<Post> = map { it.toDomain() }
