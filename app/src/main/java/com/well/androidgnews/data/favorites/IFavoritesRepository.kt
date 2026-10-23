package com.well.androidgnews.data.favorites

import com.well.androidgnews.data.model.Article
import kotlinx.coroutines.flow.Flow

/** A user's saved articles, newest first. */
interface IFavoritesRepository {

    /** Emits the current list and again on every change, including ones made on other devices. */
    fun favorites(userId: String): Flow<List<Article>>

    suspend fun add(userId: String, article: Article)

    suspend fun remove(userId: String, articleUrl: String)
}
