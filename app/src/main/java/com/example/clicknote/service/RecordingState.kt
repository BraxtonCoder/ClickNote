package com.example.clicknote.service

sealed class RecordingState {
    object Idle : RecordingState()
    object Recording : RecordingState()
    object Processing : RecordingState()
    data class Error(val message: String) : RecordingState()
} 