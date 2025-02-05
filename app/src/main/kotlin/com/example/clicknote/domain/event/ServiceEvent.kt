package com.example.clicknote.domain.event

import com.example.clicknote.domain.model.TranscriptionServiceContext

sealed class ServiceEvent {
    data class ServiceActivated(val serviceId: String) : ServiceEvent()
    data class ServiceDeactivated(val serviceId: String) : ServiceEvent()
    data class ServiceError(val serviceId: String, val error: Throwable) : ServiceEvent()
    data class ServiceInitialized(val serviceId: String, val context: TranscriptionServiceContext) : ServiceEvent()
    data class ServiceCleaned(val serviceId: String) : ServiceEvent()
} 