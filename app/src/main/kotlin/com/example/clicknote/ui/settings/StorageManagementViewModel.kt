package com.example.clicknote.ui.settings

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.clicknote.domain.repository.CloudSyncRepository
import com.example.clicknote.domain.repository.SettingsRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import javax.inject.Inject

enum class CloudStorageProvider(val displayName: String) {
    FIREBASE("Firebase Cloud"),
    AWS("Amazon AWS"),
    AZURE("Microsoft Azure"),
    GOOGLE_CLOUD("Google Cloud")
}

enum class BackupSchedule(val displayName: String, val intervalHours: Int) {
    DAILY("Daily", 24),
    WEEKLY("Weekly", 168),
    MONTHLY("Monthly", 720)
}

data class StorageManagementUiState(
    val isLoading: Boolean = false,
    val error: String? = null,
    val totalStorageUsage: Long = 0L,
    val storageLimit: Long = 0L,
    val usageByType: Map<String, Long> = emptyMap(),
    val cloudStorageProvider: CloudStorageProvider = CloudStorageProvider.FIREBASE,
    val backupSchedule: BackupSchedule = BackupSchedule.DAILY,
    val isAutoBackupEnabled: Boolean = false,
    val showDeleteConfirmation: Boolean = false
)

@HiltViewModel
class StorageManagementViewModel @Inject constructor(
    @ApplicationContext private val context: Context,
    private val cloudSyncRepository: CloudSyncRepository,
    private val settingsRepository: SettingsRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(StorageManagementUiState())
    val uiState: StateFlow<StorageManagementUiState> = _uiState.asStateFlow()

    init {
        loadStorageInfo()
        loadSettings()
    }

    private fun loadStorageInfo() {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true) }
            try {
                combine(
                    cloudSyncRepository.getStorageUsage(),
                    cloudSyncRepository.getStorageLimit(),
                    cloudSyncRepository.getStorageUsageByType()
                ) { usage, limit, usageByType ->
                    _uiState.update { state ->
                        state.copy(
                            totalStorageUsage = usage,
                            storageLimit = limit,
                            usageByType = usageByType,
                            isLoading = false
                        )
                    }
                }.collect()
            } catch (e: Exception) {
                _uiState.update { 
                    it.copy(
                        error = "Failed to load storage info: ${e.message}",
                        isLoading = false
                    )
                }
            }
        }
    }

    private fun loadSettings() {
        viewModelScope.launch {
            try {
                val settings = settingsRepository.getSettings()
                _uiState.update { state ->
                    state.copy(
                        cloudStorageProvider = settings.cloudStorageProvider,
                        backupSchedule = settings.backupSchedule,
                        isAutoBackupEnabled = settings.isAutoBackupEnabled
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

    fun updateCloudStorageProvider(provider: CloudStorageProvider) {
        viewModelScope.launch {
            try {
                settingsRepository.updateCloudStorageProvider(provider)
                _uiState.update { it.copy(cloudStorageProvider = provider) }
            } catch (e: Exception) {
                _uiState.update { 
                    it.copy(
                        error = "Failed to update storage provider: ${e.message}"
                    )
                }
            }
        }
    }

    fun updateBackupSchedule(schedule: BackupSchedule) {
        viewModelScope.launch {
            try {
                settingsRepository.updateBackupSchedule(schedule)
                _uiState.update { it.copy(backupSchedule = schedule) }
            } catch (e: Exception) {
                _uiState.update { 
                    it.copy(
                        error = "Failed to update backup schedule: ${e.message}"
                    )
                }
            }
        }
    }

    fun toggleAutoBackup(enabled: Boolean) {
        viewModelScope.launch {
            try {
                settingsRepository.setAutoBackupEnabled(enabled)
                _uiState.update { it.copy(isAutoBackupEnabled = enabled) }
            } catch (e: Exception) {
                _uiState.update { 
                    it.copy(
                        error = "Failed to update auto backup setting: ${e.message}"
                    )
                }
            }
        }
    }

    fun exportData() {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true) }
            try {
                cloudSyncRepository.exportData()
                _uiState.update { it.copy(isLoading = false) }
            } catch (e: Exception) {
                _uiState.update { 
                    it.copy(
                        error = "Failed to export data: ${e.message}",
                        isLoading = false
                    )
                }
            }
        }
    }

    fun importData() {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true) }
            try {
                cloudSyncRepository.importData()
                _uiState.update { it.copy(isLoading = false) }
            } catch (e: Exception) {
                _uiState.update { 
                    it.copy(
                        error = "Failed to import data: ${e.message}",
                        isLoading = false
                    )
                }
            }
        }
    }

    fun clearCache() {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true) }
            try {
                cloudSyncRepository.clearCache()
                loadStorageInfo()
            } catch (e: Exception) {
                _uiState.update { 
                    it.copy(
                        error = "Failed to clear cache: ${e.message}",
                        isLoading = false
                    )
                }
            }
        }
    }

    fun showDeleteConfirmation() {
        _uiState.update { it.copy(showDeleteConfirmation = true) }
    }

    fun hideDeleteConfirmation() {
        _uiState.update { it.copy(showDeleteConfirmation = false) }
    }

    fun confirmDelete() {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true) }
            try {
                cloudSyncRepository.deleteAllData()
                loadStorageInfo()
            } catch (e: Exception) {
                _uiState.update { 
                    it.copy(
                        error = "Failed to delete data: ${e.message}",
                        isLoading = false
                    )
                }
            }
        }
    }

    fun clearError() {
        _uiState.update { it.copy(error = null) }
    }
} 