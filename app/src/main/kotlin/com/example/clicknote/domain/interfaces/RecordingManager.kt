package com.example.clicknote.domain.interfaces

import com.example.clicknote.domain.model.TranscriptionState
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.StateFlow

interface RecordingManager {
    val isRecording: StateFlow<Boolean>
    val recordingState: Flow<RecordingState>
    val recordingDuration: StateFlow<Long>
    val amplitude: StateFlow<Float>
    val waveform: StateFlow<FloatArray>
    val transcriptionState: Flow<TranscriptionState>
    val transcriptionResult: Flow<String>
    val error: SharedFlow<Throwable>

    suspend fun startRecording()
    suspend fun stopRecording()
    suspend fun pauseRecording()
    suspend fun resumeRecording()
    suspend fun cancelRecording()
    suspend fun cleanup()
} 