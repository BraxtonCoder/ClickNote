package com.example.clicknote.domain.factory

import com.example.clicknote.domain.service.TranscriptionCapable

interface TranscriptionServiceFactory {
    fun createOnlineService(): TranscriptionCapable
    fun createOfflineService(): TranscriptionCapable
    fun createCombinedService(): TranscriptionCapable
    suspend fun releaseService(service: Any)
} 