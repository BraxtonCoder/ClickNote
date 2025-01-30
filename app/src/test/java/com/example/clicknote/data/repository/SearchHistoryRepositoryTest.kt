package com.example.clicknote.data.repository

import com.example.clicknote.data.dao.SearchHistoryDao
import com.example.clicknote.data.model.SearchHistory
import io.mockk.*
import io.mockk.impl.annotations.MockK
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.runTest
import org.junit.Before
import org.junit.Test
import java.time.LocalDateTime

class SearchHistoryRepositoryTest {

    @MockK
    private lateinit var searchHistoryDao: SearchHistoryDao

    private lateinit var repository: SearchHistoryRepositoryImpl

    @Before
    fun setup() {
        MockKAnnotations.init(this)
        repository = SearchHistoryRepositoryImpl(searchHistoryDao)
    }

    @Test
    fun `getRecentSearches returns flow from dao`() = runTest {
        // Given
        val mockSearches = listOf(
            SearchHistory(1, "test query", LocalDateTime.now(), 1),
            SearchHistory(2, "another query", LocalDateTime.now(), 2)
        )
        every { searchHistoryDao.getRecentSearches() } returns flowOf(mockSearches)

        // When
        val result = repository.getRecentSearches()

        // Then
        verify { searchHistoryDao.getRecentSearches() }
        coVerify(exactly = 0) { searchHistoryDao.insertSearch(any()) }
    }

    @Test
    fun `addSearch with empty query does nothing`() = runTest {
        // When
        repository.addSearch("   ")

        // Then
        coVerify(exactly = 0) { 
            searchHistoryDao.incrementUseCount(any(), any())
            searchHistoryDao.insertSearch(any())
        }
    }

    @Test
    fun `addSearch with valid query increments use count and inserts if new`() = runTest {
        // Given
        val query = "test query"
        coEvery { searchHistoryDao.incrementUseCount(any(), any()) } just Runs
        coEvery { searchHistoryDao.insertSearch(any()) } just Runs

        // When
        repository.addSearch(query)

        // Then
        coVerify { 
            searchHistoryDao.incrementUseCount(query, any())
            searchHistoryDao.insertSearch(match { 
                it.query == query && it.useCount == 1 
            })
        }
    }

    @Test
    fun `clearHistory calls dao clearSearchHistory`() = runTest {
        // Given
        coEvery { searchHistoryDao.clearSearchHistory() } just Runs

        // When
        repository.clearHistory()

        // Then
        coVerify { searchHistoryDao.clearSearchHistory() }
    }

    @Test
    fun `cleanupOldSearches calls dao deleteOldSearches with correct timestamp`() = runTest {
        // Given
        val timestamp = 1234567890L
        coEvery { searchHistoryDao.deleteOldSearches(any()) } just Runs

        // When
        repository.cleanupOldSearches(timestamp)

        // Then
        coVerify { searchHistoryDao.deleteOldSearches(timestamp) }
    }
} 