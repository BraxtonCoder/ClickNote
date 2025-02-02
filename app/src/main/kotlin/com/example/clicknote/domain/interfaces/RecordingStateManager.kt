package com.example.clicknote.domain.interfaces

import kotlinx.coroutines.flow.StateFlow
import com.example.clicknote.domain.interfaces.RecordingState

interface RecordingStateManager {
    val isRecording: StateFlow<Boolean>
    val recordingState: StateFlow<RecordingState>
    
    suspend fun setRecording(isRecording: Boolean)
    suspend fun setRecordingState(state: RecordingState)
    suspend fun cleanup()
} 