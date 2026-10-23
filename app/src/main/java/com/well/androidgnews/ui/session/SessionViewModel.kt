package com.well.androidgnews.ui.session

import androidx.lifecycle.ViewModel
import com.well.androidgnews.data.auth.IAuthRepository
import com.well.androidgnews.data.auth.User
import kotlinx.coroutines.flow.StateFlow

/** App-wide sign-in state: decides between the login screen and the rest of the app. */
class SessionViewModel(
    private val auth: IAuthRepository,
) : ViewModel() {

    val currentUser: StateFlow<User?> = auth.currentUser

    fun signOut() {
        auth.signOut()
    }
}
