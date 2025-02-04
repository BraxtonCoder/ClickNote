package com.example.clicknote.ui.notes

import android.content.Context
import android.os.VibrationEffect
import android.os.Vibrator
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.clicknote.domain.model.Note
import com.example.clicknote.domain.model.NoteWithFolder
import com.example.clicknote.domain.model.Folder
import com.example.clicknote.domain.model.TimeFilter
import com.example.clicknote.domain.repository.NoteRepository
import com.example.clicknote.domain.repository.FolderRepository
import com.example.clicknote.domain.repository.UserRepository
import com.example.clicknote.service.ClipboardService
import com.example.clicknote.service.RecordingService
import com.example.clicknote.service.ClipboardManager
import android.content.ClipData
import android.widget.Toast
import java.time.LocalDateTime
import java.time.temporal.ChronoUnit
import javax.inject.Inject
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import java.util.UUID
import java.time.format.DateTimeFormatter
import java.time.format.FormatStyle
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import com.example.clicknote.service.AnalyticsService
import com.example.clicknote.sync.SyncState

data class NotesUiState(
    val notes: List<Note> = emptyList(),
    val folders: List<Folder> = emptyList(),
    val selectedNotes: Set<String> = emptySet(),
    val isMultiSelectMode: Boolean = false,
    val searchQuery: String = "",
    val timeFilter: TimeFilter = TimeFilter.ALL,
    val isLoading: Boolean = true,
    val error: String? = null,
    val isRecording: Boolean = false,
    val syncState: SyncState = SyncState.Idle
)

