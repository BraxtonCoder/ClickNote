package com.example.clicknote.domain.service

import kotlinx.coroutines.flow.Flow
import java.io.File

interface AudioEnhancementService {
    suspend fun enhanceAudio(inputFile: File): Result<File>
    suspend fun denoise(inputFile: File): Result<File>
    suspend fun normalizeVolume(inputFile: File): Result<File>
    suspend fun removeBackground(inputFile: File): Result<File>
    suspend fun enhanceVoice(inputFile: File): Result<File>
    fun getEnhancementProgress(): Flow<Float>
    fun cancelEnhancement()
    fun isEnhancing(): Boolean
    suspend fun getAvailableEnhancements(): List<AudioEnhancement>
    suspend fun applyPreset(inputFile: File, preset: EnhancementPreset): Result<File>
}

enum class AudioEnhancement {
    NOISE_REDUCTION,
    VOLUME_NORMALIZATION,
    BACKGROUND_REMOVAL,
    VOICE_ENHANCEMENT,
    ECHO_CANCELLATION,
    CLARITY_BOOST
}

enum class EnhancementPreset {
    VOICE_CALL,
    CONFERENCE_ROOM,
    OUTDOOR,
    NOISY_ENVIRONMENT,
    MUSIC_RECORDING,
    CUSTOM
} 