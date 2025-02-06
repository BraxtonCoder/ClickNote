package com.example.clicknote.domain.state

/**
 * Represents the current state of a service in the application
 */
sealed class ServiceState {
    /**
     * Service has been initialized but is not yet active
     */
    data class Initialized(val serviceId: String) : ServiceState()

    /**
     * Service is active and running
     */
    data class Active(val serviceId: String) : ServiceState()

    /**
     * Service is inactive but still initialized
     */
    data class Inactive(val serviceId: String) : ServiceState()

    /**
     * Service encountered an error
     */
    data class Error(val serviceId: String, val error: Throwable) : ServiceState()

    /**
     * Service has been cleaned up and resources released
     */
    data class Cleaned(val serviceId: String) : ServiceState()

    companion object {
        const val INITIALIZED = "INITIALIZED"
        const val ACTIVE = "ACTIVE"
        const val INACTIVE = "INACTIVE"
        const val ERROR = "ERROR"
        const val CLEANED = "CLEANED"
    }
} 