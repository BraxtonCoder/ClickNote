package com.example.clicknote.viewmodel

import com.example.clicknote.data.model.SearchHistory
import com.example.clicknote.domain.model.TimeFilter
import com.example.clicknote.domain.repository.SearchHistoryRepository
import io.mockk.*
import io.mockk.impl.annotations.MockK
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.*
import org.junit.After
import org.junit.Assert.*
import org.junit.Before
import org.junit.Test
import java.time.LocalDateTime

@OptIn(ExperimentalCoroutinesApi::class)
class SearchViewModelTest {

    @MockK
    private lateinit var searchHistoryRepository: SearchHistoryRepository

    private lateinit var viewModel: SearchViewModel
    private val testDispatcher = StandardTestDispatcher()

    @Before
    fun setup() {
        MockKAnnotations.init(this)
        Dispatchers.setMain(testDispatcher)
        
        // Setup default mocks
        every { searchHistoryRepository.getRecentSearches() } returns flowOf(emptyList())
        coEvery { searchHistoryRepository.cleanupOldSearches(any()) } just Runs
        coEvery { searchHistoryRepository.addSearch(any()) } just Runs
        coEvery { searchHistoryRepository.clearHistory() } just Runs

        viewModel = SearchViewModel(searchHistoryRepository)
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    @Test
    fun `init cleans up old searches`() = runTest {
        // Then
        coVerify { searchHistoryRepository.cleanupOldSearches() }
    }

    @Test
    fun `onSearchQueryChange updates query and adds to history if not empty`() = runTest {
        // When
        viewModel.onSearchQueryChange("test query")
        testDispatcher.scheduler.advanceUntilIdle()

        // Then
        assertEquals("test query", viewModel.searchQuery)
        coVerify { searchHistoryRepository.addSearch("test query") }
    }

    @Test
    fun `onSearchQueryChange with empty query only updates state`() = runTest {
        // When
        viewModel.onSearchQueryChange("")
        testDispatcher.scheduler.advanceUntilIdle()

        // Then
        assertEquals("", viewModel.searchQuery)
        coVerify(exactly = 0) { searchHistoryRepository.addSearch(any()) }
    }

    @Test
    fun `onTimeFilterChange updates timeFilter`() {
        // When
        viewModel.onTimeFilterChange(TimeFilter.LAST_7_DAYS)

        // Then
        assertEquals(TimeFilter.LAST_7_DAYS, viewModel.timeFilter)
    }

    @Test
    fun `onSearchFocusChange updates isSearchActive`() = runTest {
        // When
        viewModel.onSearchFocusChange(true)
        testDispatcher.scheduler.advanceUntilIdle()

        // Then
        assertTrue(viewModel.isSearchActive.value)

        // When
        viewModel.onSearchFocusChange(false)
        testDispatcher.scheduler.advanceUntilIdle()

        // Then
        assertFalse(viewModel.isSearchActive.value)
    }

    @Test
    fun `clearSearch resets query`() {
        // Given
        viewModel.onSearchQueryChange("test")

        // When
        viewModel.clearSearch()

        // Then
        assertEquals("", viewModel.searchQuery)
    }

    @Test
    fun `clearSearchHistory calls repository`() = runTest {
        // When
        viewModel.clearSearchHistory()
        testDispatcher.scheduler.advanceUntilIdle()

        // Then
        coVerify { searchHistoryRepository.clearHistory() }
    }

    @Test
    fun `onSearchHistoryItemClick updates query and adds to history`() = runTest {
        // When
        viewModel.onSearchHistoryItemClick("test query")
        testDispatcher.scheduler.advanceUntilIdle()

        // Then
        assertEquals("test query", viewModel.searchQuery)
        coVerify { searchHistoryRepository.addSearch("test query") }
    }
} 