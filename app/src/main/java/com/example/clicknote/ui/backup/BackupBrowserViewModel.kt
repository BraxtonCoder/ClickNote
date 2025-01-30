package com.example.clicknote.ui.backup

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.clicknote.domain.model.BackupInfo
import com.example.clicknote.service.BackupSearchService
import com.example.clicknote.service.BackupSearchService.*
import com.example.clicknote.service.BackupService
import com.example.clicknote.service.BackupVerificationService
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import java.time.LocalDateTime
import javax.inject.Inject

@HiltViewModel
class BackupBrowserViewModel @Inject constructor(
    private val backupService: BackupService,
    private val searchService: BackupSearchService,
    private val verificationService: BackupVerificationService
) : ViewModel() {

    private val _uiState = MutableStateFlow(BackupBrowserUiState())
    val uiState: StateFlow<BackupBrowserUiState> = _uiState.asStateFlow()

    init {
        viewModelScope.launch {
            combine(
                searchService.getSearchFilters(),
                _uiState.map { it.searchQuery }
            ) { filters, query ->
                _uiState.value = _uiState.value.copy(isLoading = true)
                try {
                    val backups = searchService.searchBackups(query, filters)
                        .first()
                        .sortedByDescending { it.createdAt }
                    _uiState.value = _uiState.value.copy(
                        backups = backups,
                        filters = filters,
                        isLoading = false
                    )
                } catch (e: Exception) {
                    _uiState.value = _uiState.value.copy(
                        error = e.message ?: "Failed to load backups",
                        isLoading = false
                    )
                }
            }.collect()
        }
    }

    fun updateSearchQuery(query: String) {
        _uiState.value = _uiState.value.copy(searchQuery = query)
    }

    fun updateDateRange(dateRange: DateRange) {
        viewModelScope.launch {
            searchService.updateFilters(
                _uiState.value.filters.copy(
                    dateRange = dateRange,
                    customStartDate = null,
                    customEndDate = null
                )
            )
        }
    }

    fun updateCustomDateRange(start: LocalDateTime, end: LocalDateTime) {
        viewModelScope.launch {
            searchService.updateFilters(
                _uiState.value.filters.copy(
                    dateRange = DateRange.CUSTOM,
                    customStartDate = start,
                    customEndDate = end
                )
            )
        }
    }

    fun updateBackupType(type: BackupType?) {
        viewModelScope.launch {
            searchService.updateFilters(
                _uiState.value.filters.copy(backupType = type)
            )
        }
    }

    fun updateSizeFilter(sizeFilter: SizeFilter) {
        viewModelScope.launch {
            searchService.updateFilters(
                _uiState.value.filters.copy(sizeFilter = sizeFilter)
            )
        }
    }

    fun restoreBackup(backup: BackupInfo) {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true)
            try {
                // First verify the backup
                when (val result = verificationService.verifyBackup(backup)) {
                    is BackupVerificationService.BackupVerificationResult.Success -> {
                        backupService.restoreBackup(backup)
                        _uiState.value = _uiState.value.copy(
                            isLoading = false,
                            message = "Backup restored successfully"
                        )
                    }
                    is BackupVerificationService.BackupVerificationResult.Error -> {
                        _uiState.value = _uiState.value.copy(
                            isLoading = false,
                            error = "Backup verification failed: ${result.message}"
                        )
                    }
                }
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(
                    isLoading = false,
                    error = e.message ?: "Failed to restore backup"
                )
            }
        }
    }

    fun deleteBackup(backup: BackupInfo) {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true)
            try {
                backupService.deleteBackup(backup)
                _uiState.value = _uiState.value.copy(
                    isLoading = false,
                    message = "Backup deleted successfully"
                )
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(
                    isLoading = false,
                    error = e.message ?: "Failed to delete backup"
                )
            }
        }
    }

    fun clearError() {
        _uiState.value = _uiState.value.copy(error = null)
    }

    fun clearMessage() {
        _uiState.value = _uiState.value.copy(message = null)
    }
}

data class BackupBrowserUiState(
    val isLoading: Boolean = false,
    val backups: List<BackupInfo> = emptyList(),
    val searchQuery: String = "",
    val filters: BackupSearchFilters = BackupSearchFilters(),
    val error: String? = null,
    val message: String? = null
) 