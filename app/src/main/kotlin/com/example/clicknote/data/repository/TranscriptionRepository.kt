package com.example.clicknote.data.repository

import com.example.clicknote.data.entity.TranscriptionMetadata
import com.example.clicknote.domain.model.*
import kotlinx.coroutines.flow.Flow
import java.io.File
import java.time.LocalDateTime

interface TranscriptionRepository {
    // Metadata operations
    fun getMetadataForNote(noteId: String): Flow<TranscriptionMetadata?>
    fun getMetadataInDateRange(startDate: LocalDateTime, endDate: LocalDateTime): Flow<List<TranscriptionMetadata>>
    suspend fun getTranscriptionCount(): Int
    suspend fun getAverageConfidenceScore(): Float
    suspend fun getAverageProcessingTime(): Long
    suspend fun saveTranscription(metadata: TranscriptionMetadata)
    suspend fun updateTranscriptionStatus(noteId: String, status: TranscriptionStatus)
    suspend fun getMetadataById(id: String): TranscriptionMetadata?
    suspend fun deleteById(id: String)

    // Audio file operations
    suspend fun saveAudioFile(noteId: String, audioFile: File): Result<String>
    suspend fun deleteAudioFile(noteId: String): Result<Unit>

    // Transcription operations
    suspend fun transcribeAudio(audioData: ByteArray, settings: TranscriptionSettings): Result<String>
    suspend fun transcribeFile(file: File, settings: TranscriptionSettings): Result<String>
    suspend fun generateSummary(text: String): Result<String>
    suspend fun detectSpeakers(file: File): Result<List<String>>
    suspend fun getAvailableLanguages(): List<String>
    fun cancelTranscription()
    suspend fun cleanup()

    // Events
    val events: Flow<TranscriptionEvent>
} 