package com.example.clicknote.domain.service

import kotlinx.coroutines.flow.Flow

interface SyncService {
    suspend fun sync(): Result<Unit>
    fun getSyncStatus(): Flow<SyncStatus>
    fun isSyncing(): Boolean
    fun cancelSync()
    suspend fun getLastSyncTime(): Long
    suspend fun setSyncEnabled(enabled: Boolean)
    fun isSyncEnabled(): Flow<Boolean>
    suspend fun markAsSynced(id: String)
    suspend fun markAsPending(id: String)
    suspend fun getUnsynced(): List<String>
}

enum class SyncStatus {
    IDLE,
    SYNCING,
    COMPLETED,
    ERROR,
    CANCELLED
} 