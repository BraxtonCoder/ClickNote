package com.example.clicknote.ui.settings

import android.net.Uri
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.clicknote.domain.repository.UserRepository
import com.example.clicknote.service.BackupInfo
import com.example.clicknote.service.BackupService
import com.example.clicknote.worker.ScheduledBackupWorker
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import javax.inject.Inject

data class BackupSettingsUiState(
    val isLoading: Boolean = false,
    val error: String? = null,
    val isAutoBackupEnabled: Boolean = false,
    val backupIntervalHours: Int = 24,
    val backups: List<BackupInfo> = emptyList()
)

@HiltViewModel
class BackupSettingsViewModel @Inject constructor(
    private val backupService: BackupService,
    private val userRepository: UserRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(BackupSettingsUiState())
    val uiState: StateFlow<BackupSettingsUiState> = _uiState.asStateFlow()

    init {
        loadBackupSettings()
        loadBackups()
    }

    private fun loadBackupSettings() {
        viewModelScope.launch {
            try {
                val settings = backupService.getBackupSettings()
                _uiState.update { state ->
                    state.copy(
                        isAutoBackupEnabled = settings.isAutoBackupEnabled,
                        backupIntervalHours = settings.backupIntervalHours
                    )
                }
            } catch (e: Exception) {
                _uiState.update { it.copy(error = e.message) }
            }
        }
    }

    private fun loadBackups() {
        viewModelScope.launch {
            try {
                _uiState.update { it.copy(isLoading = true) }
                val backups = backupService.listBackups()
                _uiState.update { state ->
                    state.copy(
                        backups = backups,
                        isLoading = false
                    )
                }
            } catch (e: Exception) {
                _uiState.update { state ->
                    state.copy(
                        error = e.message,
                        isLoading = false
                    )
                }
            }
        }
    }

    fun toggleAutoBackup(enabled: Boolean) {
        viewModelScope.launch {
            try {
                backupService.setAutoBackupEnabled(enabled)
                if (enabled) {
                    ScheduledBackupWorker.schedule(
                        backupIntervalHours = uiState.value.backupIntervalHours
                    )
                } else {
                    ScheduledBackupWorker.cancel()
                }
                _uiState.update { it.copy(isAutoBackupEnabled = enabled) }
            } catch (e: Exception) {
                _uiState.update { it.copy(error = e.message) }
            }
        }
    }

    fun updateBackupInterval(hours: Int) {
        viewModelScope.launch {
            try {
                backupService.setBackupInterval(hours)
                if (uiState.value.isAutoBackupEnabled) {
                    ScheduledBackupWorker.schedule(backupIntervalHours = hours)
                }
                _uiState.update { it.copy(backupIntervalHours = hours) }
            } catch (e: Exception) {
                _uiState.update { it.copy(error = e.message) }
            }
        }
    }

    fun exportBackup(uri: Uri) {
        viewModelScope.launch {
            try {
                _uiState.update { it.copy(isLoading = true) }
                backupService.exportBackup(uri)
                loadBackups()
            } catch (e: Exception) {
                _uiState.update { state ->
                    state.copy(
                        error = e.message,
                        isLoading = false
                    )
                }
            }
        }
    }

    fun importBackup(uri: Uri) {
        viewModelScope.launch {
            try {
                _uiState.update { it.copy(isLoading = true) }
                backupService.importBackup(uri)
                loadBackups()
            } catch (e: Exception) {
                _uiState.update { state ->
                    state.copy(
                        error = e.message,
                        isLoading = false
                    )
                }
            }
        }
    }

    fun deleteBackup(path: String) {
        viewModelScope.launch {
            try {
                _uiState.update { it.copy(isLoading = true) }
                backupService.deleteBackup(path)
                loadBackups()
            } catch (e: Exception) {
                _uiState.update { state ->
                    state.copy(
                        error = e.message,
                        isLoading = false
                    )
                }
            }
        }
    }

    fun restoreBackup(path: String) {
        viewModelScope.launch {
            try {
                _uiState.update { it.copy(isLoading = true) }
                backupService.restoreBackup(path)
                loadBackups()
            } catch (e: Exception) {
                _uiState.update { state ->
                    state.copy(
                        error = e.message,
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