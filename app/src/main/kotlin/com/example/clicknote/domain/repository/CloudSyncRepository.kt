package com.example.clicknote.domain.repository

import com.example.clicknote.domain.model.CloudStorageType
import com.example.clicknote.domain.model.Note
import com.example.clicknote.domain.model.SyncError
import kotlinx.coroutines.flow.Flow

interface CloudSyncRepository {
    val isSyncing: Flow<Boolean>
    val syncProgress: Flow<Float>
    val syncErrors: Flow<List<SyncError>>
    val cloudStorageType: Flow<CloudStorageType>

    suspend fun getCloudStoragePreference(): CloudStorageType
    suspend fun setCloudStoragePreference(type: CloudStorageType)
    
    suspend fun startSync()
    suspend fun cancelSync()
    suspend fun syncPendingChanges()
    
    suspend fun schedulePeriodicSync(intervalMinutes: Long)
    suspend fun cancelPeriodicSync()
    
    suspend fun syncNote(note: Note)
    suspend fun syncNotes(notes: List<Note>)
    suspend fun deleteNote(noteId: String)
    
    suspend fun addPendingNote(note: Note)
    suspend fun getPendingNotes(): List<Note>
    suspend fun clearPendingNotes()
    
    suspend fun getStorageUsage(): Long
    suspend fun getStorageLimit(): Long
} 