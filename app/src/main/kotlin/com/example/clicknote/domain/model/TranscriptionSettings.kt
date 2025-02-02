package com.example.clicknote.domain.model

import com.example.clicknote.domain.model.TranscriptionLanguage

data class TranscriptionSettings(
    val language: TranscriptionLanguage = TranscriptionLanguage.ENGLISH,
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