package com.example.wordpressclient.data.local

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import kotlinx.coroutines.flow.Flow

@Dao
interface SavedPostDao {
    @Query("SELECT * FROM saved_posts ORDER BY savedAt DESC")
    fun observeSavedPosts(): Flow<List<SavedPostEntity>>

    @Query("SELECT id FROM saved_posts")
    fun observeSavedPostIds(): Flow<List<Int>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun save(post: SavedPostEntity)

    @Query("DELETE FROM saved_posts WHERE id = :postId")
    suspend fun remove(postId: Int)
}
