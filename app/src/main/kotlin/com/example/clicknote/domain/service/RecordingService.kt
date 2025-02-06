package com.example.clicknote.domain.service

import com.example.clicknote.domain.model.RecordingState
import kotlinx.coroutines.flow.Flow
import java.io.File

interface RecordingService {
    val recordingState: Flow<RecordingState>
    val recordingDuration: Flow<Long>
    val recordingError: Flow<String?>
    
    suspend fun startRecording(outputFile: File): Result<Unit>
    suspend fun stopRecording(): Result<File>
    suspend fun pauseRecording(): Result<Unit>
    suspend fun resumeRecording(): Result<Unit>
    suspend fun cancelRecording()
    suspend fun isRecording(): Boolean
    suspend fun cleanup()
}

enum class RecordingState {
    IDLE,
    RECORDING,
    PAUSED,
    STOPPED,
    ERROR
} 