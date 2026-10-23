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
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.well.androidgnews.data.model.Article
import com.well.androidgnews.data.model.Category
import com.well.androidgnews.ui.components.ArticlesContent

@OptIn(ExperimentalMaterial3Api::class)
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
    var accountMenuOpen by remember { mutableStateOf(false) }
    val selectedCategory by viewModel.selectedCategory.collectAsStateWithLifecycle()
    val state by viewModel.state.collectAsStateWithLifecycle()
    val isRefreshing by viewModel.isRefreshing.collectAsStateWithLifecycle()

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
                        onClick = { viewModel.selectCategory(category) },
                        text = { Text(category.label) },
                    )
                }
            }
            PullToRefreshBox(
                isRefreshing = isRefreshing,
                onRefresh = viewModel::refresh,
                modifier = Modifier.fillMaxSize(),
            ) {
                ArticlesContent(
                    state = state,
                    onRetry = viewModel::retry,
                    onArticleClick = onArticleClick,
                    favoriteUrls = favoriteUrls,
                    onToggleFavorite = onToggleFavorite,
                )
            }
        }
    }
}
