package com.example.clicknote.domain.model

sealed class ServiceEvent {
    data class ServiceInitialized(val serviceId: String) : ServiceEvent()
    data class ServiceActivated(val serviceId: String) : ServiceEvent()
    data class ServiceDeactivated(val serviceId: String) : ServiceEvent()
    data class ServiceError(val serviceId: String, val error: Throwable) : ServiceEvent()
} 