package com.example.clicknote.ui.sync

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.clicknote.domain.model.Note
import com.example.clicknote.domain.repository.SyncRepository
import com.example.clicknote.domain.repository.SyncStatus
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class SyncViewModel @Inject constructor(
    private val syncRepository: SyncRepository
) : ViewModel() {

    val syncStatus: StateFlow<SyncStatus> = syncRepository.getSyncStatus()
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = SyncStatus.IDLE
        )

    val lastSyncTime: StateFlow<Long> = syncRepository.getLastSyncTime()
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = 0L
        )

    val pendingNotes: StateFlow<List<Note>> = syncRepository.getPendingNotes()
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )

    private val _syncError = MutableStateFlow<String?>(null)
    val syncError: StateFlow<String?> = _syncError.asStateFlow()

    init {
        // Schedule periodic sync when ViewModel is created
        viewModelScope.launch {
            syncRepository.schedulePeriodicSync()
        }
    }

    fun syncNotes() {
        viewModelScope.launch {
            syncRepository.syncNotes()
                .onFailure { e ->
                    _syncError.value = e.message
                }
        }
    }

    fun syncNote(noteId: String) {
        viewModelScope.launch {
            syncRepository.syncNote(noteId)
                .onFailure { e ->
                    _syncError.value = e.message
                }
        }
    }

    fun pullNotes() {
        viewModelScope.launch {
            syncRepository.pullNotes()
                .onFailure { e ->
                    _syncError.value = e.message
                }
        }
    }

    fun clearError() {
        _syncError.value = null
    }

    override fun onCleared() {
        super.onCleared()
        viewModelScope.launch {
            syncRepository.cancelPeriodicSync()
        }
    }
} 