package com.example.clicknote.ui.settings

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.clicknote.service.ModelInfo
import com.example.clicknote.service.ModelState
import com.example.clicknote.service.WhisperModelManager
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class OfflineTranscriptionViewModel @Inject constructor(
    private val modelManager: WhisperModelManager
) : ViewModel() {

    private val _availableModels = MutableStateFlow<List<ModelInfo>>(emptyList())
    val availableModels: StateFlow<List<ModelInfo>> = _availableModels.asStateFlow()

    private val _currentModel = MutableStateFlow<ModelInfo?>(null)
    val currentModel: StateFlow<ModelInfo?> = _currentModel.asStateFlow()

    val modelState: StateFlow<ModelState> = modelManager.modelState

    init {
        loadModels()
    }

    private fun loadModels() {
        viewModelScope.launch {
            _availableModels.value = modelManager.getAvailableModels()
            _currentModel.value = modelManager.getCurrentModel()
        }
    }

    fun switchModel(model: ModelInfo) {
        viewModelScope.launch {
            modelManager.switchModel(model)
            _currentModel.value = model
            loadModels() // Refresh model list to update download states
        }
    }

    fun downloadModel(model: ModelInfo) {
        modelManager.downloadModel(model)
    }

    fun clearCache() {
        modelManager.clearCache()
        loadModels()
    }
} 