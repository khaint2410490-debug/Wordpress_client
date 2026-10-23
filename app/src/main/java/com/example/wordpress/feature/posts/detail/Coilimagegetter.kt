package com.example.wordpressclient.feature.posts.detail

import android.graphics.Canvas
import android.graphics.ColorFilter
import android.graphics.PixelFormat
import android.graphics.drawable.Drawable
import android.text.Html
import android.text.SpannableStringBuilder
import android.widget.TextView
import coil.imageLoader
import coil.request.ImageRequest

/**
 * CoilImageGetter = giúp TextView hiện được ảnh trong thẻ <img> của nội dung HTML.
 *
 * Vì sao cần: HtmlCompat.fromHtml tự nó không tải ảnh từ mạng. Nó chỉ gọi ImageGetter
 * và hỏi "ảnh có địa chỉ này đâu?". Lớp này trả lời bằng cách nhờ Coil tải ảnh.
 *
 * Cách hoạt động:
 * 1. fromHtml gặp <img> thì gọi getDrawable(src). Ta trả về ngay một "khung rỗng"
 *    (ImageDrawableWrapper) vì ảnh chưa tải xong.
 * 2. Coil tải ảnh ở nền. Tải xong thì ta nhét ảnh vào khung, đặt kích thước cho vừa
 *    chiều rộng TextView, rồi bảo TextView vẽ lại.
 *
 * Cách dùng (trong PostDetailScreen, chỗ đang gọi HtmlCompat.fromHtml):
 *   HtmlCompat.fromHtml(
 *       html,
 *       HtmlCompat.FROM_HTML_MODE_LEGACY,
 *       CoilImageGetter(textView),
 *       null
 *   )
 *
 * Lưu ý: Coil tự dùng bộ nhớ đệm (cache) chung với AsyncImage trong Compose, nên
 * ảnh đã tải ở nơi khác sẽ hiện nhanh hơn. Request gắn với vòng đời của Activity
 * (context của TextView), nên bị hủy khi Activity đóng.
 */
class CoilImageGetter(
    private val textView: TextView
) : Html.ImageGetter {

    override fun getDrawable(source: String?): Drawable? {
        // Thẻ <img> không có src thì bỏ qua, không hiện gì.
        if (source.isNullOrBlank()) return null

        // Khung rỗng trả về ngay lập tức, ảnh sẽ được nhét vào sau khi tải xong.
        val wrapper = ImageDrawableWrapper()
        wrapper.setBounds(0, 0, 1, 1)
        val context = textView.context

        val request = ImageRequest.Builder(context)
            .data(normalizeUrl(source))
            // Chỉ cần xử lý khi tải thành công. Nếu lỗi thì khung vẫn rỗng, bài viết không bị hỏng.
            .target(onSuccess = { loaded -> onImageLoaded(wrapper, loaded) })
            .build()

        // imageLoader là bộ tải ảnh dùng chung của Coil trong cả app.
        context.imageLoader.enqueue(request)

        return wrapper
    }

    /** Ảnh đã tải xong: đặt kích thước rồi yêu cầu TextView vẽ lại. */
    private fun onImageLoaded(wrapper: ImageDrawableWrapper, loaded: Drawable) {
        val intrinsicWidth = loaded.intrinsicWidth
        val intrinsicHeight = loaded.intrinsicHeight
        // Ảnh không có kích thước hợp lệ thì bỏ qua để tránh chia cho 0.
        if (intrinsicWidth <= 0 || intrinsicHeight <= 0) return

        // Chiều rộng tối đa ảnh được chiếm = bề rộng TextView trừ phần đệm hai bên.
        // Lần đầu TextView có thể chưa đo xong (width = 0) nên dùng bề rộng màn hình thay thế.
        val horizontalPadding = textView.paddingLeft + textView.paddingRight
        val availableWidth = (textView.width - horizontalPadding).takeIf { it > 0 }
            ?: (textView.resources.displayMetrics.widthPixels - horizontalPadding)

        // Chỉ thu nhỏ ảnh quá rộng, không phóng to ảnh nhỏ (tránh ảnh nhỏ bị vỡ nét).
        val scale = if (intrinsicWidth > availableWidth) {
            availableWidth.toFloat() / intrinsicWidth
        } else {
            1f
        }
        val width = (intrinsicWidth * scale).toInt().coerceAtLeast(1)
        val height = (intrinsicHeight * scale).toInt().coerceAtLeast(1)

        // Nhét ảnh vào khung và đặt cùng kích thước cho cả hai.
        wrapper.inner = loaded
        wrapper.setBounds(0, 0, width, height)

        // A fresh spannable forces TextView to recalculate line positions and image height.
        textView.post {
            textView.text = SpannableStringBuilder(textView.text)
            textView.requestLayout()
            textView.invalidate()
        }
    }

    /**
     * Chuẩn hóa địa chỉ ảnh:
     * - "//abc.com/a.jpg" (không có giao thức) thì thêm "https:".
     * - "http://" đổi thành "https://", vì từ Android 9 trở lên mặc định chặn kết nối
     *   không mã hóa. Nếu máy chủ ảnh không hỗ trợ https thì ảnh đó sẽ không hiện.
     */
    private fun normalizeUrl(source: String): String = when {
        source.startsWith("//") -> "https:$source"
        source.startsWith("http://") -> "https://" + source.removePrefix("http://")
        else -> source
    }
}

/**
 * Khung chứa ảnh. Ban đầu rỗng (không vẽ gì), có ảnh thì vẽ ảnh đó.
 * Mọi thao tác vẽ đều chuyển cho `inner`.
 */
private class ImageDrawableWrapper : Drawable(), Drawable.Callback {

    // Ảnh thật, null khi chưa tải xong.
    var inner: Drawable? = null
        set(value) {
            field?.callback = null
            field = value
            value?.callback = this
            value?.bounds = bounds
            invalidateSelf()
        }

    override fun draw(canvas: Canvas) {
        inner?.draw(canvas)
    }

    override fun onBoundsChange(bounds: android.graphics.Rect) {
        inner?.bounds = bounds
    }

    override fun setAlpha(alpha: Int) {
        inner?.alpha = alpha
    }

    override fun setColorFilter(colorFilter: ColorFilter?) {
        inner?.colorFilter = colorFilter
    }

    @Deprecated("Deprecated in Java")
    override fun getOpacity(): Int = PixelFormat.TRANSLUCENT

    override fun getIntrinsicWidth(): Int = inner?.intrinsicWidth ?: bounds.width()

    override fun getIntrinsicHeight(): Int = inner?.intrinsicHeight ?: bounds.height()

    override fun invalidateDrawable(who: Drawable) {
        invalidateSelf()
    }

    override fun scheduleDrawable(who: Drawable, what: Runnable, `when`: Long) {
        callback?.scheduleDrawable(this, what, `when`)
    }

    override fun unscheduleDrawable(who: Drawable, what: Runnable) {
        callback?.unscheduleDrawable(this, what)
    }
}