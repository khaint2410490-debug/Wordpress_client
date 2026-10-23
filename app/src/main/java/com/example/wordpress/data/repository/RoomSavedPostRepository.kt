package com.example.wordpressclient.data.repository

import com.example.wordpressclient.data.local.SavedPostDao
import com.example.wordpressclient.data.local.SavedPostEntity
import com.example.wordpressclient.data.local.toDomain
import com.example.wordpressclient.data.local.toSavedPostEntity
import com.example.wordpressclient.domain.model.Post
import com.example.wordpressclient.domain.repository.SavedPostRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject

class RoomSavedPostRepository @Inject constructor(
    private val dao: SavedPostDao
) : SavedPostRepository {
    override fun observeSavedPosts(): Flow<List<Post>> =
        dao.observeSavedPosts().map { posts -> posts.map(SavedPostEntity::toDomain) }

    override fun observeSavedPostIds(): Flow<Set<Int>> =
        dao.observeSavedPostIds().map(List<Int>::toSet)

    override suspend fun savePost(post: Post) {
        dao.save(post.toSavedPostEntity())
    }

    override suspend fun removeSavedPost(postId: Int) {
        dao.remove(postId)
    }
}
