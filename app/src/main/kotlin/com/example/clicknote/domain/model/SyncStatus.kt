package com.example.clicknote.domain.model

enum class SyncStatus {
    PENDING,    // Note needs to be synced
    SYNCING,    // Note is currently being synced
    SYNCED,     // Note has been successfully synced
    FAILED,     // Sync failed
    OFFLINE,    // Note created while offline
    CONFLICT    // Note has sync conflicts that need resolution
} 