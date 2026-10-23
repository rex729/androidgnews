package com.well.androidgnews.testing

import com.well.androidgnews.data.FakeNewsRepository
import com.well.androidgnews.data.article
import com.well.androidgnews.data.httpException
import com.well.androidgnews.data.model.Category
import com.well.androidgnews.rule.MainDispatcherRule
import com.well.androidgnews.ui.common.IArticlesUiState
import com.well.androidgnews.ui.headlines.HeadlinesViewModel
import java.io.IOException
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test

class HeadlinesViewModelTest {

    @get:Rule
    val mainDispatcherRule = MainDispatcherRule()

    private val repository = FakeNewsRepository()

    @Test
    fun `loads general headlines on start`() {
        val articles = listOf(article("a"), article("b"))
        repository.headlines = { articles }

        val viewModel = HeadlinesViewModel(repository)

        assertEquals(Category.GENERAL, viewModel.selectedCategory.value)
        assertEquals(IArticlesUiState.Success(articles), viewModel.state.value)
        assertEquals(listOf(Category.GENERAL), repository.headlineRequests)
    }

    @Test
    fun `no articles shows empty state`() {
        repository.headlines = { emptyList() }

        val viewModel = HeadlinesViewModel(repository)

        assertEquals(IArticlesUiState.Empty, viewModel.state.value)
    }

    @Test
    fun `backend failure shows error`() {
        repository.headlines = { throw httpException(502) }

        val viewModel = HeadlinesViewModel(repository)

        val state = viewModel.state.value
        assertTrue(state is IArticlesUiState.Error && "伺服器" in state.message)
    }

    @Test
    fun `selecting a category loads it`() {
        repository.headlines = { listOf(article(it.apiValue)) }
        val viewModel = HeadlinesViewModel(repository)

        viewModel.selectCategory(Category.TECHNOLOGY)

        assertEquals(Category.TECHNOLOGY, viewModel.selectedCategory.value)
        assertEquals(IArticlesUiState.Success(listOf(article("technology"))), viewModel.state.value)
    }

    @Test
    fun `returning to a loaded category uses the cache`() {
        repository.headlines = { listOf(article(it.apiValue)) }
        val viewModel = HeadlinesViewModel(repository)

        viewModel.selectCategory(Category.TECHNOLOGY)
        viewModel.selectCategory(Category.GENERAL)

        assertEquals(listOf(Category.GENERAL, Category.TECHNOLOGY), repository.headlineRequests)
        assertEquals(IArticlesUiState.Success(listOf(article("general"))), viewModel.state.value)
    }

    @Test
    fun `selecting the current category does nothing`() {
        val viewModel = HeadlinesViewModel(repository)

        viewModel.selectCategory(Category.GENERAL)

        assertEquals(listOf(Category.GENERAL), repository.headlineRequests)
    }

    @Test
    fun `failed category is not cached`() {
        repository.headlines = { if (it == Category.SPORTS) throw IOException() else listOf(article(it.apiValue)) }
        val viewModel = HeadlinesViewModel(repository)

        viewModel.selectCategory(Category.SPORTS)
        viewModel.selectCategory(Category.GENERAL)
        viewModel.selectCategory(Category.SPORTS)

        assertEquals(listOf(Category.GENERAL, Category.SPORTS, Category.SPORTS), repository.headlineRequests)
    }

    @Test
    fun `refresh fetches again even when cached`() {
        repository.headlines = { listOf(article("first")) }
        val viewModel = HeadlinesViewModel(repository)
        repository.headlines = { listOf(article("second")) }

        viewModel.refresh()

        assertEquals(listOf(Category.GENERAL, Category.GENERAL), repository.headlineRequests)
        assertEquals(IArticlesUiState.Success(listOf(article("second"))), viewModel.state.value)
        assertFalse(viewModel.isRefreshing.value)
    }

    @Test
    fun `retry after an error loads again`() {
        repository.headlines = { throw IOException() }
        val viewModel = HeadlinesViewModel(repository)
        repository.headlines = { listOf(article("a")) }

        viewModel.retry()

        assertEquals(IArticlesUiState.Success(listOf(article("a"))), viewModel.state.value)
    }
}
