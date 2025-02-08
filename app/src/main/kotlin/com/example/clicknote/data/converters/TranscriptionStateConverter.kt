package com.example.clicknote.data.converters

import com.example.clicknote.domain.model.TranscriptionState
import com.example.clicknote.ui.model.TranscriptionUiState
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class TranscriptionStateConverter @Inject constructor() {

    fun toUiState(domainState: TranscriptionState, text: String = "", duration: Long = 0L): TranscriptionUiState {
        return when (domainState) {
            TranscriptionState.PENDING -> TranscriptionUiState.Idle
            TranscriptionState.IN_PROGRESS -> TranscriptionUiState.Recording
            TranscriptionState.COMPLETED -> TranscriptionUiState.Completed(
                text = text,
                duration = duration,
                wordCount = text.split("\\s+".toRegex()).count()
            )
            TranscriptionState.FAILED -> TranscriptionUiState.Error(
                Exception("Transcription failed")
            )
            TranscriptionState.CANCELLED -> TranscriptionUiState.Cancelled(
                reason = "User cancelled"
            )
            TranscriptionState.REQUIRES_RETRY -> TranscriptionUiState.Error(
                Exception("Transcription requires retry")
            )
        }
    }

    fun toDomainState(uiState: TranscriptionUiState): TranscriptionState {
        return when (uiState) {
            is TranscriptionUiState.Idle -> TranscriptionState.PENDING
            is TranscriptionUiState.Recording -> TranscriptionState.IN_PROGRESS
            is TranscriptionUiState.Paused -> TranscriptionState.IN_PROGRESS
            is TranscriptionUiState.Processing -> TranscriptionState.IN_PROGRESS
            is TranscriptionUiState.Completed -> TranscriptionState.COMPLETED
            is TranscriptionUiState.Error -> TranscriptionState.FAILED
            is TranscriptionUiState.Cancelled -> TranscriptionState.CANCELLED
        }
    }

    fun toUiStateWithProgress(domainState: TranscriptionState, progress: Float): TranscriptionUiState {
        return when (domainState) {
            TranscriptionState.IN_PROGRESS -> TranscriptionUiState.Processing(progress)
            else -> toUiState(domainState)
        }
    }
} 