package com.example.wordpressclient.data.repository

import com.example.wordpressclient.core.common.AppError
import com.example.wordpressclient.data.local.CachedPostDao
import com.example.wordpressclient.data.local.CachedPostEntity
import com.example.wordpressclient.data.local.toCachedPostEntity
import com.example.wordpressclient.data.local.toDomain
import com.example.wordpressclient.data.mapper.toDomain
import com.example.wordpressclient.data.remote.WordPressApi
import com.example.wordpressclient.domain.model.Post
import com.example.wordpressclient.domain.repository.PostRepository
import retrofit2.HttpException
import javax.inject.Inject

/**
 * WordPressPostRepository = phần cài đặt thật của PostRepository.
 *
 * Việc của lớp này: gọi WordPressApi, nhận PostDto, đổi sang Post bằng PostMapper,
 * rồi gói kết quả vào Result để ViewModel dùng.
 *
 * - @Inject constructor: Hilt tự tạo lớp này và truyền WordPressApi vào
 *   (WordPressApi được cung cấp bởi NetworkModule).
 * - Retrofit với hàm suspend tự chạy ở luồng nền, nên ở đây không cần đổi luồng thủ công.
 */
class WordPressPostRepository @Inject constructor(
    private val api: WordPressApi,
    private val cachedPostDao: CachedPostDao
) : PostRepository {

    /**
     * Lấy danh sách bài viết, rồi chuyển từng PostDto thành Post.
     */
    override suspend fun getPosts(
        page: Int,
        perPage: Int,
        search: String?,
        categoryId: Int?
    ): Result<List<Post>> {
        val result = safeApiCall {
            val response = api.getPosts(
                page = page,
                perPage = perPage,
                search = search,
                categories = categoryId,
                embed = "author,wp:featuredmedia"
            )

            val posts = when {
                response.isSuccessful -> response.body().orEmpty().toDomain()
                response.code() == 400 && page > 1 -> emptyList()
                else -> throw HttpException(response)
            }

            if (search == null && categoryId == null && (page == 1 || posts.isNotEmpty())) {
                cachedPostDao.cachePostListPage(
                    posts = posts.map(Post::toCachedPostEntity),
                    page = page,
                    perPage = perPage
                )
            }

            posts
        }

        return result.recoverCatching { error ->
            if (
                error is AppError.Network &&
                search == null &&
                categoryId == null
            ) {
                val cachedPosts = cachedPostDao.getCachedPostList()
                if (cachedPosts.isNotEmpty()) {
                    return@recoverCatching cachedPosts
                        .drop((page - 1) * perPage)
                        .take(perPage)
                        .map(CachedPostEntity::toDomain)
                }
            }
            throw error
        }
    }

    /**
     * Lấy một bài viết theo ID rồi chuyển sang Post.
     * Nếu ID không tồn tại, Retrofit ném HttpException (404) và safeApiCall chuyển thành NotFound.
     */
    override suspend fun getPostById(id: Int): Result<Post> {
        val result = safeApiCall {
            api.getPostById(id, embed = "author,wp:featuredmedia")
                .toDomain()
                .also { post ->
                    cachedPostDao.upsertPost(post.toCachedPostEntity())
                }
        }

        return result.recoverCatching { error ->
            if (error is AppError.Network) {
                cachedPostDao.getCachedPost(id)?.toDomain() ?: throw error
            } else {
                throw error
            }
        }
    }
}