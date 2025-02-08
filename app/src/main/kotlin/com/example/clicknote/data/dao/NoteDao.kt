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
    @Query("SELECT * FROM notes WHERE is_deleted = 0 ORDER BY modified_at DESC")
    fun getAllNotes(): Flow<List<NoteEntity>>

    @Query("SELECT * FROM notes WHERE folder_id = :folderId AND is_deleted = 0 ORDER BY modified_at DESC")
    fun getNotesInFolder(folderId: String): Flow<List<NoteEntity>>

    @Query("SELECT * FROM notes WHERE is_deleted = 1 ORDER BY modified_at DESC")
    fun getDeletedNotes(): Flow<List<NoteEntity>>

    @Query("SELECT * FROM notes WHERE (title LIKE '%' || :query || '%' OR content LIKE '%' || :query || '%') AND is_deleted = 0 ORDER BY modified_at DESC")
    fun searchNotes(query: String): Flow<List<NoteEntity>>

    @Query("SELECT * FROM notes WHERE id = :id")
    suspend fun getNoteById(id: String): NoteEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertNote(note: NoteEntity)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(notes: List<NoteEntity>)

    @Update
    suspend fun updateNote(note: NoteEntity)

    @Query("DELETE FROM notes WHERE id = :id")
    suspend fun delete(id: String)

    @Query("UPDATE notes SET is_deleted = 1, deleted_at = :deletedAt WHERE id = :id")
    suspend fun moveToTrash(id: String, deletedAt: Long)

    @Query("UPDATE notes SET is_deleted = 0, deleted_at = NULL WHERE id = :id")
    suspend fun restoreFromTrash(id: String)

    @Query("UPDATE notes SET folder_id = :folderId WHERE id = :noteId")
    suspend fun updateNoteFolder(noteId: String, folderId: String?)

    @Query("UPDATE notes SET is_pinned = :isPinned WHERE id = :noteId")
    suspend fun updatePinned(noteId: String, isPinned: Boolean)

    @Query("DELETE FROM notes WHERE is_deleted = 1 AND deleted_at < :expirationDate")
    suspend fun deleteExpiredNotes(expirationDate: Long)

    @Query("SELECT * FROM notes WHERE is_deleted = 0 ORDER BY created_at DESC")
    fun getActiveNotes(): Flow<List<NoteEntity>>

    @Query("SELECT * FROM notes WHERE folder_id = :folderId AND is_deleted = 0 ORDER BY created_at DESC")
    fun getNotesByFolder(folderId: String): Flow<List<NoteEntity>>

    @Query("SELECT * FROM notes WHERE sync_status = :status")
    fun getNotesByStatus(status: String): Flow<List<NoteEntity>>

    @Query("""
        SELECT * FROM notes 
        WHERE sync_status = :status
        ORDER BY modified_at DESC
    """)
    fun getSyncingNotes(status: String = SyncStatus.IN_PROGRESS.name): Flow<List<NoteEntity>>

    @Query("""
        SELECT * FROM notes 
        WHERE sync_status = :status
        ORDER BY modified_at DESC
    """)
    fun getSyncedNotes(status: String = SyncStatus.COMPLETED.name): Flow<List<NoteEntity>>

    @Query("""
        SELECT * FROM notes 
        WHERE sync_status = :status
        ORDER BY modified_at DESC
    """)
    fun getFailedSyncNotes(status: String = SyncStatus.FAILED.name): Flow<List<NoteEntity>>

    @Query("SELECT COUNT(*) FROM notes WHERE sync_status = :status")
    suspend fun getSyncedNoteCount(status: String = SyncStatus.COMPLETED.name): Int

    @Query("UPDATE notes SET sync_status = :status WHERE id = :noteId")
    suspend fun updateSyncStatus(noteId: String, status: String)

    @Query("UPDATE notes SET sync_status = :status WHERE id IN (:noteIds)")
    suspend fun updateSyncStatus(noteIds: List<String>, status: String)

    @Query("SELECT * FROM note_entity WHERE sync_status = :status")
    suspend fun getNotesBySyncStatus(status: SyncStatus): List<NoteEntity>

    @Transaction
    suspend fun syncNotes(serverNotes: List<NoteEntity>) {
        val localNotes = getNotesBySyncStatus(SyncStatus.PENDING)
        updateSyncStatus(localNotes.map { it.id }, SyncStatus.IN_PROGRESS.name)

        try {
            insertAll(serverNotes)
            updateSyncStatus(localNotes.map { it.id }, SyncStatus.COMPLETED.name)
        } catch (e: Exception) {
            updateSyncStatus(localNotes.map { it.id }, SyncStatus.FAILED.name)
            throw e
        }
    }

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
} 