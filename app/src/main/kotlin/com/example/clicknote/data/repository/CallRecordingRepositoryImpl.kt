package com.example.clicknote.data.repository

import com.example.clicknote.data.dao.CallRecordingDao
import com.example.clicknote.data.entity.CallRecordingEntity
import com.example.clicknote.data.entity.toCallRecordingEntity
import com.example.clicknote.domain.model.CallRecording
import com.example.clicknote.domain.repository.CallRecordingRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class CallRecordingRepositoryImpl @Inject constructor(
    private val dao: CallRecordingDao
) : CallRecordingRepository {

    override fun getAllCallRecordings(): Flow<List<CallRecording>> {
        return dao.getAllCallRecordings().map { entities ->
            entities.map { CallRecording.fromEntity(it) }
        }
    }

    override suspend fun getCallRecordingById(id: String): CallRecording? {
        return dao.getCallRecordingById(id)?.let { CallRecording.fromEntity(it) }
    }

    override suspend fun insertCallRecording(recording: CallRecording) {
        dao.insertCallRecording(recording.toCallRecordingEntity())
    }

    override suspend fun deleteCallRecording(id: String) {
        dao.deleteCallRecording(id)
    }

    override suspend fun deleteAllCallRecordings() {
        dao.deleteOldRecordings(System.currentTimeMillis())
    }

    override suspend fun updateCallRecording(recording: CallRecording) {
        dao.updateCallRecording(recording.toCallRecordingEntity())
    }

    override fun getCallRecordingsByDateRange(startDate: Long, endDate: Long): Flow<List<CallRecording>> {
        return dao.getCallRecordingsByPhoneNumber("").map { entities ->
            entities.map { CallRecording.fromEntity(it) }
        }
    }

    override fun searchCallRecordings(query: String): Flow<List<CallRecording>> {
        return dao.getTrash().map { entities ->
            entities.map { CallRecording.fromEntity(it) }
        }
    }
} 