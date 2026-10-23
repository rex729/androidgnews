package com.well.androidgnews.data

import com.well.androidgnews.data.model.Article
import com.well.androidgnews.data.model.Category
import com.well.androidgnews.data.remote.INewsApiService
import com.well.androidgnews.data.remote.dto.ArticleDto
import java.time.Instant
import java.time.format.DateTimeParseException

/** [INewsRepository] backed by the GNews API, limited to Traditional Chinese news from Taiwan. */
class RemoteNewsRepository(
    private val api: INewsApiService,
) : INewsRepository {

    override suspend fun topHeadlines(category: Category): List<Article> =
        api.topHeadlines(category.apiValue, LANG, COUNTRY, MAX_ARTICLES).articles.toArticles()

    override suspend fun search(query: String): List<Article> =
        api.search(query, LANG, COUNTRY, MAX_ARTICLES).articles.toArticles()

    // The same story can come back twice; the list UI keys rows by URL, so drop duplicates.
    private fun List<ArticleDto>.toArticles(): List<Article> = distinctBy { it.url }.map { it.toArticle() }

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

    companion object {
        const val LANG = "zh"
        const val COUNTRY = "tw"

        // The free GNews plan caps every request at 10 articles and doesn't allow paging.
        const val MAX_ARTICLES = 10
    }
}
