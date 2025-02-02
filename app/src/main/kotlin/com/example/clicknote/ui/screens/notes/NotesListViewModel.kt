package com.example.clicknote.ui.screens.notes

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.clicknote.domain.model.Note
import com.example.clicknote.domain.repository.NoteRepository
import com.example.clicknote.service.ClipboardService
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import java.time.LocalDateTime
import javax.inject.Inject

@HiltViewModel
class NotesListViewModel @Inject constructor(
    private val noteRepository: NoteRepository,
    private val clipboardService: ClipboardService
) : ViewModel() {

    private val _searchQuery = MutableStateFlow("")
    val searchQuery = _searchQuery.asStateFlow()

    private val _selectedNotes = MutableStateFlow<Set<String>>(emptySet())
    val selectedNotes = _selectedNotes.asStateFlow()

    var isDateRangeFilterVisible by mutableStateOf(false)
        private set

    private val _dateRange = MutableStateFlow<Pair<LocalDateTime?, LocalDateTime?>>(null to null)

    @OptIn(ExperimentalCoroutinesApi::class)
    val notes = combine(
        _searchQuery,
        _dateRange
    ) { query, dateRange ->
        Triple(query, dateRange.first, dateRange.second)
    }.flatMapLatest { (query, startDate, endDate) ->
        noteRepository.getNotes(
            query = query,
            startDate = startDate,
            endDate = endDate
        )
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = emptyList()
    )

    fun setSearchQuery(query: String) {
        _searchQuery.value = query
    }

    fun setDateRange(startDate: LocalDateTime?, endDate: LocalDateTime?) {
        _dateRange.value = startDate to endDate
        isDateRangeFilterVisible = false
    }

    fun showDateRangeFilter() {
        isDateRangeFilterVisible = true
    }

    fun hideDateRangeFilter() {
        isDateRangeFilterVisible = false
    }

    fun toggleNoteSelection(noteId: String) {
        _selectedNotes.update { selected ->
            if (noteId in selected) {
                selected - noteId
            } else {
                selected + noteId
            }
        }
    }

    fun clearSelection() {
        _selectedNotes.value = emptySet()
    }

    fun copySelectedNotes() {
        viewModelScope.launch {
            val selectedNotesList = notes.value.filter { it.id in selectedNotes.value }
            val text = selectedNotesList.joinToString("\n\n") { it.content }
            clipboardService.copyToClipboard(text)
            clearSelection()
        }
    }

    fun moveSelectedNotesToTrash() {
        viewModelScope.launch {
            selectedNotes.value.forEach { noteId ->
                noteRepository.moveToTrash(noteId)
            }
            clearSelection()
        }
    }
} 