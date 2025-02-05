package com.example.clicknote.data.event

import com.example.clicknote.domain.event.TranscriptionEventHandler
import com.example.clicknote.domain.model.TranscriptionEvent
import com.example.clicknote.domain.model.Summary
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.asSharedFlow
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class TranscriptionEventHandlerImpl @Inject constructor() : TranscriptionEventHandler {

    private val _events = MutableSharedFlow<TranscriptionEvent>()
    override val events: Flow<TranscriptionEvent> = _events.asSharedFlow()

    override suspend fun handleTranscriptionStarted() {
        _events.emit(TranscriptionEvent.TranscriptionStarted)
    }

    override suspend fun handleTranscriptionCompleted(text: String) {
        _events.emit(TranscriptionEvent.TranscriptionCompleted(
            result = com.example.clicknote.domain.model.TranscriptionResult(
                text = text,
                confidence = 1.0f,
                language = "en",
                segments = emptyList(),
                speakers = emptyMap(),
                duration = 0L,
                wordCount = text.split(" ").size,
                timestamp = System.currentTimeMillis()
            )
        ))
    }

    override suspend fun handleTranscriptionError(error: Throwable) {
        _events.emit(TranscriptionEvent.Error(error.message ?: "Unknown error"))
    }

    override suspend fun handleSpeakerDetected(speakerId: String) {
        _events.emit(TranscriptionEvent.SpeakerDetectionCompleted(1))
    }

    override suspend fun handleSummaryGenerated(summary: String) {
        _events.emit(TranscriptionEvent.SummaryGenerationCompleted(
            Summary(
                id = java.util.UUID.randomUUID().toString(),
                noteId = java.util.UUID.randomUUID().toString(),
                content = summary,
                wordCount = summary.split(" ").size,
                sourceWordCount = 0
            )
        ))
    }

    override suspend fun handleLanguageDetected(language: String) {
        _events.emit(TranscriptionEvent.LanguageDetected(language))
    }

    override suspend fun handleProgressUpdate(progress: Float) {
        _events.emit(TranscriptionEvent.Progress(progress))
    }

    override suspend fun reset() {
        // No need to emit any event for reset
    }
} 