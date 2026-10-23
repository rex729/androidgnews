package com.well.androidgnews.ui.headlines

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.well.androidgnews.data.INewsRepository
import com.well.androidgnews.data.model.Category
import com.well.androidgnews.ui.common.IArticlesUiState
import com.well.androidgnews.ui.common.loadArticles
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class HeadlinesViewModel(
    private val repository: INewsRepository,
) : ViewModel() {

    private val _selectedCategory = MutableStateFlow(Category.GENERAL)
    val selectedCategory: StateFlow<Category> = _selectedCategory.asStateFlow()

    private val _state = MutableStateFlow<IArticlesUiState>(IArticlesUiState.Loading)
    val state: StateFlow<IArticlesUiState> = _state.asStateFlow()

    private val _isRefreshing = MutableStateFlow(false)
    val isRefreshing: StateFlow<Boolean> = _isRefreshing.asStateFlow()

    // The free plan only allows 100 requests a day, so switching tabs reuses what was already loaded;
    // only pull-to-refresh hits the network again.
    private val cache = mutableMapOf<Category, IArticlesUiState.Success>()
    private var loadJob: Job? = null

    init {
        load(Category.GENERAL, isRefresh = false)
    }

    fun selectCategory(category: Category) {
        if (category == _selectedCategory.value) return
        _selectedCategory.value = category
        val cached = cache[category]
        if (cached != null) {
            loadJob?.cancel()
            _isRefreshing.value = false
            _state.value = cached
        } else {
            load(category, isRefresh = false)
        }
    }

    fun refresh() = load(_selectedCategory.value, isRefresh = true)

    fun retry() = load(_selectedCategory.value, isRefresh = false)

    private fun load(category: Category, isRefresh: Boolean) {
        loadJob?.cancel()
        loadJob = viewModelScope.launch {
            if (isRefresh) _isRefreshing.value = true else _state.value = IArticlesUiState.Loading
            try {
                val result = loadArticles { repository.topHeadlines(category) }
                if (result is IArticlesUiState.Success) cache[category] = result
                _state.value = result
            } finally {
                _isRefreshing.value = false
            }
        }
    }
}
