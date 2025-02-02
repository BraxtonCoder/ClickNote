package com.example.clicknote.data.state

import com.example.clicknote.domain.state.TranscriptionServiceState
import com.example.clicknote.domain.service.TranscriptionCapable
import javax.inject.Inject
import javax.inject.Singleton
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

@Singleton
class TranscriptionServiceStateImpl @Inject constructor() : TranscriptionServiceState {
    private val _isRecording = MutableStateFlow(false)
    private val _isProcessing = MutableStateFlow(false)
    private val _isError = MutableStateFlow(false)
    private val _errorMessage = MutableStateFlow<String?>(null)
    private val _activeService = MutableStateFlow<TranscriptionCapable?>(null)

    override val isRecording: StateFlow<Boolean> = _isRecording.asStateFlow()
    override val isProcessing: StateFlow<Boolean> = _isProcessing.asStateFlow()
    override val isError: StateFlow<Boolean> = _isError.asStateFlow()
    override val errorMessage: StateFlow<String?> = _errorMessage.asStateFlow()
    override val activeService: StateFlow<TranscriptionCapable?> = _activeService.asStateFlow()

    override fun setRecording(recording: Boolean) {
        _isRecording.value = recording
    }

    override fun setProcessing(processing: Boolean) {
        _isProcessing.value = processing
    }

    override fun setError(error: Boolean, message: String?) {
        _isError.value = error
        _errorMessage.value = message
    }

    override fun setActiveService(service: TranscriptionCapable?) {
        _activeService.value = service
    }

    override fun clearState() {
        _activeService.value = null
        reset()
    }

    override fun reset() {
        _isRecording.value = false
        _isProcessing.value = false
        _isError.value = false
        _errorMessage.value = null
    }
} 