package com.example.clicknote.ui.screens.note

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.clicknote.domain.model.Note
import com.example.clicknote.domain.repository.NoteRepository
import com.example.clicknote.service.AudioPlayerService
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class NoteDetailViewModel @Inject constructor(
    private val noteRepository: NoteRepository,
    private val audioPlayer: AudioPlayerService
) : ViewModel() {

    private val _uiState = MutableStateFlow(NoteDetailUiState())
    val uiState = _uiState.asStateFlow()

    private var currentNote: Note? = null

    init {
        viewModelScope.launch {
            audioPlayer.progress.collect { progress ->
                _uiState.update { it.copy(audioProgress = progress) }
            }
        }
    }

    fun loadNote(noteId: String) {
        viewModelScope.launch {
            noteRepository.getNoteById(noteId).collect { note ->
                currentNote = note
                _uiState.update { state ->
                    state.copy(note = note)
                }
                if (note?.hasAudio == true) {
                    audioPlayer.prepare(note.audioPath!!)
                }
            }
        }
    }

    fun setSearchQuery(query: String) {
        _uiState.update { it.copy(searchQuery = query) }
    }

    fun setShowingSummary(showing: Boolean) {
        _uiState.update { it.copy(showingSummary = showing) }
    }

    fun toggleEditing() {
        _uiState.update { it.copy(isEditing = !it.isEditing) }
    }

    fun updateContent(content: String) {
        viewModelScope.launch {
            currentNote?.let { note ->
                noteRepository.updateNote(note.copy(content = content))
            }
        }
    }

    fun togglePin() {
        viewModelScope.launch {
            currentNote?.let { note ->
                noteRepository.togglePin(note.id)
            }
        }
    }

    fun moveToTrash() {
        viewModelScope.launch {
            currentNote?.let { note ->
                noteRepository.moveToTrash(note.id)
            }
        }
    }

    fun toggleAudioPlayback() {
        if (uiState.value.isPlaying) {
            audioPlayer.pause()
        } else {
            audioPlayer.play()
        }
        _uiState.update { it.copy(isPlaying = !it.isPlaying) }
    }

    fun seekTo(position: Float) {
        audioPlayer.seekTo(position)
    }

    override fun onCleared() {
        super.onCleared()
        audioPlayer.release()
    }
}

data class NoteDetailUiState(
    val note: Note? = null,
    val searchQuery: String = "",
    val showingSummary: Boolean = false,
    val isEditing: Boolean = false,
    val isPlaying: Boolean = false,
    val audioProgress: Float = 0f
) 