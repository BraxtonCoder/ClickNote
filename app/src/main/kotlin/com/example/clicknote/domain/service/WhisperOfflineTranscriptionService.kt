package com.example.clicknote.domain.service

import com.example.clicknote.domain.model.TranscriptionSegment
import com.example.clicknote.domain.model.Speaker
import kotlinx.coroutines.flow.Flow
import java.io.File

interface WhisperOfflineTranscriptionService {
    val transcriptionProgress: Flow<Float>
    val detectedSpeakers: Flow<List<Speaker>>

    suspend fun transcribe(audioFile: File, detectSpeakers: Boolean = false): String
    fun startRealtimeTranscription(): Flow<TranscriptionSegment>
    suspend fun stopRealtimeTranscription()
} 