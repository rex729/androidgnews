package com.well.androidgnews.data.auth

import kotlinx.coroutines.flow.StateFlow

interface IAuthRepository {

    /** The signed-in user, or null when signed out. Updates on sign-in, sign-out and token expiry. */
    val currentUser: StateFlow<User?>

    /** Signs in with the ID token from Google's account picker (Credential Manager). */
    suspend fun signInWithGoogle(idToken: String)

    fun signOut()
}
