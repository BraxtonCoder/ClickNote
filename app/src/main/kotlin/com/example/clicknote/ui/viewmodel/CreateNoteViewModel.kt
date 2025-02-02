package com.example.clicknote.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.clicknote.domain.repository.NoteRepository
import com.example.clicknote.service.AudioRecorder
import com.example.clicknote.service.WhisperService
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import java.io.File
import java.time.LocalDateTime
import javax.inject.Inject

sealed class CreateNoteEvent {
    data object NavigateBack : CreateNoteEvent()
    data class ShowError(val message: String) : CreateNoteEvent()
    data class ShowSuccess(val message: String) : CreateNoteEvent()
}

sealed class TranscriptionState {
    data object Idle : TranscriptionState()
    data object Loading : TranscriptionState()
    data class Success(val text: String) : TranscriptionState()
    data class Error(val message: String) : TranscriptionState()
}

@HiltViewModel
class CreateNoteViewModel @Inject constructor(
    private val noteRepository: NoteRepository,
    private val audioRecorder: AudioRecorder,
    private val whisperService: WhisperService
) : ViewModel() {

    private val _isRecording = MutableStateFlow(false)
    val isRecording = _isRecording.asStateFlow()

    private val _audioAmplitudes = MutableStateFlow<List<Int>>(emptyList())
    val audioAmplitudes = _audioAmplitudes.asStateFlow()

    private val _transcriptionState = MutableStateFlow<TranscriptionState>(TranscriptionState.Idle)
    val transcriptionState = _transcriptionState.asStateFlow()

    private val _isSaving = MutableStateFlow(false)
    val isSaving = _isSaving.asStateFlow()

    private val _events = MutableSharedFlow<CreateNoteEvent>()
    val events = _events.asSharedFlow()

    private var recordingFile: File? = null

    init {
        viewModelScope.launch {
            audioRecorder.amplitudes.collect { amplitude ->
                _audioAmplitudes.update { amplitudes ->
                    (amplitudes + amplitude).takeLast(50)
                }
            }
        }
    }

    fun startRecording() {
        viewModelScope.launch {
            try {
                recordingFile = audioRecorder.startRecording()
                _isRecording.value = true
            } catch (e: Exception) {
                _events.emit(CreateNoteEvent.ShowError("Failed to start recording: ${e.message}"))
            }
        }
    }

    fun stopRecording() {
        viewModelScope.launch {
            try {
                audioRecorder.stopRecording()
                _isRecording.value = false
                
                recordingFile?.let { file ->
                    _transcriptionState.value = TranscriptionState.Loading
                    try {
                        val result = whisperService.transcribe(file)
                        result.onSuccess { text ->
                            _transcriptionState.value = TranscriptionState.Success(text)
                        }.onFailure { error ->
                            _transcriptionState.value = TranscriptionState.Error("Transcription failed: ${error.message}")
                            _events.emit(CreateNoteEvent.ShowError("Failed to transcribe audio: ${error.message}"))
                        }
                    } catch (e: Exception) {
                        _transcriptionState.value = TranscriptionState.Error("Transcription failed: ${e.message}")
                        _events.emit(CreateNoteEvent.ShowError("Failed to transcribe audio: ${e.message}"))
                    }
                }
            } catch (e: Exception) {
                _events.emit(CreateNoteEvent.ShowError("Failed to stop recording: ${e.message}"))
            }
        }
    }

    fun saveNote(title: String, content: String) {
        viewModelScope.launch {
            _isSaving.value = true
            try {
                noteRepository.createNote(
                    title = title.ifBlank { null },
                    content = content,
                    audioFile = recordingFile,
                    createdAt = LocalDateTime.now()
                )
                _events.emit(CreateNoteEvent.ShowSuccess("Note saved successfully"))
                _events.emit(CreateNoteEvent.NavigateBack)
            } catch (e: Exception) {
                _events.emit(CreateNoteEvent.ShowError("Failed to save note: ${e.message}"))
            } finally {
                _isSaving.value = false
            }
        }
    }

    override fun onCleared() {
        super.onCleared()
        viewModelScope.launch {
            if (isRecording.value) {
                audioRecorder.stopRecording()
            }
        }
    }
} 