package com.example.clicknote.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.clicknote.domain.model.Folder
import com.example.clicknote.domain.repository.FolderRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class DrawerViewModel @Inject constructor(
    private val folderRepository: FolderRepository
) : ViewModel() {

    private val _selectedFolder = MutableStateFlow<Folder?>(null)
    val selectedFolder = _selectedFolder.asStateFlow()

    val folders = folderRepository.getAllFolders()
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )

    private val _showFolderDialog = MutableStateFlow(false)
    val showFolderDialog = _showFolderDialog.asStateFlow()

    private val _selectedFolderForEdit = MutableStateFlow<Folder?>(null)
    val selectedFolderForEdit = _selectedFolderForEdit.asStateFlow()

    fun selectFolder(folder: Folder?) {
        _selectedFolder.value = folder
    }

    fun showAddFolderDialog() {
        _showFolderDialog.value = true
        _selectedFolderForEdit.value = null
    }

    fun showEditFolderDialog(folder: Folder) {
        _selectedFolderForEdit.value = folder
        _showFolderDialog.value = true
    }

    fun hideDialog() {
        _showFolderDialog.value = false
        _selectedFolderForEdit.value = null
    }

    fun createFolder(name: String, color: Int) {
        viewModelScope.launch {
            folderRepository.createFolder(name, color)
            hideDialog()
        }
    }

    fun updateFolder(folder: Folder, newName: String, newColor: Int) {
        viewModelScope.launch {
            folderRepository.updateFolder(folder.copy(name = newName, color = newColor))
            hideDialog()
        }
    }

    fun deleteFolder(folder: Folder) {
        viewModelScope.launch {
            folderRepository.deleteFolder(folder)
            if (_selectedFolder.value?.id == folder.id) {
                _selectedFolder.value = null
            }
        }
    }
} 