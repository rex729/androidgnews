package com.well.androidgnews.testing

import com.well.androidgnews.data.RemoteNewsRepository
import com.well.androidgnews.data.model.Category
import com.well.androidgnews.data.remote.INewsApiService
import com.well.androidgnews.data.remote.dto.ArticleDto
import com.well.androidgnews.data.remote.dto.ArticlesResponse
import com.well.androidgnews.data.remote.dto.SourceDto
import java.time.Instant
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test

class RemoteNewsRepositoryTest {

    private val api = FakeNewsApiService()
    private val repository = RemoteNewsRepository(api)

    @Test
    fun `top headlines asks the backend for the category`() = runTest {
        repository.topHeadlines(Category.TECHNOLOGY)

        assertEquals(listOf(Request("headlines", "technology")), api.requests)
    }

    @Test
    fun `search asks the backend for the query`() = runTest {
        repository.search("台積電")

        assertEquals(listOf(Request("search", "台積電")), api.requests)
    }

    @Test
    fun `maps api fields to articles`() = runTest {
        api.response = response(
            ArticleDto(
                title = "標題",
                description = "摘要",
                url = "https://news/1",
                image = "https://img/1.jpg",
                publishedAt = "2026-10-23T01:02:03Z",
                source = SourceDto(name = "中央社"),
            ),
        )

        val article = repository.topHeadlines(Category.GENERAL).single()

        assertEquals("標題", article.title)
        assertEquals("摘要", article.description)
        assertEquals("https://news/1", article.url)
        assertEquals("https://img/1.jpg", article.imageUrl)
        assertEquals(Instant.parse("2026-10-23T01:02:03Z"), article.publishedAt)
        assertEquals("中央社", article.sourceName)
    }

    @Test
    fun `unparseable date becomes null instead of failing`() = runTest {
        api.response = response(dto("https://news/1").copy(publishedAt = "yesterday"))

        assertNull(repository.topHeadlines(Category.GENERAL).single().publishedAt)
    }

    @Test
    fun `missing source becomes empty name`() = runTest {
        api.response = response(dto("https://news/1").copy(source = null))

        assertEquals("", repository.topHeadlines(Category.GENERAL).single().sourceName)
    }

    @Test
    fun `duplicate urls are dropped keeping the first`() = runTest {
        api.response = response(
            dto("https://news/1").copy(title = "first"),
            dto("https://news/2"),
            dto("https://news/1").copy(title = "duplicate"),
        )

        val articles = repository.search("q")

        assertEquals(listOf("https://news/1", "https://news/2"), articles.map { it.url })
        assertEquals("first", articles.first().title)
    }

    private fun dto(url: String) = ArticleDto(title = "title", url = url)

    private fun response(vararg articles: ArticleDto) =
        ArticlesResponse(totalArticles = articles.size, articles = articles.toList())

    private data class Request(val endpoint: String, val term: String)

    /** Stands in for the Retrofit-generated service: records the call and returns [response]. */
    private class FakeNewsApiService : INewsApiService {
        var response = ArticlesResponse()
        val requests = mutableListOf<Request>()

        override suspend fun headlines(category: String): ArticlesResponse {
            requests += Request("headlines", category)
            return response
        }

        override suspend fun search(query: String): ArticlesResponse {
            requests += Request("search", query)
            return response
        }
    }
}
