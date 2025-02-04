package com.example.clicknote.data.event

import com.example.clicknote.domain.event.TranscriptionEventDispatcher
import com.example.clicknote.domain.model.TranscriptionEvent
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.asSharedFlow
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class TranscriptionEventDispatcherImpl @Inject constructor() : TranscriptionEventDispatcher {
    private val _events = MutableSharedFlow<TranscriptionEvent>(replay = 1)
    override val events: Flow<TranscriptionEvent> = _events.asSharedFlow()

    override suspend fun emit(event: TranscriptionEvent) {
        _events.emit(event)
    }

    override suspend fun emitError(error: Throwable) {
        emit(TranscriptionEvent.Error(error.message ?: "Unknown error occurred"))
    }
} 