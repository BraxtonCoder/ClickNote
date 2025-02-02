package com.example.clicknote.domain.selector

import com.example.clicknote.domain.model.TranscriptionServiceContext
import com.example.clicknote.domain.service.TranscriptionCapable

interface TranscriptionServiceSelector {
    fun selectService(context: TranscriptionServiceContext): TranscriptionCapable
    fun getOnlineService(): TranscriptionCapable
    fun getOfflineService(): TranscriptionCapable
} 