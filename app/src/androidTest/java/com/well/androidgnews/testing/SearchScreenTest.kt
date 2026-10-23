package com.well.androidgnews.testing

import androidx.compose.runtime.remember
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.hasSetTextAction
import androidx.compose.ui.test.junit4.v2.createComposeRule
import androidx.compose.ui.test.onAllNodesWithText
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performImeAction
import androidx.compose.ui.test.performTextInput
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.well.androidgnews.ui.search.SearchScreen
import com.well.androidgnews.ui.search.SearchViewModel
import com.well.androidgnews.ui.theme.NewsTheme
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class SearchScreenTest {

    @get:Rule
    val compose = createComposeRule()

    private val repository = UiFakeNewsRepository()

    private fun showScreen() {
        compose.setContent {
            NewsTheme {
                SearchScreen(
                    viewModel = remember { SearchViewModel(repository) },
                    onBack = {},
                    onArticleClick = {},
                )
            }
        }
    }

    @Test
    fun showsHintBeforeSearching() {
        showScreen()

        compose.onNodeWithText("輸入關鍵字後按搜尋").assertIsDisplayed()
        assertTrue(repository.searchRequests.isEmpty())
    }

    @Test
    fun submittingFromKeyboardShowsResults() {
        showScreen()

        compose.onNode(hasSetTextAction()).performTextInput("台積電")
        compose.onNode(hasSetTextAction()).performImeAction()

        compose.waitUntil(5_000) {
            compose.onAllNodesWithText("搜尋結果：台積電").fetchSemanticsNodes().isNotEmpty()
        }
        compose.onNodeWithText("搜尋結果：台積電").assertIsDisplayed()
        assertEquals(listOf("台積電"), repository.searchRequests)
    }

    @Test
    fun noResultsShowsEmptyMessage() {
        repository.searchResults = { emptyList() }
        showScreen()

        compose.onNode(hasSetTextAction()).performTextInput("找不到")
        compose.onNode(hasSetTextAction()).performImeAction()

        compose.waitUntil(5_000) {
            compose.onAllNodesWithText("沒有找到相關新聞").fetchSemanticsNodes().isNotEmpty()
        }
    }
}
