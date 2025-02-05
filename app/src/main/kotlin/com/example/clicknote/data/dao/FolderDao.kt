package com.example.clicknote.data.dao

import androidx.room.*
import com.example.clicknote.data.entity.FolderEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface FolderDao {
    @Query("SELECT * FROM folders WHERE is_deleted = 0 ORDER BY sort_order ASC, name ASC")
    fun getAllFolders(): Flow<List<FolderEntity>>

    @Query("SELECT * FROM folders WHERE is_deleted = 1 ORDER BY deleted_at DESC")
    fun getDeletedFolders(): Flow<List<FolderEntity>>

    @Query("SELECT * FROM folders WHERE id = :id")
    suspend fun getFolderById(id: String): FolderEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(folder: FolderEntity)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(folders: List<FolderEntity>)

    @Update
    suspend fun update(folder: FolderEntity)

    @Update
    suspend fun updateAll(folders: List<FolderEntity>)

    @Query("UPDATE folders SET is_deleted = 1, deleted_at = :timestamp WHERE id = :id")
    suspend fun moveToTrash(id: String, timestamp: Long = System.currentTimeMillis())

    @Query("UPDATE folders SET is_deleted = 0, deleted_at = NULL WHERE id = :id")
    suspend fun restoreFromTrash(id: String)

    @Query("DELETE FROM folders WHERE id = :id")
    suspend fun delete(id: String)

    @Query("DELETE FROM folders WHERE is_deleted = 1 AND deleted_at < :timestamp")
    suspend fun deleteExpiredFolders(timestamp: Long)

    @Query("SELECT EXISTS(SELECT 1 FROM folders WHERE name = :name AND is_deleted = 0)")
    suspend fun folderExists(name: String): Boolean

    @Query("SELECT COUNT(*) FROM folders WHERE is_deleted = 0")
    fun getTotalFoldersCount(): Int

    @Query("SELECT COUNT(*) FROM folders WHERE is_deleted = 1")
    fun getDeletedFoldersCount(): Int

    @Query("SELECT * FROM folders WHERE sync_status = :status")
    suspend fun getFoldersBySyncStatus(status: Int): List<FolderEntity>

    @Query("UPDATE folders SET sync_status = :status WHERE id IN (:ids)")
    suspend fun updateSyncStatus(ids: List<String>, status: Int)

    @Query("UPDATE folders SET sort_order = :sortOrder WHERE id = :id")
    suspend fun updateSortOrder(id: String, sortOrder: Int)

    @Query("UPDATE folders SET name = :name WHERE id = :id")
    suspend fun updateName(id: String, name: String)

    @Query("UPDATE folders SET color = :color WHERE id = :id")
    suspend fun updateColor(id: String, color: Int)

    @Query("SELECT * FROM folders WHERE name LIKE '%' || :query || '%' AND is_deleted = 0 ORDER BY sort_order ASC, name ASC")
    fun searchFolders(query: String): Flow<List<FolderEntity>>
} 