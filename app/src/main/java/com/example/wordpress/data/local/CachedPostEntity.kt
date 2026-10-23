package com.example.wordpressclient.data.local

import androidx.room.Entity
import androidx.room.PrimaryKey
import com.example.wordpressclient.domain.model.Post

@Entity(tableName = "cached_posts")
data class CachedPostEntity(
    @PrimaryKey val id: Int,
    val title: String,
    val excerpt: String,
    val contentHtml: String,
    val date: String,
    val link: String,
    val featuredImageUrl: String?,
    val authorName: String?,
    val authorAvatarUrl: String?,
    val cachedAt: Long
)

@Entity(tableName = "cached_post_list")
data class CachedPostListEntry(
    @PrimaryKey val postId: Int,
    val position: Int
)

fun Post.toCachedPostEntity(cachedAt: Long = System.currentTimeMillis()) = CachedPostEntity(
    id = id,
    title = title,
    excerpt = excerpt,
    contentHtml = contentHtml,
    date = date,
    link = link,
    featuredImageUrl = featuredImageUrl,
    authorName = authorName,
    authorAvatarUrl = authorAvatarUrl,
    cachedAt = cachedAt
)

fun CachedPostEntity.toDomain() = Post(
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
