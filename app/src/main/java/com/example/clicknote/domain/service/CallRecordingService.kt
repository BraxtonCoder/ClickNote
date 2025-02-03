package com.example.clicknote.domain.service

import kotlinx.coroutines.flow.Flow

interface CallRecordingService {
    suspend fun startCallRecording()
    suspend fun stopCallRecording()
    suspend fun isCallInProgress(): Boolean
    suspend fun getCallDuration(): Long
    suspend fun getCallTranscription(): String
    suspend fun getCallSummary(): String
    suspend fun getRecordingStatus(): Flow<RecordingStatus>
    
    enum class RecordingStatus {
        IDLE,
        RECORDING,
        PROCESSING,
        COMPLETED,
        ERROR
    }
} 