package com.example.clicknote.data.dao

import androidx.room.*
import com.example.clicknote.data.entity.TranscriptionSegmentEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface TranscriptionSegmentDao {
    @Query("SELECT * FROM transcription_segments")
    fun getAllSegments(): Flow<List<TranscriptionSegmentEntity>>

    @Query("SELECT * FROM transcription_segments WHERE noteId = :noteId")
    fun getSegmentsForNote(noteId: String): Flow<List<TranscriptionSegmentEntity>>

    @Query("SELECT * FROM transcription_segments WHERE startTime BETWEEN :startTime AND :endTime")
    fun getSegmentsByTimeRange(startTime: Long, endTime: Long): Flow<List<TranscriptionSegmentEntity>>

    @Query("SELECT * FROM transcription_segments WHERE speakerId = :speakerId")
    fun getSegmentsBySpeaker(speakerId: String): Flow<List<TranscriptionSegmentEntity>>

    @Query("SELECT * FROM transcription_segments WHERE text LIKE '%' || :query || '%'")
    fun searchSegments(query: String): Flow<List<TranscriptionSegmentEntity>>

    @Query("SELECT COUNT(*) FROM transcription_segments")
    suspend fun getSegmentCount(): Int

    @Query("SELECT COUNT(*) FROM transcription_segments WHERE speakerId = :speakerId")
    suspend fun countSegmentsBySpeaker(speakerId: String): Int

    @Query("SELECT DISTINCT speakerId FROM transcription_segments WHERE noteId = :noteId AND speakerId IS NOT NULL")
    fun getUniqueSpeakers(noteId: String): Flow<List<String>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertSegment(segment: TranscriptionSegmentEntity)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertSegments(segments: List<TranscriptionSegmentEntity>)

    @Update
    suspend fun updateSegment(segment: TranscriptionSegmentEntity)

    @Delete
    suspend fun deleteSegment(segment: TranscriptionSegmentEntity)

    @Query("DELETE FROM transcription_segments WHERE noteId = :noteId")
    suspend fun deleteSegmentsForNote(noteId: String)

    @Query("UPDATE transcription_segments SET speakerId = :speakerId WHERE id = :segmentId")
    suspend fun updateSpeakerId(segmentId: String, speakerId: String)

    @Transaction
    suspend fun replaceSegmentsForNote(noteId: String, segments: List<TranscriptionSegmentEntity>) {
        deleteSegmentsForNote(noteId)
        insertSegments(segments)
    }
} 