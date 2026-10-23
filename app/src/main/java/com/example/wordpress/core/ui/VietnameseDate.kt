package com.example.wordpressclient.core.ui

/**
 * Đổi chuỗi ngày ISO 8601 của WordPress sang dạng ngày Việt Nam: dd/MM/yyyy.
 *
 * Ví dụ: "2024-05-01T10:30:00" -> "01/05/2024".
 *
 * Cách làm: chỉ cắt 10 ký tự đầu ("2024-05-01") rồi đảo thứ tự năm-tháng-ngày.
 * Không dùng java.time hay SimpleDateFormat nên không cần bật desugaring
 * (minSdk = 24, trong khi java.time cần API 26 trở lên).
 *
 * Giới hạn: không đổi múi giờ. Chuỗi `date` của WordPress là giờ của site,
 * ta giữ nguyên ngày đó, không quy về giờ của máy người dùng.
 *
 * Nếu chuỗi sai định dạng thì trả lại nguyên chuỗi gốc, để app không bị lỗi.
 */
fun String.toVietnameseDate(): String {
    // Lấy phần ngày, bỏ phần giờ. Ví dụ "2024-05-01".
    val parts = take(10).split("-")

    // Phải có đúng 3 phần: năm, tháng, ngày.
    if (parts.size != 3) return this

    val (year, month, day) = parts

    // Kiểm tra độ dài và chỉ gồm chữ số, tránh hiện chuỗi lạ.
    val isValid = year.length == 4 && month.length == 2 && day.length == 2 &&
        parts.all { part -> part.all(Char::isDigit) }

    return if (isValid) "$day/$month/$year" else this
}