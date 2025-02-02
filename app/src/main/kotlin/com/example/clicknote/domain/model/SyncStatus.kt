package com.example.clicknote.domain.model

enum class SyncStatus {
    IDLE,           // No sync in progress
    SYNCING,        // Sync in progress
    ERROR,          // Sync error occurred
    OFFLINE,        // Device is offline
    STORAGE_FULL,   // Cloud storage is full
    UNAUTHORIZED,   // User is not authorized
    DISABLED,       // Sync is disabled by user
    CONFLICT,       // Sync conflict detected
    COMPLETED       // Sync completed successfully
} 