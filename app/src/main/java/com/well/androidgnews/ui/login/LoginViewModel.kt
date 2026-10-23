package com.well.androidgnews.ui.login

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.well.androidgnews.data.auth.IAuthRepository
import kotlin.coroutines.cancellation.CancellationException
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

sealed interface ILoginUiState {
    data object Idle : ILoginUiState
    data object SigningIn : ILoginUiState
    data class Error(val message: String) : ILoginUiState
}

class LoginViewModel(
    private val auth: IAuthRepository,
) : ViewModel() {

    private val _state = MutableStateFlow<ILoginUiState>(ILoginUiState.Idle)
    val state: StateFlow<ILoginUiState> = _state.asStateFlow()

    /**
     * Runs the whole sign-in: [pickGoogleAccount] shows Google's account picker (it needs an
     * Activity, so the screen supplies it), then the ID token is exchanged for a Firebase session.
     * Success needs no state here: the session's current user changes and the app leaves this screen.
     */
    fun signIn(pickGoogleAccount: suspend () -> IGoogleSignInResult) {
        if (_state.value == ILoginUiState.SigningIn) return
        viewModelScope.launch {
            _state.value = ILoginUiState.SigningIn
            _state.value = when (val picked = pickGoogleAccount()) {
                IGoogleSignInResult.Cancelled -> ILoginUiState.Idle
                is IGoogleSignInResult.Failure -> ILoginUiState.Error(picked.message)
                is IGoogleSignInResult.Success -> try {
                    auth.signInWithGoogle(picked.idToken)
                    ILoginUiState.Idle
                } catch (e: CancellationException) {
                    throw e
                } catch (e: Exception) {
                    ILoginUiState.Error("登入失敗，請檢查網路後再試")
                }
            }
        }
    }
}
