package com.example.clicknote.domain.model

enum class CloudStorageType {
    NONE,       // No cloud storage
    LOCAL,      // Local storage only
    LOCAL_CLOUD, // User's personal cloud storage
    FIREBASE    // Firebase cloud storage
} 