package com.example.clicknote.service

import kotlinx.coroutines.flow.Flow
import java.io.File

interface AudioEnhancer {
    suspend fun enhance(audioData: ByteArray): ByteArray
    suspend fun enhanceAudioFile(file: File): File
    suspend fun enhanceAudioStream(audioStream: Flow<ByteArray>): Flow<ByteArray>
    fun isAvailable(): Boolean
    suspend fun cleanup()
    fun setHighPassEnabled(enabled: Boolean)
    fun setVoiceClarityEnabled(enabled: Boolean)
} 