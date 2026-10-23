package com.well.androidgnews.data

import com.well.androidgnews.data.model.Article
import com.well.androidgnews.data.model.Category

/** In-memory [INewsRepository] that records every request and returns (or throws) whatever the test sets. */
class FakeNewsRepository : INewsRepository {

    var headlines: (Category) -> List<Article> = { emptyList() }
    var searchResults: (String) -> List<Article> = { emptyList() }

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
}
