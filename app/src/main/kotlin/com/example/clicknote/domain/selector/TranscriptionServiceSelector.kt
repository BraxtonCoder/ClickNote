package com.example.clicknote.domain.selector

import com.example.clicknote.domain.model.TranscriptionServiceContext
import com.example.clicknote.domain.service.TranscriptionCapable

interface TranscriptionServiceSelector {
    /**
     * Selects the appropriate transcription service based on the given context
     */
    fun selectService(context: TranscriptionServiceContext): TranscriptionCapable

    /**
     * Checks if a given service is available in the current context
     */
    fun isServiceAvailable(service: TranscriptionCapable, context: TranscriptionServiceContext): Boolean
} 