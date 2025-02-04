package com.example.clicknote.domain.model

enum class TranscriptionStatus {
    IDLE,
    INITIALIZING,
    RECORDING,
    TRANSCRIBING,
    PROCESSING,
    COMPLETED,
    ERROR,
    CANCELLED
} 