package com.example.clicknote.service.impl

import com.example.clicknote.domain.interfaces.TranscriptionStateManager
import com.example.clicknote.domain.state.ServiceStateManager
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import javax.inject.Inject
import javax.inject.Singleton
import javax.inject.Provider

@Singleton
class DefaultTranscriptionStateManager @Inject constructor(
    private val serviceStateManager: Provider<ServiceStateManager>
) : TranscriptionStateManager {
    private val _currentState = MutableStateFlow<ServiceState?>(null)
    override val currentState: StateFlow<ServiceState?> = _currentState

    override fun startRecording() {
        _currentState.value = ServiceState.Recording
    }

    override fun stopRecording() {
        _currentState.value = ServiceState.Idle
    }

    override fun startProcessing() {
        _currentState.value = ServiceState.Processing
    }

    override fun finishProcessing() {
        _currentState.value = ServiceState.Idle
    }

    override fun handleError(error: Throwable) {
        _currentState.value = ServiceState.Error(error)
    }

    override fun updateServiceState(state: ServiceState) {
        _currentState.value = state
    }
} 