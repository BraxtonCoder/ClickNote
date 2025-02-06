package com.example.clicknote.domain.model

/**
 * Represents the current state of an audio recording
 */
enum class RecordingState {
    IDLE,           // Not recording
    PREPARING,      // Setting up recording
    RECORDING,      // Currently recording
    PAUSED,         // Recording paused
    STOPPING,       // Stopping recording
    STOPPED,        // Recording stopped
    ERROR;          // Error occurred during recording

    companion object {
        fun fromString(state: String): RecordingState {
            return try {
                valueOf(state.uppercase())
            } catch (e: IllegalArgumentException) {
                IDLE
            }
        }
    }
} 