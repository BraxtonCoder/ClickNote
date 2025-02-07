package com.example.clicknote.data.repository

import com.example.clicknote.data.dao.TranscriptionSegmentDao
import com.example.clicknote.data.entity.TranscriptionSegmentEntity
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
        return segmentDao.getAllSegments()
            .map { entities -> entities.map { it.toDomain() } }
    }

    override fun getSegmentsForNote(noteId: String): Flow<List<TranscriptionSegment>> {
        return segmentDao.getSegmentsForNote(noteId)
            .map { entities -> entities.map { it.toDomain() } }
    }

    override fun getSegmentsByTimeRange(startTime: Long, endTime: Long): Flow<List<TranscriptionSegment>> {
        return segmentDao.getSegmentsByTimeRange(startTime, endTime)
            .map { entities -> entities.map { it.toDomain() } }
    }

    override fun getSegmentsBySpeaker(speakerId: String): Flow<List<TranscriptionSegment>> {
        return segmentDao.getSegmentsBySpeaker(speakerId)
            .map { entities -> entities.map { it.toDomain() } }
    }

    override fun searchSegments(query: String): Flow<List<TranscriptionSegment>> {
        return segmentDao.searchSegments(query)
            .map { entities -> entities.map { it.toDomain() } }
    }

    override fun getUniqueSpeakers(noteId: String): Flow<List<String>> {
        return segmentDao.getUniqueSpeakers(noteId)
    }

    override suspend fun insertSegment(noteId: String, segment: TranscriptionSegment) {
        segmentDao.insertSegment(TranscriptionSegmentEntity.fromDomain(noteId, segment))
    }

    override suspend fun insertSegments(noteId: String, segments: List<TranscriptionSegment>) {
        val entities = segments.map { TranscriptionSegmentEntity.fromDomain(noteId, it) }
        segmentDao.insertSegments(entities)
    }

    override suspend fun updateSegment(noteId: String, segment: TranscriptionSegment) {
        segmentDao.updateSegment(TranscriptionSegmentEntity.fromDomain(noteId, segment))
    }

    override suspend fun deleteSegment(noteId: String, segment: TranscriptionSegment) {
        segmentDao.deleteSegment(TranscriptionSegmentEntity.fromDomain(noteId, segment))
    }

    override suspend fun deleteSegmentsForNote(noteId: String) {
        segmentDao.deleteSegmentsForNote(noteId)
    }

    override suspend fun updateSpeakerId(segmentId: String, speakerId: String) {
        segmentDao.updateSpeakerId(segmentId, speakerId)
    }

    override suspend fun replaceSegmentsForNote(noteId: String, segments: List<TranscriptionSegment>) {
        val entities = segments.map { TranscriptionSegmentEntity.fromDomain(noteId, it) }
        segmentDao.replaceSegmentsForNote(noteId, entities)
    }

    override suspend fun getSegmentCount(): Int {
        return segmentDao.getSegmentCount()
    }

    override suspend fun countSegmentsBySpeaker(speakerId: String): Int {
        return segmentDao.countSegmentsBySpeaker(speakerId)
    }
} 