package com.example.clicknote.service.impl

import com.example.clicknote.service.TranscriptionStateManager
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class DefaultTranscriptionStateManager @Inject constructor() : TranscriptionStateManager {
    private val _isTranscribing = MutableStateFlow(false)
    override val isTranscribing: StateFlow<Boolean> = _isTranscribing

    private val _isOnline = MutableStateFlow(true)
    override val isOnline: StateFlow<Boolean> = _isOnline

    private val _transcriptionProgress = MutableStateFlow(0f)
    override val transcriptionProgress: StateFlow<Float> = _transcriptionProgress

    override fun setTranscribing(transcribing: Boolean) {
        _isTranscribing.value = transcribing
        if (!transcribing) {
            _transcriptionProgress.value = 0f
        }
    }

    override fun setOnline(online: Boolean) {
        _isOnline.value = online
    }

    override fun isOnline(): Boolean = _isOnline.value

    override fun isTranscribing(): Boolean = _isTranscribing.value

    override fun updateProgress(progress: Float) {
        _transcriptionProgress.value = progress.coerceIn(0f, 1f)
    }
} 