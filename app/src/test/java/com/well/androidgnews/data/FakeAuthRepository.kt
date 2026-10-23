package com.well.androidgnews.data

import com.well.androidgnews.data.auth.IAuthRepository
import com.well.androidgnews.data.auth.User
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

/** In-memory [IAuthRepository]: signing in with token "x" makes user "uid-x" current. */
class FakeAuthRepository(initialUser: User? = null) : IAuthRepository {

    private val _currentUser = MutableStateFlow(initialUser)
    override val currentUser: StateFlow<User?> = _currentUser.asStateFlow()

    val signInTokens = mutableListOf<String>()
    var signInError: Exception? = null

    override suspend fun signInWithGoogle(idToken: String) {
        signInTokens += idToken
        signInError?.let { throw it }
        _currentUser.value = user("uid-$idToken")
    }

    override fun signOut() {
        _currentUser.value = null
    }

    fun switchTo(user: User?) {
        _currentUser.value = user
    }

    companion object {
        fun user(id: String) = User(id = id, name = "Name $id", email = "$id@example.com")
    }
}
