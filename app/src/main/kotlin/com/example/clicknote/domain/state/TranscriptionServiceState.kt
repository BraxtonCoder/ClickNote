package com.example.clicknote.domain.state

import kotlinx.coroutines.flow.StateFlow

interface TranscriptionServiceState {
    val isRecording: StateFlow<Boolean>
    val isProcessing: StateFlow<Boolean>
    val isError: StateFlow<Boolean>
    val errorMessage: StateFlow<String?>

    fun setRecording(recording: Boolean)
    fun setProcessing(processing: Boolean)
    fun setError(error: Boolean, message: String?)
    fun clearState()
    fun reset()
} 