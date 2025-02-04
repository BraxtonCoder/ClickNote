package com.example.clicknote.domain.lifecycle

import com.example.clicknote.domain.model.ServiceType
import com.example.clicknote.domain.service.Service

interface ServiceLifecycleManager {
    suspend fun initializeService(type: ServiceType)
    suspend fun activateService(serviceId: String)
    suspend fun deactivateService(serviceId: String)
    suspend fun cleanupService(serviceId: String)
    suspend fun handleServiceError(serviceId: String, error: Throwable)
    suspend fun getService(serviceId: String): Service?
    suspend fun getService(type: ServiceType): Service?
} 