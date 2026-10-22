package com.example.wordpressclient.data.mapper

import androidx.core.text.HtmlCompat

/**
 * Hàm hỗ trợ dùng chung cho các mapper: đổi chuỗi HTML thành văn bản thường.
 *
 * - HtmlCompat.fromHtml: bỏ thẻ HTML và giải mã các ký tự đặc biệt (ví dụ &amp;, &#8211;).
 * - replace("\uFFFC", ""): fromHtml có thể để lại ký tự thay thế cho thẻ <img>,
 *   ta xóa nó đi để không hiện ô vuông lạ.
 * - trim(): bỏ khoảng trắng và dòng trống thừa ở đầu cuối.
 *
 * `internal`: chỉ dùng trong module này, các mapper cùng package gọi được mà không cần import.
 */
internal fun String.htmlToPlainText(): String =
    HtmlCompat.fromHtml(this, HtmlCompat.FROM_HTML_MODE_COMPACT)
        .toString()
        .replace("\uFFFC", "")
        .trim()