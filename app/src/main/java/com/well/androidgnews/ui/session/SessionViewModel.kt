package com.well.androidgnews.ui.session

import androidx.lifecycle.ViewModel
import com.well.androidgnews.analytics.IAnalytics
import com.well.androidgnews.data.auth.IAuthRepository
import com.well.androidgnews.data.auth.User
import kotlinx.coroutines.flow.StateFlow

/** App-wide sign-in state: decides between the login screen and the rest of the app. */
class SessionViewModel(
    private val auth: IAuthRepository,
    private val analytics: IAnalytics,
) : ViewModel() {

    val currentUser: StateFlow<User?> = auth.currentUser

    fun signOut() {
        auth.signOut()
        analytics.signedOut()
    }
}
