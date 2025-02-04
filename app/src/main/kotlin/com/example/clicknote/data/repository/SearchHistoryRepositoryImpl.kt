package com.example.clicknote.data.repository

import com.example.clicknote.data.dao.SearchHistoryDao
import com.example.clicknote.data.entity.SearchHistory as SearchHistoryEntity
import com.example.clicknote.domain.model.SearchHistory
import com.example.clicknote.domain.model.SearchType
import com.example.clicknote.domain.repository.SearchHistoryRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import java.time.LocalDateTime
import java.util.UUID
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class SearchHistoryRepositoryImpl @Inject constructor(
    private val searchHistoryDao: SearchHistoryDao
) : SearchHistoryRepository {

    override fun getRecentSearches(limit: Int): Flow<List<SearchHistory>> =
        searchHistoryDao.getRecentSearches(LocalDateTime.now().minusDays(7)).map { entities ->
            entities.map { it.toDomain() }
        }

    override fun getSearchesByType(type: SearchType): Flow<List<SearchHistory>> =
        searchHistoryDao.getSearchHistoryByType(type.name).map { entities ->
            entities.map { it.toDomain() }
        }

    override suspend fun addSearch(query: String, type: SearchType) {
        val search = SearchHistoryEntity(
            id = UUID.randomUUID().toString(),
            query = query,
            type = type.name,
            useCount = 1,
            lastUsed = LocalDateTime.now()
        )
        searchHistoryDao.insert(search)
    }

    override suspend fun updateSearch(search: SearchHistory) {
        searchHistoryDao.update(
            SearchHistoryEntity(
                id = search.id,
                query = search.query,
                type = search.type.name,
                useCount = search.useCount,
                lastUsed = LocalDateTime.now()
            )
        )
    }

    override suspend fun deleteSearch(search: SearchHistory) {
        searchHistoryDao.deleteSearchHistory(search.id)
    }

    override suspend fun clearSearchHistory() {
        searchHistoryDao.clearAllSearchHistory()
    }

    override suspend fun clearSearchHistoryOlderThan(timestamp: Long) {
        // Convert timestamp to LocalDateTime and clear older entries
        val dateTime = LocalDateTime.ofEpochSecond(timestamp / 1000, 0, java.time.ZoneOffset.UTC)
        searchHistoryDao.getRecentSearches(dateTime).collect { searches ->
            searches.forEach { search ->
                searchHistoryDao.deleteSearchHistory(search.id)
            }
        }
    }

    override fun getPopularSearches(limit: Int): Flow<List<SearchHistory>> =
        searchHistoryDao.getTopSearches(limit).map { entities ->
            entities.map { it.toDomain() }
        }

    override suspend fun getSearchCount(): Int =
        searchHistoryDao.getAllSearchHistory().map { it.size }.first()

    override suspend fun incrementUseCount(searchId: String) {
        searchHistoryDao.incrementUseCount(searchId, LocalDateTime.now())
    }

    private fun SearchHistoryEntity.toDomain() = SearchHistory(
        id = id,
        query = query,
        type = SearchType.valueOf(type),
        useCount = useCount,
        timestamp = lastUsed.toEpochSecond(java.time.ZoneOffset.UTC) * 1000
    )
} 