@HiltViewModel
class NotesViewModel @Inject constructor(
    private val noteRepository: NoteRepository,
    private val folderRepository: FolderRepository,
    private val userRepository: UserRepository,
    private val clipboardManager: ClipboardManager,
    private val clipboardService: ClipboardService,
    private val recordingService: RecordingService,
    private val analyticsService: AnalyticsService,
    @ApplicationContext private val context: Context
) : ViewModel() {

    private val _uiState = MutableStateFlow(NotesUiState())
    val uiState: StateFlow<NotesUiState> = _uiState.asStateFlow()

    private val _selectedNotes = MutableStateFlow<Set<String>>(emptySet())
    
    init {
        loadNotes()
        loadFolders()
        observeRecordingState()
        viewModelScope.launch {
            // Collect notes
            noteRepository.notes.collect { notes ->
                _uiState.update { it.copy(notes = notes) }
            }
        }

        viewModelScope.launch {
            // Collect sync state
            noteRepository.syncState.collect { syncState ->
                _uiState.update { it.copy(syncState = syncState) }
            }
        }

        // Start auto-sync
        noteRepository.startAutoSync()
    }

    private fun loadNotes() {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true) }
            try {
                noteRepository.getAllNotes()
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
                _uiState.update { it.copy(
                    isLoading = false,
                    error = e.message ?: "Failed to load notes"
                ) }
                analyticsService.trackError(e.message ?: "Failed to load notes", "NotesScreen")
            }
        }
    }

    private fun loadFolders() {
        viewModelScope.launch {
            try {
                folderRepository.getAllFolders()
                    .collect { folders ->
                        _uiState.update { it.copy(folders = folders) }
                    }
            } catch (e: Exception) {
                _uiState.update { it.copy(error = e.message) }
                analyticsService.trackError(e.message ?: "Failed to load folders", "NotesScreen")
            }
        }
    }

    private fun observeRecordingState() {
        viewModelScope.launch {
            recordingService.isRecording
                .collect { isRecording ->
                    _uiState.update { it.copy(isRecording = isRecording) }
                }
        }
    }

    fun startRecording() {
        viewModelScope.launch {
            if (uiState.value.isRecording) {
                recordingService.stopRecording()
            } else {
                recordingService.startRecording()
            }
        }
    }

    fun updateSearchQuery(query: String) {
        _uiState.update { it.copy(searchQuery = query) }
        searchNotes(query)
    }

    fun searchNotes(query: String) {
        viewModelScope.launch {
            try {
                noteRepository.searchNotes(query)
                    .map { notes -> filterNotes(notes) }
                    .collect { filteredNotes ->
                        _uiState.update { it.copy(notes = filteredNotes) }
                        analyticsService.trackSearch(query, filteredNotes.size)
                    }
            } catch (e: Exception) {
                _uiState.update { it.copy(error = e.message) }
                analyticsService.trackError(e.message ?: "Failed to search notes", "NotesScreen")
            }
        }
    }

    fun clearSearch() {
        _uiState.update { it.copy(searchQuery = "") }
        loadNotes()
    }

    fun updateTimeFilter(filter: TimeFilter) {
        _uiState.update { it.copy(timeFilter = filter) }
        loadNotes()
    }

    private fun filterNotes(notes: List<Note>): List<Note> {
        if (_uiState.value.timeFilter == TimeFilter.ALL) {
            return notes.sortedByDescending { it.timestamp }
        }

        val now = LocalDateTime.now()
        return notes.filter { note ->
            when (_uiState.value.timeFilter) {
                TimeFilter.TODAY -> note.timestamp.toLocalDate() == now.toLocalDate()
                TimeFilter.LAST_7_DAYS -> note.timestamp.isAfter(now.minusDays(7))
                TimeFilter.LAST_30_DAYS -> note.timestamp.isAfter(now.minusDays(30))
                TimeFilter.LAST_3_MONTHS -> note.timestamp.isAfter(now.minusMonths(3))
                TimeFilter.LAST_6_MONTHS -> note.timestamp.isAfter(now.minusMonths(6))
                TimeFilter.LAST_YEAR -> note.timestamp.isAfter(now.minusYears(1))
                TimeFilter.CUSTOM -> true
                TimeFilter.ALL -> true
            }
        }.sortedByDescending { it.timestamp }
    }

    fun toggleNoteSelection(noteId: String) {
        _selectedNotes.update { currentSelection ->
            if (noteId in currentSelection) {
                currentSelection - noteId
            } else {
                currentSelection + noteId
            }
        }
    }

    fun clearSelection() {
        _selectedNotes.value = emptySet()
    }

    fun copySelectedNotes() {
        viewModelScope.launch {
            val selectedNotes = uiState.value.notes.filter { it.id in _selectedNotes.value }
            val noteTexts = selectedNotes.joinToString("\n\n") { note ->
                buildString {
                    appendLine(note.title)
                    appendLine(note.content)
                    appendLine("Created: ${note.createdAt.format(DateTimeFormatter.ofLocalizedDateTime(FormatStyle.MEDIUM))}")
                }
            }
            
            val clip = ClipData.newPlainText("Notes", noteTexts)
            clipboardManager.setPrimaryClip(clip)
            
            // Show toast confirmation
            Toast.makeText(context, "${selectedNotes.size} notes copied", Toast.LENGTH_SHORT).show()
            
            clearSelection()
        }
    }

    fun moveSelectedNotesToTrash() {
        viewModelScope.launch {
            _selectedNotes.value.forEach { noteId ->
                moveNoteToTrash(noteId)
            }
            clearSelection()
        }
    }

    fun toggleNotePin(noteId: String) {
        viewModelScope.launch {
            try {
                val note = uiState.value.notes.find { it.id == noteId }
                note?.let {
                    noteRepository.updateNote(it.copy(
                        isPinned = !it.isPinned,
                        timestamp = LocalDateTime.now()
                    ))
                }
            } catch (e: Exception) {
                _uiState.update { it.copy(error = e.message) }
            }
        }
    }

    fun moveNoteToTrash(noteId: String) {
        viewModelScope.launch {
            try {
                noteRepository.moveToTrash(listOf(noteId))
            } catch (e: Exception) {
                _uiState.update { it.copy(error = e.message) }
            }
        }
    }

    fun moveToFolder(noteId: String, folderId: String?) {
        viewModelScope.launch {
            try {
                noteRepository.updateNoteFolder(noteId, folderId)
            } catch (e: Exception) {
                _uiState.update { it.copy(error = "Failed to move note to folder: ${e.message}") }
            }
        }
    }

    fun copyNoteToClipboard(note: Note) {
        clipboardService.copyToClipboard(note.content)
    }

    fun createFolder(name: String, color: Color) {
        viewModelScope.launch {
            try {
                folderRepository.createFolder(name, color.toArgb())
            } catch (e: Exception) {
                _uiState.update { it.copy(error = e.message) }
            }
        }
    }

    fun renameFolder(folderId: String, newName: String) {
        viewModelScope.launch {
            try {
                folderRepository.renameFolder(folderId, newName)
            } catch (e: Exception) {
                _uiState.update { it.copy(error = e.message) }
            }
        }
    }

    fun deleteFolder(folderId: String) {
        viewModelScope.launch {
            try {
                folderRepository.deleteFolder(folderId)
            } catch (e: Exception) {
                _uiState.update { it.copy(error = e.message) }
            }
        }
    }

    fun createNote(): String {
        return UUID.randomUUID().toString()
    }

    fun deleteNote(note: Note) {
        viewModelScope.launch {
            try {
                noteRepository.softDeleteNote(note)
                analyticsService.trackNoteDeleted(note)
            } catch (e: Exception) {
                _uiState.update { it.copy(error = e.message) }
                analyticsService.trackError(e.message ?: "Failed to delete note", "NotesScreen")
            }
        }
    }

    fun restoreNote(note: Note) {
        viewModelScope.launch {
            try {
                noteRepository.restoreNote(note)
                analyticsService.trackNoteRestored(note)
            } catch (e: Exception) {
                _uiState.update { it.copy(error = e.message) }
                analyticsService.trackError(e.message ?: "Failed to restore note", "NotesScreen")
            }
        }
    }

    fun syncNotes() {
        viewModelScope.launch {
            try {
                noteRepository.syncNotes()
            } catch (e: Exception) {
                _uiState.update { it.copy(error = e.message) }
            }
        }
    }

    fun dismissError() {
        _uiState.update { it.copy(error = null) }
    }

    fun insertNote(note: Note) {
        viewModelScope.launch {
            try {
                noteRepository.insertNote(note)
            } catch (e: Exception) {
                _uiState.update { it.copy(error = e.message) }
            }
        }
    }

    fun updateNote(note: Note) {
        viewModelScope.launch {
            try {
                noteRepository.updateNote(note)
            } catch (e: Exception) {
                _uiState.update { it.copy(error = e.message) }
            }
        }
    }

    fun deleteNote(id: String) {
        viewModelScope.launch {
            try {
                noteRepository.deleteNote(id)
            } catch (e: Exception) {
                _uiState.update { it.copy(error = e.message) }
            }
        }
    }

    fun restoreNote(id: String) {
        viewModelScope.launch {
            try {
                noteRepository.restoreNote(id)
            } catch (e: Exception) {
                _uiState.update { it.copy(error = e.message) }
            }
        }
    }
} 