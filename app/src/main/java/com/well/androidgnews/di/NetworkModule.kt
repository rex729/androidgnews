package com.well.androidgnews.di

import android.util.Log
import com.well.androidgnews.BuildConfig
import com.well.androidgnews.data.remote.ApiKeyInterceptor
import com.well.androidgnews.data.remote.GNewsApiService
import dagger.Module
import dagger.Provides
import java.util.concurrent.TimeUnit
import javax.inject.Singleton
import kotlinx.serialization.json.Json
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.kotlinx.serialization.asConverterFactory

@Module
object NetworkModule {

    private val apiKeyInUrl = Regex("${ApiKeyInterceptor.QUERY_PARAM}=[^&\\s]+")

    @Provides
    @Singleton
    fun provideJson(): Json = Json { ignoreUnknownKeys = true }

    @Provides
    @Singleton
    fun provideOkHttpClient(apiKeyInterceptor: ApiKeyInterceptor): OkHttpClient =
        OkHttpClient.Builder()
            .addInterceptor(apiKeyInterceptor)
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

    @Provides
    @Singleton
    fun provideRetrofit(client: OkHttpClient, json: Json): Retrofit =
        Retrofit.Builder()
            .baseUrl(GNewsApiService.BASE_URL)
            .client(client)
            .addConverterFactory(json.asConverterFactory("application/json".toMediaType()))
            .build()

    @Provides
    @Singleton
    fun provideGNewsApiService(retrofit: Retrofit): GNewsApiService =
        retrofit.create(GNewsApiService::class.java)
}
