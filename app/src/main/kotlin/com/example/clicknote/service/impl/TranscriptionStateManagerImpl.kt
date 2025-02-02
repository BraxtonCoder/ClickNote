package com.example.clicknote.service.impl

import com.example.clicknote.domain.interfaces.TranscriptionStateManager
import com.example.clicknote.domain.model.TranscriptionState
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import java.io.File
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class TranscriptionStateManagerImpl @Inject constructor() : TranscriptionStateManager {
    private val _currentState = MutableStateFlow<TranscriptionState>(TranscriptionState.Idle)
    override val currentState: Flow<TranscriptionState> = _currentState.asStateFlow()

    private val _isRecording = MutableStateFlow(false)
    override val isRecording: Flow<Boolean> = _isRecording.asStateFlow()

    private val _amplitude = MutableStateFlow(0f)
    override val amplitude: Flow<Float> = _amplitude.asStateFlow()

    private val _speakers = MutableStateFlow<List<String>>(emptyList())
    override val speakers: Flow<List<String>> = _speakers.asStateFlow()

    private val _isTranscribing = MutableStateFlow(false)
    override val isTranscribing: Flow<Boolean> = _isTranscribing.asStateFlow()

    private val _currentFile = MutableStateFlow<File?>(null)
    override val currentFile: Flow<File?> = _currentFile.asStateFlow()

    private val _isOfflineMode = MutableStateFlow(false)
    override val isOfflineMode: Flow<Boolean> = _isOfflineMode.asStateFlow()

    override suspend fun startRecording() {
        _isRecording.value = true
        _currentState.value = TranscriptionState.Recording
    }

    override suspend fun stopRecording() {
        _isRecording.value = false
        _currentState.value = TranscriptionState.Completed("", 0L)
    }

    override suspend fun pauseRecording() {
        _isRecording.value = false
        _currentState.value = TranscriptionState.Paused
    }

    override suspend fun resumeRecording() {
        _isRecording.value = true
        _currentState.value = TranscriptionState.Recording
    }

    override suspend fun cancelRecording() {
        _isRecording.value = false
        _currentState.value = TranscriptionState.Cancelled()
        reset()
    }

    override suspend fun updateAmplitude(value: Float) {
        _amplitude.value = value
    }

    override suspend fun updateSpeakers(speakers: List<String>) {
        _speakers.value = speakers
    }

    override suspend fun setTranscribing(isTranscribing: Boolean) {
        _isTranscribing.value = isTranscribing
        if (isTranscribing) {
            _currentState.value = TranscriptionState.Processing(0f)
        }
    }

    override suspend fun setCurrentFile(file: File?) {
        _currentFile.value = file
    }

    override suspend fun reset() {
        _isRecording.value = false
        _amplitude.value = 0f
        _speakers.value = emptyList()
        _isTranscribing.value = false
        _currentFile.value = null
        _currentState.value = TranscriptionState.Idle
    }

    override suspend fun setOfflineMode(isOffline: Boolean) {
        _isOfflineMode.value = isOffline
    }
} 