package com.well.androidgnews.di

import android.util.Log
import com.well.androidgnews.BuildConfig
import com.well.androidgnews.data.GNewsRepository
import com.well.androidgnews.data.INewsRepository
import com.well.androidgnews.data.remote.ApiKeyInterceptor
import com.well.androidgnews.data.remote.IGNewsApiService
import java.util.concurrent.TimeUnit
import kotlinx.serialization.json.Json
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.kotlinx.serialization.asConverterFactory

/**
 * Builds the app's shared objects by hand, in dependency order:
 * OkHttpClient → Retrofit → IGNewsApiService → INewsRepository.
 * One instance lives in [com.well.androidgnews.NewsApplication].
 */
class AppContainer(apiKey: String) {

    private val json = Json { ignoreUnknownKeys = true }

    private val okHttpClient: OkHttpClient = OkHttpClient.Builder()
        .addInterceptor(ApiKeyInterceptor(apiKey))
        .apply {
            if (BuildConfig.DEBUG) {
                // Logged URLs carry the key, so mask it before it hits logcat.
                val logging = HttpLoggingInterceptor { message ->
                    Log.d("OkHttp", message.replace(apiKeyInUrl, "${ApiKeyInterceptor.QUERY_PARAM}=***"))
                }
                logging.level = HttpLoggingInterceptor.Level.BASIC
                addInterceptor(logging)
            }
        }
        .connectTimeout(15, TimeUnit.SECONDS)
        .readTimeout(15, TimeUnit.SECONDS)
        .build()

    private val retrofit: Retrofit = Retrofit.Builder()
        .baseUrl(IGNewsApiService.BASE_URL)
        .client(okHttpClient)
        .addConverterFactory(json.asConverterFactory("application/json".toMediaType()))
        .build()

    private val gNewsApiService: IGNewsApiService = retrofit.create(IGNewsApiService::class.java)

    val newsRepository: INewsRepository = GNewsRepository(gNewsApiService)

    private companion object {
        val apiKeyInUrl = Regex("${ApiKeyInterceptor.QUERY_PARAM}=[^&\\s]+")
    }
}
