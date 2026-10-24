package com.well.androidgnews.ui.favorites

import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.tooling.preview.PreviewLightDark
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.well.androidgnews.data.model.Article
import com.well.androidgnews.ui.common.IArticlesUiState
import com.well.androidgnews.ui.components.ArticlesContent
import com.well.androidgnews.ui.components.previewArticles
import com.well.androidgnews.ui.theme.NewsTheme

@Composable
fun FavoritesScreen(
    viewModel: FavoritesViewModel,
    onBack: () -> Unit,
    onArticleClick: (Article) -> Unit,
) {
    val favorites by viewModel.favorites.collectAsStateWithLifecycle()
    val favoriteUrls by viewModel.favoriteUrls.collectAsStateWithLifecycle()

    FavoritesContent(
        favorites = favorites,
        favoriteUrls = favoriteUrls,
        onToggleFavorite = viewModel::toggle,
        onBack = onBack,
        onArticleClick = onArticleClick,
    )
}

/** The favorites screen drawn from state alone, so previews can show it without a ViewModel. */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun FavoritesContent(
    favorites: List<Article>,
    favoriteUrls: Set<String>,
    onToggleFavorite: (Article) -> Unit,
    onBack: () -> Unit,
    onArticleClick: (Article) -> Unit,
) {

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("我的收藏") },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "返回")
                    }
                },
            )
        },
    ) { innerPadding ->
        ArticlesContent(
            state = if (favorites.isEmpty()) IArticlesUiState.Idle else IArticlesUiState.Success(favorites),
            onRetry = {},
            onArticleClick = onArticleClick,
            favoriteUrls = favoriteUrls,
            onToggleFavorite = onToggleFavorite,
            idleMessage = "還沒有收藏的新聞。\n點新聞卡片上的愛心就能收藏。",
            modifier = Modifier.padding(innerPadding),
        )
    }
}

@PreviewLightDark
@Composable
private fun FavoritesListPreview() {
    NewsTheme {
        FavoritesContent(
            favorites = previewArticles,
            favoriteUrls = previewArticles.mapTo(HashSet()) { it.url },
            onToggleFavorite = {},
            onBack = {},
            onArticleClick = {},
        )
    }
}

@Preview
@Composable
private fun FavoritesEmptyPreview() {
    NewsTheme {
        FavoritesContent(
            favorites = emptyList(),
            favoriteUrls = emptySet(),
            onToggleFavorite = {},
            onBack = {},
            onArticleClick = {},
        )
    }
}
