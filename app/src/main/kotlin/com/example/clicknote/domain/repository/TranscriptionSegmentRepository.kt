package com.example.clicknote.domain.repository

import com.example.clicknote.domain.model.TranscriptionSegment
import kotlinx.coroutines.flow.Flow

interface TranscriptionSegmentRepository {
    fun getAllSegments(): Flow<List<TranscriptionSegment>>
    fun getSegmentsForNote(noteId: String): Flow<List<TranscriptionSegment>>
    fun getSegmentsByTimeRange(startTime: Long, endTime: Long): Flow<List<TranscriptionSegment>>
    fun getSegmentsBySpeaker(speakerId: String): Flow<List<TranscriptionSegment>>
    fun searchSegments(query: String): Flow<List<TranscriptionSegment>>
    fun getUniqueSpeakers(noteId: String): Flow<List<String>>
    
    suspend fun insertSegment(noteId: String, segment: TranscriptionSegment)
    suspend fun insertSegments(noteId: String, segments: List<TranscriptionSegment>)
    suspend fun updateSegment(noteId: String, segment: TranscriptionSegment)
    suspend fun deleteSegment(noteId: String, segment: TranscriptionSegment)
    suspend fun deleteSegmentsForNote(noteId: String)
    suspend fun updateSpeakerId(segmentId: String, speakerId: String)
    suspend fun replaceSegmentsForNote(noteId: String, segments: List<TranscriptionSegment>)
    suspend fun getSegmentCount(): Int
    suspend fun countSegmentsBySpeaker(speakerId: String): Int
} 