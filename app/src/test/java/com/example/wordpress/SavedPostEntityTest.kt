package com.example.wordpress

import com.example.wordpressclient.data.local.SavedPostEntity
import com.example.wordpressclient.data.local.toCachedPostEntity
import com.example.wordpressclient.data.local.toDomain
import com.example.wordpressclient.data.local.toSavedPostEntity
import com.example.wordpressclient.domain.model.Post
import org.junit.Assert.assertEquals
import org.junit.Test

class SavedPostEntityTest {
    @Test
    fun savedPostRoundTripsAllDisplayFields() {
        val post = Post(
            id = 42,
            title = "Saved post",
            excerpt = "Excerpt",
            contentHtml = "<p>Content</p>",
            date = "2026-10-23T12:00:00",
            link = "https://example.com/post",
            featuredImageUrl = "https://example.com/featured.jpg",
            authorName = "Author",
            authorAvatarUrl = "https://example.com/avatar.jpg"
        )

        val entity = post.toSavedPostEntity(savedAt = 123L)

        assertEquals(post, entity.toDomain())
        assertEquals(123L, entity.savedAt)
        assertEquals(post.id, entity.id)
    }

    @Test
    fun savedPostEntityCanBeReadAfterStorage() {
        val entity = SavedPostEntity(
            id = 7,
            title = "Stored",
            excerpt = "",
            contentHtml = "",
            date = "2026-10-23",
            link = "",
            featuredImageUrl = null,
            authorName = null,
            authorAvatarUrl = null,
            savedAt = 1L
        )

        assertEquals("Stored", entity.toDomain().title)
        assertEquals(7, entity.toDomain().id)
    }

    @Test
    fun cachedPostRoundTripsForOfflineReading() {
        val post = Post(
            id = 15,
            title = "Offline post",
            excerpt = "Cached excerpt",
            contentHtml = "<p>Cached body</p>",
            date = "2026-10-23T12:00:00",
            link = "https://example.com/offline",
            featuredImageUrl = null,
            authorName = "Writer",
            authorAvatarUrl = "https://example.com/writer.jpg"
        )

        val cached = post.toCachedPostEntity(cachedAt = 456L)

        assertEquals(post, cached.toDomain())
        assertEquals(456L, cached.cachedAt)
        assertEquals(15, cached.id)
    }
}
