package com.example.clicknote.ui.viewmodel

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.clicknote.data.model.SearchHistory
import com.example.clicknote.domain.model.TimeFilter
import com.example.clicknote.domain.repository.SearchHistoryRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class SearchViewModel @Inject constructor(
    private val searchHistoryRepository: SearchHistoryRepository
) : ViewModel() {

    var searchQuery by mutableStateOf("")
        private set

    var timeFilter by mutableStateOf(TimeFilter.ALL)
        private set

    private val _isSearchActive = MutableStateFlow(false)
    val isSearchActive: StateFlow<Boolean> = _isSearchActive

    val recentSearches: StateFlow<List<SearchHistory>> = searchHistoryRepository
        .getRecentSearches()
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )

    init {
        // Clean up old searches on init
        viewModelScope.launch {
            searchHistoryRepository.cleanupOldSearches()
        }
    }

    fun onSearchQueryChange(query: String) {
        searchQuery = query
        viewModelScope.launch {
            if (query.isNotEmpty()) {
                searchHistoryRepository.addSearch(query)
            }
        }
    }

    fun onTimeFilterChange(filter: TimeFilter) {
        timeFilter = filter
    }

    fun onSearchFocusChange(focused: Boolean) {
        _isSearchActive.value = focused
    }

    fun clearSearch() {
        searchQuery = ""
    }

    fun clearSearchHistory() {
        viewModelScope.launch {
            searchHistoryRepository.clearHistory()
        }
    }

    fun onSearchHistoryItemClick(query: String) {
        searchQuery = query
        viewModelScope.launch {
            searchHistoryRepository.addSearch(query)
        }
    }
} 