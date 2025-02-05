package com.example.clicknote.domain.model

data class TranscriptionSettings(
    val noteId: String,
    val language: String = "en",
    val model: String = "base",
    val enableSpeakerDetection: Boolean = false,
    val enableTimestamps: Boolean = true,
    val enablePunctuation: Boolean = true
)

enum class AudioQuality {
    LOW,    // 8kHz, mono
    MEDIUM, // 16kHz, mono
    HIGH    // 44.1kHz, stereo
} 