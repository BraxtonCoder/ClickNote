package com.example.clicknote.domain.interfaces

import com.example.clicknote.domain.state.TranscriptionServiceState
import kotlinx.coroutines.flow.StateFlow

interface TranscriptionStateManager {
    val currentState: StateFlow<TranscriptionServiceState?>
    
    fun startTranscription()
    fun stopTranscription()
    fun pauseTranscription()
    fun resumeTranscription()
    
    fun updateTranscriptionState(state: TranscriptionServiceState)
    fun clearTranscriptionState()
    
    fun isTranscribing(): Boolean
    fun isPaused(): Boolean
} 