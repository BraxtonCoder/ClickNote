package com.example.clicknote.domain.event

import kotlinx.coroutines.flow.SharedFlow

/**
 * Interface for handling service-related events in the application.
 * Uses a SharedFlow to emit ServiceEvent instances to observers.
 */
interface ServiceEventBus {
    /**
     * Flow of service events that can be observed by subscribers
     */
    val events: SharedFlow<ServiceEvent>

    /**
     * Emits a service event to all observers
     * @param event The service event to emit
     */
    suspend fun emit(event: ServiceEvent)
} 