package com.example.clicknote.data.dao

import androidx.room.*
import com.example.clicknote.data.entity.TranscriptionSegment
import kotlinx.coroutines.flow.Flow

@Dao
interface TranscriptionSegmentDao {
    @Query("SELECT * FROM transcription_segments WHERE noteId = :noteId ORDER BY startTime ASC")
    fun getSegmentsByNoteId(noteId: String): Flow<List<TranscriptionSegment>>

    @Query("SELECT * FROM transcription_segments WHERE noteId = :noteId AND speakerId = :speakerId ORDER BY startTime ASC")
    fun getSegmentsBySpeaker(noteId: String, speakerId: String): Flow<List<TranscriptionSegment>>

    @Query("""
        SELECT * FROM transcription_segments 
        WHERE noteId = :noteId 
        AND content LIKE '%' || :query || '%' 
        ORDER BY startTime ASC
    """)
    fun searchSegments(noteId: String, query: String): Flow<List<TranscriptionSegment>>

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

    @Transaction
    @Query("""
        SELECT * FROM transcription_segments 
        WHERE noteId = :noteId 
        AND startTime >= :startTime 
        AND endTime <= :endTime 
        ORDER BY startTime ASC
    """)
    fun getSegmentsByTimeRange(
        noteId: String,
        startTime: Long,
        endTime: Long
    ): Flow<List<TranscriptionSegment>>

    @Query("""
        SELECT COUNT(*) FROM transcription_segments 
        WHERE noteId = :noteId 
        AND speakerId IS NOT NULL
    """)
    suspend fun getSegmentsWithSpeakerCount(noteId: String): Int

    @Query("SELECT DISTINCT speakerId FROM transcription_segments WHERE noteId = :noteId AND speakerId IS NOT NULL")
    suspend fun getUniqueSpeakerIds(noteId: String): List<String>
} 