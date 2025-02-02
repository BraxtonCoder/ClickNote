package com.example.clicknote.data.dao

import androidx.room.*
import com.example.clicknote.data.entity.TranscriptionTimestamp
import kotlinx.coroutines.flow.Flow

@Dao
interface TranscriptionTimestampDao {
    @Query("""
        SELECT * FROM transcription_timestamps 
        WHERE note_id = :noteId 
        ORDER BY start_time ASC
    """)
    fun getTimestampsForNote(noteId: String): Flow<List<TranscriptionTimestamp>>

    @Query("""
        SELECT * FROM transcription_timestamps 
        WHERE note_id = :noteId AND speaker = :speaker 
        ORDER BY start_time ASC
    """)
    fun getTimestampsBySpeaker(noteId: String, speaker: String): Flow<List<TranscriptionTimestamp>>

    @Query("""
        SELECT * FROM transcription_timestamps 
        WHERE note_id = :noteId 
            AND start_time >= :startTime 
            AND end_time <= :endTime 
        ORDER BY start_time ASC
    """)
    fun getTimestampsInRange(
        noteId: String,
        startTime: Long,
        endTime: Long
    ): Flow<List<TranscriptionTimestamp>>

    @Query("""
        SELECT * FROM transcription_timestamps 
        WHERE note_id = :noteId 
            AND text LIKE '%' || :query || '%' 
        ORDER BY start_time ASC
    """)
    fun searchTimestamps(noteId: String, query: String): Flow<List<TranscriptionTimestamp>>

    @Query("SELECT COUNT(*) FROM transcription_timestamps WHERE note_id = :noteId")
    suspend fun getTimestampCount(noteId: String): Int

    @Query("SELECT AVG(confidence) FROM transcription_timestamps WHERE note_id = :noteId")
    suspend fun getAverageConfidence(noteId: String): Float?

    @Query("SELECT DISTINCT speaker FROM transcription_timestamps WHERE note_id = :noteId AND speaker IS NOT NULL")
    suspend fun getSpeakers(noteId: String): List<String>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(timestamp: TranscriptionTimestamp)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(timestamps: List<TranscriptionTimestamp>)

    @Update
    suspend fun update(timestamp: TranscriptionTimestamp)

    @Delete
    suspend fun delete(timestamp: TranscriptionTimestamp)

    @Query("DELETE FROM transcription_timestamps WHERE note_id = :noteId")
    suspend fun deleteAllForNote(noteId: String)

    @Transaction
    suspend fun replaceTimestamps(noteId: String, newTimestamps: List<TranscriptionTimestamp>) {
        deleteAllForNote(noteId)
        insertAll(newTimestamps)
    }
} 