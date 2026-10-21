package com.example.wordpressclient.core.common

/**
 * AppError = các loại lỗi mà app của bạn phân biệt được, kèm thông báo thân thiện cho người dùng.
 *
 * Vì sao cần: lỗi thô như HttpException hay UnknownHostException khó hiểu với người dùng,
 * và để lộ chi tiết kỹ thuật ra giao diện. AppError gom chúng về vài loại dễ xử lý.
 *
 * Kế thừa Exception để có thể đặt thẳng vào Result.failure(...) của PostRepository,
 * và ViewModel đọc `message` để đưa vào UiState.Error.
 *
 * `sealed class` nghĩa là tập hợp loại lỗi đã đóng kín, nên `when` buộc bạn xử lý đủ mọi loại.
 *
 * File này chỉ chứa định nghĩa, không import Retrofit hay OkHttp, để core/common không phụ thuộc
 * vào thư viện mạng. Việc đổi lỗi thô thành AppError sẽ làm ở tầng data.
 */
sealed class AppError(
    // Thông báo hiện cho người dùng.
    override val message: String,
    // Lỗi gốc, giữ lại để ghi log hoặc gỡ lỗi (không hiện cho người dùng).
    override val cause: Throwable? = null
) : Exception(message, cause) {

    /** Mất kết nối, không có mạng hoặc quá thời gian chờ. */
    class Network(cause: Throwable? = null) : AppError(
        message = "Không thể kết nối. Vui lòng kiểm tra mạng và thử lại.",
        cause = cause
    )

    /** Không tìm thấy dữ liệu (ví dụ bài viết đã bị xóa, ứng với mã HTTP 404). */
    class NotFound(cause: Throwable? = null) : AppError(
        message = "Không tìm thấy nội dung này.",
        cause = cause
    )

    /** Server trả lỗi (mã HTTP khác 404), [code] là mã HTTP để tiện gỡ lỗi. */
    class Server(val code: Int, cause: Throwable? = null) : AppError(
        message = "Máy chủ đang gặp sự cố. Vui lòng thử lại sau.",
        cause = cause
    )

    /** Dữ liệu nhận về không đọc được (sai cấu trúc JSON). */
    class Parsing(cause: Throwable? = null) : AppError(
        message = "Dữ liệu nhận về không hợp lệ.",
        cause = cause
    )

    /** Mọi lỗi còn lại mà ta chưa phân loại. */
    class Unknown(cause: Throwable? = null) : AppError(
        message = "Đã xảy ra lỗi không xác định.",
        cause = cause
    )
}