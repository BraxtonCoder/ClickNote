package com.example.clicknote.domain.state

import com.example.clicknote.domain.service.TranscriptionCapable
import kotlinx.coroutines.flow.Flow

/**
 * Interface for managing service states in the application
 */
interface ServiceStateManager {
    /**
     * Current state of the service
     */
    val currentState: Flow<ServiceState>

    /**
     * Update the service state
     */
    suspend fun updateState(state: ServiceState)

    /**
     * Get the current state synchronously
     */
    suspend fun getCurrentState(): ServiceState

    /**
     * Reset the service state to initial state
     */
    suspend fun reset()

    /**
     * Clean up resources
     */
    suspend fun cleanup()
}

/**
 * Represents changes in service state that can trigger state transitions
 */
sealed class ServiceStateChange {
    /**
     * Service has been deactivated
     */
    data class Deactivated(val serviceId: String) : ServiceStateChange()

    /**
     * Service has been activated
     */
    data class Activated(val serviceId: String, val service: TranscriptionCapable) : ServiceStateChange()

    /**
     * Service operation failed
     */
    data class Failed(val serviceId: String, val error: Throwable) : ServiceStateChange()

    /**
     * Service has been cleaned up
     */
    data class Cleaned(val serviceId: String) : ServiceStateChange()
}