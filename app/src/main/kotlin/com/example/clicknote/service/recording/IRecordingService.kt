package com.example.clicknote.service.recording

import kotlinx.coroutines.flow.StateFlow

interface IRecordingService {
    val waveformData: StateFlow<FloatArray>
    suspend fun startRecording()
    suspend fun pauseRecording()
    suspend fun stopRecording()
    suspend fun isRecording(): Boolean
} 