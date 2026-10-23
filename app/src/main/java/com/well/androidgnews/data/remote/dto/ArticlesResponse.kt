package com.well.androidgnews.data.remote.dto

import kotlinx.serialization.Serializable

@Serializable
data class ArticlesResponse(
    val totalArticles: Int = 0,
    val articles: List<ArticleDto> = emptyList(),
)

@Serializable
data class ArticleDto(
    val title: String,
    val description: String? = null,
    val url: String,
    val image: String? = null,
    val publishedAt: String? = null,
    val source: SourceDto? = null,
)

@Serializable
data class SourceDto(
    val name: String? = null,
    val url: String? = null,
)
