package com.example.clicknote.domain.service

import kotlinx.coroutines.flow.Flow

interface AudioRecorderService {
    fun startRecording(outputPath: String)
    fun stopRecording()
    fun pauseRecording()
    fun resumeRecording()
    fun isRecording(): Boolean
    fun isPaused(): Boolean
    fun getAmplitude(): Flow<Float>
    fun getDuration(): Long
    fun getOutputPath(): String?
    fun deleteRecording()
} 