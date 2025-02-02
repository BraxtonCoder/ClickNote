package com.example.clicknote.domain.service

import kotlinx.coroutines.flow.Flow
import java.io.File

interface AudioRecorder {
    val isRecording: Boolean
    val amplitude: Flow<Int>
    
    suspend fun startRecording(outputFile: File)
    suspend fun stopRecording()
    suspend fun pauseRecording()
    suspend fun resumeRecording()
    fun getAmplitude(): Int
    fun release()
    fun cleanup()
} 