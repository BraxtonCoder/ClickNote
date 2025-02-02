package com.example.clicknote.domain.event

import com.example.clicknote.domain.model.TranscriptionServiceContext

sealed class ServiceEvent {
    data class ServiceInitialized(val serviceId: String, val context: TranscriptionServiceContext) : ServiceEvent()
    data class ServiceActivated(val serviceId: String) : ServiceEvent()
    data class ServiceReleased(val serviceId: String) : ServiceEvent()
    data class ServiceError(val message: String, val error: Throwable) : ServiceEvent()
    object AllServicesReleased : ServiceEvent()
} 