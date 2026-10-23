package com.well.androidgnews.data

import com.well.androidgnews.data.model.Article
import com.well.androidgnews.data.model.Category

/**
 * Where the UI gets news from. ViewModels depend on this interface only, so tests can hand them
 * a fake instead of the real network-backed [GNewsRepository].
 */
interface INewsRepository {

    suspend fun topHeadlines(category: Category): List<Article>

    suspend fun search(query: String): List<Article>
}
