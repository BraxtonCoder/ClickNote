package com.example.clicknote.model

data class RecordingState(
    val isRecording: Boolean = false,
    val isPaused: Boolean = false,
    val currentAmplitude: Int = 0,
    val recordingDuration: Long = 0L,
    val transcription: String? = null,
    val isTranscribing: Boolean = false,
    val error: String? = null,
    val audioFilePath: String? = null
) 