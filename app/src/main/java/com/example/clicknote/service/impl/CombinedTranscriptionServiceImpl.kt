package com.example.clicknote.service.impl

import android.content.Context
import com.example.clicknote.service.*
import com.example.clicknote.domain.preferences.UserPreferencesDataStore
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.*
import kotlinx.coroutines.flow.*
import javax.inject.Inject
import javax.inject.Provider
import javax.inject.Singleton
import java.io.File
import dagger.Lazy

@Singleton
class CombinedTranscriptionServiceImpl @Inject constructor(
    private val onlineTranscriptionService: Provider<OnlineTranscriptionService>,
    private val offlineTranscriptionService: Provider<WhisperOfflineTranscriptionService>,
    private val userPreferences: Provider<UserPreferencesDataStore>,
    private val connectivityManager: NetworkConnectivityManager,
    private val audioEnhancer: AudioEnhancer,
    private val notificationHandler: NotificationHandler,
    private val performanceMonitor: PerformanceMonitor
) : TranscriptionService {

    private var currentJob: Job? = null
    private val scope = CoroutineScope(Dispatchers.Default + SupervisorJob())
    
    private val _transcriptionProgress = MutableStateFlow(0f)
    override val progress: Flow<Float> = _transcriptionProgress.asStateFlow()
    
    private val _isTranscribing = MutableStateFlow(false)
    override val isTranscribing: Flow<Boolean> = _isTranscribing.asStateFlow()

    override suspend fun transcribeStream(audioStream: Flow<ByteArray>): Flow<TranscriptionResult> = flow {
        _isTranscribing.value = true
        try {
            audioStream.collect { audioData ->
                performanceMonitor.trackAudioProcessing()
                audioEnhancer.enhance(audioData)
                val result = if (connectivityManager.isNetworkAvailable() && userPreferences.get().isOnlineTranscriptionEnabled()) {
                    onlineTranscriptionService.get().transcribeAudioData(audioData)
                } else {
                    offlineTranscriptionService.get().transcribeAudioData(audioData)
                }
                emit(result)
                
                if (userPreferences.get().shouldShowNotifications()) {
                    notificationHandler.showTranscriptionNotification(result)
                }
            }
        } finally {
            _isTranscribing.value = false
        }
    }

    override suspend fun transcribeFile(audioFile: File): TranscriptionResult {
        return withContext(Dispatchers.IO) {
            _isTranscribing.value = true
            try {
                performanceMonitor.trackFileTranscription(audioFile)
                val result = if (connectivityManager.isNetworkAvailable() && userPreferences.get().isOnlineTranscriptionEnabled()) {
                    onlineTranscriptionService.get().transcribeFile(audioFile)
                } else {
                    offlineTranscriptionService.get().transcribeFile(audioFile)
                }
                result
            } finally {
                _isTranscribing.value = false
            }
        }
    }

    override fun startRealtimeTranscription(audioStream: Flow<ByteArray>) {
        currentJob?.cancel()
        currentJob = scope.launch {
            _isTranscribing.value = true
            try {
                performanceMonitor.startMonitoring("realtime_transcription")
                audioEnhancer.setupAudioEffects(0) // Default audio session ID

                if (shouldUseOnlineService()) {
                    onlineTranscriptionService.get().startRealtimeTranscription()
                } else {
                    offlineTranscriptionService.get().startRealtimeTranscription()
                }
            } finally {
                _isTranscribing.value = false
                performanceMonitor.stopMonitoring("realtime_transcription")
            }
        }
    }

    override fun stopRealtimeTranscription() {
        currentJob?.cancel()
        currentJob = null
        _isTranscribing.value = false
        audioEnhancer.releaseAudioEffects()
    }

    override suspend fun cleanup() {
        stopRealtimeTranscription()
        scope.cancel()
        onlineTranscriptionService.get().cleanup()
        offlineTranscriptionService.get().cleanup()
    }

    override suspend fun generateSummary(text: String): Result<String> {
        return if (userPreferences.get().isOnlineTranscriptionEnabled()) {
            onlineTranscriptionService.get().generateSummary(text)
        } else {
            Result.failure(Exception("Summary generation not available offline"))
        }
    }

    override suspend fun detectSpeakers(audioFile: File): List<Speaker> {
        return if (userPreferences.get().isOnlineTranscriptionEnabled()) {
            onlineTranscriptionService.get().detectSpeakers(audioFile)
        } else {
            offlineTranscriptionService.get().detectSpeakers(audioFile)
        }
    }

    override fun getAvailableLanguages(): List<Language> {
        return if (userPreferences.get().isOnlineTranscriptionEnabled()) {
            // Combine both online and offline supported languages
            (onlineTranscriptionService.get().getAvailableLanguages() + 
             offlineTranscriptionService.get().getAvailableLanguages()).distinctBy { it.code }
        } else {
            offlineTranscriptionService.get().getAvailableLanguages()
        }
    }

    private suspend fun shouldUseOnlineService(): Boolean {
        return connectivityManager.isNetworkAvailable() &&
                !userPreferences.get().offlineTranscriptionEnabled.first()
    }
} 