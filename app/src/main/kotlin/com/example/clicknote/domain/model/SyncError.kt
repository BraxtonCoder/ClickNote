package com.example.clicknote.domain.model

data class SyncError(
    val id: String = java.util.UUID.randomUUID().toString(),
    val message: String,
    val timestamp: Long = System.currentTimeMillis(),
    val noteId: String? = null,
    val type: SyncErrorType = SyncErrorType.UNKNOWN
)

enum class SyncErrorType {
    NETWORK,
    STORAGE_FULL,
    AUTHENTICATION,
    PERMISSION_DENIED,
    RATE_LIMIT,
    UNKNOWN
} 