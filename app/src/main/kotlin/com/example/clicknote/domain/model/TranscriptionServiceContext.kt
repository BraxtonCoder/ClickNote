package com.example.clicknote.domain.model

data class TranscriptionServiceContext(
    val requiresOnline: Boolean = false,
    val allowFallback: Boolean = true
) 