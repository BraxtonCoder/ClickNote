package com.example.clicknote.data.repository

import com.example.clicknote.domain.repository.TranscriptionRepository
import com.example.clicknote.domain.event.TranscriptionEventDispatcher
import com.example.clicknote.domain.model.*
import com.example.clicknote.domain.provider.TranscriptionServiceProvider
import com.example.clicknote.domain.selector.TranscriptionServiceSelector
import com.example.clicknote.domain.service.*
import kotlinx.coroutines.flow.*
import java.io.File
import javax.inject.Inject
import javax.inject.Provider
import javax.inject.Singleton

@Singleton
class TranscriptionRepositoryImpl @Inject constructor(
    private val serviceProvider: Provider<TranscriptionServiceProvider>,
    private val serviceSelector: Provider<TranscriptionServiceSelector>,
    private val eventDispatcher: Provider<TranscriptionEventDispatcher>
) : TranscriptionRepository {

    override val events: Flow<TranscriptionEvent> = eventDispatcher.get().events

    override suspend fun transcribeAudio(
        audioData: ByteArray, 
        settings: TranscriptionSettings
    ): Result<String> = withErrorHandling(
        TranscriptionServiceContext(settings)
    ) { service ->
        service.transcribeAudio(audioData, settings)
    }

    override suspend fun transcribeFile(
        file: File, 
        settings: TranscriptionSettings
    ): Result<String> = withErrorHandling(
        TranscriptionServiceContext(settings)
    ) { service ->
        service.transcribeFile(file, settings)
    }

    override suspend fun generateSummary(text: String): Result<String> {
        return Result.success(text) // TODO: Implement summary generation
    }

    override suspend fun detectSpeakers(file: File): Result<List<String>> {
        return Result.success(listOf("Speaker 1")) // TODO: Implement speaker detection
    }

    override suspend fun getAvailableLanguages(): List<String> =
        runCatching {
            serviceSelector.get().getOnlineService().getAvailableLanguages()
        }.getOrElse { 
            serviceSelector.get().getOfflineService().getAvailableLanguages()
        }

    override fun cancelTranscription() {
        serviceProvider.get().getActiveService()?.cancelTranscription()
        runCatching {
            eventDispatcher.get().emit(TranscriptionEvent.Cancelled)
        }
    }

    override suspend fun cleanup() {
        serviceProvider.get().cleanup()
    }

    private suspend fun <T> withErrorHandling(
        context: TranscriptionServiceContext,
        block: suspend (TranscriptionService) -> Result<T>
    ): Result<T> {
        return try {
            val service = serviceProvider.get().getServiceForSettings(context)
            block(service)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}