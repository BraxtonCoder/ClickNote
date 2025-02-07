package com.example.clicknote.domain.service

import kotlinx.coroutines.flow.StateFlow
import java.io.File

interface TranscriptionManager {
    val isTranscribing: StateFlow<Boolean>
    val currentFile: StateFlow<File?>

    /**
     * Transcribes an audio file and associates it with a note
     * @param file The audio file to transcribe
     * @param noteId The ID of the note to associate the transcription with
     */
    suspend fun transcribeAudio(file: File, noteId: String)

    /**
     * Cancels any ongoing transcription
     */
    suspend fun cancelTranscription()

    /**
     * Cleans up any resources used by the transcription manager
     */
    suspend fun cleanup()
} 