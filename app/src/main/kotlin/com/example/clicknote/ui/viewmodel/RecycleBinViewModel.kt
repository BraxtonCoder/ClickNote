package com.example.clicknote.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.clicknote.domain.model.Note
import com.example.clicknote.domain.repository.NoteRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class RecycleBinViewModel @Inject constructor(
    private val noteRepository: NoteRepository
) : ViewModel() {

    private val _isLoading = MutableStateFlow(false)
    val isLoading = _isLoading.asStateFlow()

    val deletedNotes = noteRepository.getDeletedNotes()
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )

    private val _selectedNotes = MutableStateFlow<Set<Long>>(emptySet())
    val selectedNotes = _selectedNotes.asStateFlow()

    private val _snackbarMessage = MutableStateFlow<SnackbarMessage?>(null)
    val snackbarMessage = _snackbarMessage.asStateFlow()

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

    fun restoreSelectedNotes() {
        viewModelScope.launch {
            try {
                _isLoading.value = true
                selectedNotes.value.forEach { noteId ->
                    noteRepository.restoreFromTrash(noteId)
                }
                _snackbarMessage.value = SnackbarMessage(
                    message = "Notes restored successfully"
                )
                clearSelection()
            } catch (e: Exception) {
                _snackbarMessage.value = SnackbarMessage(
                    message = "Failed to restore notes: ${e.message}"
                )
            } finally {
                _isLoading.value = false
            }
        }
    }

    fun deleteSelectedNotesPermanently() {
        viewModelScope.launch {
            try {
                _isLoading.value = true
                selectedNotes.value.forEach { noteId ->
                    noteRepository.deleteNotePermanently(noteId)
                }
                _snackbarMessage.value = SnackbarMessage(
                    message = "Notes deleted permanently"
                )
                clearSelection()
            } catch (e: Exception) {
                _snackbarMessage.value = SnackbarMessage(
                    message = "Failed to delete notes: ${e.message}"
                )
            } finally {
                _isLoading.value = false
            }
        }
    }

    fun deleteNotePermanently(noteId: Long) {
        viewModelScope.launch {
            try {
                _isLoading.value = true
                noteRepository.deleteNotePermanently(noteId)
                _snackbarMessage.value = SnackbarMessage(
                    message = "Note deleted permanently"
                )
            } catch (e: Exception) {
                _snackbarMessage.value = SnackbarMessage(
                    message = "Failed to delete note: ${e.message}"
                )
            } finally {
                _isLoading.value = false
            }
        }
    }

    fun clearSnackbarMessage() {
        _snackbarMessage.value = null
    }
} 