package com.well.androidgnews.data

import com.well.androidgnews.data.model.Article
import com.well.androidgnews.data.model.Category
import com.well.androidgnews.data.remote.GNewsApiService
import com.well.androidgnews.data.remote.dto.ArticleDto
import java.time.Instant
import java.time.format.DateTimeParseException
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class NewsRepository @Inject constructor(
    private val api: GNewsApiService,
) {

    suspend fun topHeadlines(category: Category): List<Article> =
        api.topHeadlines(category.apiValue, LANG, COUNTRY, MAX_ARTICLES).articles.distinctBy { it.url }.map { it.toArticle() }

    suspend fun search(query: String): List<Article> =
        api.search(query, LANG, COUNTRY, MAX_ARTICLES).articles.distinctBy { it.url }.map { it.toArticle() }

    private fun ArticleDto.toArticle() = Article(
        title = title,
        description = description,
        url = url,
        imageUrl = image,
        publishedAt = publishedAt?.let {
            try {
                Instant.parse(it)
            } catch (e: DateTimeParseException) {
                null
            }
        },
        sourceName = source?.name.orEmpty(),
    )

    private companion object {
        const val LANG = "zh"
        const val COUNTRY = "tw"

        // The free GNews plan caps every request at 10 articles and doesn't allow paging.
        const val MAX_ARTICLES = 10
    }
}
