package com.example.clicknote.ui.screens

import androidx.compose.ui.test.*
import androidx.compose.ui.test.junit4.createComposeRule
import com.example.clicknote.domain.model.TimeFilter
import com.example.clicknote.ui.theme.ClickNoteTheme
import com.example.clicknote.ui.viewmodel.SearchViewModel
import io.mockk.every
import io.mockk.mockk
import kotlinx.coroutines.flow.MutableStateFlow
import org.junit.Rule
import org.junit.Test

class NotesScreenTest {

    @get:Rule
    val composeTestRule = createComposeRule()

    @Test
    fun searchBar_isDisplayed() {
        // Given
        val mockViewModel = mockk<SearchViewModel>(relaxed = true) {
            every { searchQuery } returns ""
            every { timeFilter } returns TimeFilter.ALL
            every { recentSearches } returns MutableStateFlow(emptyList())
            every { isSearchActive } returns MutableStateFlow(false)
        }

        // When
        composeTestRule.setContent {
            ClickNoteTheme {
                NotesScreen(
                    onOpenDrawer = {},
                    viewModel = mockViewModel
                )
            }
        }

        // Then
        composeTestRule.onNodeWithText("Search notes").assertExists()
    }

    @Test
    fun searchBar_showsRecentSearches_whenFocused() {
        // Given
        val mockViewModel = mockk<SearchViewModel>(relaxed = true) {
            every { searchQuery } returns ""
            every { timeFilter } returns TimeFilter.ALL
            every { recentSearches } returns MutableStateFlow(emptyList())
            every { isSearchActive } returns MutableStateFlow(true)
        }

        // When
        composeTestRule.setContent {
            ClickNoteTheme {
                NotesScreen(
                    onOpenDrawer = {},
                    viewModel = mockViewModel
                )
            }
        }

        // Then
        composeTestRule.onNodeWithText("Recent Searches").assertExists()
    }

    @Test
    fun emptyState_showsNoNotesMessage_whenNoSearchQuery() {
        // Given
        val mockViewModel = mockk<SearchViewModel>(relaxed = true) {
            every { searchQuery } returns ""
            every { timeFilter } returns TimeFilter.ALL
            every { recentSearches } returns MutableStateFlow(emptyList())
            every { isSearchActive } returns MutableStateFlow(false)
        }

        // When
        composeTestRule.setContent {
            ClickNoteTheme {
                NotesScreen(
                    onOpenDrawer = {},
                    viewModel = mockViewModel
                )
            }
        }

        // Then
        composeTestRule.onNodeWithText("No notes yet").assertExists()
    }

    @Test
    fun emptyState_hidesNoNotesMessage_whenSearching() {
        // Given
        val mockViewModel = mockk<SearchViewModel>(relaxed = true) {
            every { searchQuery } returns "test"
            every { timeFilter } returns TimeFilter.ALL
            every { recentSearches } returns MutableStateFlow(emptyList())
            every { isSearchActive } returns MutableStateFlow(true)
        }

        // When
        composeTestRule.setContent {
            ClickNoteTheme {
                NotesScreen(
                    onOpenDrawer = {},
                    viewModel = mockViewModel
                )
            }
        }

        // Then
        composeTestRule.onNodeWithText("No notes yet").assertDoesNotExist()
    }
} 