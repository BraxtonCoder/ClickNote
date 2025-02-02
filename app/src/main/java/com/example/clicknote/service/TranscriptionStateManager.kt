package com.example.clicknote.service

import kotlinx.coroutines.flow.StateFlow

interface TranscriptionStateManager {
    val isTranscribing: StateFlow<Boolean>
    val isOnline: StateFlow<Boolean>
    val transcriptionProgress: StateFlow<Float>
    
    fun setTranscribing(transcribing: Boolean)
    fun setOnline(online: Boolean)
    fun isOnline(): Boolean
    fun isTranscribing(): Boolean
    fun updateProgress(progress: Float)
} 