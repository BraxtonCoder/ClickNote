package com.example.clicknote.domain.model

sealed class ServiceState {
    object Idle : ServiceState()
    object Initializing : ServiceState()
    object Ready : ServiceState()
    object Processing : ServiceState()
    data class Error(val message: String) : ServiceState()
    data class Progress(val progress: Float) : ServiceState()
} 