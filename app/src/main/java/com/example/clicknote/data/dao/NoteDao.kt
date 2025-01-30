package com.example.clicknote.data.dao

import androidx.room.*
import androidx.room.RoomWarnings
import com.example.clicknote.data.entity.NoteEntity
import com.example.clicknote.data.entity.NoteWithFolderEntity
import com.example.clicknote.data.model.SyncStatus
import kotlinx.coroutines.flow.Flow

@Dao
interface NoteDao {
    @Transaction
    @Query("""
        SELECT * FROM notes 
        WHERE is_deleted = 0 
        ORDER BY is_pinned DESC, created_at DESC
    """)
    fun getAllNotes(): Flow<List<NoteEntity>>
    
    @Transaction
    @Query("""
        SELECT * FROM notes 
        WHERE folder_id = :folderId 
        AND is_deleted = 0 
        ORDER BY is_pinned DESC, created_at DESC
    """)
    fun getNotesInFolder(folderId: String): Flow<List<NoteEntity>>
    
    @Transaction
    @Query("""
        SELECT * FROM notes 
        WHERE (title LIKE '%' || :query || '%' OR content LIKE '%' || :query || '%')
        AND is_deleted = 0 
        ORDER BY is_pinned DESC, created_at DESC
    """)
    fun searchNotes(query: String): Flow<List<NoteEntity>>
    
    @Transaction
    @Query("""
        SELECT * FROM notes 
        WHERE is_deleted = 1 
        ORDER BY deleted_at DESC
    """)
    fun getDeletedNotes(): Flow<List<NoteEntity>>

    @Transaction
    @Query("SELECT * FROM notes WHERE id = :id")
    suspend fun getNoteById(id: String): NoteEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(note: NoteEntity): Long
    
    @Update
    suspend fun update(note: NoteEntity)
    
    @Query("UPDATE notes SET is_deleted = 1, deleted_at = :timestamp WHERE id = :id")
    suspend fun moveToTrash(id: String, timestamp: Long = System.currentTimeMillis())
    
    @Query("UPDATE notes SET is_deleted = 0, deleted_at = NULL WHERE id = :id")
    suspend fun restoreFromTrash(id: String)
    
    @Query("DELETE FROM notes WHERE id = :id")
    suspend fun delete(id: String)
    
    @Query("DELETE FROM notes WHERE is_deleted = 1 AND deleted_at <= :timestamp")
    suspend fun deleteExpiredNotes(timestamp: Long)
    
    @Query("UPDATE notes SET folder_id = :folderId WHERE id IN (:noteIds)")
    suspend fun moveNotesToFolder(noteIds: List<String>, folderId: String?)
    
    @Query("UPDATE notes SET is_pinned = :isPinned WHERE id = :id")
    suspend fun updatePinned(id: String, isPinned: Boolean)
    
    @Transaction
    @Query("""
        SELECT * FROM notes 
        WHERE created_at BETWEEN :startTimestamp AND :endTimestamp
        AND is_deleted = 0
        ORDER BY created_at DESC
    """)
    fun getNotesByDateRange(startTimestamp: Long, endTimestamp: Long): Flow<List<NoteEntity>>

    @Query("""
        SELECT COUNT(*) FROM notes 
        WHERE created_at BETWEEN :startTimestamp AND :endTimestamp
        AND is_deleted = 0
    """)
    fun getNotesCountInDateRange(startTimestamp: Long, endTimestamp: Long): Int

    @Query("SELECT COUNT(*) FROM notes WHERE is_deleted = 0")
    fun getTotalNotesCount(): Int

    @Transaction
    @Query("""
        SELECT n.* FROM notes n 
        LEFT JOIN folders f ON n.folder_id = f.id 
        WHERE n.is_deleted = 0 
        ORDER BY n.is_pinned DESC, n.created_at DESC
    """)
    fun getAllNotesWithFolders(): Flow<List<NoteWithFolderEntity>>

