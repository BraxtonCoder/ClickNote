package com.example.clicknote.data.repository

import com.example.clicknote.data.dao.SearchHistoryDao
import com.example.clicknote.data.entity.SearchHistoryEntity
import com.example.clicknote.domain.model.SearchHistory
import com.example.clicknote.domain.model.SearchType
import com.example.clicknote.domain.repository.SearchHistoryRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import java.util.UUID
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
        searchHistoryDao.getSearchesByType(type.name).map { entities ->
            entities.map { it.toDomain() }
        }

    override suspend fun addSearch(query: String, type: SearchType) {
        val search = SearchHistoryEntity(
            id = UUID.randomUUID().toString(),
            query = query,
            type = type.name,
            useCount = 1,
            timestamp = System.currentTimeMillis()
        )
        searchHistoryDao.insertSearch(search)
    }

    override suspend fun updateSearch(search: SearchHistory) {
        searchHistoryDao.updateSearch(
            SearchHistoryEntity(
                id = search.id,
                query = search.query,
                type = search.type.name,
                useCount = search.useCount,
                timestamp = search.timestamp
            )
        )
    }

    override suspend fun deleteSearch(search: SearchHistory) {
        searchHistoryDao.deleteSearch(
            SearchHistoryEntity(
                id = search.id,
                query = search.query,
                type = search.type.name,
                useCount = search.useCount,
                timestamp = search.timestamp
            )
        )
    }

    override suspend fun clearSearchHistory() {
        searchHistoryDao.clearSearchHistory()
    }

    override suspend fun clearSearchHistoryOlderThan(timestamp: Long) {
        searchHistoryDao.clearSearchHistoryOlderThan(timestamp)
    }

    override fun getPopularSearches(limit: Int): Flow<List<SearchHistory>> =
        searchHistoryDao.getPopularSearches(limit).map { entities ->
            entities.map { it.toDomain() }
        }

    override suspend fun getSearchCount(): Int =
        searchHistoryDao.getSearchCount()

    override suspend fun incrementUseCount(searchId: String) {
        val search = searchHistoryDao.getSearchById(searchId)
        search?.let {
            searchHistoryDao.updateSearch(it.copy(
                useCount = it.useCount + 1,
                timestamp = System.currentTimeMillis()
            ))
        }
    }

    private fun SearchHistoryEntity.toDomain() = SearchHistory(
        id = id,
        query = query,
        type = SearchType.valueOf(type),
        useCount = useCount,
        timestamp = timestamp
    )
} 