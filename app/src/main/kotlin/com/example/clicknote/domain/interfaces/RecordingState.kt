package com.example.clicknote.domain.interfaces

sealed class RecordingState {
    data object Idle : RecordingState()
    data object Recording : RecordingState()
    data object Paused : RecordingState()
    data object Processing : RecordingState()
    data object Completed : RecordingState()
    data class Error(val message: String) : RecordingState()
    data class Cancelled(val reason: String? = null) : RecordingState()
}
