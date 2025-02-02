package com.example.clicknote.service

import com.example.clicknote.domain.model.TranscriptionEvent
import kotlinx.coroutines.flow.Flow

interface TranscriptionEventHandler {
    val events: Flow<TranscriptionEvent>
    suspend fun handleEvent(event: TranscriptionEvent)
    suspend fun clearEvents()
} 