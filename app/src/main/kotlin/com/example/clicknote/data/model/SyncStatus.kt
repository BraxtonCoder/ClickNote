package com.example.clicknote.data.model

enum class SyncStatus {
    PENDING,    // Not yet synced to cloud
    SYNCING,    // Currently being synced
    SYNCED,     // Successfully synced to cloud
    FAILED,     // Sync failed
    CONFLICT    // Conflict detected during sync
} 