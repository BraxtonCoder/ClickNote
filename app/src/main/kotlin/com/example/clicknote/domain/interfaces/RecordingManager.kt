package com.example.clicknote.domain.interfaces

import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.StateFlow

interface RecordingManager {
    val isRecording: StateFlow<Boolean>
    val recordingState: StateFlow<RecordingState>
    val recordingDuration: StateFlow<Long>
    val amplitude: StateFlow<Float>
    val waveform: StateFlow<FloatArray>
    val transcriptionResult: SharedFlow<String>
    val error: SharedFlow<Throwable>

    suspend fun startRecording()
    suspend fun stopRecording()
    suspend fun pauseRecording()
    suspend fun resumeRecording()
    suspend fun cleanup()
} 