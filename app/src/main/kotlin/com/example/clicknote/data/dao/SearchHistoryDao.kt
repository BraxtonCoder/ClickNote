package com.example.clicknote.data.dao

import androidx.room.*
import com.example.clicknote.data.entity.SearchHistory
import kotlinx.coroutines.flow.Flow
import java.time.LocalDateTime

@Dao
interface SearchHistoryDao {
    @Query("SELECT * FROM search_history WHERE is_deleted = 0 ORDER BY last_used DESC")
    fun getAllSearchHistory(): Flow<List<SearchHistory>>

    @Query("SELECT * FROM search_history WHERE is_deleted = 0 AND type = :type ORDER BY last_used DESC")
    fun getSearchHistoryByType(type: String): Flow<List<SearchHistory>>

    @Query("SELECT * FROM search_history WHERE is_deleted = 0 AND query LIKE '%' || :query || '%' ORDER BY last_used DESC")
    fun searchHistory(query: String): Flow<List<SearchHistory>>

    @Query("SELECT * FROM search_history WHERE id = :id AND is_deleted = 0")
    suspend fun getSearchHistoryById(id: String): SearchHistory?

    @Query("""
        UPDATE search_history 
        SET use_count = use_count + 1, 
            last_used = :lastUsed 
        WHERE id = :id
    """)
    suspend fun incrementUseCount(id: String, lastUsed: LocalDateTime = LocalDateTime.now())

    @Query("SELECT * FROM search_history WHERE is_deleted = 0 ORDER BY use_count DESC LIMIT :limit")
    fun getMostUsedSearches(limit: Int): Flow<List<SearchHistory>>

    @Query("SELECT * FROM search_history WHERE is_deleted = 0 AND type = :type ORDER BY use_count DESC LIMIT :limit")
    fun getMostUsedSearchesByType(type: String, limit: Int): Flow<List<SearchHistory>>

    @Query("SELECT * FROM search_history WHERE is_deleted = 0 AND last_used >= :timestamp ORDER BY last_used DESC")
    fun getRecentSearches(timestamp: LocalDateTime): Flow<List<SearchHistory>>

    @Query("UPDATE search_history SET is_deleted = 1 WHERE id = :id")
    suspend fun deleteSearchHistory(id: String)

    @Query("UPDATE search_history SET is_deleted = 1")
    suspend fun clearAllSearchHistory()

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(searchHistory: SearchHistory)

    @Update
    suspend fun update(searchHistory: SearchHistory)

    @Query("SELECT * FROM search_history WHERE is_deleted = 0 ORDER BY use_count DESC, last_used DESC LIMIT :limit")
    fun getTopSearches(limit: Int): Flow<List<SearchHistory>>
} 