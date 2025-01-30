package com.example.clicknote.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.clicknote.domain.model.Folder
import com.example.clicknote.domain.model.Note
import com.example.clicknote.domain.repository.FolderRepository
import com.example.clicknote.domain.repository.NoteRepository
import com.example.clicknote.domain.repository.UserRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class MainViewModel @Inject constructor(
    private val noteRepository: NoteRepository,
    private val folderRepository: FolderRepository,
    private val userRepository: UserRepository
) : ViewModel() {

    private val _selectedFolderId = MutableStateFlow<String?>(null)
    val selectedFolderId: StateFlow<String?> = _selectedFolderId.asStateFlow()

    private val _searchQuery = MutableStateFlow("")
    val searchQuery: StateFlow<String> = _searchQuery.asStateFlow()

    private val _isUserSignedIn = MutableStateFlow(false)
    val isUserSignedIn: StateFlow<Boolean> = _isUserSignedIn.asStateFlow()

    val folders: StateFlow<List<Folder>> = folderRepository.getAllFolders()
        .stateIn(viewModelScope, SharingStarted.Lazily, emptyList())

    val notes: StateFlow<List<Note>> = combine(
        _selectedFolderId,
        _searchQuery,
        noteRepository.getAllNotes()
    ) { folderId, query, allNotes ->
        allNotes.filter { note ->
            val matchesFolder = folderId == null || note.folderId == folderId
            val matchesQuery = query.isEmpty() || note.content.contains(query, ignoreCase = true)
            matchesFolder && matchesQuery
        }
    }.stateIn(viewModelScope, SharingStarted.Lazily, emptyList())

    init {
        viewModelScope.launch {
            userRepository.isUserSignedIn.collect { isSignedIn ->
                _isUserSignedIn.value = isSignedIn
            }
        }
    }

    fun setSelectedFolder(folderId: String?) {
        _selectedFolderId.value = folderId
    }

    fun setSearchQuery(query: String) {
        _searchQuery.value = query
    }

    fun createFolder(name: String, color: Int) {
        viewModelScope.launch {
            folderRepository.createFolder(name, color)
        }
    }

    fun updateFolder(folderId: String, name: String, color: Int) {
        viewModelScope.launch {
            folderRepository.updateFolder(folderId, name, color)
        }
    }

    fun deleteFolder(folderId: String) {
        viewModelScope.launch {
            folderRepository.deleteFolder(folderId)
            if (_selectedFolderId.value == folderId) {
                _selectedFolderId.value = null
            }
        }
    }

    fun moveNotesToFolder(noteIds: List<String>, folderId: String?) {
        viewModelScope.launch {
            noteRepository.moveNotesToFolder(noteIds, folderId)
        }
    }

    fun deleteNotes(noteIds: List<String>) {
        viewModelScope.launch {
            noteRepository.moveNotesToTrash(noteIds)
        }
    }

    fun restoreNotes(noteIds: List<String>) {
        viewModelScope.launch {
            noteRepository.restoreNotesFromTrash(noteIds)
        }
    }

    fun permanentlyDeleteNotes(noteIds: List<String>) {
        viewModelScope.launch {
            noteRepository.permanentlyDeleteNotes(noteIds)
        }
    }

    fun signOut() {
        viewModelScope.launch {
            userRepository.signOut()
        }
    }
} 