package com.example.clicknote.domain.interfaces

import kotlinx.coroutines.flow.StateFlow
import java.io.File

interface TranscriptionStateManager {
    val isTranscribing: StateFlow<Boolean>
    val currentFile: StateFlow<File?>
    val isOfflineMode: StateFlow<Boolean>
    
    suspend fun setTranscribing(isTranscribing: Boolean)
    suspend fun setCurrentFile(file: File?)
    suspend fun setOfflineMode(isOffline: Boolean)
    suspend fun cleanup()
} 