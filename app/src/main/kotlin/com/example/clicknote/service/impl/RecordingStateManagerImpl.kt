package com.example.clicknote.service.impl

import com.example.clicknote.domain.interfaces.RecordingStateManager
import com.example.clicknote.domain.interfaces.RecordingState
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class RecordingStateManagerImpl @Inject constructor() : RecordingStateManager {
    private val _isRecording = MutableStateFlow(false)
    override val isRecording: StateFlow<Boolean> = _isRecording.asStateFlow()

    private val _recordingState = MutableStateFlow<RecordingState>(RecordingState.Idle)
    override val recordingState: StateFlow<RecordingState> = _recordingState.asStateFlow()

    override suspend fun setRecording(isRecording: Boolean) {
        _isRecording.value = isRecording
    }

    override suspend fun setRecordingState(state: RecordingState) {
        _recordingState.value = state
    }

    override suspend fun cleanup() {
        _isRecording.value = false
        _recordingState.value = RecordingState.Idle
    }
} 