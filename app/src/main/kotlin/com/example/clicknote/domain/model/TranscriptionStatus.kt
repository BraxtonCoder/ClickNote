package com.example.clicknote.domain.model

enum class TranscriptionStatus {
    IDLE,
    PENDING,
    PROCESSING,
    COMPLETED,
    ERROR,
    AUDIO_SAVED,
    AUDIO_DELETED
} 