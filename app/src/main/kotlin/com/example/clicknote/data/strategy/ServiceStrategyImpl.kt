package com.example.clicknote.data.strategy

import com.example.clicknote.domain.model.*
import com.example.clicknote.domain.service.TranscriptionCapable
import com.example.clicknote.domain.service.NetworkMonitor
import com.example.clicknote.domain.preferences.UserPreferences
import com.example.clicknote.domain.strategy.ServiceStrategy
import com.example.clicknote.di.qualifiers.OnlineCapable
import com.example.clicknote.di.qualifiers.OfflineCapable
import javax.inject.Inject
import javax.inject.Singleton
import java.io.File

enum class TranscriptionMode {
    ONLINE,
    OFFLINE,
    AUTO
}

@Singleton
class ServiceStrategyImpl @Inject constructor(
    @OnlineCapable private val onlineService: TranscriptionCapable,
    @OfflineCapable private val offlineService: TranscriptionCapable,
    private val networkMonitor: NetworkMonitor,
    private val userPreferences: UserPreferences
) : ServiceStrategy {

    override fun determineServiceType(context: TranscriptionServiceContext): ServiceType {
        return when (getService(TranscriptionMode.AUTO)) {
            onlineService -> ServiceType.ONLINE_TRANSCRIPTION
            offlineService -> ServiceType.OFFLINE_TRANSCRIPTION
            else -> ServiceType.OFFLINE_TRANSCRIPTION // Default to offline as fallback
        }
    }

    override fun createServiceContext(requireOnline: Boolean): TranscriptionServiceContext {
        return TranscriptionServiceContext(
            isNetworkAvailable = networkMonitor.isNetworkAvailable(),
            isOnlineTranscriptionEnabled = userPreferences.isOnlineTranscriptionEnabled(),
            preferOfflineMode = userPreferences.preferOfflineMode(),
            requireOnline = requireOnline
        )
    }

    override suspend fun validateServiceContext(context: TranscriptionServiceContext): Boolean {
        return if (context.requireOnline) {
            context.isNetworkAvailable && context.isOnlineTranscriptionEnabled
        } else {
            true
        }
    }

    override fun getService(mode: TranscriptionMode): TranscriptionCapable {
        return when (mode) {
            TranscriptionMode.ONLINE -> {
                if (networkMonitor.isNetworkAvailable() && userPreferences.isOnlineTranscriptionEnabled()) {
                    onlineService
                } else {
                    offlineService // Fallback to offline if online is not available
                }
            }
            TranscriptionMode.OFFLINE -> offlineService
            TranscriptionMode.AUTO -> selectAppropriateService()
        }
    }

    private fun selectAppropriateService(): TranscriptionCapable {
        val context = createServiceContext(requireOnline = false)
        return when {
            !context.isNetworkAvailable -> offlineService
            !context.isOnlineTranscriptionEnabled -> offlineService
            context.preferOfflineMode -> offlineService
            else -> onlineService
        }
    }

    override suspend fun transcribeAudio(
        audioData: ByteArray,
        settings: TranscriptionSettings,
        mode: TranscriptionMode
    ): Result<TranscriptionResult> = getService(mode).transcribeAudio(audioData, settings)

    override suspend fun transcribeFile(
        file: File,
        settings: TranscriptionSettings,
        mode: TranscriptionMode
    ): Result<TranscriptionResult> = getService(mode).transcribeFile(file, settings)
} 