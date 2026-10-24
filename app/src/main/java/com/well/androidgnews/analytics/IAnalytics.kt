package com.well.androidgnews.analytics

import com.well.androidgnews.data.model.Article

/**
 * What the app reports about usage and who is using it. ViewModels and screens depend on this
 * interface only, so tests can pass a fake that records calls instead of sending anything.
 */
interface IAnalytics {

    fun screenViewed(screen: String)

    fun articleOpened(article: Article)

    fun searched(query: String)

    fun favoriteAdded(article: Article)

    fun favoriteRemoved(article: Article)

    fun signedIn()

    fun signedOut()

    /** Ties later events and crash reports to this (pseudonymous Firebase uid) user; null clears it. */
    fun setUser(userId: String?)
}
