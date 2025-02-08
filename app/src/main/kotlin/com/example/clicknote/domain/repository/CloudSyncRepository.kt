package com.example.clicknote.domain.repository

import com.example.clicknote.domain.model.Note
import com.example.clicknote.domain.model.SyncStatus
import com.example.clicknote.domain.model.SyncError
import com.example.clicknote.domain.model.CloudStorageType
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.StateFlow
import java.io.File

interface CloudSyncRepository {
    val isSyncing: StateFlow<Boolean>
    val syncProgress: StateFlow<Float>
    val syncErrors: StateFlow<List<SyncError>>
    val cloudStorageType: StateFlow<CloudStorageType>
    val syncStatus: StateFlow<SyncStatus>
    val isOnline: StateFlow<Boolean>
    val lastSyncTime: StateFlow<Long>

    suspend fun getCloudStoragePreference(): CloudStorageType
    suspend fun setCloudStoragePreference(type: CloudStorageType)
    suspend fun startSync()
    suspend fun startPeriodicSync(intervalMillis: Long)
    suspend fun stopPeriodicSync()
    suspend fun addPendingNote(note: Note)
    suspend fun getPendingNotes(): List<Note>
    suspend fun clearPendingNotes()
    suspend fun getStorageUsage(): Result<Long>
    suspend fun getStorageLimit(): Result<Long>
    suspend fun syncNote(noteId: String): Result<Unit>
    suspend fun syncAudio(audioFile: File): Result<String>
} 