package com.example.clicknote.data.dao

import androidx.room.*
import com.example.clicknote.data.entity.TranscriptionMetadata
import kotlinx.coroutines.flow.Flow
import java.time.LocalDateTime

@Dao
interface TranscriptionMetadataDao {
    @Query("""
        SELECT * FROM transcription_metadata 
        WHERE is_deleted = 0 
        ORDER BY created_at DESC
    """)
    fun getAllMetadata(): Flow<List<TranscriptionMetadata>>

    @Query("""
        SELECT * FROM transcription_metadata 
        WHERE note_id = :noteId AND is_deleted = 0
    """)
    suspend fun getMetadataForNote(noteId: String): TranscriptionMetadata?

    @Query("""
        SELECT * FROM transcription_metadata 
        WHERE is_deleted = 0 AND created_at >= :timestamp 
        ORDER BY created_at DESC
    """)
    fun getRecentMetadata(timestamp: LocalDateTime): Flow<List<TranscriptionMetadata>>

    @Query("SELECT COUNT(*) FROM transcription_metadata WHERE is_deleted = 0")
    suspend fun getTranscriptionCount(): Int

    @Query("SELECT AVG(confidence_score) FROM transcription_metadata WHERE is_deleted = 0")
    suspend fun getAverageConfidence(): Float?

    @Query("SELECT AVG(processing_time) FROM transcription_metadata WHERE is_deleted = 0")
    suspend fun getAverageProcessingTime(): Long?

    @Query("""
        SELECT tm.* FROM transcription_metadata tm
        INNER JOIN note_entity n ON tm.note_id = n.id
        WHERE tm.is_deleted = 0 AND n.folder_id = :folderId
        ORDER BY tm.created_at DESC
    """)
    fun getMetadataByFolder(folderId: String): Flow<List<TranscriptionMetadata>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(metadata: TranscriptionMetadata)

    @Update
    suspend fun update(metadata: TranscriptionMetadata)

    @Query("UPDATE transcription_metadata SET is_deleted = 1 WHERE id = :id")
    suspend fun delete(id: String)

    @Query("UPDATE transcription_metadata SET is_deleted = 1 WHERE note_id = :noteId")
    suspend fun deleteByNoteId(noteId: String)

    @Query("SELECT * FROM transcription_metadata WHERE createdAt BETWEEN :startDate AND :endDate")
    suspend fun getMetadataInDateRange(startDate: LocalDateTime, endDate: LocalDateTime): List<TranscriptionMetadata>

    @Query("SELECT * FROM transcription_metadata WHERE id = :id")
    suspend fun getMetadataById(id: String): TranscriptionMetadata?

    @Query("DELETE FROM transcription_metadata WHERE id = :id")
    suspend fun deleteById(id: String)

    @Query("UPDATE transcription_metadata SET status = :status WHERE noteId = :noteId")
    suspend fun updateStatus(noteId: String, status: String)

    @Query("UPDATE transcription_metadata SET processingTime = :processingTime WHERE noteId = :noteId")
    suspend fun updateProcessingTime(noteId: String, processingTime: Long)
} 