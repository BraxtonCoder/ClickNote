package com.example.clicknote.domain.interfaces

import kotlinx.coroutines.flow.StateFlow
import com.example.clicknote.domain.interfaces.RecordingState

interface AudioRecordingService {
    val recordingState: StateFlow<RecordingState>
    val isRecording: StateFlow<Boolean>
    
    fun startRecording()
    fun stopRecording()
    fun pauseRecording()
    fun resumeRecording()
    fun release()
} 