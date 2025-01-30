package com.example.clicknote.ui.bin

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.clicknote.domain.model.Note
import com.example.clicknote.domain.repository.NoteRepository
import com.example.clicknote.service.ClipboardService
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import javax.inject.Inject

data class RecycleBinUiState(
    val notes: List<Note> = emptyList(),
    val isLoading: Boolean = false,
    val error: String? = null
)

@HiltViewModel
class RecycleBinViewModel @Inject constructor(
    private val noteRepository: NoteRepository,
    private val clipboardService: ClipboardService
) : ViewModel() {

    private val _uiState = MutableStateFlow(RecycleBinUiState())
    val uiState: StateFlow<RecycleBinUiState> = _uiState.asStateFlow()

    init {
        loadDeletedNotes()
    }

    private fun loadDeletedNotes() {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true) }
            try {
                noteRepository.getNotesInTrash()
                    .collect { notes ->
                        _uiState.update {
                            it.copy(
                                notes = notes,
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

    fun copyNote(note: Note) {
        viewModelScope.launch {
            clipboardService.copyToClipboard(note.content)
        }
    }

    fun restoreNotes(noteIds: List<String>) {
        viewModelScope.launch {
            try {
                noteRepository.restoreFromTrash(noteIds).getOrThrow()
            } catch (e: Exception) {
                _uiState.update {
                    it.copy(error = e.message)
                }
            }
        }
    }

    fun deleteNotesPermanently(noteIds: List<String>) {
        viewModelScope.launch {
            try {
                noteRepository.permanentlyDelete(noteIds).getOrThrow()
            } catch (e: Exception) {
                _uiState.update {
                    it.copy(error = e.message)
                }
            }
        }
    }

    fun clearRecycleBin() {
        viewModelScope.launch {
            try {
                noteRepository.clearRecycleBin()
            } catch (e: Exception) {
                _uiState.update {
                    it.copy(error = e.message)
                }
            }
        }
    }

    fun deleteOldNotes() {
        viewModelScope.launch {
            try {
                val thirtyDaysAgo = java.time.LocalDateTime.now().minusDays(30)
                noteRepository.deleteNotesOlderThan(thirtyDaysAgo)
            } catch (e: Exception) {
                _uiState.update {
                    it.copy(error = "Failed to delete old notes: ${e.message}")
                }
            }
        }
    }
} 