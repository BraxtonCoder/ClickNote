package com.example.clicknote.domain.repository

import com.example.clicknote.data.model.SearchHistory
import kotlinx.coroutines.flow.Flow

interface SearchHistoryRepository {
    fun getRecentSearches(): Flow<List<SearchHistory>>
    suspend fun addSearch(query: String)
    suspend fun clearHistory()
    suspend fun cleanupOldSearches(olderThan: Long = System.currentTimeMillis() - 30L * 24 * 60 * 60 * 1000) // 30 days
} 