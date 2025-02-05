package com.example.clicknote.domain.model

data class TranscriptionServiceContext(
    val isNetworkAvailable: Boolean = true,
    val isOnlineTranscriptionEnabled: Boolean = true,
    val preferOfflineMode: Boolean = false,
    val requireOnline: Boolean = false
) 