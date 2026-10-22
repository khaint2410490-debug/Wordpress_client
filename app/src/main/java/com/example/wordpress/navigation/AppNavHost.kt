package com.example.wordpressclient.navigation

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.example.wordpressclient.feature.posts.detail.PostDetailScreen
import com.example.wordpressclient.feature.posts.detail.PostDetailViewModel
import com.example.wordpressclient.feature.posts.list.PostListScreen
import com.example.wordpressclient.feature.search.SearchScreen

/**
 * AppNavHost = nơi nối các màn hình lại với nhau.
 *
 * NavHost giữ "ngăn xếp" các màn hình đã mở. Mỗi khối composable(...) bên dưới nói:
 * "khi đang ở route này thì hiện màn hình kia".
 *
 * Mọi việc chuyển màn hình nằm ở đây. Các Screen chỉ nhận callback (onPostClick,
 * onBackClick...) và không biết navController hay route, nên dễ tái sử dụng và kiểm thử.
 *
 * @param navController Cho phép truyền từ ngoài vào (ví dụ MainActivity) khi sau này
 *                      cần điều khiển điều hướng từ thanh dưới. Không truyền thì tự tạo.
 */
@Composable
fun AppNavHost(
    modifier: Modifier = Modifier,
    navController: NavHostController = rememberNavController()
) {
    NavHost(
        navController = navController,
        // Màn hình hiện đầu tiên khi mở app.
        startDestination = Routes.POST_LIST,
        modifier = modifier
    ) {

        // ----- Danh sách bài viết -----
        composable(route = Routes.POST_LIST) {
            PostListScreen(
                onSearchClick = {
                    navController.navigate(Routes.SEARCH)
                },
                // Bấm vào một bài thì mở màn hình chi tiết với ID tương ứng.
                onPostClick = { postId ->
                    navController.navigate(Routes.postDetail(postId))
                }
            )
        }

        // ----- Chi tiết bài viết -----
        composable(
            route = Routes.POST_DETAIL,
            // Khai báo tham số postId là số nguyên để Navigation truyền đúng kiểu Int
            // vào SavedStateHandle của PostDetailViewModel.
            arguments = listOf(
                navArgument(PostDetailViewModel.POST_ID_ARG) { type = NavType.IntType }
            )
        ) {
            PostDetailScreen(
                // Bấm nút quay lại thì bỏ màn hình hiện tại khỏi ngăn xếp.
                onBackClick = { navController.popBackStack() }
            )
        }

        // ----- Tìm kiếm -----
        composable(route = Routes.SEARCH) {
            SearchScreen(
                // Bấm vào kết quả cũng mở màn hình chi tiết giống danh sách.
                onPostClick = { postId ->
                    navController.navigate(Routes.postDetail(postId))
                }
            )
        }
    }
}