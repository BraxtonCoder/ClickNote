package com.example.clicknote.data.entity

enum class TranscriptionState {
    IDLE,           // Initial state
    RECORDING,      // Currently recording
    PAUSED,         // Recording paused
    PROCESSING,     // Processing/transcribing
    COMPLETED,      // Successfully transcribed
    ERROR,          // Error occurred
    CANCELLED       // Recording cancelled
} 