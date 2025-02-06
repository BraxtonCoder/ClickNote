package com.example.clicknote.data.dao

import androidx.room.*
import androidx.room.RoomWarnings
import com.example.clicknote.data.entity.NoteEntity
import com.example.clicknote.data.entity.NoteWithFolderEntity
import com.example.clicknote.domain.model.SyncStatus
import kotlinx.coroutines.flow.Flow
import java.time.LocalDateTime

@Dao
interface NoteDao {
    @Transaction
    @Query("""
        SELECT * FROM note_entity 
        WHERE is_deleted = 0 
        ORDER BY is_pinned DESC, created_at DESC
    """)
    fun getAllNotes(): Flow<List<NoteEntity>>
    
    @Transaction
    @Query("""
        SELECT * FROM note_entity 
        WHERE folder_id = :folderId 
        AND is_deleted = 0 
        ORDER BY is_pinned DESC, created_at DESC
    """)
    fun getNotesInFolder(folderId: String): Flow<List<NoteEntity>>
    
    @Transaction
    @Query("""
        SELECT * FROM note_entity 
        WHERE (title LIKE '%' || :query || '%' OR content LIKE '%' || :query || '%')
        AND is_deleted = 0 
        ORDER BY is_pinned DESC, created_at DESC
    """)
    fun searchNotes(query: String): Flow<List<NoteEntity>>
    
    @Transaction
    @Query("""
        SELECT * FROM note_entity 
        WHERE is_deleted = 1 
        ORDER BY deleted_at DESC
    """)
    fun getDeletedNotes(): Flow<List<NoteEntity>>

    @Transaction
    @Query("SELECT * FROM note_entity WHERE id = :id")
    suspend fun getNoteById(id: String): NoteEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertNote(note: NoteEntity): Long
    
    @Update
    suspend fun updateNote(note: NoteEntity)
    
    @Query("UPDATE note_entity SET is_deleted = 1, deleted_at = :timestamp WHERE id = :id")
    suspend fun moveToTrash(id: String, timestamp: LocalDateTime = LocalDateTime.now())
    
    @Query("UPDATE note_entity SET is_deleted = 0, deleted_at = NULL WHERE id = :id")
    suspend fun restoreFromTrash(id: String)
    
    @Query("DELETE FROM note_entity WHERE id = :id")
    suspend fun delete(id: String)
    
    @Query("DELETE FROM note_entity WHERE is_deleted = 1 AND deleted_at <= :timestamp")
    suspend fun deleteExpiredNotes(timestamp: LocalDateTime)
    
    @Query("UPDATE note_entity SET folder_id = :folderId WHERE id IN (:noteIds)")
    suspend fun moveNotesToFolder(noteIds: List<String>, folderId: String?)
    
    @Query("UPDATE note_entity SET is_pinned = :isPinned WHERE id = :id")
    suspend fun updatePinned(id: String, isPinned: Boolean)
    
    @Transaction
    @Query("""
        SELECT * FROM note_entity 
        WHERE created_at BETWEEN :startDate AND :endDate
        AND is_deleted = 0
        ORDER BY created_at DESC
    """)
    fun getNotesByDateRange(startDate: LocalDateTime, endDate: LocalDateTime): Flow<List<NoteEntity>>

    @Query("""
        SELECT COUNT(*) FROM note_entity 
        WHERE created_at BETWEEN :startDate AND :endDate
        AND is_deleted = 0
    """)
    fun getNotesCountInDateRange(startDate: LocalDateTime, endDate: LocalDateTime): Int

    @Query("SELECT COUNT(*) FROM note_entity WHERE is_deleted = 0")
    fun getTotalNotesCount(): Int

    @Transaction
    @Query("""
        SELECT * FROM note_entity 
        WHERE is_deleted = 0 
        ORDER BY is_pinned DESC, created_at DESC
    """)
    fun getAllNotesWithFolders(): Flow<List<NoteWithFolderEntity>>

    @Transaction
    @Query("""
        SELECT * FROM note_entity 
        WHERE (title LIKE '%' || :query || '%' OR content LIKE '%' || :query || '%')
        AND is_deleted = 0 
        ORDER BY is_pinned DESC, created_at DESC
    """)
    fun searchNotesWithFolders(query: String): Flow<List<NoteWithFolderEntity>>

    @Query("SELECT * FROM note_entity WHERE sync_status = :status")
    suspend fun getNotesBySyncStatus(status: SyncStatus): List<NoteEntity>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(notes: List<NoteEntity>)

    @Update
    suspend fun updateNotes(notes: List<NoteEntity>)

    @Query("UPDATE note_entity SET sync_status = :status WHERE id IN (:noteIds)")
    suspend fun updateSyncStatus(noteIds: List<String>, status: SyncStatus)

    @Query("UPDATE note_entity SET folder_id = :folderId WHERE id = :noteId")
    suspend fun updateNoteFolder(noteId: String, folderId: String?)

    @Query("SELECT COUNT(*) FROM note_entity WHERE is_deleted = 1")
    fun getDeletedNotesCount(): Flow<Int>

    @Query("""
        SELECT * FROM note_entity 
        WHERE is_deleted = 0 AND has_audio = 1 
        ORDER BY created_at DESC
    """)
    fun getNotesWithAudio(): Flow<List<NoteEntity>>

    @Query("""
        SELECT * FROM note_entity 
        WHERE is_deleted = 0 AND source = :source 
        ORDER BY created_at DESC
    """)
    fun getNotesBySource(source: String): Flow<List<NoteEntity>>

    @Transaction
    suspend fun syncNotes(serverNotes: List<NoteEntity>) {
        val localNotes = getNotesBySyncStatus(SyncStatus.PENDING)
        updateSyncStatus(localNotes.map { it.id }, SyncStatus.SYNCING)

        try {
            insertAll(serverNotes)
            updateSyncStatus(localNotes.map { it.id }, SyncStatus.SYNCED)
        } catch (e: Exception) {
            updateSyncStatus(localNotes.map { it.id }, SyncStatus.FAILED)
            throw e
        }
    }

    @Query("""
        SELECT * FROM note_entity 
        WHERE sync_status = :syncedStatus
        ORDER BY modified_at DESC
    """)
    fun getSyncedNotes(syncedStatus: String = SyncStatus.SYNCED.name): Flow<List<NoteEntity>>

    @Query("""
        SELECT * FROM note_entity 
        WHERE sync_status = :failedStatus
        ORDER BY modified_at DESC
    """)
    fun getFailedSyncNotes(failedStatus: String = SyncStatus.FAILED.name): Flow<List<NoteEntity>>

    @Query("UPDATE note_entity SET speakers = :speakers WHERE id = :noteId")
    suspend fun updateSpeakers(noteId: String, speakers: List<String>)

    @Query("UPDATE note_entity SET summary = :summary WHERE id = :noteId")
    suspend fun updateSummary(noteId: String, summary: String?)

    @Query("UPDATE note_entity SET key_points = :keyPoints WHERE id = :noteId")
    suspend fun updateKeyPoints(noteId: String, keyPoints: List<String>)
} 