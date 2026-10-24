package com.well.androidgnews.data

import com.well.androidgnews.analytics.IAnalytics
import com.well.androidgnews.data.model.Article

/** Records analytics calls as readable strings, e.g. "search:台積電", "favorite_add:https://x". */
class FakeAnalytics : IAnalytics {

    val events = mutableListOf<String>()
    var userId: String? = null

    override fun screenViewed(screen: String) { events += "screen:$screen" }
    override fun articleOpened(article: Article) { events += "open:${article.url}" }
    override fun searched(query: String) { events += "search:$query" }
    override fun favoriteAdded(article: Article) { events += "favorite_add:${article.url}" }
    override fun favoriteRemoved(article: Article) { events += "favorite_remove:${article.url}" }
    override fun signedIn() { events += "login" }
    override fun signedOut() { events += "logout" }
    override fun setUser(userId: String?) { this.userId = userId }
}
