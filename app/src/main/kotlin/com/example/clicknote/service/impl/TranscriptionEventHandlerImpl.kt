package com.example.clicknote.service.impl

import com.example.clicknote.service.TranscriptionEventHandler
import com.example.clicknote.domain.model.TranscriptionEvent
import kotlinx.coroutines.flow.*
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class TranscriptionEventHandlerImpl @Inject constructor() : TranscriptionEventHandler {
    private val _events = MutableSharedFlow<TranscriptionEvent>()
    override val events: Flow<TranscriptionEvent> = _events.asSharedFlow()

    override suspend fun handleEvent(event: TranscriptionEvent) {
        _events.emit(event)
    }

    override suspend fun clearEvents() {
        // No need to clear a SharedFlow, but we can reset any internal state if needed
    }
} 