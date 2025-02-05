package com.example.clicknote.domain.worker

import kotlinx.coroutines.flow.Flow
import java.io.File

interface CloudSyncWorker {
    suspend fun syncNote(noteId: String): Result<Unit>
    suspend fun syncAudio(audioFile: File): Result<String>
    suspend fun syncAll(): Result<Unit>
    fun getSyncProgress(): Flow<Float>
    fun cancelSync()
    suspend fun isSyncing(): Boolean
    suspend fun getLastSyncTime(): Long
    suspend fun getPendingSyncItems(): List<String>
} 