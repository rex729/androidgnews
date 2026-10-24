package com.well.androidgnews.testing

import com.well.androidgnews.data.FakeAnalytics
import com.well.androidgnews.data.FakeAuthRepository
import com.well.androidgnews.rule.MainDispatcherRule
import com.well.androidgnews.ui.login.IGoogleSignInResult
import com.well.androidgnews.ui.login.ILoginUiState
import com.well.androidgnews.ui.login.LoginViewModel
import com.well.androidgnews.ui.session.SessionViewModel
import java.io.IOException
import kotlinx.coroutines.CompletableDeferred
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test

class LoginViewModelTest {

    @get:Rule
    val mainDispatcherRule = MainDispatcherRule()

    private val auth = FakeAuthRepository()
    private val analytics = FakeAnalytics()
    private val viewModel = LoginViewModel(auth, analytics)

    @Test
    fun `picking an account signs in with its token`() {
        viewModel.signIn { IGoogleSignInResult.Success("token-1") }

        assertEquals(listOf("token-1"), auth.signInTokens)
        assertEquals("uid-token-1", auth.currentUser.value?.id)
        assertEquals(ILoginUiState.Idle, viewModel.state.value)
        assertEquals(listOf("login"), analytics.events)
    }

    @Test
    fun `closing the account picker is not an error`() {
        viewModel.signIn { IGoogleSignInResult.Cancelled }

        assertEquals(ILoginUiState.Idle, viewModel.state.value)
        assertTrue(auth.signInTokens.isEmpty())
    }

    @Test
    fun `account picker failure is shown`() {
        viewModel.signIn { IGoogleSignInResult.Failure("無法使用 Google 登入") }

        assertEquals(ILoginUiState.Error("無法使用 Google 登入"), viewModel.state.value)
        assertNull(auth.currentUser.value)
    }

    @Test
    fun `firebase rejecting the token is shown`() {
        auth.signInError = IOException("offline")

        viewModel.signIn { IGoogleSignInResult.Success("token-1") }

        assertTrue(viewModel.state.value is ILoginUiState.Error)
        assertNull(auth.currentUser.value)
        assertTrue(analytics.events.isEmpty())
    }

    @Test
    fun `second tap while signing in is ignored`() {
        val picker = CompletableDeferred<IGoogleSignInResult>()
        var pickerOpened = 0

        viewModel.signIn { pickerOpened++; picker.await() }
        viewModel.signIn { pickerOpened++; picker.await() }
        assertEquals(ILoginUiState.SigningIn, viewModel.state.value)

        picker.complete(IGoogleSignInResult.Success("token-1"))

        assertEquals(1, pickerOpened)
        assertEquals(listOf("token-1"), auth.signInTokens)
    }

    @Test
    fun `signing out clears the session`() {
        val session = SessionViewModel(auth, analytics)
        viewModel.signIn { IGoogleSignInResult.Success("token-1") }

        session.signOut()

        assertNull(session.currentUser.value)
        assertEquals(listOf("login", "logout"), analytics.events)
    }
}
