package com.example.clicknote.data.dao

import androidx.room.*
import com.example.clicknote.data.entity.SearchHistoryEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface SearchHistoryDao {
    @Query("SELECT * FROM search_history ORDER BY timestamp DESC")
    fun getAllSearchHistory(): Flow<List<SearchHistoryEntity>>

    @Query("SELECT * FROM search_history ORDER BY timestamp DESC LIMIT :limit")
    fun getRecentSearches(limit: Int = 10): Flow<List<SearchHistoryEntity>>

    @Query("SELECT * FROM search_history WHERE type = :type ORDER BY timestamp DESC")
    fun getSearchHistoryByType(type: String): Flow<List<SearchHistoryEntity>>

    @Query("SELECT * FROM search_history ORDER BY use_count DESC LIMIT :limit")
    fun getPopularSearches(limit: Int = 10): Flow<List<SearchHistoryEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(searchHistory: SearchHistoryEntity)

    @Update
    suspend fun update(searchHistory: SearchHistoryEntity)

    @Query("DELETE FROM search_history")
    suspend fun clearAll()

    @Query("DELETE FROM search_history WHERE query = :query")
    suspend fun deleteByQuery(query: String)

    @Query("DELETE FROM search_history WHERE timestamp < :timestamp")
    suspend fun deleteOlderThan(timestamp: Long)

    @Query("UPDATE search_history SET use_count = use_count + 1 WHERE id = :searchId")
    suspend fun incrementUseCount(searchId: String)

    @Query("SELECT COUNT(*) FROM search_history")
    suspend fun getSearchCount(): Int
} 