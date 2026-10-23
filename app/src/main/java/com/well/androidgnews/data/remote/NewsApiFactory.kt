package com.well.androidgnews.data.remote

import kotlinx.serialization.json.Json
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import retrofit2.Retrofit
import retrofit2.converter.kotlinx.serialization.asConverterFactory

private val json = Json { ignoreUnknownKeys = true }

/** Builds the Retrofit-backed [INewsApiService]. Shared by the app and its contract tests. */
fun createNewsApiService(baseUrl: String, client: OkHttpClient): INewsApiService =
    Retrofit.Builder()
        .baseUrl(baseUrl)
        .client(client)
        .addConverterFactory(json.asConverterFactory("application/json".toMediaType()))
        .build()
        .create(INewsApiService::class.java)
