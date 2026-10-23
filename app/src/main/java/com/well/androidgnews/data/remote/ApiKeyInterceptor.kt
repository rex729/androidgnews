package com.well.androidgnews.data.remote

import okhttp3.Interceptor
import okhttp3.Response

class ApiKeyInterceptor(
    private val apiKey: String,
) : Interceptor {

    override fun intercept(chain: Interceptor.Chain): Response {
        val request = chain.request()
        val url = request.url.newBuilder()
            .addQueryParameter(QUERY_PARAM, apiKey)
            .build()
        return chain.proceed(request.newBuilder().url(url).build())
    }

    companion object {
        const val QUERY_PARAM = "apikey"
    }
}
