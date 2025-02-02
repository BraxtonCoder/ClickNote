package com.example.clicknote.domain.model

sealed class TranscriptionEvent {
    data class Started(val timestamp: Long) : TranscriptionEvent()
    data class Progress(val progress: Float) : TranscriptionEvent()
    data class SpeakerDetected(val speaker: String) : TranscriptionEvent()
    data class Transcribed(val text: String, val confidence: Float) : TranscriptionEvent()
    data class Error(val message: String) : TranscriptionEvent()
    data class Completed(val text: String, val duration: Long) : TranscriptionEvent()
    data class Cancelled(val reason: String? = null) : TranscriptionEvent()
    object Stopped : TranscriptionEvent()
    object Paused : TranscriptionEvent()
    object Resumed : TranscriptionEvent()
} 