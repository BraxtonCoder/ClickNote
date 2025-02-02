package com.example.clicknote.data.entity

enum class TranscriptionState {
    PENDING,        // Waiting to be transcribed
    IN_PROGRESS,    // Currently being transcribed
    COMPLETED,      // Successfully transcribed
    FAILED,         // Transcription failed
    CANCELLED       // Transcription was cancelled
} 