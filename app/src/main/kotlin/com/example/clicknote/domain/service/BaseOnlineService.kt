package com.example.clicknote.domain.service

interface BaseOnlineService : BaseService {
    suspend fun isOnline(): Boolean
    suspend fun initialize()
    override suspend fun cleanup()
    suspend fun getServiceStatus(): ServiceStatus
    
    enum class ServiceStatus {
        READY,
        INITIALIZING,
        ERROR,
        OFFLINE
    }
} 