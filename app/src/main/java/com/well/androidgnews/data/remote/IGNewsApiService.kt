package com.well.androidgnews.data.remote

import com.well.androidgnews.data.remote.dto.ArticlesResponse
import retrofit2.http.GET
import retrofit2.http.Query

/** https://gnews.io/docs/v4 — the API key is appended by [ApiKeyInterceptor]. */
interface IGNewsApiService {

    @GET("top-headlines")
    suspend fun topHeadlines(
        @Query("category") category: String,
        @Query("lang") lang: String,
        @Query("country") country: String,
        @Query("max") max: Int,
    ): ArticlesResponse

    @GET("search")
    suspend fun search(
        @Query("q") query: String,
        @Query("lang") lang: String,
        @Query("country") country: String,
        @Query("max") max: Int,
    ): ArticlesResponse

    companion object {
        const val BASE_URL = "https://gnews.io/api/v4/"
    }
}
