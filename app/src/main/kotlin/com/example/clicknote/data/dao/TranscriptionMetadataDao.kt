package com.example.clicknote.data.dao

import androidx.room.*
import com.example.clicknote.data.entity.TranscriptionMetadata
import kotlinx.coroutines.flow.Flow

@Dao
interface TranscriptionMetadataDao {
    @Query("""
        SELECT * FROM transcription_metadata 
        WHERE note_id = :noteId
    """)
    fun getMetadataForNote(noteId: String): Flow<TranscriptionMetadata?>

    @Query("""
        SELECT * FROM transcription_metadata 
        ORDER BY created_at DESC
    """)
    fun getAllMetadata(): Flow<List<TranscriptionMetadata>>

    @Query("""
        SELECT * FROM transcription_metadata 
        WHERE created_at BETWEEN :startDate AND :endDate 
        ORDER BY created_at DESC
    """)
    fun getMetadataInDateRange(startDate: Long, endDate: Long): Flow<List<TranscriptionMetadata>>

    @Query("""
        SELECT COUNT(*) FROM transcription_metadata 
        WHERE created_at >= :since AND is_offline = :isOffline
    """)
    suspend fun getTranscriptionCount(since: Long, isOffline: Boolean): Int

    @Query("SELECT AVG(confidence_score) FROM transcription_metadata WHERE note_id IN (:noteIds)")
    suspend fun getAverageConfidenceScore(noteIds: List<String>): Float

    @Query("SELECT AVG(processing_time) FROM transcription_metadata WHERE model = :model")
    suspend fun getAverageProcessingTime(model: String): Long

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(metadata: TranscriptionMetadata)

    @Update
    suspend fun update(metadata: TranscriptionMetadata)

    @Delete
    suspend fun delete(metadata: TranscriptionMetadata)

    @Query("DELETE FROM transcription_metadata WHERE note_id = :noteId")
    suspend fun deleteByNoteId(noteId: String)

    @Query("""
        SELECT tm.* FROM transcription_metadata tm
        INNER JOIN notes n ON tm.note_id = n.id
        WHERE n.folder_id = :folderId
        ORDER BY tm.created_at DESC
    """)
    fun getMetadataForFolder(folderId: String): Flow<List<TranscriptionMetadata>>
} 