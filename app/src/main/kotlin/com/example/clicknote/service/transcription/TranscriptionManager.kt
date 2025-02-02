package com.example.clicknote.service.transcription

interface TranscriptionManager {
    suspend fun processAudioData(buffer: ShortArray, size: Int)
    suspend fun finalizeTranscription(): String
    suspend fun reset()
} 