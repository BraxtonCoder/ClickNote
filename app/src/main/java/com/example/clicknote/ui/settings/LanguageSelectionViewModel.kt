package com.example.clicknote.ui.settings

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.clicknote.localization.LocalizationManager
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class LanguageSelectionViewModel @Inject constructor(
    private val localizationManager: LocalizationManager
) : ViewModel() {

    private val _uiState = MutableStateFlow(LanguageSelectionUiState())
    val uiState: StateFlow<LanguageSelectionUiState> = _uiState.asStateFlow()

    init {
        loadLanguages()
    }

    private fun loadLanguages() {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true) }
            
            try {
                val currentLanguage = localizationManager.getCurrentLanguage()
                val languageGroups = localizationManager.getLanguageGroups()
                val rtlLanguages = localizationManager.getSupportedLanguages().keys.filter { 
                    localizationManager.isRTL(it) 
                }.toSet()
                
                _uiState.update {
                    it.copy(
                        isLoading = false,
                        languageGroups = languageGroups,
                        selectedLanguage = currentLanguage,
                        rtlLanguages = rtlLanguages
                    )
                }
            } catch (e: Exception) {
                _uiState.update {
                    it.copy(
                        isLoading = false,
                        error = e.message ?: "Failed to load languages"
                    )
                }
            }
        }
    }

    fun setLanguage(languageCode: String) {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true) }
            
            try {
                localizationManager.setLanguage(languageCode)
                _uiState.update {
                    it.copy(
                        isLoading = false,
                        selectedLanguage = languageCode
                    )
                }
            } catch (e: Exception) {
                _uiState.update {
                    it.copy(
                        isLoading = false,
                        error = e.message ?: "Failed to set language"
                    )
                }
            }
        }
    }

    fun clearError() {
        _uiState.update { it.copy(error = null) }
    }
}

data class LanguageSelectionUiState(
    val isLoading: Boolean = false,
    val languageGroups: Map<String, List<Pair<String, String>>> = emptyMap(),
    val selectedLanguage: String = "",
    val rtlLanguages: Set<String> = emptySet(),
    val error: String? = null
) 