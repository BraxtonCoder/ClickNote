package com.example.clicknote.domain.repository

import com.example.clicknote.domain.model.Note
import kotlinx.coroutines.flow.Flow

interface SyncRepository {
    /**
     * Get the current sync status as a flow
     */
    fun getSyncStatus(): Flow<SyncStatus>

    /**
     * Sync all pending notes to the cloud
     */
    suspend fun syncNotes(): Result<Unit>

    /**
     * Sync a specific note to the cloud
     */
    suspend fun syncNote(noteId: String): Result<Unit>

    /**
     * Pull notes from the cloud
     */
    suspend fun pullNotes(): Result<Unit>

    /**
     * Get notes that need syncing
     */
    fun getPendingNotes(): Flow<List<Note>>

    /**
     * Update sync status for a note
     */
    suspend fun updateNoteStatus(noteId: String, status: Int)

    /**
     * Schedule periodic sync
     */
    suspend fun schedulePeriodicSync()

    /**
     * Cancel periodic sync
     */
    suspend fun cancelPeriodicSync()

    /**
     * Get last sync time
     */
    fun getLastSyncTime(): Flow<Long>
}

enum class SyncStatus {
    IDLE,
    SYNCING,
    ERROR,
    SUCCESS
} 