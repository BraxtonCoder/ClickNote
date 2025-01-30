package com.example.clicknote.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.clicknote.domain.model.Note
import com.example.clicknote.domain.model.NoteWithFolder
import com.example.clicknote.domain.repository.NoteRepository
import com.example.clicknote.domain.DispatcherProvider
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class NoteViewModel @Inject constructor(
    private val noteRepository: NoteRepository,
    private val dispatchers: DispatcherProvider
) : ViewModel() {

    private val _selectedNotes = MutableStateFlow<Set<String>>(emptySet())
    val selectedNotes = _selectedNotes.asStateFlow()

    private val _currentFolderId = MutableStateFlow<String?>(null)
    val currentFolderId = _currentFolderId.asStateFlow()

    private val _searchQuery = MutableStateFlow("")
    val searchQuery = _searchQuery.asStateFlow()

    private val _dateRange = MutableStateFlow<Pair<Long, Long>?>(null)
    val dateRange = _dateRange.asStateFlow()

    val notes = combine(
        currentFolderId,
        searchQuery,
        dateRange
    ) { folderId, query, range ->
        Triple(folderId, query, range)
    }.flatMapLatest { (folderId, query, range) ->
        when {
            query.isNotBlank() -> noteRepository.searchNotesWithFolders(query)
            range != null -> noteRepository.getNotesInDateRange(range.first, range.second)
            folderId != null -> noteRepository.getNotesByFolder(folderId)
            else -> noteRepository.getAllNotesWithFolders()
        }
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = emptyList()
    )

    val pinnedNotes = noteRepository.getPinnedNotes()
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )

    val deletedNotes = noteRepository.getDeletedNotes()
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )

    fun setCurrentFolder(folderId: String?) {
        _currentFolderId.value = folderId
    }

    fun setSearchQuery(query: String) {
        _searchQuery.value = query
    }

    fun setDateRange(start: Long, end: Long) {
        _dateRange.value = start to end
    }

    fun clearDateRange() {
        _dateRange.value = null
    }

    fun toggleNoteSelection(noteId: String) {
        _selectedNotes.update { selected ->
            if (selected.contains(noteId)) {
                selected - noteId
            } else {
                selected + noteId
            }
        }
    }

    fun clearSelection() {
        _selectedNotes.value = emptySet()
    }

    fun createNote(
        title: String,
        content: String,
        hasAudio: Boolean = false,
        audioPath: String? = null
    ) {
        viewModelScope.launch(dispatchers.io) {
            val note = Note.create(
                title = title,
                content = content,
                folderId = currentFolderId.value,
                hasAudio = hasAudio,
                audioPath = audioPath
            )
            noteRepository.insertNote(note)
        }
    }

    fun updateNote(note: Note) {
        viewModelScope.launch(dispatchers.io) {
            noteRepository.updateNote(note)
        }
    }

    fun deleteNote(noteId: String) {
        viewModelScope.launch(dispatchers.io) {
            noteRepository.deleteNote(noteId)
        }
    }

    fun moveToTrash(noteId: String) {
        viewModelScope.launch(dispatchers.io) {
            noteRepository.moveToTrash(noteId)
        }
    }

    fun restoreFromTrash(noteId: String) {
        viewModelScope.launch(dispatchers.io) {
            noteRepository.restoreFromTrash(noteId)
        }
    }

    fun permanentlyDelete(noteId: String) {
        viewModelScope.launch(dispatchers.io) {
            noteRepository.permanentlyDelete(noteId)
        }
    }

    fun togglePin(noteId: String) {
        viewModelScope.launch(dispatchers.io) {
            noteRepository.togglePin(noteId)
        }
    }

    fun moveToFolder(noteId: String, folderId: String?) {
        viewModelScope.launch(dispatchers.io) {
            noteRepository.moveToFolder(noteId, folderId)
        }
    }

    fun moveSelectedNotesToFolder(folderId: String?) {
        viewModelScope.launch(dispatchers.io) {
            noteRepository.moveNotesToFolder(selectedNotes.value.toList(), folderId)
            clearSelection()
        }
    }

    fun deleteSelectedNotes() {
        viewModelScope.launch(dispatchers.io) {
            selectedNotes.value.forEach { noteId ->
                noteRepository.deleteNote(noteId)
            }
            clearSelection()
        }
    }

    fun moveSelectedNotesToTrash() {
        viewModelScope.launch(dispatchers.io) {
            selectedNotes.value.forEach { noteId ->
                noteRepository.moveToTrash(noteId)
            }
            clearSelection()
        }
    }

    fun clearRecycleBin() {
        viewModelScope.launch(dispatchers.io) {
            noteRepository.clearRecycleBin()
        }
    }
} 