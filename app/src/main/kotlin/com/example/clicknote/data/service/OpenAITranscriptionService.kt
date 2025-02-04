package com.example.clicknote.data.service

import com.aallam.openai.api.audio.TranscriptionRequest
import com.aallam.openai.api.file.FileSource
import com.aallam.openai.api.model.ModelId
import com.aallam.openai.client.OpenAI
import com.example.clicknote.domain.model.*
import com.example.clicknote.domain.service.TranscriptionCapable
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.receiveAsFlow
import okio.source
import java.io.File
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class OpenAITranscriptionService @Inject constructor(
    private val openAI: OpenAI
) : TranscriptionCapable {
    private val transcriptionProgress = MutableStateFlow(0f)
    private var isCurrentlyTranscribing = false
    private var shouldCancel = false

    override val id: String = "openai_transcription_service"
    private var initialized = false
    private var currentText = ""
    private var currentStatus = TranscriptionCapable.TranscriptionStatus.IDLE
    
    private val _events = Channel<TranscriptionEvent>(Channel.BUFFERED)
    override val events: Flow<TranscriptionEvent> = _events.receiveAsFlow()

    override suspend fun cleanup() {
        initialized = false
        currentText = ""
        currentStatus = TranscriptionCapable.TranscriptionStatus.IDLE
        _events.close()
    }

    override fun isInitialized(): Boolean = initialized

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

    override suspend fun transcribeAudio(
        audioData: ByteArray,
        settings: TranscriptionSettings
    ): Result<String> = runCatching {
        val tempFile = File.createTempFile("audio", ".wav")
        tempFile.writeBytes(audioData)
        
        try {
            val request = TranscriptionRequest(
                audio = tempFile,
                model = ModelId(settings.model),
                language = settings.language,
                prompt = settings.prompt,
                temperature = settings.temperature
            )
            
            val response = openAI.transcription(request)
            response.text
        } finally {
            tempFile.delete()
        }
    }

    override suspend fun transcribeFile(
        file: String,
        settings: TranscriptionSettings
    ): Result<String> = runCatching {
        val audioFile = File(file)
        if (!audioFile.exists()) {
            throw IllegalArgumentException("Audio file does not exist")
        }
        
        val request = TranscriptionRequest(
            audio = audioFile,
            model = ModelId(settings.model),
            language = settings.language,
            prompt = settings.prompt,
            temperature = settings.temperature
        )
        
        val response = openAI.transcription(request)
        response.text
    }

    override suspend fun detectLanguage(audioData: ByteArray): Result<String> = runCatching {
        val tempFile = File.createTempFile("audio", ".wav")
        tempFile.writeBytes(audioData)
        
        try {
            val request = TranscriptionRequest(
                audio = tempFile,
                model = ModelId("whisper-1")
            )
            
            val response = openAI.transcription(request)
            response.text.takeIf { it.isNotBlank() }?.let { "en" } ?: throw IllegalStateException("Language detection failed")
        } finally {
            tempFile.delete()
        }
    }

    override suspend fun getAvailableLanguages(): Result<List<String>> = runCatching {
        listOf("en", "es", "fr", "de", "it", "pt", "nl", "ru", "ja", "ko", "zh")
    }

    override suspend fun detectSpeakers(audioData: ByteArray): Result<Int> = runCatching {
        // OpenAI doesn't provide speaker detection directly
        // This is a placeholder implementation
        1
    }

    override suspend fun identifySpeakers(audioData: ByteArray): Result<List<String>> = runCatching {
        // OpenAI doesn't provide speaker identification directly
        // This is a placeholder implementation
        listOf("Speaker 1")
    }

    override suspend fun generateSummary(
        text: String,
        template: SummaryTemplate?
    ): Result<Summary> = runCatching {
        // Use OpenAI chat completion for summarization
        Summary(
            id = "summary_1",
            content = "Summary not implemented yet",
            wordCount = 4,
            sourceWordCount = text.split(" ").size
        )
    }

    override suspend fun startTranscription() {
        currentStatus = TranscriptionCapable.TranscriptionStatus.RECORDING
        _events.send(TranscriptionEvent.Started())
        _events.send(TranscriptionEvent.StatusChanged(currentStatus))
    }

    override suspend fun stopTranscription() {
        currentStatus = TranscriptionCapable.TranscriptionStatus.COMPLETED
        _events.send(TranscriptionEvent.Stopped())
        _events.send(TranscriptionEvent.StatusChanged(currentStatus))
    }

    override suspend fun getTranscriptionText(): String = currentText

    override suspend fun getTranscriptionStatus(): TranscriptionCapable.TranscriptionStatus = currentStatus

    override suspend fun enhanceAudio(audioData: ByteArray): ByteArray {
        // OpenAI doesn't provide audio enhancement
        // This is a placeholder implementation
        return audioData
    }
} 