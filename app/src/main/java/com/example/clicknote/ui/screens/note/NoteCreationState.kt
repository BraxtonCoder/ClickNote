package com.example.clicknote.ui.screens.note

data class NoteCreationState(
    val title: String = "",
    val content: String = "",
    val isRecording: Boolean = false,
    val isTranscribing: Boolean = false,
    val audioAmplitudes: List<Float> = emptyList(),
    val transcriptionProgress: Float = 0f,
    val hasAudioFile: Boolean = false,
    val error: String? = null,
    val isSaving: Boolean = false
) 