package com.well.androidgnews.testing

import com.well.androidgnews.data.remote.INewsApiService
import com.well.androidgnews.data.remote.createNewsApiService
import kotlinx.coroutines.test.runTest
import okhttp3.Interceptor
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.Protocol
import okhttp3.Request
import okhttp3.Response
import okhttp3.ResponseBody.Companion.toResponseBody
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test

/**
 * Checks what Retrofit actually sends to GNews (base URL, paths, query parameters) and that a
 * response in GNews's JSON shape parses. A fake "server" answers, so no request leaves the machine.
 */
class NewsApiContractTest {

    private val sent = mutableListOf<Request>()
    private val api = createNewsApiService(INewsApiService.BASE_URL, fakeGNews(fixture("gnews/headlines.json")))

    @Test
    fun `top headlines calls GET gnews top-headlines with category and filters`() = runTest {
        api.topHeadlines("technology", "zh", "tw", 10)

        val request = sent.single()
        assertEquals("GET", request.method)
        assertEquals("gnews.io", request.url.host)
        assertEquals("/api/v4/top-headlines", request.url.encodedPath)
        assertEquals("technology", request.url.queryParameter("category"))
        assertEquals("zh", request.url.queryParameter("lang"))
        assertEquals("tw", request.url.queryParameter("country"))
        assertEquals("10", request.url.queryParameter("max"))
    }

    @Test
    fun `search calls GET gnews search with the query`() = runTest {
        api.search("台積電 財報", "zh", "tw", 10)

        val request = sent.single()
        assertEquals("/api/v4/search", request.url.encodedPath)
        assertEquals("台積電 財報", request.url.queryParameter("q"))
    }

    @Test
    fun `gnews response parses, ignoring fields the app doesn't use`() = runTest {
        val response = api.topHeadlines("general", "zh", "tw", 10)

        assertEquals(2, response.totalArticles)
        val first = response.articles[0]
        assertEquals("台積電宣布擴大先進封裝產能", first.title)
        assertEquals("https://example.com/news/1", first.url)
        assertEquals("https://example.com/img/1.jpg", first.image)
        assertEquals("2026-10-23T01:02:03Z", first.publishedAt)
        assertEquals("中央社", first.source?.name)
    }

    @Test
    fun `null image description and source parse as null`() = runTest {
        val second = api.topHeadlines("general", "zh", "tw", 10).articles[1]

        assertNull(second.image)
        assertNull(second.description)
        assertNull(second.source)
    }

    /** An OkHttp client whose "server" records each request and answers [body], with no real network. */
    private fun fakeGNews(body: String) = OkHttpClient.Builder()
        .addInterceptor(
            Interceptor { chain ->
                sent += chain.request()
                Response.Builder()
                    .request(chain.request())
                    .protocol(Protocol.HTTP_1_1)
                    .code(200)
                    .message("OK")
                    .body(body.toResponseBody("application/json".toMediaType()))
                    .build()
            },
        )
        .build()

    private fun fixture(path: String): String =
        checkNotNull(javaClass.classLoader?.getResource(path)) { "missing test resource $path" }.readText()
}
