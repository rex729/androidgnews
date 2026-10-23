package com.well.androidgnews.di

import com.well.androidgnews.BuildConfig
import com.well.androidgnews.data.INewsRepository
import com.well.androidgnews.data.RemoteNewsRepository
import com.well.androidgnews.data.remote.INewsApiService
import com.well.androidgnews.data.remote.certificatePinnerFor
import com.well.androidgnews.data.remote.createNewsApiService
import java.util.concurrent.TimeUnit
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor

/**
 * Builds the app's shared objects by hand, in dependency order:
 * OkHttpClient → INewsApiService (Retrofit) → INewsRepository.
 * One instance lives in [com.well.androidgnews.NewsApplication].
 */
class AppContainer(backendBaseUrl: String, backendCertPins: List<String>) {

    private val okHttpClient: OkHttpClient = OkHttpClient.Builder()
        .apply {
            certificatePinnerFor(backendBaseUrl, backendCertPins)?.let(::certificatePinner)
            if (BuildConfig.DEBUG) {
                addInterceptor(HttpLoggingInterceptor().apply { level = HttpLoggingInterceptor.Level.BASIC })
            }
        }
        .connectTimeout(15, TimeUnit.SECONDS)
        .readTimeout(15, TimeUnit.SECONDS)
        .build()

    private val newsApiService: INewsApiService = createNewsApiService(backendBaseUrl, okHttpClient)

    val newsRepository: INewsRepository = RemoteNewsRepository(newsApiService)
}
