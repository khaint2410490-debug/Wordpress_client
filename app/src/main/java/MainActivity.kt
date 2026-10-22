package com.example.wordpressclient

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import com.example.wordpressclient.core.ui.theme.WordPressClientTheme
import com.example.wordpressclient.navigation.AppNavHost
import dagger.hilt.android.AndroidEntryPoint

/**
 * MainActivity = màn hình (Activity) duy nhất của app.
 *
 * App dùng kiểu "một Activity, nhiều màn hình Compose": Activity này chỉ làm khung,
 * còn việc chuyển giữa danh sách, chi tiết, tìm kiếm... do AppNavHost đảm nhiệm.
 *
 * @AndroidEntryPoint cho phép Hilt cung cấp dependency cho Activity này và cho các
 * ViewModel được lấy bằng hiltViewModel() bên trong các màn hình Compose.
 * Thiếu annotation này, hiltViewModel() sẽ báo lỗi khi chạy.
 */
@AndroidEntryPoint
class MainActivity : ComponentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // setContent: đặt giao diện Compose làm nội dung của Activity.
        setContent {
            // Áp dụng theme (màu, kiểu chữ, sáng/tối) cho mọi thứ bên trong.
            WordPressClientTheme {
                // Toàn bộ điều hướng giữa các màn hình.
                AppNavHost()
            }
        }
    }
}