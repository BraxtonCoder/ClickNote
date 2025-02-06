package com.example.clicknote.domain.service

/**
 * Base interface for all services in the application
 */
interface BaseService {
    /**
     * Unique identifier for the service
     */
    val id: String

    /**
     * Check if the service is initialized
     */
    fun isInitialized(): Boolean

    /**
     * Cleanup any resources used by the service
     */
    suspend fun cleanup()
} 