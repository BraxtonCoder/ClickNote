package com.example.clicknote.domain.model

/**
 * Represents the synchronization status of a note or other data
 */
enum class SyncStatus {
    IDLE,
    PENDING,
    SYNCING,
    IN_PROGRESS,
    COMPLETED,
    COMPLETED_WITH_ERRORS,
    SUCCESS,
    FAILED,
    CANCELLED,
    CONFLICT,
    OFFLINE,
    ERROR;

    companion object {
        fun fromString(status: String): SyncStatus {
            return try {
                valueOf(status.uppercase())
            } catch (e: IllegalArgumentException) {
                PENDING
            }
        }

        fun isTerminalState(status: SyncStatus): Boolean {
            return when (status) {
                COMPLETED, COMPLETED_WITH_ERRORS, FAILED, CANCELLED, CONFLICT, SUCCESS, ERROR -> true
                else -> false
            }
        }

        fun isErrorState(status: SyncStatus): Boolean {
            return when (status) {
                FAILED, CONFLICT, COMPLETED_WITH_ERRORS, ERROR -> true
                else -> false
            }
        }

        fun requiresRetry(status: SyncStatus): Boolean {
            return when (status) {
                FAILED, CONFLICT, ERROR -> true
                else -> false
            }
        }
    }
} 