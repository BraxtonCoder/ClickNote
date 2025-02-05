package com.example.clicknote.domain.state

sealed class ServiceState {
    data class Initialized(val serviceId: String) : ServiceState()
    data class Active(val serviceId: String) : ServiceState()
    data class Inactive(val serviceId: String) : ServiceState()
    data class Error(val serviceId: String, val error: Throwable) : ServiceState()
    data class Cleaned(val serviceId: String) : ServiceState()

    companion object {
        val INITIALIZED = "INITIALIZED"
        val ACTIVE = "ACTIVE"
        val INACTIVE = "INACTIVE"
        val ERROR = "ERROR"
        val CLEANED = "CLEANED"
    }
} 