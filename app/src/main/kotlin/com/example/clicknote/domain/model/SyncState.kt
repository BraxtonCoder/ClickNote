package com.example.clicknote.domain.model

sealed class SyncState {
    object Idle : SyncState()
    object Syncing : SyncState()
    data class Error(val message: String) : SyncState()
    data class Success(val timestamp: Long = System.currentTimeMillis()) : SyncState()
} 