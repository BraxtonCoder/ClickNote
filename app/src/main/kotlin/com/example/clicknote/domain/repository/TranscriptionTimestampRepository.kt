package com.example.clicknote.domain.repository

import com.example.clicknote.data.entity.TranscriptionTimestamp
import kotlinx.coroutines.flow.Flow

interface TranscriptionTimestampRepository {
    fun getTimestampsForNote(noteId: String): Flow<List<TranscriptionTimestamp>>
    
    fun getTimestampsBySpeaker(noteId: String, speaker: String): Flow<List<TranscriptionTimestamp>>
    
    fun getTimestampsInRange(
        noteId: String,
        startTime: Long,
        endTime: Long
    ): Flow<List<TranscriptionTimestamp>>
    
    fun searchTimestamps(noteId: String, query: String): Flow<List<TranscriptionTimestamp>>
    
    suspend fun getTimestampCount(noteId: String): Int
    
    suspend fun getAverageConfidence(noteId: String): Float?
    
    suspend fun getSpeakers(noteId: String): List<String>
    
    suspend fun insert(timestamp: TranscriptionTimestamp)
    
    suspend fun insertAll(timestamps: List<TranscriptionTimestamp>)
    
    suspend fun update(timestamp: TranscriptionTimestamp)
    
    suspend fun delete(timestamp: TranscriptionTimestamp)
    
    suspend fun deleteAllForNote(noteId: String)
    
    suspend fun replaceTimestamps(noteId: String, newTimestamps: List<TranscriptionTimestamp>)
} 