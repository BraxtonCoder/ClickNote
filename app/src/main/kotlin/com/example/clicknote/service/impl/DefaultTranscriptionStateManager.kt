package com.example.clicknote.service.impl

import com.example.clicknote.domain.interfaces.TranscriptionStateManager
import com.example.clicknote.domain.state.TranscriptionServiceState
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class DefaultTranscriptionStateManager @Inject constructor() : TranscriptionStateManager {
    private val _currentState = MutableStateFlow<TranscriptionServiceState?>(null)
    override val currentState: StateFlow<TranscriptionServiceState?> = _currentState
    
    private var isActive = false
    private var isPausedState = false
    
    override fun startTranscription() {
        isActive = true
        isPausedState = false
    }
    
    override fun stopTranscription() {
        isActive = false
        isPausedState = false
        clearTranscriptionState()
    }
    
    override fun pauseTranscription() {
        isPausedState = true
    }
    
    override fun resumeTranscription() {
        isPausedState = false
    }
    
    override fun updateTranscriptionState(state: TranscriptionServiceState) {
        _currentState.value = state
    }
    
    override fun clearTranscriptionState() {
        _currentState.value = null
    }
    
    override fun isTranscribing(): Boolean = isActive && !isPausedState
    
    override fun isPaused(): Boolean = isPausedState
} 