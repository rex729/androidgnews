package com.well.androidgnews.data.favorites

import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import com.well.androidgnews.data.model.Article
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.tasks.await

/** [IFavoritesRepository] on Cloud Firestore, so favorites follow the account across devices. */
class FirestoreFavoritesRepository(
    private val db: FirebaseFirestore,
) : IFavoritesRepository {

    private fun favoritesOf(userId: String) =
        db.collection("users").document(userId).collection("favorites")

    override fun favorites(userId: String): Flow<List<Article>> = callbackFlow {
        val registration = favoritesOf(userId)
            .orderBy(FavoriteDocument.SAVED_AT, Query.Direction.DESCENDING)
            .addSnapshotListener { snapshot, error ->
                if (error != null) {
                    close(error)
                    return@addSnapshotListener
                }
                val articles = snapshot?.documents.orEmpty().mapNotNull { FavoriteDocument.articleFrom(it.data.orEmpty()) }
                trySend(articles)
            }
        awaitClose { registration.remove() }
    }

    override suspend fun add(userId: String, article: Article) {
        val fields = FavoriteDocument.fieldsOf(article) + (FavoriteDocument.SAVED_AT to FieldValue.serverTimestamp())
        favoritesOf(userId).document(FavoriteDocument.documentId(article.url)).set(fields).await()
    }

    override suspend fun remove(userId: String, articleUrl: String) {
        favoritesOf(userId).document(FavoriteDocument.documentId(articleUrl)).delete().await()
    }
}
