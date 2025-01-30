package com.example.clicknote.data.dao

import androidx.room.*
import com.example.clicknote.data.entity.SearchHistory
import kotlinx.coroutines.flow.Flow

@Dao
interface SearchHistoryDao {
    @Query("""
        SELECT * FROM search_history 
        WHERE type = :type 
        ORDER BY last_used DESC 
        LIMIT :limit
    """)
    fun getRecentSearches(type: String, limit: Int = 10): Flow<List<SearchHistory>>

    @Query("""
        SELECT * FROM search_history 
        WHERE type = :type 
        ORDER BY use_count DESC 
        LIMIT :limit
    """)
    fun getPopularSearches(type: String, limit: Int = 10): Flow<List<SearchHistory>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(searchHistory: SearchHistory)

    @Update
    suspend fun update(searchHistory: SearchHistory)

    @Query("DELETE FROM search_history WHERE id = :id")
    suspend fun delete(id: String)

    @Query("DELETE FROM search_history WHERE type = :type")
    suspend fun clearSearchHistory(type: String)

    @Query("""
        SELECT * FROM search_history 
        WHERE query = :query AND type = :type 
        LIMIT 1
    """)
    suspend fun findExistingSearch(query: String, type: String): SearchHistory?

    @Query("""
        UPDATE search_history 
        SET use_count = use_count + 1, 
            last_used = :timestamp 
        WHERE id = :id
    """)
    suspend fun incrementUseCount(id: String, timestamp: Long = System.currentTimeMillis())

    @Query("""
        SELECT * FROM search_history 
        WHERE type = :type 
        ORDER BY use_count DESC, last_used DESC 
        LIMIT :limit
    """)
    fun getSearchSuggestions(type: String, limit: Int = 5): Flow<List<SearchHistory>>

    @Query("DELETE FROM search_history")
    suspend fun clearAllSearchHistory()

    @Query("""
        SELECT * FROM search_history 
        WHERE type = :type 
        AND query LIKE '%' || :query || '%' 
        ORDER BY use_count DESC, last_used DESC 
        LIMIT :limit
    """)
    fun searchHistory(
        query: String,
        type: String,
        limit: Int = 5
    ): Flow<List<SearchHistory>>

    @Delete
    suspend fun deleteSearch(search: SearchHistory)

    @Query("""
        DELETE FROM search_history 
        WHERE last_used < :timestamp 
        AND type = :type
    """)
    suspend fun clearOldSearches(
        type: String,
        timestamp: Long
    )

    @Transaction
    suspend fun recordSearch(
        query: String,
        type: String,
        resultCount: Int
    ) {
        val existingSearch = findExistingSearch(query, type)
        if (existingSearch != null) {
            update(existingSearch.copy(
                useCount = existingSearch.useCount + 1,
                lastUsed = System.currentTimeMillis(),
                resultCount = resultCount
            ))
        } else {
            insert(SearchHistory(
                query = query,
                type = type,
                resultCount = resultCount
            ))
        }
    }

    @Query("""
        SELECT * FROM search_history 
        WHERE use_count >= :minUseCount 
        AND type = :type 
        ORDER BY use_count DESC, last_used DESC 
        LIMIT :limit
    """)
    fun getFrequentSearches(
        type: String,
        minUseCount: Int = 3,
        limit: Int = 5
    ): Flow<List<SearchHistory>>
} 