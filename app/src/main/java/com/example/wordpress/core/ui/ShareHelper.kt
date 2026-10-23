package com.example.wordpressclient.core.ui

import android.app.Activity
import android.content.Context
import android.content.Intent

/**
 * Mở hộp chọn ứng dụng của Android để chia sẻ một bài viết (tiêu đề + đường dẫn).
 *
 * Cách dùng trong Compose (ví dụ ở PostDetailScreen):
 *   val context = LocalContext.current
 *   IconButton(onClick = { context.sharePost(post.title, post.link) }) {
 *       Icon(Icons.Default.Share, contentDescription = "Chia sẻ")
 *   }
 *
 * @return true nếu đã mở hộp chia sẻ, false nếu bài không có đường dẫn để chia sẻ
 *         (khi đó nơi gọi có thể hiện thông báo, ví dụ Toast).
 */
fun Context.sharePost(title: String, link: String): Boolean {
    // Không có link thì không có gì để chia sẻ.
    if (link.isBlank()) return false

    // ACTION_SEND + text/plain: gửi một đoạn chữ cho ứng dụng nào người dùng chọn
    // (Zalo, Messenger, Gmail...).
    val sendIntent = Intent(Intent.ACTION_SEND).apply {
        type = "text/plain"
        // Một số ứng dụng (như email) dùng tiêu đề này làm chủ đề thư.
        putExtra(Intent.EXTRA_SUBJECT, title)
        putExtra(Intent.EXTRA_TEXT, "$title\n$link")
    }

    // createChooser: luôn hiện hộp chọn, dù máy đã đặt ứng dụng mặc định.
    val chooser = Intent.createChooser(sendIntent, "Chia sẻ bài viết")

    // Nếu Context không phải Activity thì Android yêu cầu cờ NEW_TASK mới mở được.
    if (this !is Activity) chooser.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)

    startActivity(chooser)
    return true
}