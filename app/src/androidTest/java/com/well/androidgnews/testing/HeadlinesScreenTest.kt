package com.well.androidgnews.testing

import androidx.compose.runtime.remember
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.v2.createComposeRule
import androidx.compose.ui.test.onAllNodesWithText
import androidx.compose.ui.test.onNodeWithContentDescription
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.well.androidgnews.data.model.Category
import com.well.androidgnews.ui.headlines.HeadlinesScreen
import com.well.androidgnews.ui.headlines.HeadlinesViewModel
import com.well.androidgnews.ui.theme.NewsTheme
import java.io.IOException
import org.junit.Assert.assertEquals
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class HeadlinesScreenTest {

    @get:Rule
    val compose = createComposeRule()

    private val repository = UiFakeNewsRepository()
    private val openedUrls = mutableListOf<String>()
    private var searchClicks = 0

    private fun showScreen() {
        compose.setContent {
            NewsTheme {
                HeadlinesScreen(
                    viewModel = remember { HeadlinesViewModel(repository) },
                    onSearchClick = { searchClicks++ },
                    onArticleClick = { openedUrls += it.url },
                )
            }
        }
    }

    private fun waitForText(text: String) =
        compose.waitUntil(5_000) { compose.onAllNodesWithText(text).fetchSemanticsNodes().isNotEmpty() }

    @Test
    fun showsGeneralHeadlinesOnStart() {
        showScreen()

        waitForText("頭條的新聞")
        compose.onNodeWithText("頭條的新聞").assertIsDisplayed()
    }

    @Test
    fun tappingATabLoadsThatCategory() {
        showScreen()
        waitForText("頭條的新聞")

        compose.onNodeWithText("財經").performClick()

        waitForText("財經的新聞")
        compose.onNodeWithText("財經的新聞").assertIsDisplayed()
        assertEquals(listOf(Category.GENERAL, Category.BUSINESS), repository.headlineRequests)
    }

    @Test
    fun errorShowsRetryWhichReloads() {
        repository.headlines = { throw IOException() }
        showScreen()
        waitForText("重試")
        compose.onNodeWithText("網路連線失敗，請檢查網路後再試").assertIsDisplayed()

        repository.headlines = { listOf(UiFakeNewsRepository.article("重試後的新聞")) }
        compose.onNodeWithText("重試").performClick()

        waitForText("重試後的新聞")
        compose.onNodeWithText("重試後的新聞").assertIsDisplayed()
    }

    @Test
    fun tappingAnArticleOpensIt() {
        showScreen()
        waitForText("頭條的新聞")

        compose.onNodeWithText("頭條的新聞").performClick()

        assertEquals(listOf(UiFakeNewsRepository.article("頭條的新聞").url), openedUrls)
    }

    @Test
    fun searchIconNavigatesToSearch() {
        showScreen()

        compose.onNodeWithContentDescription("搜尋").performClick()

        assertEquals(1, searchClicks)
    }
}
