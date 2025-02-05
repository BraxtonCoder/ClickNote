package com.example.clicknote.domain.model

sealed class TranscriptionEvent {
    data class Started(val noteId: String) : TranscriptionEvent()
    data class Progress(val noteId: String, val progress: Float) : TranscriptionEvent()
    data class Completed(val noteId: String, val text: String) : TranscriptionEvent()
    data class Failed(val noteId: String, val error: String?) : TranscriptionEvent()
    data class AudioSaved(val noteId: String, val path: String) : TranscriptionEvent()
    data class AudioDeleted(val noteId: String) : TranscriptionEvent()
    data class Error(val message: String) : TranscriptionEvent()
} 