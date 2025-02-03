package com.example.clicknote.domain.service

interface BaseOnlineService {
    suspend fun isOnline(): Boolean
    suspend fun initialize()
    suspend fun cleanup()
    suspend fun getServiceStatus(): ServiceStatus
    
    enum class ServiceStatus {
        READY,
        INITIALIZING,
        ERROR,
        OFFLINE
    }
} 