package com.example.clicknote.domain.model

sealed class TranscriptionEvent {
    object ServiceInitialized : TranscriptionEvent()
    object ServiceStopped : TranscriptionEvent()
    object TranscriptionStarted : TranscriptionEvent()
    data class TranscriptionCompleted(val result: TranscriptionResult) : TranscriptionEvent()
    object TranscriptionCancelled : TranscriptionEvent()
    object SummaryGenerationStarted : TranscriptionEvent()
    data class SummaryGenerationCompleted(val summary: Summary) : TranscriptionEvent()
    object SpeakerDetectionStarted : TranscriptionEvent()
    data class SpeakerDetectionCompleted(val speakerCount: Int) : TranscriptionEvent()
    data class Error(val message: String) : TranscriptionEvent()
    data class Progress(val value: Float) : TranscriptionEvent()
    data class LanguageDetected(val language: String) : TranscriptionEvent()
    data class SpeakersIdentified(val speakers: Map<String, String>) : TranscriptionEvent()
} 