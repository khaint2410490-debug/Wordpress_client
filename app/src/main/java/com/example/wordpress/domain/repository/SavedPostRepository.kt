package com.example.wordpressclient.domain.repository

import com.example.wordpressclient.domain.model.Post
import kotlinx.coroutines.flow.Flow

interface SavedPostRepository {
    fun observeSavedPosts(): Flow<List<Post>>

    fun observeSavedPostIds(): Flow<Set<Int>>

    suspend fun savePost(post: Post)

    suspend fun removeSavedPost(postId: Int)
}
