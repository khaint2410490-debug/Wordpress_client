package com.example.wordpressclient.core.ui.theme

import android.os.Build
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.dynamicDarkColorScheme
import androidx.compose.material3.dynamicLightColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.platform.LocalContext

/**
 * Theme.kt = ghép màu (Color.kt) và kiểu chữ (Type.kt) thành một theme dùng chung cho app.
 */

// Gán từng màu gốc vào đúng vai trò của nó trong giao diện sáng.
private val LightColorScheme = lightColorScheme(
    primary = LightPrimary,
    onPrimary = LightOnPrimary,
    secondary = LightSecondary,
    onSecondary = LightOnSecondary,
    background = LightBackground,
    onBackground = LightOnBackground,
    surface = LightSurface,
    onSurface = LightOnSurface,
    error = LightError
)

// Tương tự cho giao diện tối.
private val DarkColorScheme = darkColorScheme(
    primary = DarkPrimary,
    onPrimary = DarkOnPrimary,
    secondary = DarkSecondary,
    onSecondary = DarkOnSecondary,
    background = DarkBackground,
    onBackground = DarkOnBackground,
    surface = DarkSurface,
    onSurface = DarkOnSurface,
    error = DarkError
)

/**
 * Bọc quanh toàn bộ giao diện (gọi trong MainActivity: setContent { WordPressClientTheme { ... } }).
 *
 * @param darkTheme    Mặc định theo cài đặt sáng/tối của điện thoại.
 * @param dynamicColor Nếu true và máy chạy Android 12 trở lên, dùng màu lấy từ hình nền
 *                     của người dùng thay vì bảng màu ở trên. Đặt false nếu muốn app
 *                     luôn có màu thương hiệu cố định.
 * @param content      Phần giao diện bên trong sẽ được áp dụng theme này.
 */
@Composable
fun WordPressClientTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    dynamicColor: Boolean = true,
    content: @Composable () -> Unit
) {
    val colorScheme = when {
        // Android 12 (API 31) trở lên mới có màu động.
        dynamicColor && Build.VERSION.SDK_INT >= Build.VERSION_CODES.S -> {
            val context = LocalContext.current
            if (darkTheme) dynamicDarkColorScheme(context) else dynamicLightColorScheme(context)
        }
        darkTheme -> DarkColorScheme
        else -> LightColorScheme
    }

    MaterialTheme(
        colorScheme = colorScheme,
        typography = AppTypography,
        content = content
    )
}