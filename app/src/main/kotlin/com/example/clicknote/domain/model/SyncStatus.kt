package com.example.clicknote.domain.model

/**
 * Represents the synchronization status of a note or other data
 */
enum class SyncStatus {
    PENDING,    // Waiting to be synced
    IN_PROGRESS,
    COMPLETED,
    FAILED,     // Sync failed
    CANCELLED,
    CONFLICT,   // Sync conflict detected
    OFFLINE,    // Offline, will sync when online
    SYNCED;     // Successfully synced with cloud

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