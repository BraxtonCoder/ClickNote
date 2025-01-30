package com.example.clicknote.data

sealed class RecordingState {
    object Idle : RecordingState()
    data class Recording(val partialTranscription: String = "") : RecordingState()
    data class Error(val message: String) : RecordingState()
    object Paused : RecordingState()
} 