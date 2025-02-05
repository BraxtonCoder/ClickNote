package com.example.clicknote.domain.worker

import java.io.File
import kotlinx.coroutines.flow.Flow

interface AudioEnhancementWorker {
    suspend fun enhanceAudio(inputFile: File, outputFile: File): Result<File>
    fun getEnhancementProgress(): Flow<Float>
    fun cancelEnhancement()
    suspend fun isEnhancing(): Boolean
    suspend fun getSupportedEnhancements(): List<EnhancementType>
}

enum class EnhancementType {
    NOISE_REDUCTION,
    VOICE_CLARITY,
    ECHO_CANCELLATION,
    BACKGROUND_NOISE_SUPPRESSION,
    VOLUME_NORMALIZATION
} 