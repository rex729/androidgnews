package com.well.androidgnews

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.ui.platform.LocalContext
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.well.androidgnews.ui.headlines.HeadlinesScreen
import com.well.androidgnews.ui.headlines.HeadlinesViewModel
import com.well.androidgnews.ui.search.SearchScreen
import com.well.androidgnews.ui.search.SearchViewModel
import com.well.androidgnews.ui.theme.NewsTheme
import com.well.androidgnews.util.openUrl

class MainActivity : ComponentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        val repository = (application as NewsApplication).container.newsRepository

        setContent {
            NewsTheme {
                val navController = rememberNavController()
                val context = LocalContext.current

                NavHost(navController = navController, startDestination = Routes.HEADLINES) {
                    composable(Routes.HEADLINES) {
                        HeadlinesScreen(
                            viewModel = viewModel { HeadlinesViewModel(repository) },
                            onSearchClick = { navController.navigate(Routes.SEARCH) },
                            onArticleClick = { context.openUrl(it.url) },
                        )
                    }
                    composable(Routes.SEARCH) {
                        SearchScreen(
                            viewModel = viewModel { SearchViewModel(repository) },
                            onBack = { navController.popBackStack() },
                            onArticleClick = { context.openUrl(it.url) },
                        )
                    }
                }
            }
        }
    }

    private object Routes {
        const val HEADLINES = "headlines"
        const val SEARCH = "search"
    }
}
