package com.example.clicknote.presentation.settings

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.clicknote.domain.model.SubscriptionPlan
import com.example.clicknote.domain.repository.SubscriptionRepository
import com.example.clicknote.domain.repository.AuthRepository
import com.mixpanel.android.mpmetrics.MixpanelAPI
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import javax.inject.Inject
import com.example.clicknote.domain.preferences.UserPreferencesDataStore
import com.example.clicknote.domain.model.AudioQuality
import com.example.clicknote.domain.model.CloudProvider

@HiltViewModel
class SettingsViewModel @Inject constructor(
    private val subscriptionRepository: SubscriptionRepository,
    private val userPreferences: UserPreferencesDataStore,
    private val authRepository: AuthRepository,
    private val analytics: MixpanelAPI
) : ViewModel() {

    private val _state = MutableStateFlow(SettingsState())
    val state = _state.asStateFlow()

    init {
        observeSubscriptionStatus()
        observeUserPreferences()
        observeAuthState()
    }

    private fun observeSubscriptionStatus() {
        viewModelScope.launch {
            subscriptionRepository.getCurrentPlan()
                .catch { e ->
                    _state.update { 
                        it.copy(error = e.message ?: "Failed to load subscription status")
                    }
                }
                .collect { plan ->
                    _state.update { 
                        it.copy(
                            currentPlan = plan,
                            isSubscribed = plan != null && plan.id != "free"
                        )
                    }
                }
        }

        viewModelScope.launch {
            try {
                val remainingNotes = subscriptionRepository.getRemainingFreeNotes()
                _state.update { it.copy(remainingFreeNotes = remainingNotes) }
            } catch (e: Exception) {
                _state.update { 
                    it.copy(error = e.message ?: "Failed to load remaining notes count")
                }
            }
        }
    }

    private fun observeUserPreferences() {
        viewModelScope.launch {
            userPreferences.audioSavingEnabled.collect { enabled ->
                _state.update { it.copy(saveAudio = enabled) }
            }
            userPreferences.audioQuality.collect { quality ->
                _state.update { it.copy(audioQuality = quality) }
            }
            userPreferences.showSilentNotifications.collect { enabled ->
                _state.update { it.copy(showSilentNotifications = enabled) }
            }
            userPreferences.cloudSyncEnabled.collect { enabled ->
                _state.update { it.copy(cloudStorageEnabled = enabled) }
            }
            userPreferences.cloudProvider.collect { provider ->
                _state.update { it.copy(selectedCloudProvider = provider) }
            }
            userPreferences.vibrationEnabled.collect { enabled ->
                _state.update { it.copy(vibrationEnabled = enabled) }
            }
            userPreferences.buttonTriggerDelay.collect { delay ->
                _state.update { it.copy(buttonTriggerDelay = delay) }
            }
        }
    }

    private fun observeAuthState() {
        viewModelScope.launch {
            authRepository.getCurrentUser().collect { user ->
                _state.update {
                    it.copy(
                        isSignedIn = user != null,
                        userEmail = user?.email
                    )
                }
            }
        }
    }

    fun updateSaveAudio(enabled: Boolean) {
        viewModelScope.launch {
            userPreferences.setAudioSavingEnabled(enabled)
            analytics.track("Setting Changed", mapOf(
                "setting" to "save_audio",
                "value" to enabled.toString()
            ))
        }
    }

    fun updateAudioQuality(quality: AudioQuality) {
        viewModelScope.launch {
            userPreferences.setAudioQuality(quality)
            analytics.track("Setting Changed", mapOf(
                "setting" to "audio_quality",
                "value" to quality.name
            ))
        }
    }

    fun updateShowSilentNotifications(enabled: Boolean) {
        viewModelScope.launch {
            userPreferences.setShowSilentNotifications(enabled)
            analytics.track("Setting Changed", mapOf(
                "setting" to "silent_notifications",
                "value" to enabled.toString()
            ))
        }
    }

    fun updateCloudStorageEnabled(enabled: Boolean) {
        viewModelScope.launch {
            userPreferences.setCloudSyncEnabled(enabled)
            analytics.track("Setting Changed", mapOf(
                "setting" to "cloud_storage",
                "value" to enabled.toString()
            ))
        }
    }

    fun updateCloudProvider(provider: CloudProvider) {
        viewModelScope.launch {
            userPreferences.setCloudProvider(provider)
            analytics.track("Setting Changed", mapOf(
                "setting" to "cloud_provider",
                "value" to provider.name
            ))
        }
    }

    fun updateVibrationEnabled(enabled: Boolean) {
        viewModelScope.launch {
            userPreferences.setVibrationEnabled(enabled)
            analytics.track("Setting Changed", mapOf(
                "setting" to "vibration",
                "value" to enabled.toString()
            ))
        }
    }

    fun updateButtonTriggerDelay(delay: Int) {
        viewModelScope.launch {
            userPreferences.setButtonTriggerDelay(delay)
            analytics.track("Setting Changed", mapOf(
                "setting" to "button_trigger_delay",
                "value" to delay.toString()
            ))
        }
    }

    fun signOut() {
        viewModelScope.launch {
            authRepository.signOut()
            analytics.track("User Signed Out")
        }
    }
}

data class SettingsState(
    val isSubscribed: Boolean = false,
    val currentPlan: SubscriptionPlan? = null,
    val remainingFreeNotes: Int = 0,
    val saveAudio: Boolean = true,
    val audioQuality: AudioQuality = AudioQuality.HIGH,
    val showSilentNotifications: Boolean = true,
    val cloudStorageEnabled: Boolean = false,
    val selectedCloudProvider: CloudProvider = CloudProvider.AWS,
    val vibrationEnabled: Boolean = true,
    val buttonTriggerDelay: Int = 750,
    val isSignedIn: Boolean = false,
    val userEmail: String? = null,
    val error: String? = null
) 