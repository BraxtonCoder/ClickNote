package com.example.clicknote.ui.settings

import android.content.Context
import android.content.Intent
import android.net.Uri
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.clicknote.domain.model.StorageLocation
import com.example.clicknote.domain.model.TranscriptionLanguage
import com.example.clicknote.domain.repository.CloudSyncRepository
import com.example.clicknote.domain.repository.SettingsRepository
import com.example.clicknote.domain.repository.UserRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import javax.inject.Inject

data class SettingsUiState(
    val isSignedIn: Boolean = false,
    val email: String? = null,
    val isPremium: Boolean = false,
    val version: String = "1.0.0",
    val error: String? = null,
    // Recording settings
    val saveAudioFiles: Boolean = true,
    val highQualityAudio: Boolean = false,
    val backgroundRecording: Boolean = false,
    // Storage settings
    val storageLocation: StorageLocation = StorageLocation.LOCAL,
    val autoBackup: Boolean = false,
    // Transcription settings
    val transcriptionLanguage: TranscriptionLanguage = TranscriptionLanguage.EN,
    val offlineTranscription: Boolean = true,
    val autoPunctuation: Boolean = true,
    // Notification settings
    val showNotifications: Boolean = true,
    val notificationActions: Boolean = true,
    // Accessibility settings
    val vibrationFeedback: Boolean = true,
    val highContrast: Boolean = false,
    val isLoading: Boolean = false,
    val isCloudSyncEnabled: Boolean = false,
    val isOfflineModeEnabled: Boolean = false,
    val storageUsage: Long = 0L,
    val storageLimit: Long = 0L,
    val currentSubscriptionPlan: SubscriptionPlan = SubscriptionPlan.FREE,
    val remainingFreeNotes: Int = 3
)

