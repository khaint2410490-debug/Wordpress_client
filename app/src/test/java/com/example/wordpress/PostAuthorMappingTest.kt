package com.example.wordpress

import com.example.wordpressclient.data.remote.dto.PostDto
import kotlinx.serialization.json.Json
import org.junit.Assert.assertEquals
import org.junit.Test

class PostAuthorMappingTest {

    @Test
    fun embeddedAuthorAndAvatarAreMappedFromWordPressResponse() {
        val json = Json { ignoreUnknownKeys = true }
        val response = """
            {
              "id": 16,
              "date": "2026-10-22T21:57:19",
              "title": {"rendered": "Sample post"},
              "content": {"rendered": "<p>Sample content</p>"},
              "author": 285881470,
              "featured_media": 0,
              "_embedded": {
                "author": [{
                  "name": "Tùng",
                  "avatar_urls": {
                    "24": "https://example.com/avatar-24.png",
                    "96": "https://example.com/avatar-96.png"
                  }
                }],
                "wp:featuredmedia": []
              }
            }
        """.trimIndent()

        val post = json.decodeFromString<PostDto>(response)

        assertEquals("Tùng", post.embedded?.author?.firstOrNull()?.name)
        assertEquals(
            "https://example.com/avatar-96.png",
            post.embedded?.author?.firstOrNull()?.avatarUrls?.get("96")
        )
    }
}
