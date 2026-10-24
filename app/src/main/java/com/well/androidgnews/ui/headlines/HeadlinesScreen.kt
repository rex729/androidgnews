package com.well.androidgnews.ui.headlines

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccountCircle
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.Search
import androidx.compose.foundation.layout.Box
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.PrimaryScrollableTabRow
import androidx.compose.material3.Tab
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.pulltorefresh.PullToRefreshBox
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.tooling.preview.PreviewLightDark
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.well.androidgnews.data.model.Article
import com.well.androidgnews.data.model.Category
import com.well.androidgnews.ui.common.IArticlesUiState
import com.well.androidgnews.ui.components.ArticlesContent
import com.well.androidgnews.ui.components.previewArticles
import com.well.androidgnews.ui.theme.NewsTheme

@Composable
fun HeadlinesScreen(
    viewModel: HeadlinesViewModel,
    favoriteUrls: Set<String>,
    onToggleFavorite: (Article) -> Unit,
    accountLabel: String,
    onSearchClick: () -> Unit,
    onFavoritesClick: () -> Unit,
    onSignOut: () -> Unit,
    onArticleClick: (Article) -> Unit,
) {
    val selectedCategory by viewModel.selectedCategory.collectAsStateWithLifecycle()
    val state by viewModel.state.collectAsStateWithLifecycle()
    val isRefreshing by viewModel.isRefreshing.collectAsStateWithLifecycle()

    HeadlinesContent(
        state = state,
        selectedCategory = selectedCategory,
        isRefreshing = isRefreshing,
        onSelectCategory = viewModel::selectCategory,
        onRefresh = viewModel::refresh,
        onRetry = viewModel::retry,
        favoriteUrls = favoriteUrls,
        onToggleFavorite = onToggleFavorite,
        accountLabel = accountLabel,
        onSearchClick = onSearchClick,
        onFavoritesClick = onFavoritesClick,
        onSignOut = onSignOut,
        onArticleClick = onArticleClick,
    )
}

/** The headlines screen drawn from state alone, so previews can show it without a ViewModel. */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun HeadlinesContent(
    state: IArticlesUiState,
    selectedCategory: Category,
    isRefreshing: Boolean,
    onSelectCategory: (Category) -> Unit,
    onRefresh: () -> Unit,
    onRetry: () -> Unit,
    favoriteUrls: Set<String>,
    onToggleFavorite: (Article) -> Unit,
    accountLabel: String,
    onSearchClick: () -> Unit,
    onFavoritesClick: () -> Unit,
    onSignOut: () -> Unit,
    onArticleClick: (Article) -> Unit,
) {
    var accountMenuOpen by remember { mutableStateOf(false) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("新聞") },
                actions = {
                    IconButton(onClick = onSearchClick) {
                        Icon(Icons.Default.Search, contentDescription = "搜尋")
                    }
                    IconButton(onClick = onFavoritesClick) {
                        Icon(Icons.Default.Favorite, contentDescription = "我的收藏")
                    }
                    Box {
                        IconButton(onClick = { accountMenuOpen = true }) {
                            Icon(Icons.Default.AccountCircle, contentDescription = "帳號")
                        }
                        DropdownMenu(expanded = accountMenuOpen, onDismissRequest = { accountMenuOpen = false }) {
                            DropdownMenuItem(text = { Text(accountLabel) }, onClick = {}, enabled = false)
                            DropdownMenuItem(
                                text = { Text("登出") },
                                onClick = {
                                    accountMenuOpen = false
                                    onSignOut()
                                },
                            )
                        }
                    }
                },
            )
        },
    ) { innerPadding ->
        Column(Modifier.padding(innerPadding)) {
            PrimaryScrollableTabRow(
                selectedTabIndex = selectedCategory.ordinal,
                edgePadding = 8.dp,
            ) {
                Category.entries.forEach { category ->
                    Tab(
                        selected = category == selectedCategory,
                        onClick = { onSelectCategory(category) },
                        text = { Text(category.label) },
                    )
                }
            }
            PullToRefreshBox(
                isRefreshing = isRefreshing,
                onRefresh = onRefresh,
                modifier = Modifier.fillMaxSize(),
            ) {
                ArticlesContent(
                    state = state,
                    onRetry = onRetry,
                    onArticleClick = onArticleClick,
                    favoriteUrls = favoriteUrls,
                    onToggleFavorite = onToggleFavorite,
                )
            }
        }
    }
}

@Composable
private fun HeadlinesPreview(state: IArticlesUiState, isRefreshing: Boolean = false) {
    NewsTheme {
        HeadlinesContent(
            state = state,
            selectedCategory = Category.GENERAL,
            isRefreshing = isRefreshing,
            onSelectCategory = {},
            onRefresh = {},
            onRetry = {},
            favoriteUrls = setOf(previewArticles[1].url),
            onToggleFavorite = {},
            accountLabel = "user@example.com",
            onSearchClick = {},
            onFavoritesClick = {},
            onSignOut = {},
            onArticleClick = {},
        )
    }
}

@PreviewLightDark
@Composable
private fun HeadlinesSuccessPreview() {
    HeadlinesPreview(IArticlesUiState.Success(previewArticles))
}

@Preview
@Composable
private fun HeadlinesRefreshingPreview() {
    HeadlinesPreview(IArticlesUiState.Success(previewArticles), isRefreshing = true)
}

@Preview
@Composable
private fun HeadlinesLoadingPreview() {
    HeadlinesPreview(IArticlesUiState.Loading)
}

@Preview
@Composable
private fun HeadlinesErrorPreview() {
    HeadlinesPreview(IArticlesUiState.Error("網路連線失敗，請檢查網路後再試"))
}
