package com.example.clicknote.service

import com.example.clicknote.domain.model.Note
import kotlinx.coroutines.flow.Flow

interface CloudSyncService {
    suspend fun syncNotes()
    suspend fun uploadNote(note: Note)
    suspend fun downloadNote(noteId: String): Note?
    suspend fun deleteNote(noteId: String)
    fun getSyncStatus(): Flow<SyncStatus>
    suspend fun enableSync(enabled: Boolean)
    suspend fun cleanup()
}

enum class SyncStatus {
    IDLE,
    SYNCING,
    ERROR,
    SUCCESS
} 