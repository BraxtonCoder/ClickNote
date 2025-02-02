package com.example.clicknote.domain.interfaces

import kotlinx.coroutines.flow.Flow
import java.io.File

interface AudioRecorder {
    val amplitude: Flow<Int>
    val isRecording: Boolean
    
    suspend fun startRecording(outputFile: File)
    suspend fun stopRecording()
    suspend fun pauseRecording()
    suspend fun resumeRecording()
    fun getAmplitude(): Int
    fun release()
    fun cleanup()
} 