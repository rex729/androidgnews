package com.well.androidgnews.di

import android.util.Log
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.well.androidgnews.BuildConfig
import com.well.androidgnews.data.INewsRepository
import com.well.androidgnews.data.auth.FirebaseAuthRepository
import com.well.androidgnews.data.auth.IAuthRepository
import com.well.androidgnews.data.favorites.FirestoreFavoritesRepository
import com.well.androidgnews.data.favorites.IFavoritesRepository
import com.well.androidgnews.data.RemoteNewsRepository
import com.well.androidgnews.data.remote.ApiKeyInterceptor
import com.well.androidgnews.data.remote.INewsApiService
import com.well.androidgnews.data.remote.createNewsApiService
import java.util.concurrent.TimeUnit
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor

/**
 * Builds the app's shared objects by hand, in dependency order:
 * OkHttpClient → INewsApiService (Retrofit) → INewsRepository, plus Firebase Auth and Firestore
 * for sign-in and favorites.
 * One instance lives in [com.well.androidgnews.NewsApplication].
 */
class AppContainer(apiKey: String) {

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

    private val newsApiService: INewsApiService = createNewsApiService(INewsApiService.BASE_URL, okHttpClient)

    val newsRepository: INewsRepository = RemoteNewsRepository(newsApiService)

    val authRepository: IAuthRepository by lazy { FirebaseAuthRepository(FirebaseAuth.getInstance()) }

    val favoritesRepository: IFavoritesRepository by lazy {
        FirestoreFavoritesRepository(FirebaseFirestore.getInstance())
    }

    private companion object {
        val apiKeyInUrl = Regex("${ApiKeyInterceptor.QUERY_PARAM}=[^&\\s]+")
    }
}
