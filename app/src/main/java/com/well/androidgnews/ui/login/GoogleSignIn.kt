package com.well.androidgnews.ui.login

import android.content.Context
import android.util.Log
import androidx.credentials.ClearCredentialStateRequest
import androidx.credentials.CredentialManager
import androidx.credentials.CustomCredential
import androidx.credentials.GetCredentialRequest
import androidx.credentials.exceptions.GetCredentialCancellationException
import androidx.credentials.exceptions.GetCredentialException
import com.google.android.libraries.identity.googleid.GetSignInWithGoogleOption
import com.google.android.libraries.identity.googleid.GoogleIdTokenCredential
import com.google.android.libraries.identity.googleid.GoogleIdTokenParsingException

/** Outcome of Google's account picker. */
sealed interface IGoogleSignInResult {
    data class Success(val idToken: String) : IGoogleSignInResult
    data object Cancelled : IGoogleSignInResult
    data class Failure(val message: String) : IGoogleSignInResult
}

/**
 * Shows Google's account picker through Credential Manager and returns the chosen account's ID
 * token. [serverClientId] is the project's *web* OAuth client id (R.string.default_web_client_id,
 * generated from google-services.json); Firebase verifies the token against it.
 * [context] must be an Activity, since the picker is a UI.
 */
suspend fun requestGoogleIdToken(context: Context, serverClientId: String): IGoogleSignInResult {
    val request = GetCredentialRequest.Builder()
        .addCredentialOption(GetSignInWithGoogleOption.Builder(serverClientId).build())
        .build()
    return try {
        val credential = CredentialManager.create(context).getCredential(context, request).credential
        if (credential is CustomCredential &&
            credential.type == GoogleIdTokenCredential.TYPE_GOOGLE_ID_TOKEN_CREDENTIAL
        ) {
            IGoogleSignInResult.Success(GoogleIdTokenCredential.createFrom(credential.data).idToken)
        } else {
            IGoogleSignInResult.Failure("不支援的登入方式")
        }
    } catch (e: GetCredentialCancellationException) {
        // Also what a misconfigured project looks like (e.g. the signing SHA-1 isn't registered in
        // Firebase: "[16] Account reauth failed"), so leave a trace instead of failing silently.
        Log.w(TAG, "Google sign-in cancelled: ${e.message}")
        IGoogleSignInResult.Cancelled
    } catch (e: GetCredentialException) {
        Log.w(TAG, "Google sign-in failed: ${e.type}", e)
        IGoogleSignInResult.Failure("無法使用 Google 登入：${e.message}")
    } catch (e: GoogleIdTokenParsingException) {
        IGoogleSignInResult.Failure("Google 登入回傳的資料無法解析")
    }
}

private const val TAG = "GoogleSignIn"

/** Forgets the account chosen in the picker, so the next sign-in asks again instead of reusing it. */
suspend fun clearGoogleCredentialState(context: Context) {
    CredentialManager.create(context).clearCredentialState(ClearCredentialStateRequest())
}
