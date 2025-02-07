package com.example.clicknote.service

import com.aallam.openai.api.audio.TranscriptionRequest
import com.aallam.openai.api.file.FileSource
import com.aallam.openai.api.model.ModelId
import com.aallam.openai.client.OpenAI
import com.example.clicknote.domain.model.TranscriptionResult
import com.example.clicknote.domain.model.TranscriptionSegment
import com.google.gson.Gson
import com.google.gson.annotations.SerializedName
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okio.source
import java.io.File
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class OpenAiService @Inject constructor(
    private val openAI: OpenAI,
    private val gson: Gson
) {
    suspend fun transcribeAudio(file: File, language: String? = null): Result<TranscriptionResult> = runCatching {
        withContext(Dispatchers.IO) {
            val startTime = System.currentTimeMillis()
            
            val request = TranscriptionRequest(
                audio = FileSource(file.name, file.source()),
                model = ModelId("whisper-1"),
                language = language
            )
            
            val response = openAI.transcription(request)
            val duration = System.currentTimeMillis() - startTime
            
            TranscriptionResult(
                text = response.text,
                confidence = 1.0f,
                language = language ?: "auto",
                duration = duration,
                segments = emptyList(),
                timestamp = System.currentTimeMillis()
            )
        }
    }

    suspend fun transcribeAudioWithTimestamps(file: File, language: String? = null): Result<TranscriptionResult> = runCatching {
        withContext(Dispatchers.IO) {
            val startTime = System.currentTimeMillis()
            
            val request = TranscriptionRequest(
                audio = FileSource(file.name, file.source()),
                model = ModelId("whisper-1"),
                language = language,
                format = "verbose_json"
            )
            
            val response = openAI.transcription(request)
            val duration = System.currentTimeMillis() - startTime
            
            // Parse the verbose JSON response
            val verboseResponse = gson.fromJson(response.text, WhisperVerboseResponse::class.java)
            
            TranscriptionResult(
                text = verboseResponse.text,
                confidence = verboseResponse.segments.map { it.confidence }.average().toFloat(),
                language = language ?: verboseResponse.language,
                duration = duration,
                segments = verboseResponse.segments.map { segment ->
                    TranscriptionSegment(
                        text = segment.text,
                        startTime = segment.start,
                        endTime = segment.end,
                        confidence = segment.confidence
                    )
                },
                timestamp = System.currentTimeMillis()
            )
        }
    }

    suspend fun generateSummary(text: String): String {
        // Implementation needed
        throw UnsupportedOperationException("Method not implemented")
    }

    suspend fun detectSpeakers(audioFile: File): List<String> {
        // Implementation needed
        throw UnsupportedOperationException("Method not implemented")
    }

    suspend fun cleanup() {
        // Implementation needed
        throw UnsupportedOperationException("Method not implemented")
    }

    private data class WhisperVerboseResponse(
        val task: String,
        val language: String,
        val duration: Double,
        val text: String,
        val segments: List<WhisperSegment>
    )

    private data class WhisperSegment(
        val id: Int,
        val seek: Int,
        val start: Double,
        val end: Double,
        val text: String,
        val tokens: List<Int>,
        val temperature: Double,
        @SerializedName("avg_logprob")
        val avgLogprob: Double,
        @SerializedName("compression_ratio")
        val compressionRatio: Double,
        @SerializedName("no_speech_prob")
        val noSpeechProb: Double,
        val confidence: Float
    )
} 