package com.well.androidgnews.data

import com.well.androidgnews.data.favorites.IFavoritesRepository
import com.well.androidgnews.data.model.Article
import java.io.IOException
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.update

/** In-memory [IFavoritesRepository], keyed by user id, newest first; can be told to fail. */
class FakeFavoritesRepository : IFavoritesRepository {

    private val store = MutableStateFlow<Map<String, List<Article>>>(emptyMap())
    var failReads = false
    var failWrites = false

    override fun favorites(userId: String): Flow<List<Article>> =
        if (failReads) flow { throw IOException("offline") } else store.map { it[userId].orEmpty() }

    override suspend fun add(userId: String, article: Article) {
        if (failWrites) throw IOException("offline")
        store.update { all -> all + (userId to listOf(article) + all[userId].orEmpty().filter { it.url != article.url }) }
    }

    override suspend fun remove(userId: String, articleUrl: String) {
        if (failWrites) throw IOException("offline")
        store.update { all -> all + (userId to all[userId].orEmpty().filter { it.url != articleUrl }) }
    }

    fun favoritesOf(userId: String): List<Article> = store.value[userId].orEmpty()
}
