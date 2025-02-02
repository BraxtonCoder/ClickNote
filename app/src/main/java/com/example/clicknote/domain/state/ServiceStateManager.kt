package com.example.clicknote.domain.state

import com.example.clicknote.domain.service.TranscriptionCapable
import kotlinx.coroutines.flow.StateFlow

interface ServiceStateManager {
    val state: StateFlow<ServiceState>
    val currentService: StateFlow<TranscriptionCapable?>
    suspend fun activateService(service: TranscriptionCapable)
    suspend fun deactivateService()
    fun getCurrentService(): TranscriptionCapable?
}

sealed class ServiceState {
    object Idle : ServiceState()
    data class Active(val service: TranscriptionCapable) : ServiceState()
    data class Error(val error: Throwable) : ServiceState()
}

sealed class ServiceStateChange {
    data class Initialized(val service: TranscriptionCapable) : ServiceStateChange()
    data class Failed(val error: Throwable) : ServiceStateChange()
    object Released : ServiceStateChange()
} 