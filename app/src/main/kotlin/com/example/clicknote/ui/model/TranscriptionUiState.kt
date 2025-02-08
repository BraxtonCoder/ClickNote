package com.example.clicknote.ui.model

/**
 * Represents the UI state of transcription
 */
sealed class TranscriptionUiState {
    data object Idle : TranscriptionUiState()
    data object Recording : TranscriptionUiState()
    data object Paused : TranscriptionUiState()
    data class Processing(val progress: Float) : TranscriptionUiState()
    data class Completed(
        val text: String = "",
        val duration: Long = 0L,
        val wordCount: Int = 0
    ) : TranscriptionUiState()
    data class Error(val error: Throwable) : TranscriptionUiState()
    data class Cancelled(val reason: String? = null) : TranscriptionUiState()

    val isActive: Boolean
        get() = this is Recording || this is Processing

    val isComplete: Boolean
        get() = this is Completed

    val isError: Boolean
        get() = this is Error

    val isCancelled: Boolean
        get() = this is Cancelled

    val canStart: Boolean
        get() = this is Idle || this is Completed || this is Error || this is Cancelled

    val canPause: Boolean
        get() = this is Recording

    val canResume: Boolean
        get() = this is Paused

    val canCancel: Boolean
        get() = isActive || this is Paused

    companion object {
        fun initial() = Idle
    }
} 