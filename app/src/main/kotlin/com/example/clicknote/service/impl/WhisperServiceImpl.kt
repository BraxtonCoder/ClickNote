package com.example.clicknote.service.impl

import android.content.Context
import com.example.clicknote.domain.preferences.UserPreferencesDataStore
import com.example.clicknote.domain.model.TranscriptionLanguage
import com.example.clicknote.domain.service.WhisperService
import com.example.clicknote.domain.service.WhisperTranscription
import com.example.clicknote.domain.service.WhisperSegment
import com.example.clicknote.domain.service.WhisperModelInfo
import com.example.clicknote.service.WhisperLib
import com.example.clicknote.service.api.OpenAiApi
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.*
import kotlinx.coroutines.flow.*
import java.io.File
import javax.inject.Inject
import javax.inject.Singleton
import com.aallam.openai.api.audio.TranscriptionRequest
import com.aallam.openai.api.file.FileSource
import com.aallam.openai.client.OpenAI
import okio.source
import com.example.clicknote.domain.repository.PreferencesRepository
import org.tensorflow.lite.task.audio.classifier.AudioClassifier
import com.example.clicknote.domain.model.TranscriptionSettings
import com.example.clicknote.di.ApplicationScope
import com.example.clicknote.data.model.TranscriptionResult
import com.example.clicknote.domain.model.TranscriptionState

@Singleton
class WhisperServiceImpl @Inject constructor(
    @ApplicationContext private val context: Context,
    private val openAiApi: OpenAiApi,
    private val userPreferences: UserPreferencesDataStore,
    private val preferencesRepository: PreferencesRepository,
    @ApplicationScope private val coroutineScope: CoroutineScope
) : WhisperService {

    private val _transcriptionProgress = MutableStateFlow(0f)
    override val transcriptionProgress: Flow<Float> = _transcriptionProgress.asStateFlow()

    private val _transcriptionState = MutableStateFlow<TranscriptionState>(TranscriptionState.Idle)
    override val transcriptionState: Flow<TranscriptionState> = _transcriptionState.asStateFlow()

    private var whisperModel: Any? = null // Replace with actual Whisper model type
    private val modelName = "base"

    private var classifier: AudioClassifier? = null
    private var isInitialized = false
    private var isTranscribing = false

    init {
        coroutineScope.launch {
            initializeWhisperModel()
        }
    }

    private suspend fun initializeWhisperModel() = withContext(Dispatchers.IO) {
        try {
            _transcriptionProgress.value = 0.1f
            val modelFile = File(context.filesDir, "whisper-model.bin")
            if (!modelFile.exists()) {
                context.assets.open("whisper-tiny-en.tflite").use { input ->
                    modelFile.outputStream().use { output ->
                        input.copyTo(output)
                        _transcriptionProgress.value = 0.2f
                    }
                }
            }
            
            _transcriptionProgress.value = 0.3f
            whisperModel = WhisperLib.createModel(modelFile.absolutePath)
            if (whisperModel == null) {
                throw IllegalStateException("Failed to create Whisper model")
            }
            
            isInitialized = true
            _transcriptionProgress.value = 0.4f
        } catch (e: Exception) {
            isInitialized = false
            _transcriptionState.value = TranscriptionState.Error(e)
            throw e
        }
    }

    override suspend fun transcribe(audioFile: File): Result<String> = runCatching {
        isTranscribing = true
        _transcriptionState.value = TranscriptionState.Processing(0f)
        try {
            val result = openAiApi.transcribe(audioFile)
            _transcriptionState.value = TranscriptionState.Completed(result, 0L)
            result
        } catch (e: Exception) {
            // Fallback to offline transcription
            val offlineResult = transcribeOffline(audioFile)
            _transcriptionState.value = TranscriptionState.Completed(offlineResult, 0L)
            offlineResult
        } finally {
            isTranscribing = false
            _transcriptionProgress.value = 0f
        }
    }

    override suspend fun transcribeWithTimestamps(audioFile: File, language: TranscriptionLanguage?): TranscriptionResult {
        return try {
            openAiApi.transcribeWithTimestamps(audioFile, language)
        } catch (e: Exception) {
            // Fallback to offline transcription
            transcribeOfflineWithTimestamps(audioFile)
        }
    }

    override suspend fun transcribeStream(audioStream: Flow<ByteArray>): Flow<String> = flow {
        try {
            openAiApi.transcribeStream(audioStream).collect { text ->
                emit(text)
            }
        } catch (e: Exception) {
            // Fallback to offline transcription
            transcribeOfflineStream(audioStream).collect { text ->
                emit(text)
            }
        }
    }

    override suspend fun detectSpeakers(audioFile: File): List<String> {
        return try {
            openAiApi.detectSpeakers(audioFile)
        } catch (e: Exception) {
            // Fallback to offline speaker detection
            detectSpeakersOffline(audioFile)
        }
    }

    override fun isAvailable(): Boolean = isInitialized

    override fun cancelTranscription() {
        isTranscribing = false
        _transcriptionState.value = TranscriptionState.Cancelled()
        _transcriptionProgress.value = 0f
    }

    private suspend fun transcribeOffline(audioFile: File): String {
        initializeModelIfNeeded()
        return withContext(Dispatchers.Default) {
            // Implement offline transcription using Whisper model
            "Offline transcription placeholder"
        }
    }

    private suspend fun transcribeOfflineWithTimestamps(audioFile: File): TranscriptionResult {
        initializeModelIfNeeded()
        return withContext(Dispatchers.Default) {
            // Implement offline transcription with timestamps
            TranscriptionResult(
                text = "Offline transcription placeholder",
                segments = emptyList()
            )
        }
    }

    private fun transcribeOfflineStream(audioStream: Flow<ByteArray>): Flow<String> = flow {
        initializeModelIfNeeded()
        // Implement offline stream transcription
        emit("Offline stream transcription placeholder")
    }

    private suspend fun detectSpeakersOffline(audioFile: File): List<String> {
        initializeModelIfNeeded()
        return withContext(Dispatchers.Default) {
            // Implement offline speaker detection
            listOf("Speaker 1", "Speaker 2")
        }
    }

    private suspend fun initializeModelIfNeeded() {
        if (whisperModel == null) {
            whisperModel = withContext(Dispatchers.IO) {
                // Initialize Whisper model
                Any() // Replace with actual model initialization
            }
        }
    }

    override suspend fun initialize() = withContext(Dispatchers.IO) {
        if (!isInitialized) {
            classifier = AudioClassifier.createFromFile(context, "whisper_model.tflite")
            isInitialized = true
        }
    }

    override suspend fun cleanup() = withContext(Dispatchers.IO) {
        classifier?.close()
        classifier = null
        isInitialized = false
        whisperModel = null
    }

    companion object {
        private const val STREAM_BUFFER_SIZE = 32 * 1024L // 32KB chunks
    }
} 