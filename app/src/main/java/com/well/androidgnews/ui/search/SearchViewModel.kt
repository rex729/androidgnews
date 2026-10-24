package com.well.androidgnews.ui.search

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.well.androidgnews.analytics.IAnalytics
import com.well.androidgnews.data.INewsRepository
import com.well.androidgnews.ui.common.IArticlesUiState
import com.well.androidgnews.ui.common.loadArticles
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class SearchViewModel(
    private val repository: INewsRepository,
    private val analytics: IAnalytics,
) : ViewModel() {

    private val _query = MutableStateFlow("")
    val query: StateFlow<String> = _query.asStateFlow()

    private val _state = MutableStateFlow<IArticlesUiState>(IArticlesUiState.Idle)
    val state: StateFlow<IArticlesUiState> = _state.asStateFlow()

    private var searchJob: Job? = null

    fun onQueryChange(value: String) {
        _query.value = value
    }

    /** Searches only on explicit submit (not per keystroke) to stay within the daily request quota. */
    fun search() {
        val query = _query.value.trim()
        if (query.isEmpty()) return
        analytics.searched(query)
        searchJob?.cancel()
        searchJob = viewModelScope.launch {
            _state.value = IArticlesUiState.Loading
            _state.value = loadArticles { repository.search(query) }
        }
    }
}
