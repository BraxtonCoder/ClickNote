package com.example.clicknote.domain.model

sealed class TranscriptionState {
    data object Idle : TranscriptionState()
    data object Recording : TranscriptionState()
    data object Paused : TranscriptionState()
    data class Processing(val progress: Float) : TranscriptionState()
    data class Completed(val text: String, val duration: Long) : TranscriptionState()
    data class Error(val error: Throwable) : TranscriptionState()
    data class Cancelled(val reason: String? = null) : TranscriptionState()
} 