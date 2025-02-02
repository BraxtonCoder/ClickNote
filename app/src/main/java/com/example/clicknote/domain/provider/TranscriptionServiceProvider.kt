package com.example.clicknote.domain.provider

import com.example.clicknote.domain.model.TranscriptionServiceContext
import com.example.clicknote.domain.service.TranscriptionCapable

interface TranscriptionServiceProvider {
    fun getServiceForSettings(context: TranscriptionServiceContext): TranscriptionCapable
    fun getActiveService(): TranscriptionCapable?
    suspend fun cleanup()
} 