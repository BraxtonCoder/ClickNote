package com.example.clicknote.ui

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.clicknote.domain.model.Folder
import com.example.clicknote.domain.repository.FolderRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import javax.inject.Inject

data class MainScreenState(
    val folders: List<Folder> = emptyList(),
    val selectedFolderId: String? = null,
    val showCreateFolderDialog: Boolean = false,
    val showFolderOptionsDialog: Boolean = false,
    val selectedFolderForOptions: Folder? = null
)

@HiltViewModel
class MainViewModel @Inject constructor(
    private val folderRepository: FolderRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(MainScreenState())
    val uiState: StateFlow<MainScreenState> = _uiState.asStateFlow()

    init {
        viewModelScope.launch {
            folderRepository.getAllFolders().collect { folders ->
                _uiState.update { it.copy(folders = folders) }
            }
        }
    }

    fun selectFolder(folderId: String) {
        _uiState.update { it.copy(selectedFolderId = folderId) }
    }

    fun showCreateFolderDialog() {
        _uiState.update { it.copy(showCreateFolderDialog = true) }
    }

    fun hideCreateFolderDialog() {
        _uiState.update { it.copy(showCreateFolderDialog = false) }
    }

    fun showFolderOptionsDialog(folder: Folder) {
        _uiState.update { 
            it.copy(
                showFolderOptionsDialog = true,
                selectedFolderForOptions = folder
            )
        }
    }

    fun hideFolderOptionsDialog() {
        _uiState.update { 
            it.copy(
                showFolderOptionsDialog = false,
                selectedFolderForOptions = null
            )
        }
    }
} 