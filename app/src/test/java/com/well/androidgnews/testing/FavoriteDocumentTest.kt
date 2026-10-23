package com.well.androidgnews.testing

import com.well.androidgnews.data.favorites.FavoriteDocument
import com.well.androidgnews.data.model.Article
import java.io.File
import java.time.Instant
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotEquals
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test

class FavoriteDocumentTest {

    private val article = Article(
        title = "標題",
        description = "摘要",
        url = "https://news.example/a/b?c=1",
        imageUrl = "https://img.example/1.jpg",
        publishedAt = Instant.parse("2026-10-23T01:02:03Z"),
        sourceName = "中央社",
    )

    @Test
    fun `document id is a stable sha-256 hex of the url, safe as a firestore id`() {
        val id = FavoriteDocument.documentId(article.url)

        assertEquals(id, FavoriteDocument.documentId(article.url))
        assertTrue(id.matches(Regex("[0-9a-f]{64}")))
        assertNotEquals(id, FavoriteDocument.documentId("https://news.example/other"))
    }

    @Test
    fun `article survives a round trip through the stored fields`() {
        assertEquals(article, FavoriteDocument.articleFrom(FavoriteDocument.fieldsOf(article)))
    }

    @Test
    fun `optional fields may be missing`() {
        val fields = mapOf(FavoriteDocument.TITLE to "t", FavoriteDocument.URL to "https://x")

        val restored = FavoriteDocument.articleFrom(fields)!!

        assertNull(restored.description)
        assertNull(restored.imageUrl)
        assertNull(restored.publishedAt)
        assertEquals("", restored.sourceName)
    }

    @Test
    fun `documents without title or url are skipped`() {
        assertNull(FavoriteDocument.articleFrom(mapOf(FavoriteDocument.URL to "https://x")))
        assertNull(FavoriteDocument.articleFrom(mapOf(FavoriteDocument.TITLE to "t")))
    }

    @Test
    fun `unparseable stored date becomes null`() {
        val fields = FavoriteDocument.fieldsOf(article) + (FavoriteDocument.PUBLISHED_AT to "yesterday")

        assertNull(FavoriteDocument.articleFrom(fields)!!.publishedAt)
    }

    @Test
    fun `stored fields match the allow-list in firestore rules`() {
        // A field the rules don't allow would make every save fail with PERMISSION_DENIED.
        val rules = File("../firestore.rules").readText()
        val allowed = Regex("""hasOnly\(\s*\[([^\]]*)]""").find(rules)!!.groupValues[1]
            .split(',').map { it.trim().trim('\'') }.toSet()

        assertEquals(allowed, FavoriteDocument.fieldsOf(article).keys + FavoriteDocument.SAVED_AT)
    }
}
