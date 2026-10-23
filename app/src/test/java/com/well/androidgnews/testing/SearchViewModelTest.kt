package com.well.androidgnews.testing

import com.well.androidgnews.data.FakeNewsRepository
import com.well.androidgnews.data.article
import com.well.androidgnews.rule.MainDispatcherRule
import com.well.androidgnews.ui.common.IArticlesUiState
import com.well.androidgnews.ui.search.SearchViewModel
import java.io.IOException
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test

class SearchViewModelTest {

    @get:Rule
    val mainDispatcherRule = MainDispatcherRule()

    private val repository = FakeNewsRepository()
    private val viewModel = SearchViewModel(repository)

    @Test
    fun `starts idle without searching`() {
        assertEquals(IArticlesUiState.Idle, viewModel.state.value)
        assertTrue(repository.searchRequests.isEmpty())
    }

    @Test
    fun `typing does not search until submitted`() {
        viewModel.onQueryChange("台積電")

        assertEquals("台積電", viewModel.query.value)
        assertTrue(repository.searchRequests.isEmpty())
    }

    @Test
    fun `blank query is ignored`() {
        viewModel.onQueryChange("   ")

        viewModel.search()

        assertTrue(repository.searchRequests.isEmpty())
        assertEquals(IArticlesUiState.Idle, viewModel.state.value)
    }

    @Test
    fun `search trims the query and shows results`() {
        val articles = listOf(article("a"))
        repository.searchResults = { articles }
        viewModel.onQueryChange("  台積電 ")

        viewModel.search()

        assertEquals(listOf("台積電"), repository.searchRequests)
        assertEquals(IArticlesUiState.Success(articles), viewModel.state.value)
    }

    @Test
    fun `no results shows empty state`() {
        viewModel.onQueryChange("nothing")

        viewModel.search()

        assertEquals(IArticlesUiState.Empty, viewModel.state.value)
    }

    @Test
    fun `network failure shows error`() {
        repository.searchResults = { throw IOException() }
        viewModel.onQueryChange("台積電")

        viewModel.search()

        val state = viewModel.state.value
        assertTrue(state is IArticlesUiState.Error && "網路" in state.message)
    }
}
