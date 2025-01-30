package com.example.clicknote.service

import com.example.clicknote.domain.model.TranscriptionResult
import com.example.clicknote.domain.preferences.UserPreferencesDataStore
import com.example.clicknote.domain.service.TranscriptionService
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.onEach
import java.io.File
import javax.inject.Inject
import javax.inject.Singleton
import javax.inject.Provider

@Singleton
class TranscriptionManager @Inject constructor(
    private val onlineTranscriptionService: Provider<OnlineTranscriptionService>,
    private val offlineTranscriptionService: Provider<WhisperOfflineTranscriptionService>,
    private val userPreferences: UserPreferencesDataStore,
    private val connectivityManager: NetworkConnectivityManager
) : TranscriptionService {

    override fun transcribeStream(): Flow<String> {
        val service = getActiveService()
        return service.transcribeStream()
            .onEach { transcription ->
                // Log successful transcription
            }
            .catch { error ->
                // If online transcription fails, try falling back to offline
                if (service == onlineTranscriptionService.get() && !service.isOfflineMode()) {
                    emit("Falling back to offline transcription...")
                    offlineTranscriptionService.get().transcribeStream()
                        .collect { emit(it) }
                } else {
                    emit("Transcription error: ${error.message}")
                }
            }
    }

    override suspend fun transcribeFile(audioFile: File): TranscriptionResult {
        return try {
            getActiveService().transcribeFile(audioFile)
        } catch (e: Exception) {
            // If online transcription fails, try falling back to offline
            if (getActiveService() == onlineTranscriptionService.get()) {
                offlineTranscriptionService.get().transcribeFile(audioFile)
            } else {
                TranscriptionResult.Error("Failed to transcribe file: ${e.message}")
            }
        }
    }

    override fun isOfflineMode(): Boolean = getActiveService().isOfflineMode()

    override fun getLanguages(): List<String> = getActiveService().getLanguages()

    private fun getActiveService(): TranscriptionService {
        val preferOffline = userPreferences.getPreferOfflineTranscription()
        val isOnline = connectivityManager.isNetworkAvailable()

        return when {
            preferOffline -> offlineTranscriptionService.get()
            isOnline -> onlineTranscriptionService.get()
            else -> offlineTranscriptionService.get()
        }
    }

    companion object {
        private const val TAG = "TranscriptionManager"
    }
} 