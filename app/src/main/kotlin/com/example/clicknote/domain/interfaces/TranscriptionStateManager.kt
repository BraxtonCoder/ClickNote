package com.example.clicknote.domain.interfaces

import com.example.clicknote.domain.model.TranscriptionState
import kotlinx.coroutines.flow.Flow
import java.io.File

interface TranscriptionStateManager {
    val currentState: Flow<TranscriptionState>
    val isRecording: Flow<Boolean>
    val amplitude: Flow<Float>
    val speakers: Flow<List<String>>
    val isTranscribing: Flow<Boolean>
    val currentFile: Flow<File?>
    val isOfflineMode: Flow<Boolean>

    suspend fun startRecording()
    suspend fun stopRecording()
    suspend fun pauseRecording()
    suspend fun resumeRecording()
    suspend fun cancelRecording()
    suspend fun updateAmplitude(value: Float)
    suspend fun updateSpeakers(speakers: List<String>)
    suspend fun setTranscribing(isTranscribing: Boolean)
    suspend fun setCurrentFile(file: File?)
    suspend fun reset()
} 