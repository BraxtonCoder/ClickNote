package com.example.clicknote.domain.usecase

import com.example.clicknote.domain.model.*
import com.example.clicknote.domain.repository.TranscriptionRepository
import com.example.clicknote.domain.provider.TranscriptionServiceProvider
import com.example.clicknote.domain.selector.TranscriptionServiceSelector
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import java.io.File
import javax.inject.Inject

class TranscriptionUseCase @Inject constructor(
    private val repository: TranscriptionRepository,
    private val serviceProvider: TranscriptionServiceProvider,
    private val serviceSelector: TranscriptionServiceSelector,
    private val scope: CoroutineScope
) {
    private val _transcriptionState = MutableStateFlow<TranscriptionState>(TranscriptionState.Idle)
    val transcriptionState: StateFlow<TranscriptionState> = _transcriptionState.asStateFlow()

    suspend fun transcribeAudio(
        audioData: ByteArray,
        settings: TranscriptionSettings = TranscriptionSettings()
    ): Result<TranscriptionResult> {
        _transcriptionState.value = TranscriptionState.Processing
        return try {
            val service = serviceProvider.getService()
            service.transcribeAudio(audioData, settings).also { result ->
                result.onSuccess { transcription ->
                    _transcriptionState.value = TranscriptionState.Success(transcription)
                }.onFailure { error ->
                    _transcriptionState.value = TranscriptionState.Error(error)
                }
            }
        } catch (e: Exception) {
            _transcriptionState.value = TranscriptionState.Error(e)
            Result.failure(e)
        }
    }

    suspend fun transcribeFile(
        file: File,
        settings: TranscriptionSettings = TranscriptionSettings()
    ): Result<TranscriptionResult> {
        _transcriptionState.value = TranscriptionState.Processing
        return try {
            val service = serviceProvider.getService()
            service.transcribeFile(file, settings).also { result ->
                result.onSuccess { transcription ->
                    _transcriptionState.value = TranscriptionState.Success(transcription)
                }.onFailure { error ->
                    _transcriptionState.value = TranscriptionState.Error(error)
                }
            }
        } catch (e: Exception) {
            _transcriptionState.value = TranscriptionState.Error(e)
            Result.failure(e)
        }
    }

    suspend fun generateSummary(
        text: String,
        template: SummaryTemplate = SummaryTemplate.Default
    ): Result<Summary> {
        return try {
            val service = serviceProvider.getService()
            service.generateSummary(text, template)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun detectLanguage(audioData: ByteArray): Result<String> {
        return try {
            val service = serviceProvider.getService()
            service.detectLanguage(audioData)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun detectSpeakers(audioData: ByteArray): Result<Int> {
        return try {
            val service = serviceProvider.getService()
            service.detectSpeakers(audioData)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun identifySpeakers(audioData: ByteArray): Result<Map<String, String>> {
        return try {
            val service = serviceProvider.getService()
            service.identifySpeakers(audioData)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    fun observeEvents(): Flow<TranscriptionEvent> {
        return serviceProvider.getService().events
    }

    fun cleanup() {
        scope.launch {
            serviceProvider.cleanup()
        }
    }
}

sealed class TranscriptionState {
    object Idle : TranscriptionState()
    object Processing : TranscriptionState()
    data class Success(val result: TranscriptionResult) : TranscriptionState()
    data class Error(val error: Throwable) : TranscriptionState()
} 