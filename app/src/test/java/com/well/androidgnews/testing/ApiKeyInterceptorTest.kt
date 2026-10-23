package com.well.androidgnews.testing

import com.well.androidgnews.data.remote.ApiKeyInterceptor
import okhttp3.Interceptor
import okhttp3.OkHttpClient
import okhttp3.Protocol
import okhttp3.Request
import okhttp3.Response
import okhttp3.ResponseBody.Companion.toResponseBody
import org.junit.Assert.assertEquals
import org.junit.Test

class ApiKeyInterceptorTest {

    @Test
    fun `adds the api key as a query parameter`() {
        val sent = send("https://gnews.io/api/v4/search?q=news")

        assertEquals("secret", sent.url.queryParameter("apikey"))
    }

    @Test
    fun `keeps the existing query parameters`() {
        val sent = send("https://gnews.io/api/v4/search?q=news&lang=zh")

        assertEquals("news", sent.url.queryParameter("q"))
        assertEquals("zh", sent.url.queryParameter("lang"))
    }

    /**
     * Sends a request through [ApiKeyInterceptor] and returns what would have gone out.
     * A second interceptor answers in place of the network, so no real connection is made.
     */
    private fun send(url: String): Request {
        var sent: Request? = null
        val client = OkHttpClient.Builder()
            .addInterceptor(ApiKeyInterceptor("secret"))
            .addInterceptor(
                Interceptor { chain ->
                    sent = chain.request()
                    Response.Builder()
                        .request(chain.request())
                        .protocol(Protocol.HTTP_1_1)
                        .code(200)
                        .message("OK")
                        .body("".toResponseBody())
                        .build()
                },
            )
            .build()
        client.newCall(Request.Builder().url(url).build()).execute().close()
        return checkNotNull(sent)
    }
}
