package com.well.androidgnews.data.remote

import com.well.androidgnews.data.remote.dto.ArticlesResponse
import retrofit2.http.GET
import retrofit2.http.Query

/**
 * The app's own backend (../AndroidGNewsBackend). It holds the GNews API key, fixes the
 * language/country filters, and caches responses, so the app only says what it wants.
 */
interface INewsApiService {

    @GET("api/headlines")
    suspend fun headlines(@Query("category") category: String): ArticlesResponse

    @GET("api/search")
    suspend fun search(@Query("q") query: String): ArticlesResponse
}
