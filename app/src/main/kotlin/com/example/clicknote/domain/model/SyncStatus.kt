package com.example.clicknote.domain.model

/**
 * Represents the synchronization status of a note
 */
enum class SyncStatus {
    PENDING,    // Note needs to be synced
    SYNCING,    // Note is currently being synced
    SYNCED,     // Note has been successfully synced
    FAILED,     // Sync failed
    OFFLINE,    // Note created while offline
    CONFLICT,   // Note has sync conflicts that need resolution
    DELETED,    // Note has been deleted and needs to be synced
    STORAGE_FULL,   // Cloud storage quota exceeded
    UNAUTHORIZED,   // User not authorized to sync
    DISABLED,       // Sync disabled by user
    IDLE           // No sync activity needed
} 