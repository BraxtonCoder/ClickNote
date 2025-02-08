package com.example.clicknote.domain.model

enum class SyncErrorType {
    UNKNOWN,
    NETWORK,
    PERMISSION,
    CONFLICT,
    STORAGE,
    AUTHENTICATION,
    SERVER,
    TIMEOUT,
    QUOTA_EXCEEDED
} 