    @Transaction
    @Query("""
        SELECT n.* FROM notes n 
        LEFT JOIN folders f ON n.folder_id = f.id 
        WHERE (n.title LIKE '%' || :query || '%' OR n.content LIKE '%' || :query || '%')
        AND n.is_deleted = 0 
        ORDER BY n.is_pinned DESC, n.created_at DESC
    """)
    fun searchNotesWithFolders(query: String): Flow<List<NoteWithFolderEntity>>

    @Query("SELECT * FROM notes WHERE sync_status = :status")
    suspend fun getNotesBySyncStatus(status: SyncStatus): List<NoteEntity>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertNote(note: NoteEntity)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertNotes(notes: List<NoteEntity>)

    @Update
    suspend fun updateNote(note: NoteEntity)

    @Update
    suspend fun updateNotes(notes: List<NoteEntity>)

    @Query("UPDATE notes SET sync_status = :status WHERE id IN (:noteIds)")
    suspend fun updateSyncStatus(noteIds: List<String>, status: SyncStatus)

    @Query("UPDATE notes SET is_deleted = 1, deleted_at = :timestamp WHERE id = :id")
    suspend fun softDeleteNote(id: String, timestamp: Long = System.currentTimeMillis())

    @Query("UPDATE notes SET is_deleted = 0, deleted_at = NULL WHERE id = :id")
    suspend fun restoreNote(id: String)

    @Query("DELETE FROM notes WHERE id = :id")
    suspend fun hardDeleteNote(id: String)

    @Query("DELETE FROM notes WHERE is_deleted = 1 AND deleted_at < :timestamp")
    suspend fun deleteOldNotes(timestamp: Long)

    @Transaction
    suspend fun syncNotes(serverNotes: List<NoteEntity>) {
        // Update sync status for all notes being processed
        val localNotes = getNotesBySyncStatus(SyncStatus.PENDING)
        updateSyncStatus(localNotes.map { it.id }, SyncStatus.SYNCING)

        try {
            // Insert or update server notes
            insertNotes(serverNotes)
            
            // Update sync status to SYNCED
            updateSyncStatus(localNotes.map { it.id }, SyncStatus.SYNCED)
        } catch (e: Exception) {
            // Update sync status to ERROR for failed notes
            updateSyncStatus(localNotes.map { it.id }, SyncStatus.ERROR)
            throw e
        }
    }

    @Query("SELECT * FROM notes WHERE sync_status = :status")
    fun getNotesBySyncStatus(status: Int): Flow<List<NoteEntity>>

    @Query("SELECT * FROM notes WHERE sync_status = :status")
    suspend fun getNotesBySyncStatusSuspend(status: Int): List<NoteEntity>

    @Transaction
    @Query("SELECT * FROM notes WHERE id = :id")
    suspend fun getNoteWithFolder(id: String): NoteWithFolderEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(notes: List<NoteEntity>)

    @Query("UPDATE notes SET folder_id = :folderId WHERE id = :noteId")
    suspend fun updateNoteFolder(noteId: String, folderId: String?)

    @Query("UPDATE notes SET is_pinned = :isPinned WHERE id = :noteId")
    suspend fun updateNotePinned(noteId: String, isPinned: Boolean)

    @Query("UPDATE notes SET sync_status = :status WHERE id = :noteId")
    suspend fun updateSyncStatus(noteId: String, status: Int)

    @Query("SELECT COUNT(*) FROM notes WHERE is_deleted = 1")
    fun getDeletedNotesCount(): Flow<Int>

    @Query("""
        SELECT * FROM notes 
        WHERE is_deleted = 0 
        AND (title LIKE '%' || :query || '%' OR content LIKE '%' || :query || '%')
        ORDER BY is_pinned DESC, created_at DESC
    """)
    fun searchNotes(query: String): Flow<List<NoteEntity>>

    @Query("""
        SELECT * FROM notes 
        WHERE is_deleted = 0 
        AND created_at BETWEEN :startTime AND :endTime
        ORDER BY is_pinned DESC, created_at DESC
    """)
    fun getNotesByDateRange(startTime: Long, endTime: Long): Flow<List<NoteEntity>>
} 