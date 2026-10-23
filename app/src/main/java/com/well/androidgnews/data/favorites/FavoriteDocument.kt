package com.well.androidgnews.data.favorites

import com.well.androidgnews.data.model.Article
import java.security.MessageDigest
import java.time.Instant
import java.time.format.DateTimeParseException

/**
 * How a favorite is stored in Firestore at `users/{uid}/favorites/{documentId}`. Kept free of
 * Firestore types so the mapping can be unit-tested; must match the field list in firestore.rules.
 */
object FavoriteDocument {
    const val TITLE = "title"
    const val DESCRIPTION = "description"
    const val URL = "url"
    const val IMAGE_URL = "imageUrl"
    const val PUBLISHED_AT = "publishedAt"
    const val SOURCE_NAME = "sourceName"
    const val SAVED_AT = "savedAt"

    /**
     * Document id for an article: the SHA-256 of its URL in hex. URLs contain "/" (not allowed in
     * ids) and can be long; hashing makes saving the same article twice land on the same document.
     */
    fun documentId(articleUrl: String): String =
        MessageDigest.getInstance("SHA-256")
            .digest(articleUrl.toByteArray(Charsets.UTF_8))
            .joinToString("") { "%02x".format(it) }

    /** Fields to write, except [SAVED_AT], which the repository sets to the server's time. */
    fun fieldsOf(article: Article): Map<String, Any?> = mapOf(
        TITLE to article.title,
        DESCRIPTION to article.description,
        URL to article.url,
        IMAGE_URL to article.imageUrl,
        PUBLISHED_AT to article.publishedAt?.toString(),
        SOURCE_NAME to article.sourceName,
    )

    /** Reads a stored document back; null if a required field is missing or malformed. */
    fun articleFrom(fields: Map<String, Any?>): Article? {
        val title = fields[TITLE] as? String ?: return null
        val url = fields[URL] as? String ?: return null
        return Article(
            title = title,
            description = fields[DESCRIPTION] as? String,
            url = url,
            imageUrl = fields[IMAGE_URL] as? String,
            publishedAt = (fields[PUBLISHED_AT] as? String)?.let {
                try {
                    Instant.parse(it)
                } catch (e: DateTimeParseException) {
                    null
                }
            },
            sourceName = fields[SOURCE_NAME] as? String ?: "",
        )
    }
}
