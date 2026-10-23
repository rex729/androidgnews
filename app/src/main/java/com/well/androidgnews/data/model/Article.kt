package com.well.androidgnews.data.model

import java.time.Instant

data class Article(
    val title: String,
    val description: String?,
    val url: String,
    val imageUrl: String?,
    val publishedAt: Instant?,
    val sourceName: String,
)
