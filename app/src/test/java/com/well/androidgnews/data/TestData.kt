package com.well.androidgnews.data

import com.well.androidgnews.data.model.Article
import okhttp3.ResponseBody.Companion.toResponseBody
import retrofit2.HttpException
import retrofit2.Response

fun article(url: String, title: String = "title for $url") = Article(
    title = title,
    description = null,
    url = url,
    imageUrl = null,
    publishedAt = null,
    sourceName = "source",
)

/** The exception Retrofit throws for a non-2xx response with the given status [code]. */
fun httpException(code: Int) = HttpException(Response.error<Any>(code, "".toResponseBody()))
