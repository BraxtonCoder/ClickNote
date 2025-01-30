package com.example.clicknote.service

import java.io.File
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.SharedFlow

interface RecordingManager {
    val isRecording: StateFlow<Boolean>
    val recordingDuration: StateFlow<Long>
    val amplitude: StateFlow<Float>
    val waveform: StateFlow<FloatArray>
    val transcriptionResult: SharedFlow<String>
    val error: SharedFlow<Throwable>
    
    suspend fun startRecording(): File
    suspend fun stopRecording(): File
    suspend fun pauseRecording()
    suspend fun resumeRecording()
    fun getAmplitude(): Float
    fun release()
    suspend fun cleanup()
}

sealed class RecordingState {
    object Idle : RecordingState()
    data class Recording(
        val amplitude: Int = 0,
        val duration: Long = 0L
    ) : RecordingState()
    object Paused : RecordingState()
    object Processing : RecordingState()
} 