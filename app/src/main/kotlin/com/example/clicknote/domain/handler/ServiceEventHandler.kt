package com.example.clicknote.domain.handler

import com.example.clicknote.domain.service.Service
import com.example.clicknote.domain.service.TranscriptionCapable
import kotlinx.coroutines.flow.Flow

interface ServiceEventHandler {
    val currentService: Flow<TranscriptionCapable?>
    suspend fun handleServiceActivated(service: Service)
    suspend fun handleServiceDeactivated(service: Service)
    suspend fun handleServiceError(service: Service, error: Throwable)
    suspend fun handleServiceInitialized(service: Service)
    suspend fun handleServiceCleanup(service: Service)
}