package com.example.clicknote.domain.model

data class TranscriptionSettings(
    val preferOfflineMode: Boolean,
    val isNetworkAvailable: Boolean,
    val selectedLanguage: String = "en",
    val enhanceAudio: Boolean = true,
    val detectSpeakers: Boolean = true
) {
    val shouldUseOnlineMode: Boolean
        get() = !preferOfflineMode && isNetworkAvailable
} 