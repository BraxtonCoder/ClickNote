package com.example.clicknote.data.dao

import androidx.room.*
import com.example.clicknote.data.entity.TranscriptionTimestamp
import kotlinx.coroutines.flow.Flow

@Dao
interface TranscriptionTimestampDao {
    @Query("SELECT * FROM transcription_timestamps WHERE note_id = :noteId ORDER BY start_time ASC")
    fun getTimestampsForNote(noteId: String): Flow<List<TranscriptionTimestamp>>
    
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
    
    @Query("""
        SELECT * FROM transcription_timestamps 
        WHERE note_id = :noteId 
        AND text LIKE '%' || :query || '%' 
        ORDER BY start_time ASC
    """)
    fun searchTimestamps(noteId: String, query: String): Flow<List<TranscriptionTimestamp>>
} 