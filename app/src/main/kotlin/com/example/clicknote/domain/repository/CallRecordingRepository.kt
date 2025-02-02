package com.example.clicknote.domain.repository

import com.example.clicknote.domain.model.CallRecording
import kotlinx.coroutines.flow.Flow

interface CallRecordingRepository {
    fun getAllCallRecordings(): Flow<List<CallRecording>>
    
    suspend fun getCallRecordingById(id: String): CallRecording?
    
    suspend fun insertCallRecording(recording: CallRecording)
    
    suspend fun deleteCallRecording(id: String)
    
    suspend fun deleteAllCallRecordings()
    
    suspend fun updateCallRecording(recording: CallRecording)
    
    fun getCallRecordingsByDateRange(startDate: Long, endDate: Long): Flow<List<CallRecording>>
    
    fun searchCallRecordings(query: String): Flow<List<CallRecording>>
} 