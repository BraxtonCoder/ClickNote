package com.example.clicknote.domain.service

import com.example.clicknote.domain.model.Note
import kotlinx.coroutines.flow.Flow

interface CloudSyncService {
    suspend fun syncNotes(): Result<Unit>
    suspend fun uploadNote(note: Note): Result<Unit>
    suspend fun downloadNote(noteId: String): Result<Note>
    suspend fun deleteNote(noteId: String): Result<Unit>
    fun getSyncProgress(): Flow<Float>
    fun isSyncing(): Boolean
    fun cancelSync()
    suspend fun getLastSyncTime(): Long
    suspend fun setSyncEnabled(enabled: Boolean)
    fun isSyncEnabled(): Flow<Boolean>
} 