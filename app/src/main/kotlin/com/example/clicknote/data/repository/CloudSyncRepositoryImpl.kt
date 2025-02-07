package com.example.clicknote.data.repository

import android.content.Context
import com.example.clicknote.domain.model.*
import com.example.clicknote.domain.preferences.UserPreferencesDataStore
import com.example.clicknote.domain.repository.CloudSyncRepository
import com.example.clicknote.domain.repository.NoteRepository
import com.example.clicknote.domain.service.FirebaseService
import com.example.clicknote.di.qualifiers.ApplicationScope
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.*
import kotlinx.coroutines.flow.*
import java.io.File
import java.util.UUID
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class CloudSyncRepositoryImpl @Inject constructor(
    @ApplicationContext private val context: Context,
    private val noteRepository: NoteRepository,
    private val preferences: UserPreferencesDataStore,
    @ApplicationScope private val scope: CoroutineScope,
    private val firebaseService: FirebaseService
) : CloudSyncRepository {

    private val _isSyncing = MutableStateFlow(false)
    override val isSyncing: StateFlow<Boolean> = _isSyncing.asStateFlow()

    private val _syncProgress = MutableStateFlow(0f)
    override val syncProgress: StateFlow<Float> = _syncProgress.asStateFlow()

    private val _syncErrors = MutableStateFlow<List<SyncError>>(emptyList())
    override val syncErrors: StateFlow<List<SyncError>> = _syncErrors.asStateFlow()

    override val cloudStorageType: StateFlow<CloudStorageType> = preferences.cloudStorageType.stateIn(
        scope = scope,
        started = SharingStarted.Eagerly,
        initialValue = CloudStorageType.NONE
    )

    private var syncJob: Job? = null
    private var periodicSyncJob: Job? = null
    private val pendingNotes = mutableListOf<Note>()

    private val _syncStatus: MutableStateFlow<SyncStatus> = MutableStateFlow(SyncStatus.PENDING)
    override val syncStatus: StateFlow<SyncStatus> = _syncStatus.asStateFlow()

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
                        syncNote(note.id)
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

    override suspend fun syncNote(noteId: String): Result<Unit> = runCatching {
        val note = noteRepository.getNoteById(noteId).getOrThrow()
        when (getCloudStoragePreference()) {
            CloudStorageType.LOCAL -> {
                noteRepository.updateNote(note)
            }
            CloudStorageType.LOCAL_CLOUD -> {
                noteRepository.updateNote(note)
                note.audioPath?.let { path ->
                    syncAudioToLocalCloud(path, note.id)
                }
                syncNoteDataToLocalCloud(note)
                if (note.content.isNotEmpty()) {
                    syncSummaryToLocalCloud(note.content, note.id)
                }
            }
            CloudStorageType.FIREBASE -> {
                noteRepository.updateNote(note)
                note.audioPath?.let { path ->
                    syncAudioToFirebase(path, note.id)
                }
                if (note.content.isNotEmpty()) {
                    syncSummaryToFirebase(note.content, note.id)
                }
            }
            CloudStorageType.GOOGLE_DRIVE,
            CloudStorageType.DROPBOX,
            CloudStorageType.ONEDRIVE,
            CloudStorageType.NONE -> {
                // No sync needed for these storage types yet
            }
        }
    }

    override suspend fun syncAudio(audioFile: File): Result<String> = runCatching {
        when (getCloudStoragePreference()) {
            CloudStorageType.LOCAL -> audioFile.absolutePath
            CloudStorageType.LOCAL_CLOUD -> syncAudioToLocalCloud(audioFile.absolutePath, UUID.randomUUID().toString()).getOrThrow()
            CloudStorageType.FIREBASE -> syncAudioToFirebase(audioFile.absolutePath, UUID.randomUUID().toString()).getOrThrow()
            CloudStorageType.GOOGLE_DRIVE,
            CloudStorageType.DROPBOX,
            CloudStorageType.ONEDRIVE,
            CloudStorageType.NONE -> audioFile.absolutePath
        }
    }

    private suspend fun syncAudioToLocalCloud(path: String, noteId: String): Result<String> = runCatching {
        // Implementation for syncing audio to local cloud
        path // Return the local path for now
    }

    private suspend fun syncNoteDataToLocalCloud(note: Note): Result<Unit> = runCatching {
        // Implementation for syncing note data to local cloud
    }

    private suspend fun syncSummaryToLocalCloud(summary: String, noteId: String): Result<Unit> = runCatching {
        // Implementation for syncing summary to local cloud
    }

    private suspend fun syncAudioToFirebase(path: String, noteId: String): Result<String> = runCatching {
        // Implementation for syncing audio to Firebase
        firebaseService.uploadAudio(File(path), noteId).getOrThrow()
    }

    private suspend fun syncSummaryToFirebase(summary: String, noteId: String): Result<Unit> = runCatching {
        // Implementation for syncing summary to Firebase
        firebaseService.syncNote(noteId)
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
            CloudStorageType.LOCAL -> File(context.filesDir, "notes").totalSpace
            CloudStorageType.LOCAL_CLOUD -> File(context.filesDir, "cloud").totalSpace
            CloudStorageType.FIREBASE -> 0L // TODO: Implement Firebase storage usage tracking
            CloudStorageType.GOOGLE_DRIVE,
            CloudStorageType.DROPBOX,
            CloudStorageType.ONEDRIVE,
            CloudStorageType.NONE -> 0L
        }
    }

    override suspend fun getStorageLimit(): Long {
        return when (getCloudStoragePreference()) {
            CloudStorageType.LOCAL -> File(context.filesDir, "notes").freeSpace
            CloudStorageType.LOCAL_CLOUD -> File(context.filesDir, "cloud").freeSpace
            CloudStorageType.FIREBASE -> 5L * 1024 * 1024 * 1024 // 5GB Firebase limit
            CloudStorageType.GOOGLE_DRIVE,
            CloudStorageType.DROPBOX,
            CloudStorageType.ONEDRIVE,
            CloudStorageType.NONE -> 0L
        }
    }
} 