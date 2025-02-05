package com.example.clicknote.data.repository

import android.content.Context
import com.example.clicknote.domain.model.*
import com.example.clicknote.domain.preferences.UserPreferencesDataStore
import com.example.clicknote.domain.repository.CloudSyncRepository
import com.example.clicknote.domain.repository.NoteRepository
import com.example.clicknote.di.qualifiers.ApplicationScope
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.*
import kotlinx.coroutines.flow.*
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class CloudSyncRepositoryImpl @Inject constructor(
    @ApplicationContext private val context: Context,
    private val noteRepository: NoteRepository,
    private val preferences: UserPreferencesDataStore,
    @ApplicationScope private val scope: CoroutineScope
) : CloudSyncRepository {

    private val _isSyncing = MutableStateFlow(false)
    override val isSyncing: Flow<Boolean> = _isSyncing.asStateFlow()

    private val _syncProgress = MutableStateFlow(0f)
    override val syncProgress: Flow<Float> = _syncProgress.asStateFlow()

    private val _syncErrors = MutableStateFlow<List<SyncError>>(emptyList())
    override val syncErrors: Flow<List<SyncError>> = _syncErrors.asStateFlow()

    override val cloudStorageType: Flow<CloudStorageType> = preferences.cloudStorageType

    private var syncJob: Job? = null
    private var periodicSyncJob: Job? = null
    private val pendingNotes = mutableListOf<Note>()

    override suspend fun getCloudStoragePreference(): CloudStorageType {
        return preferences.cloudStorageType.first()
    }

    override suspend fun setCloudStoragePreference(type: CloudStorageType) {
        preferences.setCloudStorageType(type)
    }

    override suspend fun startSync() {
        if (_isSyncing.value) return

        syncJob = scope.launch {
            try {
                _isSyncing.value = true
                _syncProgress.value = 0f

                val notes = noteRepository.getAllNotes().getOrNull() ?: emptyList()
                val total = notes.size.toFloat()
                var current = 0f

                notes.forEach { note ->
                    withContext(Dispatchers.IO) {
                        syncNote(note)
                        current++
                        _syncProgress.value = current / total
                    }
                }

                _syncProgress.value = 1f
            } catch (e: Exception) {
                _syncErrors.value = _syncErrors.value + SyncError(
                    message = e.message ?: "Unknown error during sync",
                    type = SyncErrorType.UNKNOWN,
                    noteId = null
                )
            } finally {
                _isSyncing.value = false
                _syncProgress.value = 0f
            }
        }
    }

    override suspend fun cancelSync() {
        syncJob?.cancelAndJoin()
        _isSyncing.value = false
        _syncProgress.value = 0f
    }

    override suspend fun syncNote(note: Note) {
        when (getCloudStoragePreference()) {
            CloudStorageType.LOCAL -> {
                // Save locally only
                noteRepository.updateNote(note).getOrNull()
            }
            CloudStorageType.LOCAL_CLOUD -> {
                try {
                    // Save locally first
                    noteRepository.updateNote(note).getOrNull()
                    
                    // Sync with user's personal cloud storage
                    note.audioPath?.let { path ->
                        // Upload audio to user's cloud storage
                        syncAudioToLocalCloud(path, note.id)
                    }
                    
                    // Sync note data
                    syncNoteDataToLocalCloud(note)
                    
                    // Sync summary if exists
                    note.summary?.let { summary ->
                        syncSummaryToLocalCloud(summary, note.id)
                    }
                } catch (e: Exception) {
                    _syncErrors.value = _syncErrors.value + SyncError(
                        message = "Failed to sync note to local cloud: ${e.message}",
                        type = SyncErrorType.NETWORK,
                        noteId = note.id
                    )
                }
            }
            CloudStorageType.FIREBASE -> {
                try {
                    // Sync note data
                    noteRepository.updateNote(note).getOrNull()
                    
                    // Sync audio file if exists
                    note.audioPath?.let { path ->
                        // Upload to Google Cloud Storage
                        syncAudioToFirebase(path, note.id)
                    }
                    
                    // Sync summary if exists
                    note.summary?.let { summary ->
                        // Store in Firestore
                        syncSummaryToFirebase(summary, note.id)
                    }
                } catch (e: Exception) {
                    _syncErrors.value = _syncErrors.value + SyncError(
                        message = "Failed to sync note to Firebase: ${e.message}",
                        type = SyncErrorType.NETWORK,
                        noteId = note.id
                    )
                }
            }
            CloudStorageType.NONE -> {
                // No sync needed
            }
        }
    }

    override suspend fun syncNotes(notes: List<Note>) {
        notes.forEach { note ->
            syncNote(note)
        }
    }

    override suspend fun deleteNote(noteId: String) {
        val note = noteRepository.getNoteById(noteId).getOrNull() ?: return
        when (getCloudStoragePreference()) {
            CloudStorageType.LOCAL -> {
                noteRepository.deleteNote(note.id).getOrNull()
            }
            CloudStorageType.LOCAL_CLOUD -> {
                try {
                    // Delete locally
                    noteRepository.deleteNote(note.id).getOrNull()
                    
                    // Delete from user's cloud storage
                    deleteFromLocalCloud(noteId)
                } catch (e: Exception) {
                    _syncErrors.value = _syncErrors.value + SyncError(
                        message = "Failed to delete note from local cloud: ${e.message}",
                        type = SyncErrorType.NETWORK,
                        noteId = noteId
                    )
                }
            }
            CloudStorageType.FIREBASE -> {
                try {
                    // Delete from Firestore
                    noteRepository.deleteNote(note.id).getOrNull()
                    
                    // Delete from Firebase Storage
                    deleteFromFirebase(noteId)
                } catch (e: Exception) {
                    _syncErrors.value = _syncErrors.value + SyncError(
                        message = "Failed to delete note from Firebase: ${e.message}",
                        type = SyncErrorType.NETWORK,
                        noteId = noteId
                    )
                }
            }
            CloudStorageType.NONE -> {
                // No sync needed
            }
        }
    }

    override suspend fun syncPendingChanges() {
        val notes = getPendingNotes()
        syncNotes(notes)
        clearPendingNotes()
    }

    override suspend fun schedulePeriodicSync(intervalMinutes: Long) {
        cancelPeriodicSync()
        periodicSyncJob = scope.launch {
            while (isActive) {
                startSync()
                delay(intervalMinutes * 60 * 1000)
            }
        }
    }

    override suspend fun cancelPeriodicSync() {
        periodicSyncJob?.cancelAndJoin()
    }

    override suspend fun addPendingNote(note: Note) {
        pendingNotes.add(note)
    }

    override suspend fun getPendingNotes(): List<Note> {
        return pendingNotes.toList()
    }

    override suspend fun clearPendingNotes() {
        pendingNotes.clear()
    }

    override suspend fun getStorageUsage(): Long {
        return when (getCloudStoragePreference()) {
            CloudStorageType.LOCAL -> calculateLocalStorageUsage()
            CloudStorageType.LOCAL_CLOUD -> getLocalCloudStorageUsage()
            CloudStorageType.FIREBASE -> getGoogleCloudStorageUsage()
            CloudStorageType.NONE -> 0L
        }
    }

    override suspend fun getStorageLimit(): Long {
        return when (getCloudStoragePreference()) {
            CloudStorageType.LOCAL -> getAvailableLocalStorage()
            CloudStorageType.LOCAL_CLOUD -> getLocalCloudStorageLimit()
            CloudStorageType.FIREBASE -> 5L * 1024 * 1024 * 1024 // 5GB Firebase limit
            CloudStorageType.NONE -> 0L
        }
    }

    private suspend fun calculateLocalStorageUsage(): Long {
        // Implementation to calculate local storage usage
        return 0L // Placeholder
    }

    private suspend fun getGoogleCloudStorageUsage(): Long {
        // Implementation to get Google Cloud Storage usage
        return 0L // Placeholder
    }

    private suspend fun getAvailableLocalStorage(): Long {
        // Implementation to get available local storage
        return Long.MAX_VALUE // Placeholder
    }

    private suspend fun syncAudioToLocalCloud(audioPath: String, noteId: String) {
        // Implementation for syncing audio to local cloud storage
    }

    private suspend fun syncNoteDataToLocalCloud(note: Note) {
        // Implementation for syncing note data to local cloud storage
    }

    private suspend fun syncSummaryToLocalCloud(summary: String, noteId: String) {
        // Implementation for syncing summary to local cloud storage
    }

    private suspend fun deleteFromLocalCloud(noteId: String) {
        // Implementation for deleting from local cloud storage
    }

    private suspend fun syncAudioToFirebase(audioPath: String, noteId: String) {
        // Implementation for syncing audio to Firebase Storage
    }

    private suspend fun syncSummaryToFirebase(summary: String, noteId: String) {
        // Implementation for syncing summary to Firebase
    }

    private suspend fun deleteFromFirebase(noteId: String) {
        // Implementation for deleting from Firebase
    }

    private suspend fun getLocalCloudStorageUsage(): Long {
        // Implementation to get user's cloud storage usage
        return 0L // Placeholder
    }

    private suspend fun getLocalCloudStorageLimit(): Long {
        // Implementation to get user's cloud storage limit
        return 15L * 1024 * 1024 * 1024 // 15GB (typical free tier limit)
    }
} 