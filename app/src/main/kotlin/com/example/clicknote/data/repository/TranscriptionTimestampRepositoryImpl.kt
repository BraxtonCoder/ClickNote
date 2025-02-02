package com.example.clicknote.data.repository

import com.example.clicknote.data.dao.TranscriptionTimestampDao
import com.example.clicknote.data.entity.TranscriptionTimestamp
import com.example.clicknote.domain.repository.TranscriptionTimestampRepository
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class TranscriptionTimestampRepositoryImpl @Inject constructor(
    private val transcriptionTimestampDao: TranscriptionTimestampDao
) : TranscriptionTimestampRepository {
    
    override fun getTimestampsForNote(noteId: String): Flow<List<TranscriptionTimestamp>> {
        return transcriptionTimestampDao.getTimestampsForNote(noteId)
    }
    
    override fun getTimestampsBySpeaker(noteId: String, speaker: String): Flow<List<TranscriptionTimestamp>> {
        return transcriptionTimestampDao.getTimestampsBySpeaker(noteId, speaker)
    }
    
    override fun getTimestampsInRange(
        noteId: String,
        startTime: Long,
        endTime: Long
    ): Flow<List<TranscriptionTimestamp>> {
        return transcriptionTimestampDao.getTimestampsInRange(noteId, startTime, endTime)
    }
    
    override fun searchTimestamps(noteId: String, query: String): Flow<List<TranscriptionTimestamp>> {
        return transcriptionTimestampDao.searchTimestamps(noteId, query)
    }
    
    override suspend fun getTimestampCount(noteId: String): Int {
        return transcriptionTimestampDao.getTimestampCount(noteId)
    }
    
    override suspend fun getAverageConfidence(noteId: String): Float? {
        return transcriptionTimestampDao.getAverageConfidence(noteId)
    }
    
    override suspend fun getSpeakers(noteId: String): List<String> {
        return transcriptionTimestampDao.getSpeakers(noteId)
    }
    
    override suspend fun insert(timestamp: TranscriptionTimestamp) {
        transcriptionTimestampDao.insert(timestamp)
    }
    
    override suspend fun insertAll(timestamps: List<TranscriptionTimestamp>) {
        transcriptionTimestampDao.insertAll(timestamps)
    }
    
    override suspend fun update(timestamp: TranscriptionTimestamp) {
        transcriptionTimestampDao.update(timestamp)
    }
    
    override suspend fun delete(timestamp: TranscriptionTimestamp) {
        transcriptionTimestampDao.delete(timestamp)
    }
    
    override suspend fun deleteAllForNote(noteId: String) {
        transcriptionTimestampDao.deleteAllForNote(noteId)
    }
    
    override suspend fun replaceTimestamps(noteId: String, newTimestamps: List<TranscriptionTimestamp>) {
        transcriptionTimestampDao.replaceTimestamps(noteId, newTimestamps)
    }
} 