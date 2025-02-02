package com.example.clicknote.domain.usecase

import com.example.clicknote.domain.model.TranscriptionSettings
import com.example.clicknote.domain.model.TranscriptionServiceContext
import com.example.clicknote.domain.repository.TranscriptionRepository
import com.example.clicknote.domain.provider.TranscriptionServiceProvider
import com.example.clicknote.domain.selector.TranscriptionServiceSelector
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch
import java.io.File
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class TranscriptionUseCase @Inject constructor(
    private val repository: TranscriptionRepository,
    private val serviceProvider: TranscriptionServiceProvider,
    private val serviceSelector: TranscriptionServiceSelector,
    private val scope: CoroutineScope
) {
    suspend fun transcribeStream(
        audioStream: Flow<ByteArray>,
        settings: TranscriptionSettings
    ): Flow<String> {
        val context = TranscriptionServiceContext(
            requiresOnline = settings.shouldUseOnlineMode,
            allowFallback = !settings.preferOfflineMode
        )
        val service = serviceSelector.selectService(context)

        return audioStream
            .map { audioData -> 
                service.transcribeAudio(audioData, settings)
            }
            .map { result ->
                result.getOrNull() ?: ""
            }
            .catch { error ->
                emit("")
            }
            .onCompletion {
                scope.launch {
                    service.cleanup()
                }
            }
    }

    suspend fun transcribeFile(
        file: File,
        settings: TranscriptionSettings
    ): Result<String> {
        val context = TranscriptionServiceContext(
            requiresOnline = settings.shouldUseOnlineMode,
            allowFallback = !settings.preferOfflineMode
        )
        val service = serviceSelector.selectService(context)

        return try {
            service.transcribeFile(file, settings)
        } finally {
            service.cleanup()
        }
    }

    suspend fun generateSummary(text: String): Result<String> {
        return repository.generateSummary(text)
    }

    suspend fun detectSpeakers(file: File): List<String> {
        return repository.detectSpeakers(file).getOrDefault(emptyList())
    }

    suspend fun cleanup() {
        serviceProvider.cleanup()
    }
} 