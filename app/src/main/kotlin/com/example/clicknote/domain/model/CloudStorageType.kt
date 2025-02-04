package com.example.clicknote.domain.model

enum class CloudStorageType {
    NONE,       // No cloud storage
    LOCAL,      // Device storage only
    LOCAL_CLOUD, // User's personal cloud storage (Google Drive, OneDrive, etc.)
    FIREBASE    // ClickNote's cloud storage (Firebase/Firestore)
} 