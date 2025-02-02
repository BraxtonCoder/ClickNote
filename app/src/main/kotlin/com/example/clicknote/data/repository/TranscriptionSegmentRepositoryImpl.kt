package com.example.clicknote.data.repository

import com.example.clicknote.data.dao.TranscriptionSegmentDao
import com.example.clicknote.domain.model.TranscriptionSegment
import com.example.clicknote.domain.repository.TranscriptionSegmentRepository
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class TranscriptionSegmentRepositoryImpl @Inject constructor(
    private val transcriptionSegmentDao: TranscriptionSegmentDao
) : TranscriptionSegmentRepository {
    
    override fun getSegmentsByNoteId(noteId: String): Flow<List<TranscriptionSegment>> {
        return transcriptionSegmentDao.getSegmentsByNoteId(noteId)
    }
    
    override fun getSegmentsBySpeakerId(speakerId: String): Flow<List<TranscriptionSegment>> {
        return transcriptionSegmentDao.getSegmentsBySpeakerId(speakerId)
    }
    
    override fun searchSegments(query: String): Flow<List<TranscriptionSegment>> {
        return transcriptionSegmentDao.searchSegments(query)
    }
    
    override suspend fun insertSegment(segment: TranscriptionSegment) {
        transcriptionSegmentDao.insertSegment(segment)
    }
    
    override suspend fun insertSegments(segments: List<TranscriptionSegment>) {
        transcriptionSegmentDao.insertSegments(segments)
    }
    
    override suspend fun updateSegment(segment: TranscriptionSegment) {
        transcriptionSegmentDao.updateSegment(segment)
    }
    
    override suspend fun deleteSegment(segment: TranscriptionSegment) {
        transcriptionSegmentDao.deleteSegment(segment)
    }
    
    override suspend fun deleteSegmentsByNoteId(noteId: String) {
        transcriptionSegmentDao.deleteSegmentsByNoteId(noteId)
    }
    
    override fun getSegmentsInTimeRange(
        noteId: String,
        startTime: Long,
        endTime: Long
    ): Flow<List<TranscriptionSegment>> {
        return transcriptionSegmentDao.getSegmentsInTimeRange(noteId, startTime, endTime)
    }
    
    override suspend fun countSegmentsWithSpeaker(speakerId: String): Int {
        return transcriptionSegmentDao.countSegmentsWithSpeaker(speakerId)
    }
    
    override fun getUniqueSpeakerIds(noteId: String): Flow<List<String>> {
        return transcriptionSegmentDao.getUniqueSpeakerIds(noteId)
    }
} 