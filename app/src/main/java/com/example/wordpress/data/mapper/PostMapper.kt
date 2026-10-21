package com.example.wordpressclient.data.mapper

import androidx.core.text.HtmlCompat
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

    // PostDto hiện chưa có dữ liệu ảnh (featuredMedia chỉ là số ID),
    // nên tạm để null. Khi dùng tham số _embed ta sẽ bổ sung phần này.
    featuredImageUrl = null
)

/**
 * Chuyển cả danh sách PostDto thành danh sách Post.
 * Dùng khi gọi API lấy nhiều bài viết cùng lúc.
 */
fun List<PostDto>.toDomain(): List<Post> = map { it.toDomain() }

/**
 * Hàm hỗ trợ (chỉ dùng trong file này): đổi chuỗi HTML thành văn bản thường.
 *
 * - HtmlCompat.fromHtml: bỏ thẻ HTML và giải mã các ký tự đặc biệt.
 * - replace("\uFFFC", ""): fromHtml có thể để lại ký tự thay thế cho thẻ <img>,
 *   ta xóa nó đi để không hiện ô vuông lạ trong tiêu đề hoặc tóm tắt.
 * - trim(): bỏ khoảng trắng và dòng trống thừa ở đầu cuối.
 */
private fun String.htmlToPlainText(): String =
    HtmlCompat.fromHtml(this, HtmlCompat.FROM_HTML_MODE_COMPACT)
        .toString()
        .replace("\uFFFC", "")
        .trim()