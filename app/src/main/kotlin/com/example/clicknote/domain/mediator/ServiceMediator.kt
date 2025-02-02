package com.example.clicknote.domain.mediator

import com.example.clicknote.domain.model.TranscriptionServiceContext
import com.example.clicknote.domain.service.TranscriptionService

interface ServiceMediator {
    suspend fun initializeService(service: TranscriptionService, context: TranscriptionServiceContext)
    suspend fun releaseService(service: TranscriptionService)
    suspend fun releaseAll()
} 