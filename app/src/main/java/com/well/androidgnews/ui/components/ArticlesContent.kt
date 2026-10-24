package com.well.androidgnews.ui.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.tooling.preview.PreviewLightDark
import androidx.compose.ui.unit.dp
import com.well.androidgnews.data.model.Article
import com.well.androidgnews.ui.common.IArticlesUiState
import com.well.androidgnews.ui.theme.NewsTheme

/** Shared list / loading / error / empty rendering for the headlines, search and favorites screens. */
@Composable
fun ArticlesContent(
    state: IArticlesUiState,
    onRetry: () -> Unit,
    onArticleClick: (Article) -> Unit,
    favoriteUrls: Set<String>,
    onToggleFavorite: (Article) -> Unit,
    modifier: Modifier = Modifier,
    idleMessage: String = "",
) {
    when (state) {
        IArticlesUiState.Idle -> CenteredMessage(idleMessage, modifier)
        IArticlesUiState.Loading -> Box(modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            CircularProgressIndicator()
        }
        IArticlesUiState.Empty -> CenteredMessage("沒有找到相關新聞", modifier)
        is IArticlesUiState.Error -> CenteredMessage(state.message, modifier) {
            Button(onClick = onRetry) { Text("重試") }
        }
        is IArticlesUiState.Success -> LazyColumn(
            modifier = modifier.fillMaxSize(),
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            items(state.articles, key = { it.url }) { article ->
                ArticleCard(
                    article = article,
                    onClick = { onArticleClick(article) },
                    isFavorite = article.url in favoriteUrls,
                    onToggleFavorite = { onToggleFavorite(article) },
                )
            }
        }
    }
}

// Scrollable so pull-to-refresh still works when there's no list to drag.
@Composable
private fun CenteredMessage(
    message: String,
    modifier: Modifier = Modifier,
    action: @Composable () -> Unit = {},
) {
    Column(
        modifier = modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(32.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp, Alignment.CenterVertically),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        Text(
            text = message,
            style = MaterialTheme.typography.bodyLarge,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            textAlign = TextAlign.Center,
        )
        action()
    }
}

@Composable
private fun ArticlesContentPreview(state: IArticlesUiState) {
    NewsTheme {
        Surface {
            ArticlesContent(
                state = state,
                onRetry = {},
                onArticleClick = {},
                favoriteUrls = setOf(previewArticles[0].url),
                onToggleFavorite = {},
                idleMessage = "輸入關鍵字後按搜尋",
            )
        }
    }
}

@PreviewLightDark
@Composable
private fun ArticlesSuccessPreview() {
    ArticlesContentPreview(IArticlesUiState.Success(previewArticles))
}

@Preview
@Composable
private fun ArticlesIdlePreview() {
    ArticlesContentPreview(IArticlesUiState.Idle)
}

@Preview
@Composable
private fun ArticlesLoadingPreview() {
    ArticlesContentPreview(IArticlesUiState.Loading)
}

@Preview
@Composable
private fun ArticlesEmptyPreview() {
    ArticlesContentPreview(IArticlesUiState.Empty)
}

@Preview
@Composable
private fun ArticlesErrorPreview() {
    ArticlesContentPreview(IArticlesUiState.Error("網路連線失敗，請檢查網路後再試"))
}