@HiltViewModel
class SettingsViewModel @Inject constructor(
    @ApplicationContext private val context: Context,
    private val settingsRepository: SettingsRepository,
    private val userRepository: UserRepository,
    private val cloudSyncRepository: CloudSyncRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(SettingsUiState())
    val uiState: StateFlow<SettingsUiState> = _uiState.asStateFlow()

    init {
        loadSettings()
        observeUserState()
        viewModelScope.launch {
            // Collect cloud sync preference
            cloudSyncRepository.getCloudStoragePreference().collect { isEnabled ->
                _uiState.value = _uiState.value.copy(isCloudSyncEnabled = isEnabled)
            }
        }

        viewModelScope.launch {
            // Collect offline mode preference
            cloudSyncRepository.isOfflineModeEnabled().collect { isEnabled ->
                _uiState.value = _uiState.value.copy(isOfflineModeEnabled = isEnabled)
            }
        }

        viewModelScope.launch {
            // Collect storage usage
            combine(
                cloudSyncRepository.getStorageUsage(),
                cloudSyncRepository.getStorageLimit()
            ) { usage, limit ->
                _uiState.value = _uiState.value.copy(
                    storageUsage = usage,
                    storageLimit = limit
                )
            }.collect()
        }

        // Load subscription info
        loadSubscriptionInfo()
    }

    private fun loadSettings() {
        viewModelScope.launch {
            try {
                val settings = settingsRepository.getSettings()
                _uiState.update { state ->
                    state.copy(
                        saveAudioFiles = settings.saveAudioFiles,
                        highQualityAudio = settings.highQualityAudio,
                        backgroundRecording = settings.backgroundRecording,
                        storageLocation = settings.storageLocation,
                        autoBackup = settings.autoBackup,
                        transcriptionLanguage = settings.transcriptionLanguage,
                        offlineTranscription = settings.offlineTranscription,
                        autoPunctuation = settings.autoPunctuation,
                        showNotifications = settings.showNotifications,
                        notificationActions = settings.notificationActions,
                        vibrationFeedback = settings.vibrationFeedback,
                        highContrast = settings.highContrast
                    )
                }
            } catch (e: Exception) {
                _uiState.update { 
                    it.copy(
                        error = "Failed to load settings: ${e.message}"
                    )
                }
            }
        }
    }

    private fun observeUserState() {
        viewModelScope.launch {
            userRepository.observeUserState().collect { user ->
                _uiState.update { 
                    it.copy(
                        isSignedIn = user != null,
                        email = user?.email,
                        isPremium = user?.isPremium ?: false
                    )
                }
            }
        }
    }

    fun signIn() {
        viewModelScope.launch {
            try {
                userRepository.signInWithGoogle()
            } catch (e: Exception) {
                _uiState.update { 
                    it.copy(
                        error = "Failed to sign in: ${e.message}"
                    )
                }
            }
        }
    }

    fun signOut() {
        viewModelScope.launch {
            try {
                userRepository.signOut()
            } catch (e: Exception) {
                _uiState.update { 
                    it.copy(
                        error = "Failed to sign out: ${e.message}"
                    )
                }
            }
        }
    }

    fun managePremium() {
        viewModelScope.launch {
            try {
                if (uiState.value.isPremium) {
                    userRepository.openSubscriptionManagement()
                } else {
                    userRepository.openPremiumUpgrade()
                }
            } catch (e: Exception) {
                _uiState.update { 
                    it.copy(
                        error = "Failed to manage premium: ${e.message}"
                    )
                }
            }
        }
    }

    // Recording settings
    fun toggleSaveAudio(enabled: Boolean) {
        updateSetting { it.copy(saveAudioFiles = enabled) }
    }

    fun toggleHighQualityAudio(enabled: Boolean) {
        updateSetting { it.copy(highQualityAudio = enabled) }
    }

    fun toggleBackgroundRecording(enabled: Boolean) {
        updateSetting { it.copy(backgroundRecording = enabled) }
    }

    // Storage settings
    fun updateStorageLocation(location: StorageLocation) {
        updateSetting { it.copy(storageLocation = location) }
    }

    fun toggleAutoBackup(enabled: Boolean) {
        updateSetting { it.copy(autoBackup = enabled) }
    }

    // Transcription settings
    fun updateTranscriptionLanguage(language: TranscriptionLanguage) {
        viewModelScope.launch {
            try {
                settingsRepository.updateTranscriptionLanguage(language)
                _uiState.update { it.copy(transcriptionLanguage = language) }
            } catch (e: Exception) {
                _uiState.update { it.copy(error = "Failed to update language setting") }
            }
        }
    }

    fun toggleOfflineTranscription(enabled: Boolean) {
        updateSetting { it.copy(offlineTranscription = enabled) }
    }

    fun toggleAutoPunctuation(enabled: Boolean) {
        updateSetting { it.copy(autoPunctuation = enabled) }
    }

    // Notification settings
    fun toggleNotifications(enabled: Boolean) {
        updateSetting { it.copy(showNotifications = enabled) }
    }

    fun toggleNotificationActions(enabled: Boolean) {
        updateSetting { it.copy(notificationActions = enabled) }
    }

    // Accessibility settings
    fun toggleVibrationFeedback(enabled: Boolean) {
        updateSetting { it.copy(vibrationFeedback = enabled) }
    }

    fun toggleHighContrast(enabled: Boolean) {
        updateSetting { it.copy(highContrast = enabled) }
    }

    private fun updateSetting(update: (SettingsUiState) -> SettingsUiState) {
        viewModelScope.launch {
            try {
                val newState = update(_uiState.value)
                _uiState.value = newState
                settingsRepository.updateSettings(
                    saveAudioFiles = newState.saveAudioFiles,
                    highQualityAudio = newState.highQualityAudio,
                    backgroundRecording = newState.backgroundRecording,
                    storageLocation = newState.storageLocation,
                    autoBackup = newState.autoBackup,
                    transcriptionLanguage = newState.transcriptionLanguage,
                    offlineTranscription = newState.offlineTranscription,
                    autoPunctuation = newState.autoPunctuation,
                    showNotifications = newState.showNotifications,
                    notificationActions = newState.notificationActions,
                    vibrationFeedback = newState.vibrationFeedback,
                    highContrast = newState.highContrast
                )
            } catch (e: Exception) {
                _uiState.update { 
                    it.copy(
                        error = "Failed to update setting: ${e.message}"
                    )
                }
            }
        }
    }

    fun openPrivacyPolicy() {
        openUrl("https://clicknote.app/privacy")
    }

    fun openTermsOfService() {
        openUrl("https://clicknote.app/terms")
    }

    private fun openUrl(url: String) {
        val intent = Intent(Intent.ACTION_VIEW, Uri.parse(url)).apply {
            addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        }
        context.startActivity(intent)
    }

    fun clearError() {
        _uiState.update { it.copy(error = null) }
    }

    fun toggleCloudSync(enabled: Boolean) {
        viewModelScope.launch {
            try {
                cloudSyncRepository.setCloudStoragePreference(enabled)
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(
                    error = e.message ?: "Failed to update cloud sync preference"
                )
            }
        }
    }

    fun toggleOfflineMode(enabled: Boolean) {
        viewModelScope.launch {
            try {
                cloudSyncRepository.setOfflineModeEnabled(enabled)
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(
                    error = e.message ?: "Failed to update offline mode"
                )
            }
        }
    }

    fun backupNow() {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true, error = null)
            try {
                cloudSyncRepository.syncNow()
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(
                    error = e.message ?: "Failed to backup data"
                )
            } finally {
                _uiState.value = _uiState.value.copy(isLoading = false)
            }
        }
    }

    fun restoreBackup() {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true, error = null)
            try {
                // TODO: Implement restore functionality
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(
                    error = e.message ?: "Failed to restore backup"
                )
            } finally {
                _uiState.value = _uiState.value.copy(isLoading = false)
            }
        }
    }

    private fun loadSubscriptionInfo() {
        viewModelScope.launch {
            try {
                // TODO: Load subscription info from billing repository
                // For now, we'll use mock data
                _uiState.value = _uiState.value.copy(
                    currentSubscriptionPlan = SubscriptionPlan.FREE,
                    remainingFreeNotes = 3
                )
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(
                    error = e.message ?: "Failed to load subscription info"
                )
            }
        }
    }
} 