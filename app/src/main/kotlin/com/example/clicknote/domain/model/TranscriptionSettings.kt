package com.example.clicknote.domain.model

data class TranscriptionSettings(
    val language: String = "en",
    val detectSpeakers: Boolean = false,
    val saveAudio: Boolean = true,
    val highQualityAudio: Boolean = false,
    val autoPunctuation: Boolean = true,
    val offlineMode: Boolean = false,
    val timestampParagraphs: Boolean = true,
    val maxDuration: Long = 3600000L // 1 hour in milliseconds
) {
    val shouldUseOnlineMode: Boolean
        get() = !offlineMode
} 