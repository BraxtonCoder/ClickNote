package com.example.clicknote.domain.model

/**
 * Represents the synchronization status of a note or other data
 */
enum class SyncStatus {
    PENDING,    // Waiting to be synced
    SYNCING,    // Currently being synced
    SYNCED,     // Successfully synced
    FAILED,     // Sync failed
    CONFLICT,   // Sync conflict detected
    OFFLINE;    // Offline, will sync when online

    companion object {
        fun fromString(status: String): SyncStatus {
            return try {
                valueOf(status.uppercase())
            } catch (e: IllegalArgumentException) {
                PENDING
            }
        }
    }
} 