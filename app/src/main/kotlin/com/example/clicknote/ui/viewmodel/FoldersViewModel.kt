package com.example.clicknote.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.clicknote.domain.model.Folder
import com.example.clicknote.domain.repository.FolderRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import java.util.*
import javax.inject.Inject

@HiltViewModel
class FoldersViewModel @Inject constructor(
    private val folderRepository: FolderRepository
) : ViewModel() {
    val folders = folderRepository.getAllFolders()
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )

    suspend fun createFolder(name: String, color: Int): Boolean {
        return try {
            folderRepository.createFolder(name = name, color = color)
            true
        } catch (e: Exception) {
            false
        }
    }

    suspend fun updateFolder(folderId: String, newName: String): Boolean {
        return try {
            folderRepository.renameFolder(folderId = folderId, newName = newName)
            true
        } catch (e: Exception) {
            false
        }
    }

    fun deleteFolder(folderId: String) {
        viewModelScope.launch {
            try {
                folderRepository.deleteFolder(folderId)
            } catch (e: Exception) {
                // Handle error if needed
            }
        }
    }

    fun moveToTrash(folderId: String) {
        viewModelScope.launch {
            try {
                folderRepository.moveToTrash(folderId)
            } catch (e: Exception) {
                // Handle error if needed
            }
        }
    }

    fun restoreFromTrash(folderId: String) {
        viewModelScope.launch {
            try {
                folderRepository.restoreFromTrash(folderId)
            } catch (e: Exception) {
                // Handle error if needed
            }
        }
    }
} 