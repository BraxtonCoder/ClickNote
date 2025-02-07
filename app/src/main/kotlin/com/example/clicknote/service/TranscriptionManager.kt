package com.example.clicknote.service

import android.content.Context
import com.example.clicknote.domain.model.TranscriptionLanguage
import com.example.clicknote.domain.model.TranscriptionSettings
import com.example.clicknote.domain.preferences.UserPreferencesDataStore
import com.example.clicknote.domain.repository.TranscriptionRepository
import com.example.clicknote.domain.service.NetworkConnectivityManager
import com.example.clicknote.domain.service.TranscriptionManager
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.*
import java.io.File
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class TranscriptionManagerImpl @Inject constructor(
    @ApplicationContext private val context: Context,
    private val transcriptionRepository: TranscriptionRepository,
    private val userPreferences: UserPreferencesDataStore,
    private val connectivityManager: NetworkConnectivityManager
) : TranscriptionManager {

    private val _isTranscribing = MutableStateFlow(false)
    override val isTranscribing: StateFlow<Boolean> = _isTranscribing.asStateFlow()

    private val _currentFile = MutableStateFlow<File?>(null)
    override val currentFile: StateFlow<File?> = _currentFile.asStateFlow()

    override suspend fun transcribeAudio(file: File, noteId: String) {
        try {
            _isTranscribing.value = true
            _currentFile.value = file

            val settings = createTranscriptionSettings(noteId)
            transcriptionRepository.transcribeFile(file, settings)
                .onSuccess { _ ->
                    // Handle successful transcription
                }
                .onFailure { _ ->
                    // Handle transcription error
                }
        } finally {
            _isTranscribing.value = false
            _currentFile.value = null
        }
    }

    override suspend fun cancelTranscription() {
        transcriptionRepository.cancelTranscription()
        _isTranscribing.value = false
        _currentFile.value = null
    }

    private suspend fun createTranscriptionSettings(noteId: String): TranscriptionSettings {
        val language = userPreferences.getTranscriptionLanguage().first()
        val speakerDetection = userPreferences.getSpeakerDetectionEnabled().first()
        val offlineMode = userPreferences.getOfflineModeEnabled().first()

        return TranscriptionSettings(
            noteId = noteId,
            language = language,
            model = "base",
            enableSpeakerDetection = speakerDetection,
            enableTimestamps = true,
            enablePunctuation = true,
            preferOfflineMode = offlineMode,
            isNetworkAvailable = connectivityManager.isNetworkAvailable()
        )
    }

    override suspend fun cleanup() {
        transcriptionRepository.cleanup()
        _isTranscribing.value = false
        _currentFile.value = null
    }

    companion object {
        private const val TAG = "TranscriptionManager"
    }
}