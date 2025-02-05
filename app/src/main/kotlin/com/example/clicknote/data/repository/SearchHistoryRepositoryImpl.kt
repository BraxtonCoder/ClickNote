package com.example.clicknote.data.repository

import com.example.clicknote.data.dao.SearchHistoryDao
import com.example.clicknote.data.entity.SearchHistoryEntity
import com.example.clicknote.domain.model.SearchHistory
import com.example.clicknote.domain.model.SearchType
import com.example.clicknote.domain.repository.SearchHistoryRepository
import kotlinx.coroutines.flow.*
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class SearchHistoryRepositoryImpl @Inject constructor(
    private val searchHistoryDao: SearchHistoryDao
) : SearchHistoryRepository {

    override fun getRecentSearches(limit: Int): Flow<List<SearchHistory>> =
        searchHistoryDao.getRecentSearches(limit).map { entities ->
            entities.map { it.toDomain() }
        }

    override fun getSearchesByType(type: SearchType): Flow<List<SearchHistory>> =
        searchHistoryDao.getSearchHistoryByType(type.name).map { entities ->
            entities.map { it.toDomain() }
        }

    override suspend fun addSearch(query: String, type: SearchType) {
        val searchHistory = SearchHistoryEntity(
            id = java.util.UUID.randomUUID().toString(),
            query = query,
            timestamp = System.currentTimeMillis(),
            useCount = 1,
            type = type.name
        )
        searchHistoryDao.insert(searchHistory)
    }

    override suspend fun updateSearch(search: SearchHistory) {
        searchHistoryDao.update(search.toEntity())
    }

    override suspend fun deleteSearch(search: SearchHistory) {
        searchHistoryDao.deleteByQuery(search.query)
    }

    override suspend fun clearSearchHistory() {
        searchHistoryDao.clearAll()
    }

    override suspend fun clearSearchHistoryOlderThan(timestamp: Long) {
        searchHistoryDao.deleteOlderThan(timestamp)
    }

    override fun getPopularSearches(limit: Int): Flow<List<SearchHistory>> =
        searchHistoryDao.getPopularSearches(limit).map { entities ->
            entities.map { it.toDomain() }
        }

    override suspend fun getSearchCount(): Int =
        searchHistoryDao.getSearchCount()

    override suspend fun incrementUseCount(searchId: String) {
        searchHistoryDao.incrementUseCount(searchId)
    }

    override suspend fun addSearchQuery(query: String) {
        addSearch(query, SearchType.GENERAL)
    }

    override suspend fun getRecentSearches(): Flow<List<String>> =
        searchHistoryDao.getRecentSearches().map { entities ->
            entities.map { it.query }
        }

    override suspend fun deleteSearchQuery(query: String) {
        searchHistoryDao.deleteByQuery(query)
    }

    private fun SearchHistoryEntity.toDomain(): SearchHistory =
        SearchHistory(
            id = id,
            query = query,
            type = SearchType.valueOf(type),
            useCount = useCount,
            timestamp = timestamp
        )

    private fun SearchHistory.toEntity(): SearchHistoryEntity =
        SearchHistoryEntity(
            id = id,
            query = query,
            type = type.name,
            useCount = useCount,
            timestamp = timestamp
        )
} 