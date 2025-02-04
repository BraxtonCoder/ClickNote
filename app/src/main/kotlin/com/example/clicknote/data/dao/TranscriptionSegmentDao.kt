package com.example.clicknote.data.dao

import androidx.room.*
import com.example.clicknote.data.entity.TranscriptionSegment
import kotlinx.coroutines.flow.Flow

@Dao
interface TranscriptionSegmentDao {
    @Query("SELECT * FROM transcription_segments ORDER BY startTime ASC")
    fun getAllSegments(): Flow<List<TranscriptionSegment>>

    @Query("SELECT * FROM transcription_segments WHERE speaker = :speakerId ORDER BY startTime ASC")
    fun getSegmentsBySpeaker(speakerId: String): Flow<List<TranscriptionSegment>>

    @Query("SELECT * FROM transcription_segments WHERE text LIKE '%' || :query || '%' ORDER BY startTime ASC")
    fun searchSegments(query: String): Flow<List<TranscriptionSegment>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertSegment(segment: TranscriptionSegment)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertSegments(segments: List<TranscriptionSegment>)

    @Update
    suspend fun updateSegment(segment: TranscriptionSegment)

    @Delete
    suspend fun deleteSegment(segment: TranscriptionSegment)

    @Query("DELETE FROM transcription_segments WHERE noteId = :noteId")
    suspend fun deleteSegmentsByNoteId(noteId: String)

    @Query("SELECT COUNT(*) FROM transcription_segments")
    suspend fun getSegmentCount(): Int

    @Query("SELECT * FROM transcription_segments WHERE startTime >= :startTime AND endTime <= :endTime ORDER BY startTime ASC")
    fun getSegmentsInTimeRange(startTime: Long, endTime: Long): Flow<List<TranscriptionSegment>>

    @Query("SELECT COUNT(*) FROM transcription_segments WHERE speaker = :speakerId")
    suspend fun countSegmentsBySpeaker(speakerId: String): Int

    @Query("SELECT DISTINCT speaker FROM transcription_segments WHERE noteId = :noteId AND speaker IS NOT NULL")
    fun getUniqueSpeakers(noteId: String): Flow<List<String>>
} 