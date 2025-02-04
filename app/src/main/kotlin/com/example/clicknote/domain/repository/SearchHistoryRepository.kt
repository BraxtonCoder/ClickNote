package com.example.clicknote.domain.repository

import com.example.clicknote.domain.model.SearchHistory
import com.example.clicknote.domain.model.SearchType
import kotlinx.coroutines.flow.Flow

interface SearchHistoryRepository {
    fun getRecentSearches(limit: Int = 10): Flow<List<SearchHistory>>
    fun getSearchesByType(type: SearchType): Flow<List<SearchHistory>>
    suspend fun addSearch(query: String, type: SearchType)
    suspend fun updateSearch(search: SearchHistory)
    suspend fun deleteSearch(search: SearchHistory)
    suspend fun clearSearchHistory()
    suspend fun clearSearchHistoryOlderThan(timestamp: Long)
    fun getPopularSearches(limit: Int = 10): Flow<List<SearchHistory>>
    suspend fun getSearchCount(): Int
    suspend fun incrementUseCount(searchId: String)
} 