package com.example.clicknote.data.repository

import com.example.clicknote.data.dao.TranscriptionSegmentDao
import com.example.clicknote.data.entity.TranscriptionSegment as TranscriptionSegmentEntity
import com.example.clicknote.domain.model.TranscriptionSegment
import com.example.clicknote.domain.repository.TranscriptionSegmentRepository
import kotlinx.coroutines.flow.*
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class TranscriptionSegmentRepositoryImpl @Inject constructor(
    private val segmentDao: TranscriptionSegmentDao
) : TranscriptionSegmentRepository {

    override fun getAllSegments(): Flow<List<TranscriptionSegment>> {
        return segmentDao.getAllSegments().map { segments ->
            segments.map { it.toDomain() }
        }
    }

    override fun getSegmentsBySpeaker(speakerId: String): Flow<List<TranscriptionSegment>> {
        return segmentDao.getSegmentsBySpeaker(speakerId).map { segments ->
            segments.map { it.toDomain() }
        }
    }

    override fun searchSegments(query: String): Flow<List<TranscriptionSegment>> {
        return segmentDao.searchSegments(query).map { segments ->
            segments.map { it.toDomain() }
        }
    }

    override suspend fun insertSegment(segment: TranscriptionSegment) {
        segmentDao.insertSegment(TranscriptionSegmentEntity.fromDomain(segment.noteId, segment))
    }

    override suspend fun insertSegments(segments: List<TranscriptionSegment>) {
        segmentDao.insertSegments(segments.map { TranscriptionSegmentEntity.fromDomain(it.noteId, it) })
    }

    override suspend fun updateSegment(segment: TranscriptionSegment) {
        segmentDao.updateSegment(TranscriptionSegmentEntity.fromDomain(segment.noteId, segment))
    }

    override suspend fun deleteSegment(segment: TranscriptionSegment) {
        segmentDao.deleteSegment(TranscriptionSegmentEntity.fromDomain(segment.noteId, segment))
    }

    override suspend fun deleteSegmentsByNoteId(noteId: String) {
        segmentDao.deleteSegmentsByNoteId(noteId)
    }

    override suspend fun getSegmentCount(): Int {
        return segmentDao.getSegmentCount()
    }

    override fun getSegmentsInTimeRange(startTime: Long, endTime: Long): Flow<List<TranscriptionSegment>> {
        return segmentDao.getSegmentsInTimeRange(startTime, endTime).map { segments ->
            segments.map { it.toDomain() }
        }
    }

    override suspend fun countSegmentsBySpeaker(speakerId: String): Int {
        return segmentDao.countSegmentsBySpeaker(speakerId)
    }

    override fun getUniqueSpeakers(noteId: String): Flow<List<String>> {
        return segmentDao.getUniqueSpeakers(noteId)
    }
} 