package com.example.clicknote.data.service

import com.example.clicknote.domain.model.*
import com.example.clicknote.domain.model.TranscriptionResult
import com.example.clicknote.domain.model.TranscriptionSettings
import com.example.clicknote.domain.service.TranscriptionService
import com.example.clicknote.domain.strategy.ServiceStrategy
import kotlinx.coroutines.flow.*
import java.io.File
import javax.inject.Inject
import javax.inject.Singleton
import kotlinx.coroutines.coroutineScope

@Singleton
class TranscriptionServiceImpl @Inject constructor(
    private val serviceStrategy: ServiceStrategy
) : TranscriptionService {
    override val id: String = "main_transcription_service"
    private val transcriptionProgress = MutableStateFlow(0f)
    private var isTranscribing = false
    private var initialized = false
    
    private val _events = MutableSharedFlow<TranscriptionEvent>()
    override val events: Flow<TranscriptionEvent> = _events.asSharedFlow()

    override fun isInitialized(): Boolean = initialized

    private suspend fun initialize() {
        if (!initialized) {
            try {
                // Initialize the service strategy
                val context = serviceStrategy.createServiceContext(requireOnline = false)
                if (serviceStrategy.validateServiceContext(context)) {
                    initialized = true
                    _events.emit(TranscriptionEvent.ServiceInitialized)
                }
            } catch (e: Exception) {
                _events.emit(TranscriptionEvent.Error("Failed to initialize transcription service: ${e.message}"))
            }
        }
    }

    override suspend fun transcribeAudio(
        audioData: ByteArray,
        settings: TranscriptionSettings
    ): Result<TranscriptionResult> = coroutineScope {
        if (!initialized) initialize()
        
        isTranscribing = true
        transcriptionProgress.value = 0f
        _events.emit(TranscriptionEvent.TranscriptionStarted)
        
        return@coroutineScope try {
            serviceStrategy.transcribeAudio(audioData, settings).also { result ->
                result.onSuccess { transcription ->
                    transcriptionProgress.value = 1f
                    _events.emit(TranscriptionEvent.TranscriptionCompleted(transcription))
                }.onFailure { error ->
                    _events.emit(TranscriptionEvent.Error("Transcription failed: ${error.message}"))
                }
                isTranscribing = false
            }
        } catch (e: Exception) {
            isTranscribing = false
            transcriptionProgress.value = 0f
            _events.emit(TranscriptionEvent.Error("Transcription failed: ${e.message}"))
            Result.failure(e)
        }
    }

    override suspend fun transcribeFile(
        file: File,
        settings: TranscriptionSettings
    ): Result<TranscriptionResult> = coroutineScope {
        if (!initialized) initialize()
        
        isTranscribing = true
        transcriptionProgress.value = 0f
        _events.emit(TranscriptionEvent.TranscriptionStarted)
        
        return@coroutineScope try {
            serviceStrategy.transcribeFile(file, settings).also { result ->
                result.onSuccess { transcription ->
                    transcriptionProgress.value = 1f
                    _events.emit(TranscriptionEvent.TranscriptionCompleted(transcription))
                }.onFailure { error ->
                    _events.emit(TranscriptionEvent.Error("Transcription failed: ${error.message}"))
                }
                isTranscribing = false
            }
        } catch (e: Exception) {
            isTranscribing = false
            transcriptionProgress.value = 0f
            _events.emit(TranscriptionEvent.Error("Transcription failed: ${e.message}"))
            Result.failure(e)
        }
    }

    override suspend fun generateSummary(text: String, template: SummaryTemplate): Result<Summary> = coroutineScope {
        if (!initialized) initialize()
        
        return@coroutineScope try {
            _events.emit(TranscriptionEvent.SummaryGenerationStarted)
            serviceStrategy.getService(com.example.clicknote.data.strategy.TranscriptionMode.AUTO)
                .generateSummary(text, template)
                .also { result ->
                    result.onSuccess { summary ->
                        _events.emit(TranscriptionEvent.SummaryGenerationCompleted(summary))
                    }.onFailure { error ->
                        _events.emit(TranscriptionEvent.Error("Summary generation failed: ${error.message}"))
                    }
                }
        } catch (e: Exception) {
            _events.emit(TranscriptionEvent.Error("Summary generation failed: ${e.message}"))
            Result.failure(e)
        }
    }

    override suspend fun generateSummary(text: String): Result<String> = runCatching {
        // Use the default general brief template
        val defaultTemplate = defaultTemplates.find { it.id == "general_brief" }
            ?: throw IllegalStateException("Default template not found")
            
        // Generate summary using the template
        generateSummary(text, defaultTemplate).map { summary ->
            summary.content
        }.getOrThrow()
    }

    override suspend fun detectLanguage(audioData: ByteArray): Result<String> = coroutineScope {
        if (!initialized) initialize()
        
        return@coroutineScope try {
            serviceStrategy.getService(com.example.clicknote.data.strategy.TranscriptionMode.AUTO)
                .detectLanguage(audioData)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun getAvailableLanguages(): Result<List<String>> = coroutineScope {
        if (!initialized) initialize()
        
        return@coroutineScope try {
            serviceStrategy.getService(com.example.clicknote.data.strategy.TranscriptionMode.AUTO)
                .getAvailableLanguages()
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun detectSpeakers(audioData: ByteArray): Result<Int> = coroutineScope {
        if (!initialized) initialize()
        
        return@coroutineScope try {
            _events.emit(TranscriptionEvent.SpeakerDetectionStarted)
            serviceStrategy.getService(com.example.clicknote.data.strategy.TranscriptionMode.AUTO)
                .detectSpeakers(audioData)
                .also { result ->
                    result.onSuccess { count ->
                        _events.emit(TranscriptionEvent.SpeakerDetectionCompleted(count))
                    }.onFailure { error ->
                        _events.emit(TranscriptionEvent.Error("Speaker detection failed: ${error.message}"))
                    }
                }
        } catch (e: Exception) {
            _events.emit(TranscriptionEvent.Error("Speaker detection failed: ${e.message}"))
            Result.failure(e)
        }
    }

    override suspend fun identifySpeakers(audioData: ByteArray): Result<Map<String, String>> = coroutineScope {
        if (!initialized) initialize()
        
        return@coroutineScope try {
            serviceStrategy.getService(com.example.clicknote.data.strategy.TranscriptionMode.AUTO)
                .identifySpeakers(audioData)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override fun getTranscriptionProgress(): Flow<Float> = transcriptionProgress

    override suspend fun cancelTranscription() {
        if (isTranscribing) {
            isTranscribing = false
            transcriptionProgress.value = 0f
            _events.emit(TranscriptionEvent.TranscriptionCancelled)
        }
    }

    override suspend fun cleanup() {
        cancelTranscription()
        initialized = false
        _events.emit(TranscriptionEvent.ServiceStopped)
    }
} 