package com.example.clicknote.data.entity

enum class NoteSource {
    NOTE,       // Regular note created in-app
    CALL,       // Note from call recording
    IMPORT,     // Imported audio file
    VOICE,      // Voice recording from accessibility service
    UNKNOWN     // Default/fallback value
} 