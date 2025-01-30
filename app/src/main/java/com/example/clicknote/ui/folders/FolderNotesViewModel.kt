package com.example.clicknote.ui.folders

import androidx.compose.ui.graphics.Color
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.clicknote.domain.model.Note
import com.example.clicknote.domain.model.NoteWithFolder
import com.example.clicknote.domain.model.Folder
import com.example.clicknote.domain.model.TimeFilter
import com.example.clicknote.domain.repository.NoteRepository
import com.example.clicknote.domain.repository.FolderRepository
import com.example.clicknote.service.ClipboardService
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import javax.inject.Inject
import java.time.LocalDateTime
import java.time.LocalDate
import androidx.lifecycle.SavedStateHandle

data class FolderNotesUiState(
    val folder: Folder? = null,
    val notes: List<NoteWithFolder> = emptyList(),
    val searchQuery: String = "",
    val timeFilter: TimeFilter = TimeFilter.ALL,
    val isLoading: Boolean = false,
    val error: String? = null,
    val selectedNotes: Set<String> = emptySet(),
    val isMultiSelectMode: Boolean = false
)

@HiltViewModel
class FolderNotesViewModel @Inject constructor(
    private val noteRepository: NoteRepository,
    private val folderRepository: FolderRepository,
    private val clipboardService: ClipboardService,
    savedStateHandle: SavedStateHandle
) : ViewModel() {
    
    private val _uiState = MutableStateFlow(FolderNotesUiState())
    val uiState: StateFlow<FolderNotesUiState> = _uiState.asStateFlow()
    
    private val folderId: String = savedStateHandle.get<String>(FOLDER_ID_KEY)
        ?: throw IllegalArgumentException("Folder ID is required")

    init {
        loadFolder()
        loadNotes()
    }

    private fun loadFolder() {
        viewModelScope.launch {
            folderRepository.getFolderById(folderId).collect { folder ->
                _uiState.update { it.copy(folder = folder) }
            }
        }
    }

    private fun loadNotes() {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true) }
            try {
                noteRepository.getNotesInFolder(folderId)
                    .collect { notes ->
                        _uiState.update {
                            it.copy(
                                notes = notes.sortedByDescending { note -> note.timestamp },
                                isLoading = false,
                                error = null
                            )
                        }
                    }
            } catch (e: Exception) {
                _uiState.update {
                    it.copy(
                        isLoading = false,
                        error = e.message
                    )
                }
            }
        }
    }

    fun loadNotes(folderId: String) {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true) }
            
            try {
                // Load folder details
                folderRepository.getFolderById(folderId).collect { folder ->
                    _uiState.update { state -> state.copy(folder = folder) }
                }

                // Load notes in folder
                noteRepository.getNotesByFolder(folderId)
                    .map { notes -> notes.map { note -> NoteWithFolder(note, uiState.value.folder) } }
                    .map { notes -> filterNotes(notes) }
                    .collect { filteredNotes ->
                        _uiState.update { state -> 
                            state.copy(
                                notes = filteredNotes,
                                isLoading = false,
                                error = null
                            )
                        }
                    }
            } catch (e: Exception) {
                _uiState.update { state -> 
                    state.copy(
                        isLoading = false,
                        error = e.message
                    )
                }
            }
        }
    }
    
    fun searchNotes(query: String) {
        viewModelScope.launch {
            try {
                val folderId = _uiState.value.folder?.id ?: return@launch
                noteRepository.searchNotes(query)
                    .map { notes -> notes.filter { it.folderId == folderId } }
                    .map { notes -> notes.map { note -> NoteWithFolder(note, uiState.value.folder) } }
                    .map { notes -> filterNotes(notes) }
                    .collect { filteredNotes ->
                        _uiState.update { state -> 
                            state.copy(
                                notes = filteredNotes,
                                searchQuery = query
                            )
                        }
                    }
            } catch (e: Exception) {
                _uiState.update { state -> state.copy(error = e.message) }
            }
        }
    }

    fun updateSearchQuery(query: String) {
        _uiState.update { it.copy(searchQuery = query) }
    }

    fun clearSearch() {
        _uiState.update { it.copy(searchQuery = "") }
        loadNotes(folderId)
    }

    fun updateTimeFilter(filter: TimeFilter) {
        _uiState.update { it.copy(timeFilter = filter) }
        loadNotes(folderId)
    }

    private fun filterNotes(notes: List<NoteWithFolder>): List<NoteWithFolder> {
        if (_uiState.value.timeFilter == TimeFilter.ALL) return notes

        val now = LocalDateTime.now()
        return notes.filter { noteWithFolder ->
            val createdAt = noteWithFolder.note.timestamp
            when (_uiState.value.timeFilter) {
                TimeFilter.TODAY -> createdAt.toLocalDate() == LocalDate.now()
                TimeFilter.LAST_7_DAYS -> createdAt.isAfter(now.minusDays(7))
                TimeFilter.LAST_30_DAYS -> createdAt.isAfter(now.minusDays(30))
                TimeFilter.LAST_3_MONTHS -> createdAt.isAfter(now.minusMonths(3))
                TimeFilter.LAST_6_MONTHS -> createdAt.isAfter(now.minusMonths(6))
                TimeFilter.LAST_YEAR -> createdAt.isAfter(now.minusYears(1))
                TimeFilter.CUSTOM -> true // Handle custom date range separately
                TimeFilter.ALL -> true
            }
        }
    }
    
    fun copySelectedNotes() {
        viewModelScope.launch {
            val selectedNotes = _uiState.value.notes.filter { note ->
                _uiState.value.selectedNotes.contains(note.id)
            }
            val combinedText = selectedNotes.joinToString("\n\n") { it.note.content }
            clipboardService.copyToClipboard(combinedText)
            clearSelection()
        }
    }

    fun toggleNoteSelection(noteId: String) {
        _uiState.update { state ->
            val selectedNotes = state.selectedNotes.toMutableSet()
            if (selectedNotes.contains(noteId)) {
                selectedNotes.remove(noteId)
            } else {
                selectedNotes.add(noteId)
            }
            state.copy(
                selectedNotes = selectedNotes,
                isMultiSelectMode = selectedNotes.isNotEmpty()
            )
        }
    }

    fun clearSelection() {
        _uiState.update { it.copy(selectedNotes = emptySet(), isMultiSelectMode = false) }
    }

    fun moveSelectedNotesToTrash() {
        viewModelScope.launch {
            try {
                val selectedNoteIds = _uiState.value.selectedNotes.toList()
                noteRepository.moveToTrash(selectedNoteIds)
                clearSelection()
            } catch (e: Exception) {
                _uiState.update { it.copy(error = e.message) }
            }
        }
    }

    fun toggleNotePin(noteId: String) {
        viewModelScope.launch {
            try {
                val note = _uiState.value.notes.find { it.note.id == noteId }
                note?.let {
                    noteRepository.updateNote(it.note.copy(isPinned = !it.note.isPinned))
                }
            } catch (e: Exception) {
                _uiState.update { it.copy(error = e.message) }
            }
        }
    }

    companion object {
        private const val FOLDER_ID_KEY = "folderId"
    }
} 