package com.example.clicknote.service

import kotlinx.coroutines.flow.Flow

interface TranscriptionEventHandler {
    fun onTranscriptionStarted()
    fun onTranscriptionCompleted(text: String)
    fun onTranscriptionError(error: Throwable)
    fun onTranscriptionProgress(progress: Float)
    fun onAudioProcessed(amplitude: Float)
    fun onSpeakersDetected(speakers: List<String>)
    fun getTranscriptionStateFlow(): Flow<TranscriptionState>
}

sealed class TranscriptionState {
    object Idle : TranscriptionState()
    object Processing : TranscriptionState()
    data class Success(val text: String) : TranscriptionState()
    data class Error(val error: Throwable) : TranscriptionState()
} 