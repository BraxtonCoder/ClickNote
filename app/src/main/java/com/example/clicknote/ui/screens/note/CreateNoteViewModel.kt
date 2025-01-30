package com.example.clicknote.ui.screens.note

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.clicknote.domain.model.Note
import com.example.clicknote.domain.repository.NoteRepository
import com.example.clicknote.service.AudioRecordingService
import com.example.clicknote.service.TranscriptionService
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import java.io.File
import javax.inject.Inject

@HiltViewModel
class CreateNoteViewModel @Inject constructor(
    private val noteRepository: NoteRepository,
    private val audioRecordingService: AudioRecordingService,
    private val transcriptionService: TranscriptionService
) : ViewModel() {

    private val _uiState = MutableStateFlow(NoteCreationState())
    val uiState: StateFlow<NoteCreationState> = _uiState.asStateFlow()

    init {
        viewModelScope.launch {
            // Collect audio amplitudes
            audioRecordingService.audioAmplitudes.collect { amplitudes ->
                _uiState.update { it.copy(audioAmplitudes = amplitudes) }
            }
        }

        viewModelScope.launch {
            // Collect transcription progress
            transcriptionService.transcriptionProgress.collect { progress ->
                _uiState.update { it.copy(transcriptionProgress = progress) }
            }
        }
    }

    fun updateTitle(title: String) {
        _uiState.update { it.copy(title = title) }
    }

    fun updateContent(content: String) {
        _uiState.update { it.copy(content = content) }
    }

    fun startRecording() {
        viewModelScope.launch {
            try {
                audioRecordingService.startRecording()
                _uiState.update { it.copy(
                    isRecording = true,
                    error = null
                ) }
            } catch (e: Exception) {
                _uiState.update { it.copy(
                    error = e.message ?: "Failed to start recording"
                ) }
            }
        }
    }

    fun stopRecording() {
        viewModelScope.launch {
            try {
                val audioFile = audioRecordingService.stopRecording()
                _uiState.update { it.copy(
                    isRecording = false,
                    isTranscribing = true,
                    error = null
                ) }
                
                // Start transcription
                transcribeAudio(audioFile)
            } catch (e: Exception) {
                _uiState.update { it.copy(
                    isRecording = false,
                    error = e.message ?: "Failed to stop recording"
                ) }
            }
        }
    }

    private suspend fun transcribeAudio(audioFile: File) {
        try {
            val transcription = transcriptionService.transcribe(audioFile)
            _uiState.update { it.copy(
                content = transcription,
                isTranscribing = false,
                hasAudioFile = true,
                error = null
            ) }
        } catch (e: Exception) {
            _uiState.update { it.copy(
                isTranscribing = false,
                error = e.message ?: "Failed to transcribe audio"
            ) }
        }
    }

    suspend fun saveNote(): Boolean {
        _uiState.update { it.copy(isSaving = true) }
        
        return try {
            val note = Note(
                title = uiState.value.title.ifEmpty { "Untitled Note" },
                content = uiState.value.content,
                hasAudio = uiState.value.hasAudioFile,
                createdAt = System.currentTimeMillis(),
                updatedAt = System.currentTimeMillis()
            )
            
            noteRepository.insertNote(note)
            _uiState.update { it.copy(isSaving = false, error = null) }
            true
        } catch (e: Exception) {
            _uiState.update { it.copy(
                isSaving = false,
                error = e.message ?: "Failed to save note"
            ) }
            false
        }
    }

    override fun onCleared() {
        super.onCleared()
        viewModelScope.launch {
            if (uiState.value.isRecording) {
                audioRecordingService.stopRecording()
            }
        }
    }
} 