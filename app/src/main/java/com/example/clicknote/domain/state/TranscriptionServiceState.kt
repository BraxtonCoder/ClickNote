package com.example.clicknote.domain.state

import com.example.clicknote.domain.service.TranscriptionCapable
import kotlinx.coroutines.flow.StateFlow

interface TranscriptionServiceState {
    val isRecording: StateFlow<Boolean>
    val isProcessing: StateFlow<Boolean>
    val isError: StateFlow<Boolean>
    val errorMessage: StateFlow<String?>
    val activeService: StateFlow<TranscriptionCapable?>

    fun setRecording(recording: Boolean)
    fun setProcessing(processing: Boolean)
    fun setError(error: Boolean, message: String?)
    fun setActiveService(service: TranscriptionCapable?)
    fun clearState()
    fun reset()
} 