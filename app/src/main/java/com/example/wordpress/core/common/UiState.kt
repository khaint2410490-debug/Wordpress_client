package com.example.wordpressclient.core.common

/**
 * UiState = mô tả "màn hình đang ở trạng thái nào" để giao diện vẽ cho đúng.
 *
 * Mỗi màn hình tải dữ liệu chỉ ở đúng MỘT trong ba trạng thái:
 *   - Loading: đang tải, hiện vòng xoay.
 *   - Success: có dữ liệu, hiện nội dung.
 *   - Error:   thất bại, hiện thông báo lỗi (và nút thử lại).
 *
 * `sealed interface` nghĩa là tập hợp trạng thái đã đóng kín. Khi dùng `when`,
 * trình biên dịch buộc bạn xử lý đủ cả ba, nên không sót trường hợp nào.
 *
 * `<out T>` là kiểu dữ liệu của trạng thái Success, ví dụ:
 *   UiState<Post>        cho màn hình chi tiết
 *   UiState<List<Post>>  cho màn hình danh sách
 */
sealed interface UiState<out T> {

    /** Đang tải dữ liệu. Là `object` vì không cần mang thêm thông tin gì. */
    data object Loading : UiState<Nothing>

    /** Tải xong, [data] là dữ liệu cần hiển thị. */
    data class Success<T>(val data: T) : UiState<T>

    /**
     * Tải thất bại, [message] là nội dung lỗi sẽ hiện cho người dùng.
     * AppError chưa được viết nên tạm dùng String. Khi có AppError,
     * ta có thể đổi tham số này sang kiểu đó.
     */
    data class Error(val message: String) : UiState<Nothing>
}