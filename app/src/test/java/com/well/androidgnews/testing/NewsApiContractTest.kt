package com.well.androidgnews.testing

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
 * Checks the app's half of the contract with ../AndroidGNewsBackend: the exact paths and query
 * parameters Retrofit sends, and that a response in the backend's JSON shape parses. The other
 * half (the backend serving those routes) is covered by the backend's tests/test_api.py.
 */
class NewsApiContractTest {

    private val sent = mutableListOf<Request>()
    private val api = createNewsApiService(BASE_URL, fakeBackend(fixture("backend/headlines.json")))

    @Test
    fun `headlines calls GET api headlines with the category`() = runTest {
        api.headlines("technology")

        val request = sent.single()
        assertEquals("GET", request.method)
        assertEquals("/api/headlines", request.url.encodedPath)
        assertEquals("technology", request.url.queryParameter("category"))
        assertEquals(setOf("category"), request.url.queryParameterNames)
    }

    @Test
    fun `search calls GET api search with the query`() = runTest {
        api.search("台積電 財報")

        val request = sent.single()
        assertEquals("/api/search", request.url.encodedPath)
        assertEquals("台積電 財報", request.url.queryParameter("q"))
        assertEquals(setOf("q"), request.url.queryParameterNames)
    }

    @Test
    fun `backend response parses, ignoring fields the app doesn't use`() = runTest {
        val response = api.headlines("general")

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
        val second = api.headlines("general").articles[1]

        assertNull(second.image)
        assertNull(second.description)
        assertNull(second.source)
    }

    /** An OkHttp client whose "server" records each request and answers [body], with no real network. */
    private fun fakeBackend(body: String) = OkHttpClient.Builder()
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

    private companion object {
        const val BASE_URL = "http://backend.test/"
    }
}
