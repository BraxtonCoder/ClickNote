package com.example.clicknote.domain.model

/**
 * Represents the synchronization status of a note
 */
enum class SyncStatus {
    /**
     * Note needs to be synced to the cloud
     */
    PENDING,

    /**
     * Note is currently being synced
     */
    SYNCING,

    /**
     * Note has been successfully synced
     */
    SUCCESS,

    /**
     * Error occurred during sync
     */
    ERROR,

    /**
     * Note was created while offline
     */
    OFFLINE,

    /**
     * Note has been deleted
     */
    DELETED,

    /**
     * Note has been modified locally and needs to be synced
     */
    MODIFIED
} 