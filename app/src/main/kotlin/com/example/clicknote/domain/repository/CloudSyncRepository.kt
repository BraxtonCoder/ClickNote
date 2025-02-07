package com.example.clicknote.domain.repository

import com.example.clicknote.domain.model.*
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.StateFlow
import java.io.File

interface CloudSyncRepository {
    val isSyncing: Flow<Boolean>
    val syncProgress: Flow<Float>
    val syncErrors: Flow<List<SyncError>>
    val cloudStorageType: Flow<CloudStorageType>
    val syncStatus: StateFlow<SyncStatus>

    suspend fun getCloudStoragePreference(): CloudStorageType
    suspend fun setCloudStoragePreference(type: CloudStorageType)
    suspend fun startSync()
    suspend fun schedulePeriodicSync(intervalMinutes: Long)
    suspend fun cancelPeriodicSync()
    suspend fun addPendingNote(note: Note)
    suspend fun getPendingNotes(): List<Note>
    suspend fun clearPendingNotes()
    suspend fun getStorageUsage(): Long
    suspend fun getStorageLimit(): Long
    suspend fun syncNote(noteId: String): Result<Unit>
    suspend fun syncAudio(audioFile: File): Result<String>
} 