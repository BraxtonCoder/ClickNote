package com.example.clicknote.data.service

import com.aallam.openai.api.audio.TranscriptionRequest
import com.aallam.openai.api.file.FileSource
import com.aallam.openai.client.OpenAI
import com.example.clicknote.domain.model.TranscriptionResult
import com.example.clicknote.domain.service.TranscriptionService
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import okio.source
import java.io.File
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class OpenAITranscriptionService @Inject constructor(
    private val openAI: OpenAI
) : TranscriptionService {
    private val transcriptionProgress = MutableStateFlow(0f)
    private var isCurrentlyTranscribing = false
    private var shouldCancel = false

    override suspend fun transcribeAudio(audioPath: String): Result<TranscriptionResult> = runCatching {
        isCurrentlyTranscribing = true
        shouldCancel = false
        transcriptionProgress.value = 0f
        
        val file = File(audioPath)
        val result = transcribeAudio(file).getOrThrow()
        
        transcriptionProgress.value = 1f
        isCurrentlyTranscribing = false
        
        TranscriptionResult(
            text = result,
            language = detectLanguage(audioPath),
            speakers = identifySpeakers(audioPath)
        )
    }

    override fun getTranscriptionProgress(): Flow<Float> = transcriptionProgress

    override fun cancelTranscription() {
        shouldCancel = true
        isCurrentlyTranscribing = false
        transcriptionProgress.value = 0f
    }

    override fun isTranscribing(): Boolean = isCurrentlyTranscribing

    override suspend fun detectLanguage(audioPath: String): String {
        // Use Whisper's language detection capability
        val file = File(audioPath)
        val request = TranscriptionRequest(
            audio = FileSource(
                name = file.name,
                source = file.source()
            ),
            model = "whisper-1"
        )
        
        return openAI.transcription(request).language ?: "en"
    }

    override suspend fun identifySpeakers(audioPath: String): List<String> {
        // For now, return a single speaker as Whisper doesn't support speaker diarization
        return listOf("Speaker 1")
    }

    override suspend fun transcribeAudio(
        audioFile: File,
        language: String?,
        prompt: String?
    ): Result<String> = runCatching {
        if (shouldCancel) {
            throw IllegalStateException("Transcription was cancelled")
        }

        val request = TranscriptionRequest(
            audio = FileSource(
                name = audioFile.name,
                source = audioFile.source()
            ),
            model = "whisper-1",
            language = language,
            prompt = prompt
        )
        
        transcriptionProgress.value = 0.5f
        val result = openAI.transcription(request).text
        transcriptionProgress.value = 1.0f
        
        result
    }

    override suspend fun transcribeAudioStream(
        audioData: ByteArray,
        language: String?,
        prompt: String?
    ): Result<String> = runCatching {
        val tempFile = File.createTempFile("audio_stream", ".wav")
        tempFile.writeBytes(audioData)
        
        try {
            transcribeAudio(tempFile, language, prompt).getOrThrow()
        } finally {
            tempFile.delete()
        }
    }
} 