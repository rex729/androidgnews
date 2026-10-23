package com.well.androidgnews

import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
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

    // Content is shown once the user answers, whatever the answer, so the first request already
    // has the permission when it was granted.
    private val localNetworkPermission =
        registerForActivityResult(ActivityResultContracts.RequestPermission()) { showContent() }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        if (savedInstanceState == null && needsLocalNetworkPermission()) {
            localNetworkPermission.launch(LOCAL_NETWORK_PERMISSION)
        } else {
            showContent()
        }
    }

    /**
     * Debug builds reach the backend on this PC at 10.0.2.2, which Android 17 treats as the local
     * network. `gradlew installDebug` grants the permission, but Android Studio's Run (which may
     * uninstall first) doesn't, so ask for it. Release builds don't declare the permission.
     */
    private fun needsLocalNetworkPermission(): Boolean =
        BuildConfig.DEBUG &&
            Build.VERSION.SDK_INT >= ANDROID_17 &&
            checkSelfPermission(LOCAL_NETWORK_PERMISSION) != PackageManager.PERMISSION_GRANTED

    private fun showContent() {
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

    private companion object {
        const val LOCAL_NETWORK_PERMISSION = "android.permission.ACCESS_LOCAL_NETWORK"
        const val ANDROID_17 = 37
    }
}
