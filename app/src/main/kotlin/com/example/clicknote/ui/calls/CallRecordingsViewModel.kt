package com.example.clicknote.ui.calls

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.clicknote.domain.model.CallRecording
import com.example.clicknote.domain.preferences.UserPreferencesDataStore
import com.example.clicknote.domain.repository.CallRecordingRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import javax.inject.Inject

data class CallRecordingsUiState(
    val recordings: List<CallRecording> = emptyList(),
    val isLoading: Boolean = false,
    val isRecordingEnabled: Boolean = false,
    val error: String? = null
)

@HiltViewModel
class CallRecordingsViewModel @Inject constructor(
    private val repository: CallRecordingRepository,
    private val userPreferences: UserPreferencesDataStore
) : ViewModel() {

    private val _uiState = MutableStateFlow(CallRecordingsUiState(isLoading = true))
    val uiState: StateFlow<CallRecordingsUiState> = _uiState.asStateFlow()

    init {
        loadRecordings()
        observeRecordingPreference()
    }

    private fun loadRecordings() {
        viewModelScope.launch {
            try {
                repository.getAllCallRecordings()
                    .collect { recordings ->
                        _uiState.update { 
                            it.copy(
                                recordings = recordings,
                                isLoading = false,
                                error = null
                            )
                        }
                    }
            } catch (e: Exception) {
                _uiState.update { 
                    it.copy(
                        isLoading = false,
                        error = "Failed to load recordings: ${e.message}"
                    )
                }
            }
        }
    }

    private fun observeRecordingPreference() {
        viewModelScope.launch {
            userPreferences.isCallRecordingEnabled()
                .collect { isEnabled ->
                    _uiState.update { it.copy(isRecordingEnabled = isEnabled) }
                }
        }
    }

    fun toggleCallRecording(enabled: Boolean) {
        viewModelScope.launch {
            userPreferences.setCallRecordingEnabled(enabled)
        }
    }

    fun deleteRecording(id: String) {
        viewModelScope.launch {
            try {
                repository.deleteCallRecording(id)
            } catch (e: Exception) {
                _uiState.update { 
                    it.copy(error = "Failed to delete recording: ${e.message}")
                }
            }
        }
    }
} 