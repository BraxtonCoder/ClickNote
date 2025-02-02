package com.example.clicknote.domain.repository

import com.example.clicknote.domain.model.Note
import com.example.clicknote.domain.model.Folder
import com.example.clicknote.domain.model.SyncStatus
import kotlinx.coroutines.flow.Flow

interface CloudSyncRepository {
    /**
     * Sync status flow
     */
    val syncStatus: Flow<SyncStatus>

    /**
     * Start sync service
     */
    suspend fun startSync()

    /**
     * Stop sync service
     */
    suspend fun stopSync()

    /**
     * Force immediate sync
     */
    suspend fun syncNow()

    /**
     * Upload a note to cloud
     */
    suspend fun uploadNote(note: Note): Result<Unit>

    /**
     * Upload multiple notes to cloud
     */
    suspend fun uploadNotes(notes: List<Note>): Result<Unit>

    /**
     * Upload a folder to cloud
     */
    suspend fun uploadFolder(folder: Folder): Result<Unit>

    /**
     * Upload multiple folders to cloud
     */
    suspend fun uploadFolders(folders: List<Folder>): Result<Unit>

    /**
     * Download note from cloud
     */
    suspend fun downloadNote(noteId: String): Result<Note>

    /**
     * Download folder from cloud
     */
    suspend fun downloadFolder(folderId: String): Result<Folder>

    /**
     * Delete note from cloud
     */
    suspend fun deleteNote(noteId: String): Result<Unit>

    /**
     * Delete folder from cloud
     */
    suspend fun deleteFolder(folderId: String): Result<Unit>

    /**
     * Get cloud storage usage
     */
    suspend fun getStorageUsage(): Result<Long>

    /**
     * Get cloud storage limit
     */
    suspend fun getStorageLimit(): Result<Long>

    /**
     * Set cloud storage preference
     */
    suspend fun setCloudStoragePreference(enabled: Boolean)

    /**
     * Get cloud storage preference
     */
    fun getCloudStoragePreference(): Flow<Boolean>

    /**
     * Get last sync time
     */
    fun getLastSyncTime(): Flow<Long?>

    /**
     * Set sync interval
     */
    suspend fun setSyncInterval(intervalMinutes: Int)

    /**
     * Get sync interval
     */
    fun getSyncInterval(): Flow<Int>

    /**
     * Check if sync is in progress
     */
    fun isSyncing(): Flow<Boolean>

    /**
     * Get sync errors
     */
    fun getSyncErrors(): Flow<List<String>>

    /**
     * Clear sync errors
     */
    suspend fun clearSyncErrors()
} 