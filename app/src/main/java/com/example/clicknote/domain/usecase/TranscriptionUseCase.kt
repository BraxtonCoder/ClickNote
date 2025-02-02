package com.example.clicknote.domain.usecase

import com.example.clicknote.domain.interfaces.*
import com.example.clicknote.domain.model.TranscriptionSettings
import com.example.clicknote.domain.repository.TranscriptionRepository
import kotlinx.coroutines.flow.*
import java.io.File
import javax.inject.Inject
import javax.inject.Provider
import javax.inject.Singleton

@Singleton
class TranscriptionUseCase @Inject constructor(
    private val repository: Provider<TranscriptionRepository>,
    private val eventHandler: Provider<TranscriptionEventHandler>,
    private val stateManager: Provider<TranscriptionStateManager>
) {
    suspend fun transcribeStream(
        audioStream: Flow<ByteArray>,
        settings: TranscriptionSettings
    ): Flow<String> {
        eventHandler.get().onTranscriptionStarted()
        stateManager.get().setTranscribing(true)

        return audioStream
            .map { audioData -> 
                repository.get().transcribeAudio(audioData, settings)
            }
            .map { result ->
                result.getOrNull() ?: ""
            }
            .onEach { text -> 
                if (text.isNotEmpty()) {
                    eventHandler.get().onTranscriptionCompleted(text)
                }
            }
            .catch { error ->
                eventHandler.get().onTranscriptionError(error)
                emit("")
            }
            .onCompletion {
                stateManager.get().setTranscribing(false)
            }
    }

    suspend fun transcribeFile(
        file: File,
        settings: TranscriptionSettings
    ): Result<String> {
        stateManager.get().setCurrentFile(file)
        eventHandler.get().onTranscriptionStarted()
        stateManager.get().setTranscribing(true)

        return try {
            val result = repository.get().transcribeFile(file, settings)
            result.onSuccess { text ->
                eventHandler.get().onTranscriptionCompleted(text)
            }.onFailure { error ->
                eventHandler.get().onTranscriptionError(error)
            }
            result
        } finally {
            stateManager.get().setTranscribing(false)
            stateManager.get().setCurrentFile(null)
        }
    }

    suspend fun generateSummary(text: String): Result<String> {
        return repository.get().generateSummary(text)
    }

    suspend fun detectSpeakers(file: File): List<String> {
        return repository.get().detectSpeakers(file).getOrDefault(emptyList())
    }

    suspend fun cleanup() {
        repository.get().cleanup()
    }
} 