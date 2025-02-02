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
            folderRepository.getAllFolders()
                .catch { error ->
                    _uiState.value = FolderUiState.Error(error.message ?: "Unknown error")
                }
                .collect { folders ->
                    _uiState.value = FolderUiState.Success(folders)
                }
        }
    }

    fun createFolder(name: String, color: Int) {
        viewModelScope.launch {
            try {
                val folder = Folder.create(name = name, color = color)
                folderRepository.createFolder(folder)
            } catch (e: Exception) {
                _uiState.value = FolderUiState.Error(e.message ?: "Failed to create folder")
            }
        }
    }

    fun deleteFolder(id: String) {
        viewModelScope.launch {
            try {
                folderRepository.deleteFolder(id)
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
}

sealed class FolderUiState {
    object Loading : FolderUiState()
    data class Success(val folders: List<Folder>) : FolderUiState()
    data class Error(val message: String) : FolderUiState()
} 