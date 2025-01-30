package com.example.clicknote.domain.service

import kotlinx.coroutines.flow.Flow
import java.io.File

interface WhisperService {
    suspend fun transcribeAudio(audioFile: File, language: String? = null): Result<WhisperTranscription>
    fun getTranscriptionProgress(): Flow<Float>
    fun cancelTranscription()
    fun isTranscribing(): Boolean
    suspend fun detectLanguage(audioFile: File): Result<String>
    suspend fun identifySpeakers(audioFile: File): Result<List<String>>
    suspend fun getAvailableLanguages(): List<String>
    suspend fun getModelInfo(): WhisperModelInfo
}

data class WhisperTranscription(
    val text: String,
    val segments: List<WhisperSegment>,
    val language: String,
    val speakers: List<String>,
    val confidence: Float,
    val duration: Long,
    val modelInfo: WhisperModelInfo
)

data class WhisperSegment(
    val text: String,
    val startTime: Long,
    val endTime: Long,
    val speaker: String?,
    val confidence: Float
)

data class WhisperModelInfo(
    val name: String,
    val version: String,
    val languages: List<String>,
    val isMultilingual: Boolean,
    val supportsTimestamps: Boolean,
    val supportsSpeakerIdentification: Boolean
) 