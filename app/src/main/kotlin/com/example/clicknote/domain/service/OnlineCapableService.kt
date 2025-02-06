package com.example.clicknote.domain.service

import com.example.clicknote.domain.model.*
import kotlinx.coroutines.flow.Flow
import java.io.File

/**
 * Interface for services that can perform online transcription operations
 */
interface OnlineCapableService : TranscriptionCapable {
    /**
     * Check if online services are available
     */
    suspend fun isOnlineAvailable(): Boolean

    /**
     * Get the current online service status
     */
    suspend fun getOnlineStatus(): OnlineServiceStatus

    /**
     * Events flow for tracking transcription events
     */
    override val events: Flow<TranscriptionEvent>
}

enum class OnlineServiceStatus {
    AVAILABLE,
    UNAVAILABLE,
    ERROR,
    RATE_LIMITED
}