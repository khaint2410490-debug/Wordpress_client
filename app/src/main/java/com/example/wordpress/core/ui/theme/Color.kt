package com.example.wordpressclient.core.ui.theme

import androidx.compose.ui.graphics.Color

/**
 * Color.kt = nơi khai báo các màu gốc của app.
 *
 * Chỉ khai báo giá trị màu ở đây, còn việc "màu nào dùng cho vai trò nào"
 * (nút chính, nền, chữ...) nằm trong Theme.kt.
 * Mã màu dạng 0xFFRRGGBB: hai số FF đầu là độ đục (FF = không trong suốt).
 */

// ----- Bảng màu cho giao diện sáng -----
val LightPrimary = Color(0xFF1B5E9E)        // màu chính (thanh tiêu đề, nút nhấn)
val LightOnPrimary = Color(0xFFFFFFFF)      // màu chữ/icon nằm trên màu chính
val LightSecondary = Color(0xFF5A6B7B)      // màu phụ
val LightOnSecondary = Color(0xFFFFFFFF)
val LightBackground = Color(0xFFFAFAFA)     // nền màn hình
val LightOnBackground = Color(0xFF1C1B1F)   // chữ trên nền
val LightSurface = Color(0xFFFFFFFF)        // nền của card, thanh điều hướng
val LightOnSurface = Color(0xFF1C1B1F)
val LightError = Color(0xFFB3261E)

// ----- Bảng màu cho giao diện tối -----
val DarkPrimary = Color(0xFF9CCAFF)
val DarkOnPrimary = Color(0xFF00315A)
val DarkSecondary = Color(0xFFBBC7D6)
val DarkOnSecondary = Color(0xFF253240)
val DarkBackground = Color(0xFF121316)
val DarkOnBackground = Color(0xFFE3E2E6)
val DarkSurface = Color(0xFF1B1C20)
val DarkOnSurface = Color(0xFFE3E2E6)
val DarkError = Color(0xFFF2B8B5)