package com.example.clicknote.domain.model

sealed class TranscriptionEvent {
    data class Progress(val progress: Float) : TranscriptionEvent()
    data class LanguageDetected(val language: String) : TranscriptionEvent()
    data class TranscriptionCompleted(val text: String) : TranscriptionEvent()
    data class Error(val error: Throwable) : TranscriptionEvent()
    object Started : TranscriptionEvent()
    object Cancelled : TranscriptionEvent()
    object Completed : TranscriptionEvent()
} 