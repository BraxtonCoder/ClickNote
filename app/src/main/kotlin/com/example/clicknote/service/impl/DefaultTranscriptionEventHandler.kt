package com.example.clicknote.service.impl

import com.example.clicknote.domain.interfaces.TranscriptionEventHandler
import com.example.clicknote.domain.model.TranscriptionEvent
import com.example.clicknote.domain.model.TranscriptionState
import kotlinx.coroutines.flow.*
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class DefaultTranscriptionEventHandler @Inject constructor() : TranscriptionEventHandler {
    private val _events = MutableSharedFlow<TranscriptionEvent>()
    override val events: Flow<TranscriptionEvent> = _events.asSharedFlow()

    private val _transcriptionState = MutableStateFlow<TranscriptionState>(TranscriptionState.Idle)
    val transcriptionState: StateFlow<TranscriptionState> = _transcriptionState.asStateFlow()

    override suspend fun handleEvent(event: TranscriptionEvent) {
        _events.emit(event)
        when (event) {
            is TranscriptionEvent.Started -> {
                _transcriptionState.value = TranscriptionState.Processing(0f)
            }
            is TranscriptionEvent.Progress -> {
                _transcriptionState.value = TranscriptionState.Processing(event.progress)
            }
            is TranscriptionEvent.Completed -> {
                _transcriptionState.value = TranscriptionState.Completed(event.text, event.duration)
            }
            is TranscriptionEvent.Error -> {
                _transcriptionState.value = TranscriptionState.Error(RuntimeException(event.message))
            }
            is TranscriptionEvent.Stopped -> {
                _transcriptionState.value = TranscriptionState.Cancelled()
            }
            is TranscriptionEvent.Paused -> {
                _transcriptionState.value = TranscriptionState.Paused
            }
            is TranscriptionEvent.Resumed -> {
                _transcriptionState.value = TranscriptionState.Recording
            }
            else -> {} // No state change needed for other events
        }
    }

    override suspend fun clearEvents() {
        // No need to clear SharedFlow as it doesn't buffer events by default
    }
} 