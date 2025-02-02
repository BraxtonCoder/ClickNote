package com.example.clicknote.domain.event

import com.example.clicknote.domain.model.TranscriptionEvent
import kotlinx.coroutines.flow.Flow

interface TranscriptionEventDispatcher {
    val events: Flow<TranscriptionEvent>
    suspend fun emit(event: TranscriptionEvent)
    suspend fun emitError(error: Throwable)
} 