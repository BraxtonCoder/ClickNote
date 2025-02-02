package com.example.clicknote.domain.service

import com.example.clicknote.service.TranscriptionSegment
import com.example.clicknote.service.Speaker
import kotlinx.coroutines.flow.Flow
import java.io.File

interface WhisperTranscriptionService {
    val transcriptionProgress: Flow<Float>
    val detectedSpeakers: Flow<List<Speaker>>

    suspend fun transcribe(audioFile: File, detectSpeakers: Boolean = false): String
    fun startRealtimeTranscription(): Flow<TranscriptionSegment>
    suspend fun stopRealtimeTranscription()
} 