package com.example.clicknote.service.impl

import com.example.clicknote.domain.interfaces.TranscriptionStateManager
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import java.io.File
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class TranscriptionStateManagerImpl @Inject constructor() : TranscriptionStateManager {
    private val _isTranscribing = MutableStateFlow(false)
    private val _transcriptionProgress = MutableStateFlow(0f)
    private val _currentSpeaker = MutableStateFlow<String?>(null)
    private val _currentFile = MutableStateFlow<File?>(null)

    override val isTranscribing: StateFlow<Boolean> = _isTranscribing.asStateFlow()
    override val transcriptionProgress: StateFlow<Float> = _transcriptionProgress.asStateFlow()
    override val currentSpeaker: StateFlow<String?> = _currentSpeaker.asStateFlow()
    override val currentFile: StateFlow<File?> = _currentFile.asStateFlow()

    override fun setTranscribing(isTranscribing: Boolean) {
        _isTranscribing.value = isTranscribing
        if (!isTranscribing) {
            _transcriptionProgress.value = 0f
            _currentSpeaker.value = null
        }
    }

    override fun setCurrentFile(file: File?) {
        _currentFile.value = file
    }

    override fun updateProgress(progress: Float) {
        _transcriptionProgress.value = progress
    }

    override fun updateSpeaker(speaker: String?) {
        _currentSpeaker.value = speaker
    }

    override fun cleanup() {
        _isTranscribing.value = false
        _transcriptionProgress.value = 0f
        _currentSpeaker.value = null
        _currentFile.value = null
    }
} 