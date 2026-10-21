package com.example.wordpressclient.core.ui.theme

import androidx.compose.material3.Typography
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.sp

/**
 * Type.kt = nơi khai báo kiểu chữ (font, cỡ chữ, độ đậm, giãn dòng) của app.
 *
 * Typography của Material 3 có sẵn nhiều vai trò (titleLarge, bodyLarge, labelSmall...).
 * Ta chỉ ghi đè những vai trò app thật sự dùng, các vai trò còn lại giữ mặc định.
 * Vì đây là app đọc blog nên bodyLarge được tăng giãn dòng để đọc đỡ mỏi mắt.
 *
 * Đơn vị `sp` là đơn vị cỡ chữ của Android, tự co giãn theo cài đặt cỡ chữ của người dùng.
 */
val AppTypography = Typography(

    // Tiêu đề bài viết ở màn hình chi tiết.
    headlineMedium = TextStyle(
        fontFamily = FontFamily.Default,
        fontWeight = FontWeight.Bold,
        fontSize = 26.sp,
        lineHeight = 34.sp
    ),

    // Tiêu đề bài viết trong danh sách.
    titleMedium = TextStyle(
        fontFamily = FontFamily.Default,
        fontWeight = FontWeight.SemiBold,
        fontSize = 18.sp,
        lineHeight = 24.sp
    ),

    // Nội dung chính, đoạn tóm tắt. Giãn dòng rộng để dễ đọc đoạn dài.
    bodyLarge = TextStyle(
        fontFamily = FontFamily.Default,
        fontWeight = FontWeight.Normal,
        fontSize = 17.sp,
        lineHeight = 28.sp
    ),

    // Chữ phụ như ngày đăng, tên danh mục.
    labelMedium = TextStyle(
        fontFamily = FontFamily.Default,
        fontWeight = FontWeight.Medium,
        fontSize = 12.sp,
        lineHeight = 16.sp
    )
)