package com.example.wordpressclient.data.local

import androidx.room.Entity
import androidx.room.PrimaryKey
import com.example.wordpressclient.domain.model.Post

@Entity(tableName = "saved_posts")
data class SavedPostEntity(
    @PrimaryKey val id: Int,
    val title: String,
    val excerpt: String,
    val contentHtml: String,
    val date: String,
    val link: String,
    val featuredImageUrl: String?,
    val authorName: String?,
    val authorAvatarUrl: String?,
    val savedAt: Long
)

fun Post.toSavedPostEntity(savedAt: Long = System.currentTimeMillis()) = SavedPostEntity(
    id = id,
    title = title,
    excerpt = excerpt,
    contentHtml = contentHtml,
    date = date,
    link = link,
    featuredImageUrl = featuredImageUrl,
    authorName = authorName,
    authorAvatarUrl = authorAvatarUrl,
    savedAt = savedAt
)

fun SavedPostEntity.toDomain() = Post(
    id = id,
    title = title,
    excerpt = excerpt,
    contentHtml = contentHtml,
    date = date,
    link = link,
    featuredImageUrl = featuredImageUrl,
    authorName = authorName,
    authorAvatarUrl = authorAvatarUrl
)
