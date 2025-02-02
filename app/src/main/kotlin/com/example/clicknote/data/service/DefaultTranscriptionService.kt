package com.example.clicknote.data.service

import com.example.clicknote.domain.model.Summary
import com.example.clicknote.domain.model.SummaryTemplate
import com.example.clicknote.domain.model.TranscriptionSettings
import com.example.clicknote.domain.transcription.TranscriptionCapable
import java.io.File
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class DefaultTranscriptionService @Inject constructor() : TranscriptionCapable {
    override val id: String = "default_transcription_service"
    private var initialized = false

    override suspend fun cleanup() {
        initialized = false
    }

    override fun isInitialized(): Boolean = initialized

    override suspend fun transcribeAudio(audioData: ByteArray, settings: TranscriptionSettings): Result<String> {
        return Result.success("Transcription not implemented yet")
    }

    override suspend fun transcribeFile(file: File, settings: TranscriptionSettings): Result<String> {
        return Result.success("File transcription not implemented yet")
    }

    override suspend fun detectLanguage(audioData: ByteArray): Result<String> {
        return Result.success("en")
    }

    override suspend fun getAvailableLanguages(): Result<List<String>> {
        return Result.success(listOf("en"))
    }

    override suspend fun detectSpeakers(audioData: ByteArray): Result<Int> {
        return Result.success(1)
    }

    override suspend fun identifySpeakers(audioData: ByteArray): Result<Map<String, String>> {
        return Result.success(mapOf("Speaker 1" to "Unknown"))
    }

    override suspend fun generateSummary(text: String, template: SummaryTemplate?): Result<Summary> {
        return Result.success(Summary(
            id = "summary_1",
            noteId = "note_1",
            content = "Summary not implemented yet",
            wordCount = text.split(" ").size,
            sourceWordCount = text.split(" ").size
        ))
    }
} 