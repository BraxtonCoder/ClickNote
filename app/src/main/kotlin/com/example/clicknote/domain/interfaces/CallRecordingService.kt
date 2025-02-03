package com.example.clicknote.domain.interfaces

interface CallRecordingService {
    suspend fun startRecording()
    suspend fun stopRecording(): String
    suspend fun isRecording(): Boolean
    suspend fun requestPermissions()
    suspend fun hasRequiredPermissions(): Boolean
    suspend fun getRecordingState(): Boolean
    suspend fun getAudioFilePath(): String?
} 