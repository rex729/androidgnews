package com.well.androidgnews.testing

import com.well.androidgnews.data.INewsRepository
import com.well.androidgnews.data.model.Article
import com.well.androidgnews.data.model.Category

/**
 * [INewsRepository] for UI tests: answers instantly from memory. (Instrumented tests can't see the
 * JVM unit tests' FakeNewsRepository, so this is a separate copy.)
 */
class UiFakeNewsRepository : INewsRepository {

    var headlines: (Category) -> List<Article> = { listOf(article("${it.label}的新聞")) }
    var searchResults: (String) -> List<Article> = { listOf(article("搜尋結果：$it")) }

    val headlineRequests = mutableListOf<Category>()
    val searchRequests = mutableListOf<String>()

    override suspend fun topHeadlines(category: Category): List<Article> {
        headlineRequests += category
        return headlines(category)
    }

    override suspend fun search(query: String): List<Article> {
        searchRequests += query
        return searchResults(query)
    }

    companion object {
        fun article(title: String) = Article(
            title = title,
            description = null,
            url = "https://example.com/${title.hashCode()}",
            imageUrl = null,
            publishedAt = null,
            sourceName = "測試來源",
        )
    }
}
