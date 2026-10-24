package com.well.androidgnews.ui.login

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.tooling.preview.PreviewLightDark
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.well.androidgnews.ui.theme.NewsTheme

@Composable
fun LoginScreen(
    viewModel: LoginViewModel,
    googleWebClientId: String,
) {
    val state by viewModel.state.collectAsStateWithLifecycle()
    val context = LocalContext.current

    LoginContent(
        state = state,
        onSignIn = { viewModel.signIn { requestGoogleIdToken(context, googleWebClientId) } },
    )
}

/** The login screen drawn from state alone, so previews can show it without a ViewModel. */
@Composable
private fun LoginContent(
    state: ILoginUiState,
    onSignIn: () -> Unit,
) {
    Scaffold { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .padding(32.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp, Alignment.CenterVertically),
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            Text("新聞", style = MaterialTheme.typography.displaySmall)
            Text(
                "登入後即可閱讀台灣新聞，並收藏想留下的文章。收藏會跟著帳號同步到你的每支手機。",
                style = MaterialTheme.typography.bodyLarge,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                textAlign = TextAlign.Center,
            )
            if (state == ILoginUiState.SigningIn) {
                CircularProgressIndicator()
            } else {
                Button(onClick = onSignIn, modifier = Modifier.fillMaxWidth()) {
                    Text("使用 Google 帳號登入")
                }
            }
            (state as? ILoginUiState.Error)?.let {
                Text(it.message, color = MaterialTheme.colorScheme.error, textAlign = TextAlign.Center)
            }
        }
    }
}

@PreviewLightDark
@Composable
private fun LoginIdlePreview() {
    NewsTheme { LoginContent(state = ILoginUiState.Idle, onSignIn = {}) }
}

@Preview
@Composable
private fun LoginSigningInPreview() {
    NewsTheme { LoginContent(state = ILoginUiState.SigningIn, onSignIn = {}) }
}

@Preview
@Composable
private fun LoginErrorPreview() {
    NewsTheme { LoginContent(state = ILoginUiState.Error("登入失敗，請檢查網路後再試"), onSignIn = {}) }
}
