package com.example.clicknote.service

import com.example.clicknote.domain.interfaces.*
import com.example.clicknote.domain.model.TranscriptionSettings
import com.example.clicknote.domain.preferences.UserPreferencesDataStore
import com.example.clicknote.domain.usecase.TranscriptionUseCase
import kotlinx.coroutines.flow.*
import java.io.File
import javax.inject.Inject
import javax.inject.Provider
import javax.inject.Singleton

@Singleton
class TranscriptionManager @Inject constructor(
    private val transcriptionUseCase: Provider<TranscriptionUseCase>,
    private val eventHandler: Provider<TranscriptionEventHandler>,
    private val stateManager: Provider<TranscriptionStateManager>,
    private val userPreferences: Provider<UserPreferencesDataStore>,
    private val connectivityManager: Provider<NetworkConnectivityManager>
) {
    val transcriptionState: Flow<TranscriptionState> = eventHandler.get().getTranscriptionStateFlow()
    val isTranscribing: StateFlow<Boolean> = stateManager.get().isTranscribing
    val currentFile: StateFlow<File?> = stateManager.get().currentFile

    suspend fun transcribeStream(audioStream: Flow<ByteArray>): Flow<String> {
        val settings = getTranscriptionSettings()
        return transcriptionUseCase.get().transcribeStream(audioStream, settings)
    }

    suspend fun transcribeFile(audioFile: File): Result<String> {
        val settings = getTranscriptionSettings()
        return transcriptionUseCase.get().transcribeFile(audioFile, settings)
    }

    suspend fun generateSummary(text: String): Result<String> {
        return transcriptionUseCase.get().generateSummary(text)
    }

    suspend fun detectSpeakers(audioFile: File): List<String> {
        return transcriptionUseCase.get().detectSpeakers(audioFile)
    }

    suspend fun cleanup() {
        transcriptionUseCase.get().cleanup()
        stateManager.get().cleanup()
    }

    private fun getTranscriptionSettings(): TranscriptionSettings {
        return TranscriptionSettings(
            preferOfflineMode = userPreferences.get().getPreferOfflineTranscription(),
            isNetworkAvailable = connectivityManager.get().isNetworkAvailable(),
            selectedLanguage = userPreferences.get().getSelectedLanguage(),
            enhanceAudio = userPreferences.get().getEnhanceAudio(),
            speakerDiarization = userPreferences.get().getSpeakerDiarization(),
            saveAudio = userPreferences.get().getSaveAudio()
        )
    }

    companion object {
        private const val TAG = "TranscriptionManager"
    }
}