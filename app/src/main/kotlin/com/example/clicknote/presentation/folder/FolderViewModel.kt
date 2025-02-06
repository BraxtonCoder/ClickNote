package com.example.clicknote.presentation.folder

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.clicknote.domain.model.Folder
import com.example.clicknote.domain.repository.FolderRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class FolderViewModel @Inject constructor(
    private val folderRepository: FolderRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow<FolderUiState>(FolderUiState.Loading)
    val uiState: StateFlow<FolderUiState> = _uiState.asStateFlow()

    init {
        loadFolders()
    }

    private fun loadFolders() {
        viewModelScope.launch {
            try {
                folderRepository.getAllFolders()
                    .onSuccess { folders ->
                        _uiState.value = FolderUiState.Success(folders)
                    }
                    .onFailure { error ->
                        _uiState.value = FolderUiState.Error(error.message ?: "Unknown error")
                    }
            } catch (e: Exception) {
                _uiState.value = FolderUiState.Error(e.message ?: "Unknown error")
            }
        }
    }

    fun createFolder(name: String) {
        viewModelScope.launch {
            try {
                folderRepository.createFolder(name = name, color = generateRandomColor())
                    .onSuccess {
                        loadFolders()
                    }
                    .onFailure { error ->
                        _uiState.value = FolderUiState.Error(error.message ?: "Failed to create folder")
                    }
            } catch (e: Exception) {
                _uiState.value = FolderUiState.Error(e.message ?: "Failed to create folder")
            }
        }
    }

    fun deleteFolder(id: String) {
        viewModelScope.launch {
            try {
                folderRepository.deleteFolder(id)
                    .onSuccess {
                        loadFolders()
                    }
                    .onFailure { error ->
                        _uiState.value = FolderUiState.Error(error.message ?: "Failed to delete folder")
                    }
            } catch (e: Exception) {
                _uiState.value = FolderUiState.Error(e.message ?: "Failed to delete folder")
            }
        }
    }

    fun updateFolder(folder: Folder) {
        viewModelScope.launch {
            try {
                folderRepository.updateFolder(folder)
            } catch (e: Exception) {
                _uiState.value = FolderUiState.Error(e.message ?: "Failed to update folder")
            }
        }
    }

    private fun generateRandomColor(): Int {
        val colors = listOf(
            0xFF1976D2.toInt(), // Blue
            0xFF388E3C.toInt(), // Green
            0xFFF57C00.toInt(), // Orange
            0xFF7B1FA2.toInt(), // Purple
            0xFFD32F2F.toInt(), // Red
            0xFF00796B.toInt(), // Teal
            0xFF689F38.toInt(), // Light Green
            0xFFE64A19.toInt()  // Deep Orange
        )
        return colors.random()
    }
}

sealed class FolderUiState {
    object Loading : FolderUiState()
    data class Success(val folders: List<Folder>) : FolderUiState()
    data class Error(val message: String) : FolderUiState()
} 