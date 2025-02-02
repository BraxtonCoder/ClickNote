package com.example.clicknote.service

import com.example.clicknote.domain.model.Note
import com.example.clicknote.domain.model.SyncStatus
import kotlinx.coroutines.flow.Flow

interface CloudSyncService {
    suspend fun sync()
    suspend fun getSyncStatus(): SyncStatus
    suspend fun enableSync()
    suspend fun disableSync()
    suspend fun clearSyncData()
    suspend fun resolveConflict(noteId: String, useLocal: Boolean)
    suspend fun getLastSyncTime(): Long
    suspend fun uploadNote(note: Note)
    suspend fun downloadNote(noteId: String): Note?
    suspend fun deleteNote(noteId: String)
    suspend fun cleanup()
}

enum class SyncStatus {
    IDLE,
    SYNCING,
    ERROR,
    SUCCESS
} 