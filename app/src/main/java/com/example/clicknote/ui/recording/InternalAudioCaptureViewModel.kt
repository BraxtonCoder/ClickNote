package com.example.clicknote.ui.recording

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.clicknote.service.InternalAudioCaptureManager
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class InternalAudioCaptureViewModel @Inject constructor(
    private val internalAudioCaptureManager: InternalAudioCaptureManager
) : ViewModel() {
    
    private val _uiState = MutableStateFlow(InternalAudioCaptureUiState())
    val uiState: StateFlow<InternalAudioCaptureUiState> = _uiState.asStateFlow()
    
    init {
        viewModelScope.launch {
            internalAudioCaptureManager.isRecording
                .onEach { isRecording ->
                    _uiState.update { it.copy(isRecording = isRecording) }
                }
                .launchIn(viewModelScope)
        }
    }
    
    fun startRecording() {
        _uiState.update { it.copy(isRequestingPermission = true) }
    }
    
    fun stopRecording() {
        internalAudioCaptureManager.stopRecording()
        _uiState.update { it.copy(isRequestingPermission = false) }
    }
}

data class InternalAudioCaptureUiState(
    val isRecording: Boolean = false,
    val isRequestingPermission: Boolean = false,
    val error: String? = null
) 