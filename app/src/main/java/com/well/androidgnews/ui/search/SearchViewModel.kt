package com.well.androidgnews.ui.search

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.well.androidgnews.data.NewsRepository
import com.well.androidgnews.ui.common.ArticlesUiState
import com.well.androidgnews.ui.common.loadArticles
import javax.inject.Inject
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class SearchViewModel @Inject constructor(
    private val repository: NewsRepository,
) : ViewModel() {

    private val _query = MutableStateFlow("")
    val query: StateFlow<String> = _query.asStateFlow()

    private val _state = MutableStateFlow<ArticlesUiState>(ArticlesUiState.Idle)
    val state: StateFlow<ArticlesUiState> = _state.asStateFlow()

    private var searchJob: Job? = null

    fun onQueryChange(value: String) {
        _query.value = value
    }

    /** Searches only on explicit submit (not per keystroke) to stay within the daily request quota. */
    fun search() {
        val query = _query.value.trim()
        if (query.isEmpty()) return
        searchJob?.cancel()
        searchJob = viewModelScope.launch {
            _state.value = ArticlesUiState.Loading
            _state.value = loadArticles { repository.search(query) }
        }
    }
}
