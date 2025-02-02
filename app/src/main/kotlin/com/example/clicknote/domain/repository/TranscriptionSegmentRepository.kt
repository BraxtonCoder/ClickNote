package com.example.clicknote.domain.repository

import com.example.clicknote.domain.model.TranscriptionSegment
import kotlinx.coroutines.flow.Flow

interface TranscriptionSegmentRepository {
    fun getSegmentsByNoteId(noteId: String): Flow<List<TranscriptionSegment>>
    
    fun getSegmentsBySpeakerId(speakerId: String): Flow<List<TranscriptionSegment>>
    
    fun searchSegments(query: String): Flow<List<TranscriptionSegment>>
    
    suspend fun insertSegment(segment: TranscriptionSegment)
    
    suspend fun insertSegments(segments: List<TranscriptionSegment>)
    
    suspend fun updateSegment(segment: TranscriptionSegment)
    
    suspend fun deleteSegment(segment: TranscriptionSegment)
    
    suspend fun deleteSegmentsByNoteId(noteId: String)
    
    fun getSegmentsInTimeRange(
        noteId: String,
        startTime: Long,
        endTime: Long
    ): Flow<List<TranscriptionSegment>>
    
    suspend fun countSegmentsWithSpeaker(speakerId: String): Int
    
    fun getUniqueSpeakerIds(noteId: String): Flow<List<String>>
} 