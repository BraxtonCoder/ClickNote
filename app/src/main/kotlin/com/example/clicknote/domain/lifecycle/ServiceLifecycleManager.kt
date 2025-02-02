package com.example.clicknote.domain.lifecycle

import com.example.clicknote.domain.model.TranscriptionServiceContext

interface ServiceLifecycleManager {
    suspend fun initializeService(context: TranscriptionServiceContext)
    suspend fun activateService(serviceId: String)
    suspend fun deactivateService(serviceId: String)
} 