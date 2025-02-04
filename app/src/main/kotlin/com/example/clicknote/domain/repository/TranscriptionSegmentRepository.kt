package com.example.clicknote.domain.repository

import com.example.clicknote.domain.model.TranscriptionSegment
import kotlinx.coroutines.flow.Flow

interface TranscriptionSegmentRepository {
    fun getAllSegments(): Flow<List<TranscriptionSegment>>
    
    fun getSegmentsBySpeaker(speakerId: String): Flow<List<TranscriptionSegment>>
    
    fun searchSegments(query: String): Flow<List<TranscriptionSegment>>
    
    suspend fun insertSegment(segment: TranscriptionSegment)
    
    suspend fun insertSegments(segments: List<TranscriptionSegment>)
    
    suspend fun updateSegment(segment: TranscriptionSegment)
    
    suspend fun deleteSegment(segment: TranscriptionSegment)
    
    suspend fun deleteSegmentsByNoteId(noteId: String)
    
    suspend fun getSegmentCount(): Int
    
    fun getSegmentsInTimeRange(startTime: Long, endTime: Long): Flow<List<TranscriptionSegment>>
    
    suspend fun countSegmentsBySpeaker(speakerId: String): Int
    
    fun getUniqueSpeakers(noteId: String): Flow<List<String>>
} 