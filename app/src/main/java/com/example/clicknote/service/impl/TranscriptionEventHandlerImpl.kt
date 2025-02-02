package com.example.clicknote.service.impl

import com.example.clicknote.service.TranscriptionEventHandler
import com.example.clicknote.service.TranscriptionState
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asSharedFlow
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class TranscriptionEventHandlerImpl @Inject constructor() : TranscriptionEventHandler {
    private val _transcriptionState = MutableSharedFlow<TranscriptionState>(replay = 1).apply {
        tryEmit(TranscriptionState.Idle)
    }
    private val _transcriptionProgress = MutableStateFlow(0f)
    private val _amplitude = MutableStateFlow(0f)
    private val _speakers = MutableStateFlow<List<String>>(emptyList())

    override fun onTranscriptionStarted() {
        _transcriptionState.tryEmit(TranscriptionState.Processing)
    }

    override fun onTranscriptionCompleted(text: String) {
        _transcriptionState.tryEmit(TranscriptionState.Success(text))
    }

    override fun onTranscriptionError(error: Throwable) {
        _transcriptionState.tryEmit(TranscriptionState.Error(error))
    }

    override fun onTranscriptionProgress(progress: Float) {
        _transcriptionProgress.value = progress
    }

    override fun onAudioProcessed(amplitude: Float) {
        _amplitude.value = amplitude
    }

    override fun onSpeakersDetected(speakers: List<String>) {
        _speakers.value = speakers
    }

    override fun getTranscriptionStateFlow(): Flow<TranscriptionState> = _transcriptionState.asSharedFlow()
} 