package com.example.wordpressclient.data.local

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Transaction

@Dao
interface CachedPostDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsertPosts(posts: List<CachedPostEntity>)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsertPost(post: CachedPostEntity)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsertListEntries(entries: List<CachedPostListEntry>)

    @Query(
        """
        SELECT cached_posts.* FROM cached_posts
        INNER JOIN cached_post_list ON cached_posts.id = cached_post_list.postId
        ORDER BY cached_post_list.position ASC
        """
    )
    suspend fun getCachedPostList(): List<CachedPostEntity>

    @Query("SELECT * FROM cached_posts WHERE id = :postId LIMIT 1")
    suspend fun getCachedPost(postId: Int): CachedPostEntity?

    @Query("DELETE FROM cached_post_list")
    suspend fun clearCachedPostList()

    @Transaction
    suspend fun cachePostListPage(
        posts: List<CachedPostEntity>,
        page: Int,
        perPage: Int
    ) {
        if (page == 1) clearCachedPostList()
        upsertPosts(posts)
        upsertListEntries(
            posts.mapIndexed { index, post ->
                CachedPostListEntry(
                    postId = post.id,
                    position = (page - 1) * perPage + index
                )
            }
        )
    }
}
