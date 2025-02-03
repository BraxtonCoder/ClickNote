package com.example.clicknote.domain.model

sealed class ServiceEvent {
    object Started : ServiceEvent()
    object Stopped : ServiceEvent()
    object Completed : ServiceEvent()
    data class Failed(val error: String) : ServiceEvent()
    data class ProgressUpdate(val progress: Float) : ServiceEvent()
} 