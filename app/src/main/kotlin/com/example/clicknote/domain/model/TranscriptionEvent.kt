package com.example.clicknote.domain.model

sealed class TranscriptionEvent {
    data class Started(val timestamp: Long = System.currentTimeMillis()) : TranscriptionEvent()
    data class Stopped(val timestamp: Long = System.currentTimeMillis()) : TranscriptionEvent()
    data class TextUpdated(val text: String, val timestamp: Long = System.currentTimeMillis()) : TranscriptionEvent()
    data class LanguageDetected(val language: String, val timestamp: Long = System.currentTimeMillis()) : TranscriptionEvent()
    data class SpeakersDetected(val count: Int, val timestamp: Long = System.currentTimeMillis()) : TranscriptionEvent()
    data class Error(val message: String, val timestamp: Long = System.currentTimeMillis()) : TranscriptionEvent()
    data class Progress(val percent: Int, val timestamp: Long = System.currentTimeMillis()) : TranscriptionEvent()
    data class StatusChanged(
        val status: com.example.clicknote.domain.service.TranscriptionCapable.TranscriptionStatus,
        val timestamp: Long = System.currentTimeMillis()
    ) : TranscriptionEvent()
} 