package com.example.clicknote.domain.transcription

import com.example.clicknote.domain.model.TranscriptionSettings

data class TranscriptionServiceContext(
    val settings: TranscriptionSettings,
    val isOnlineAvailable: Boolean = true,
    val requiresOnline: Boolean = false,
    val allowFallback: Boolean = true
) {
    val shouldUseOnline: Boolean
        get() = !settings.preferOfflineMode && 
                settings.isNetworkAvailable && 
                (isOnlineAvailable || requiresOnline)
} 