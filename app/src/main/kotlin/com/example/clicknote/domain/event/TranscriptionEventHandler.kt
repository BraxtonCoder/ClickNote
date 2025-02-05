package com.example.clicknote.domain.event

import com.example.clicknote.domain.model.TranscriptionEvent
import kotlinx.coroutines.flow.Flow

interface TranscriptionEventHandler {
    val events: Flow<TranscriptionEvent>
    
    suspend fun handleTranscriptionStarted()
    suspend fun handleTranscriptionCompleted(text: String)
    suspend fun handleTranscriptionError(error: Throwable)
    suspend fun handleSpeakerDetected(speakerId: String)
    suspend fun handleSummaryGenerated(summary: String)
    suspend fun handleLanguageDetected(language: String)
    suspend fun handleProgressUpdate(progress: Float)
    suspend fun reset()
} 