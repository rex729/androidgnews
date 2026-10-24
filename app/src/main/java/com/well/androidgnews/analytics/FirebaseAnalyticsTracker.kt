package com.well.androidgnews.analytics

import android.os.Bundle
import com.google.firebase.analytics.FirebaseAnalytics
import com.google.firebase.crashlytics.FirebaseCrashlytics
import com.well.androidgnews.data.model.Article

/**
 * [IAnalytics] on Google Analytics for Firebase, using its recommended event names where one fits
 * (screen_view, select_content, search, login) so they show up in the standard reports.
 * The user id also goes to Crashlytics, so crash reports say whose session crashed.
 */
class FirebaseAnalyticsTracker(
    private val analytics: FirebaseAnalytics,
    private val crashlytics: FirebaseCrashlytics,
) : IAnalytics {

    override fun screenViewed(screen: String) {
        log(FirebaseAnalytics.Event.SCREEN_VIEW) {
            putString(FirebaseAnalytics.Param.SCREEN_NAME, screen)
            putString(FirebaseAnalytics.Param.SCREEN_CLASS, screen)
        }
    }

    override fun articleOpened(article: Article) {
        log(FirebaseAnalytics.Event.SELECT_CONTENT) {
            putString(FirebaseAnalytics.Param.CONTENT_TYPE, "article")
            putArticle(article)
        }
    }

    override fun searched(query: String) {
        log(FirebaseAnalytics.Event.SEARCH) { putString(FirebaseAnalytics.Param.SEARCH_TERM, query.take(MAX_VALUE)) }
    }

    override fun favoriteAdded(article: Article) {
        log("favorite_add") { putArticle(article) }
    }

    override fun favoriteRemoved(article: Article) {
        log("favorite_remove") { putArticle(article) }
    }

    override fun signedIn() {
        log(FirebaseAnalytics.Event.LOGIN) { putString(FirebaseAnalytics.Param.METHOD, "google") }
    }

    override fun signedOut() {
        log("logout") {}
    }

    override fun setUser(userId: String?) {
        analytics.setUserId(userId)
        crashlytics.setUserId(userId.orEmpty())
    }

    private fun log(event: String, params: Bundle.() -> Unit) {
        analytics.logEvent(event, Bundle().apply(params))
    }

    // Analytics truncates parameter values at 100 characters.
    private fun Bundle.putArticle(article: Article) {
        putString(FirebaseAnalytics.Param.ITEM_NAME, article.title.take(MAX_VALUE))
        putString("source", article.sourceName.take(MAX_VALUE))
    }

    private companion object {
        const val MAX_VALUE = 100
    }
}
