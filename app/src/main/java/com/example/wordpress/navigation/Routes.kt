package com.example.wordpressclient.navigation

import com.example.wordpressclient.feature.posts.detail.PostDetailViewModel
import com.example.wordpressclient.feature.posts.list.PostListViewModel

/**
 * Routes = nơi khai báo "địa chỉ" của từng màn hình trong app.
 *
 * Mỗi màn hình có một chuỗi route duy nhất, giống đường dẫn của trang web.
 * Gom hết vào một chỗ để tránh gõ sai chuỗi ở nhiều nơi.
 */
object Routes {

    /** Màn hình danh sách bài viết, cũng là màn hình khởi đầu của app. */
    const val POST_LIST = "posts"

    /**
     * Mẫu route của màn hình chi tiết, có chỗ trống {postId} để điền ID bài viết.
     * Tên "postId" lấy từ PostDetailViewModel.POST_ID_ARG, bảo đảm khớp với
     * cái ViewModel đọc ra.
     */
    const val POST_DETAIL = "posts/{${PostDetailViewModel.POST_ID_ARG}}"

    /** Màn hình tìm kiếm. */
    const val SEARCH = "search"

    /** Màn hình danh sách danh mục. */
    const val CATEGORY_LIST = "categories"

    /** Mẫu route danh sách bài của một danh mục, tên tham số lấy từ PostListViewModel. */
    const val CATEGORY_POSTS = "categories/{${PostListViewModel.CATEGORY_ID_ARG}}"

    /** Tạo route thật, ví dụ categoryPosts(5) cho ra "categories/5". */
    fun categoryPosts(categoryId: Int): String = "categories/$categoryId"


    /**
     * Tạo route thật để chuyển sang màn hình chi tiết một bài cụ thể.
     * Ví dụ: postDetail(42) cho ra "posts/42".
     */
    fun postDetail(postId: Int): String = "posts/$postId"
}