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
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.well.androidgnews.data.model.Article
import com.well.androidgnews.ui.common.IArticlesUiState
import com.well.androidgnews.ui.components.ArticlesContent

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun FavoritesScreen(
    viewModel: FavoritesViewModel,
    onBack: () -> Unit,
    onArticleClick: (Article) -> Unit,
) {
    val favorites by viewModel.favorites.collectAsStateWithLifecycle()
    val favoriteUrls by viewModel.favoriteUrls.collectAsStateWithLifecycle()

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
            onToggleFavorite = viewModel::toggle,
            idleMessage = "還沒有收藏的新聞。\n點新聞卡片上的愛心就能收藏。",
            modifier = Modifier.padding(innerPadding),
        )
    }
}
