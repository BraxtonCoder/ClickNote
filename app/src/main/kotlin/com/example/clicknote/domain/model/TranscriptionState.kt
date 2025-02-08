package com.example.clicknote.domain.model

/**
 * Represents the current state of a note's transcription
 */
enum class TranscriptionState {
    PENDING,        // Waiting to be transcribed
    IN_PROGRESS,    // Currently being transcribed
    COMPLETED,      // Successfully transcribed
    FAILED,         // Transcription failed
    CANCELLED,      // Transcription was cancelled
    REQUIRES_RETRY; // Transcription needs to be retried

    companion object {
        fun fromString(state: String): TranscriptionState {
            return try {
                valueOf(state.uppercase())
            } catch (e: IllegalArgumentException) {
                PENDING
            }
        }

        fun isTerminalState(state: TranscriptionState): Boolean {
            return when (state) {
                COMPLETED, FAILED, CANCELLED -> true
                else -> false
            }
        }

        fun requiresRetry(state: TranscriptionState): Boolean {
            return when (state) {
                FAILED, REQUIRES_RETRY -> true
                else -> false
            }
        }
    }
} 