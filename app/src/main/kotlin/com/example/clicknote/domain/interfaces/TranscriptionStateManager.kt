package com.example.clicknote.domain.interfaces

import com.example.clicknote.domain.state.ServiceState
import kotlinx.coroutines.flow.StateFlow

interface TranscriptionStateManager {
    val currentState: StateFlow<ServiceState?>
    
    fun startRecording()
    fun stopRecording()
    fun startProcessing()
    fun finishProcessing()
    fun handleError(error: Throwable)
    fun updateServiceState(state: ServiceState)
    
    fun isRecording(): Boolean
    fun isProcessing(): Boolean
} 