package com.example.clicknote.data.repository

import com.example.clicknote.data.dao.SearchHistoryDao
import com.example.clicknote.domain.model.SearchHistory
import com.example.clicknote.domain.model.SearchType
import com.example.clicknote.domain.repository.SearchHistoryRepository
import kotlinx.coroutines.flow.Flow
import java.time.LocalDateTime
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class SearchHistoryRepositoryImpl @Inject constructor(
    private val searchHistoryDao: SearchHistoryDao
) : SearchHistoryRepository {
    
    override fun getRecentSearches(limit: Int): Flow<List<SearchHistory>> {
        return searchHistoryDao.getRecentSearches(limit)
    }
    
    override fun getSearchesByType(type: SearchType): Flow<List<SearchHistory>> {
        return searchHistoryDao.getSearchesByType(type)
    }
    
    override suspend fun addSearch(query: String, type: SearchType, resultCount: Int) {
        val search = SearchHistory(
            query = query,
            type = type,
            resultCount = resultCount,
            lastUsed = LocalDateTime.now(),
            createdAt = LocalDateTime.now(),
            useCount = 1
        )
        searchHistoryDao.insertSearch(search)
    }
    
    override suspend fun updateSearch(search: SearchHistory) {
        searchHistoryDao.updateSearch(search.copy(
            lastUsed = LocalDateTime.now(),
            useCount = search.useCount + 1
        ))
    }
    
    override suspend fun deleteSearch(search: SearchHistory) {
        searchHistoryDao.deleteSearch(search)
    }
    
    override suspend fun clearSearchHistory() {
        searchHistoryDao.clearSearchHistory()
    }
    
    override suspend fun clearSearchHistoryOlderThan(days: Int) {
        val cutoffDate = LocalDateTime.now().minusDays(days.toLong())
        searchHistoryDao.clearSearchHistoryOlderThan(cutoffDate)
    }
    
    override fun getPopularSearches(limit: Int): Flow<List<SearchHistory>> {
        return searchHistoryDao.getPopularSearches(limit)
    }
    
    override suspend fun getSearchCount(): Int {
        return searchHistoryDao.getSearchCount()
    }
    
    override suspend fun incrementUseCount(searchId: String) {
        searchHistoryDao.incrementUseCount(searchId)
    }
} 