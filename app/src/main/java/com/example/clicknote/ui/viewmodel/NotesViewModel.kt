package com.example.clicknote.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.clicknote.domain.model.Note
import com.example.clicknote.domain.model.TimeFilter
import com.example.clicknote.domain.repository.NoteRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import java.time.LocalDateTime
import javax.inject.Inject

data class SnackbarMessage(
    val message: String,
    val actionLabel: String? = null,
    val action: (() -> Unit)? = null
)

data class NoteCountState(
    val counts: Map<TimeFilter, Int> = emptyMap(),
    val isLoading: Boolean = false,
    val lastUpdated: LocalDateTime = LocalDateTime.now()
)

@HiltViewModel
class NotesViewModel @Inject constructor(
    private val noteRepository: NoteRepository
) : ViewModel() {

    private val _searchQuery = MutableStateFlow("")
    val searchQuery = _searchQuery.asStateFlow()

    private val _timeFilter = MutableStateFlow(TimeFilter.ALL)
    val timeFilter = _timeFilter.asStateFlow()

    private val _selectedFolderId = MutableStateFlow<Long?>(null)
    val selectedFolderId = _selectedFolderId.asStateFlow()

    private val _isLoading = MutableStateFlow(false)
    val isLoading = _isLoading.asStateFlow()

    private val _snackbarMessage = MutableStateFlow<SnackbarMessage?>(null)
    val snackbarMessage = _snackbarMessage.asStateFlow()

    private val _noteCountState = MutableStateFlow(NoteCountState())
    val noteCountState = _noteCountState.asStateFlow()

    // Cache timeout in minutes
    private val CACHE_TIMEOUT = 5L

    @OptIn(ExperimentalCoroutinesApi::class)
    val filteredNotes = combine(
        noteRepository.getAllNotes(),
        searchQuery,
        timeFilter,
        selectedFolderId
    ) { notes, query, filter, folderId ->
        _isLoading.value = true
        try {
            notes.filter { note ->
                val matchesQuery = note.title.contains(query, ignoreCase = true) ||
                    note.content.contains(query, ignoreCase = true)

                val matchesTimeFilter = when (filter) {
                    TimeFilter.ALL -> true
                    TimeFilter.TODAY -> note.createdAt.toLocalDate() == LocalDateTime.now().toLocalDate()
                    TimeFilter.LAST_7_DAYS -> note.createdAt.isAfter(LocalDateTime.now().minusDays(7))
                    TimeFilter.LAST_30_DAYS -> note.createdAt.isAfter(LocalDateTime.now().minusDays(30))
                    TimeFilter.LAST_3_MONTHS -> note.createdAt.isAfter(LocalDateTime.now().minusMonths(3))
                    TimeFilter.LAST_6_MONTHS -> note.createdAt.isAfter(LocalDateTime.now().minusMonths(6))
                    TimeFilter.LAST_YEAR -> note.createdAt.isAfter(LocalDateTime.now().minusYears(1))
                    is TimeFilter.CUSTOM -> {
                        note.createdAt.isAfter(filter.startDate.atStartOfDay()) &&
                        note.createdAt.isBefore(filter.endDate.atTime(23, 59, 59))
                    }
                }

                val matchesFolder = folderId?.let { id ->
                    note.folderId == id
                } ?: true

                matchesQuery && matchesTimeFilter && matchesFolder && !note.isDeleted
            }.sortedByDescending { it.createdAt }
        } finally {
            _isLoading.value = false
        }
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = emptyList()
    )

    init {
        observeNotes()
    }

    private fun observeNotes() {
        viewModelScope.launch {
            noteRepository.getAllNotes()
                .collect { notes ->
                    updateNoteCounts(notes)
                }
        }
    }

    private fun updateNoteCounts(notes: List<Note>) {
        viewModelScope.launch {
            // Check if cache is still valid
            val now = LocalDateTime.now()
            val lastUpdated = _noteCountState.value.lastUpdated
            if (!_noteCountState.value.isLoading && 
                !_noteCountState.value.counts.isEmpty() &&
                lastUpdated.plusMinutes(CACHE_TIMEOUT).isAfter(now)) {
                return@launch
            }

            _noteCountState.update { it.copy(isLoading = true) }
            
            try {
                val nonDeletedNotes = notes.filter { !it.isDeleted }
                
                val counts = buildMap {
                    put(TimeFilter.ALL, nonDeletedNotes.size)
                    put(TimeFilter.TODAY, nonDeletedNotes.count { 
                        it.createdAt.toLocalDate() == now.toLocalDate() 
                    })
                    put(TimeFilter.LAST_7_DAYS, nonDeletedNotes.count { 
                        it.createdAt.isAfter(now.minusDays(7))
                    })
                    put(TimeFilter.LAST_30_DAYS, nonDeletedNotes.count { 
                        it.createdAt.isAfter(now.minusDays(30))
                    })
                    put(TimeFilter.LAST_3_MONTHS, nonDeletedNotes.count { 
                        it.createdAt.isAfter(now.minusMonths(3))
                    })
                    put(TimeFilter.LAST_6_MONTHS, nonDeletedNotes.count { 
                        it.createdAt.isAfter(now.minusMonths(6))
                    })
                    put(TimeFilter.LAST_YEAR, nonDeletedNotes.count { 
                        it.createdAt.isAfter(now.minusYears(1))
                    })
                    
                    timeFilter.value.let { currentFilter ->
                        if (currentFilter is TimeFilter.CUSTOM) {
                            put(currentFilter, nonDeletedNotes.count {
                                it.createdAt.isAfter(currentFilter.startDate.atStartOfDay()) &&
                                it.createdAt.isBefore(currentFilter.endDate.atTime(23, 59, 59))
                            })
                        }
                    }
                }

                _noteCountState.update { 
                    it.copy(
                        counts = counts,
                        isLoading = false,
                        lastUpdated = now
                    )
                }
            } catch (e: Exception) {
                _noteCountState.update { 
                    it.copy(
                        isLoading = false,
                        lastUpdated = now
                    )
                }
            }
        }
    }

    fun setSearchQuery(query: String) {
        _searchQuery.value = query
    }

    fun setTimeFilter(filter: TimeFilter) {
        _timeFilter.value = filter
        // Force update counts when custom filter changes
        if (filter is TimeFilter.CUSTOM) {
            viewModelScope.launch {
                noteRepository.getAllNotes().first()?.let { notes ->
                    updateNoteCounts(notes)
                }
            }
        }
    }

    private val _selectedNotes = MutableStateFlow<Set<Long>>(emptySet())
    val selectedNotes = _selectedNotes.asStateFlow()

    fun toggleNoteSelection(noteId: Long) {
        _selectedNotes.update { selected ->
            if (selected.contains(noteId)) {
                selected - noteId
            } else {
                selected + noteId
            }
        }
    }

    fun clearSelection() {
        _selectedNotes.value = emptySet()
    }

    fun deleteSelectedNotes() {
        viewModelScope.launch {
            val selectedIds = _selectedNotes.value.toList()
            selectedIds.forEach { noteId ->
                noteRepository.moveNoteToTrash(noteId)
            }
            clearSelection()
            
            _snackbarMessage.value = SnackbarMessage(
                message = "${selectedIds.size} notes moved to trash",
                actionLabel = "Undo",
                action = {
                    viewModelScope.launch {
                        selectedIds.forEach { noteId ->
                            noteRepository.restoreNoteFromTrash(noteId)
                        }
                    }
                }
            )
        }
    }

    fun moveNoteToTrash(noteId: Long) {
        viewModelScope.launch {
            try {
                noteRepository.moveNoteToTrash(noteId)
                _snackbarMessage.value = SnackbarMessage(
                    message = "Note moved to trash",
                    actionLabel = "Undo",
                    action = {
                        viewModelScope.launch {
                            noteRepository.restoreFromTrash(noteId)
                        }
                    }
                )
            } catch (e: Exception) {
                _snackbarMessage.value = SnackbarMessage(
                    message = "Failed to move note to trash"
                )
            }
        }
    }

    fun clearSnackbarMessage() {
        _snackbarMessage.value = null
    }

    fun pinNote(noteId: Long) {
        viewModelScope.launch {
            noteRepository.toggleNotePin(noteId)
        }
    }

    fun moveNotesToFolder(noteIds: List<Long>, folderId: Long?) {
        viewModelScope.launch {
            try {
                noteIds.forEach { noteId ->
                    noteRepository.moveNoteToFolder(noteId, folderId)
                }
                _snackbarMessage.value = SnackbarMessage(
                    message = "Notes moved to folder"
                )
                clearSelection()
            } catch (e: Exception) {
                _snackbarMessage.value = SnackbarMessage(
                    message = "Failed to move notes to folder"
                )
            }
        }
    }
} 