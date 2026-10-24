package com.well.androidgnews.ui.search

import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.tooling.preview.PreviewLightDark
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.well.androidgnews.data.model.Article
import com.well.androidgnews.ui.common.IArticlesUiState
import com.well.androidgnews.ui.components.ArticlesContent
import com.well.androidgnews.ui.components.previewArticles
import com.well.androidgnews.ui.theme.NewsTheme

@Composable
fun SearchScreen(
    viewModel: SearchViewModel,
    favoriteUrls: Set<String>,
    onToggleFavorite: (Article) -> Unit,
    onBack: () -> Unit,
    onArticleClick: (Article) -> Unit,
) {
    val query by viewModel.query.collectAsStateWithLifecycle()
    val state by viewModel.state.collectAsStateWithLifecycle()

    SearchContent(
        query = query,
        state = state,
        onQueryChange = viewModel::onQueryChange,
        onSearch = viewModel::search,
        favoriteUrls = favoriteUrls,
        onToggleFavorite = onToggleFavorite,
        onBack = onBack,
        onArticleClick = onArticleClick,
    )
}

/** The search screen drawn from state alone, so previews can show it without a ViewModel. */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun SearchContent(
    query: String,
    state: IArticlesUiState,
    onQueryChange: (String) -> Unit,
    onSearch: () -> Unit,
    favoriteUrls: Set<String>,
    onToggleFavorite: (Article) -> Unit,
    onBack: () -> Unit,
    onArticleClick: (Article) -> Unit,
) {
    val focusRequester = remember { FocusRequester() }
    val keyboard = LocalSoftwareKeyboardController.current

    LaunchedEffect(Unit) { focusRequester.requestFocus() }

    Scaffold(
        topBar = {
            TopAppBar(
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "返回")
                    }
                },
                title = {
                    TextField(
                        value = query,
                        onValueChange = onQueryChange,
                        placeholder = { Text("搜尋新聞") },
                        singleLine = true,
                        keyboardOptions = KeyboardOptions(imeAction = ImeAction.Search),
                        keyboardActions = KeyboardActions(onSearch = {
                            keyboard?.hide()
                            onSearch()
                        }),
                        colors = TextFieldDefaults.colors(
                            focusedContainerColor = Color.Transparent,
                            unfocusedContainerColor = Color.Transparent,
                            focusedIndicatorColor = Color.Transparent,
                            unfocusedIndicatorColor = Color.Transparent,
                        ),
                        modifier = Modifier
                            .fillMaxWidth()
                            .focusRequester(focusRequester),
                    )
                },
            )
        },
    ) { innerPadding ->
        ArticlesContent(
            state = state,
            onRetry = onSearch,
            onArticleClick = onArticleClick,
            favoriteUrls = favoriteUrls,
            onToggleFavorite = onToggleFavorite,
            idleMessage = "輸入關鍵字後按搜尋",
            modifier = Modifier.padding(innerPadding),
        )
    }
}

@Composable
private fun SearchPreview(query: String, state: IArticlesUiState) {
    NewsTheme {
        SearchContent(
            query = query,
            state = state,
            onQueryChange = {},
            onSearch = {},
            favoriteUrls = setOf(previewArticles[0].url),
            onToggleFavorite = {},
            onBack = {},
            onArticleClick = {},
        )
    }
}

@PreviewLightDark
@Composable
private fun SearchResultsPreview() {
    SearchPreview(query = "台積電", state = IArticlesUiState.Success(previewArticles))
}

@Preview
@Composable
private fun SearchIdlePreview() {
    SearchPreview(query = "", state = IArticlesUiState.Idle)
}

@Preview
@Composable
private fun SearchEmptyPreview() {
    SearchPreview(query = "asdfgh", state = IArticlesUiState.Empty)
}
