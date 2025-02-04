package com.example.clicknote.data.repository

import android.content.Context
import android.net.ConnectivityManager
import android.net.Network
import android.net.NetworkCapabilities
import android.net.NetworkRequest
import android.net.Uri
import androidx.work.*
import com.example.clicknote.domain.preferences.UserPreferencesDataStore
import com.example.clicknote.domain.model.*
import com.example.clicknote.domain.repository.CloudSyncRepository
import com.example.clicknote.domain.service.CloudStorageService
import com.example.clicknote.domain.service.FirestoreService
import com.example.clicknote.worker.SyncWorker
import com.google.firebase.auth.FirebaseAuth
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.*
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.*
import java.io.File
import java.util.*
import java.util.concurrent.CancellationException
import java.util.concurrent.TimeUnit
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class CloudSyncRepositoryImpl @Inject constructor(
    @ApplicationContext private val context: Context,
    private val firestoreService: FirestoreService,
    private val cloudStorageService: CloudStorageService,
    private val auth: FirebaseAuth,
    private val userPreferences: UserPreferencesDataStore,
    private val workManager: WorkManager
) : CloudSyncRepository {

    companion object {
        private const val SYNC_WORK_NAME = "sync_work"
        private const val PERIODIC_SYNC_WORK_NAME = "periodic_sync_work"
        private const val MIN_SYNC_INTERVAL = 15L // minutes
        private const val AUDIO_FOLDER = "audio"
        private const val SUMMARY_FOLDER = "summaries"
    }

    private val _syncStatus = MutableStateFlow<SyncStatus>(SyncStatus.IDLE)
    override val syncStatus: Flow<SyncStatus> = _syncStatus.asStateFlow()

    private val _syncErrors = MutableStateFlow<List<String>>(emptyList())
    override val syncErrors: Flow<List<String>> = _syncErrors.asStateFlow()

    private var currentWorkId: UUID? = null

    private val networkStatus = callbackFlow {
        val connectivityManager = context.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
        val callback = object : ConnectivityManager.NetworkCallback() {
            override fun onAvailable(network: Network) {
                trySend(true)
                if (_syncStatus.value == SyncStatus.OFFLINE) {
                    _syncStatus.value = SyncStatus.IDLE
                    syncPendingChanges()
                }
            }
            override fun onLost(network: Network) {
                trySend(false)
                _syncStatus.value = SyncStatus.OFFLINE
            }
        }
        val request = NetworkRequest.Builder()
            .addCapability(NetworkCapabilities.NET_CAPABILITY_INTERNET)
            .build()
        connectivityManager.registerNetworkCallback(request, callback)
        awaitClose {
            connectivityManager.unregisterNetworkCallback(callback)
        }
    }

    override suspend fun startSync() {
        if (_syncStatus.value == SyncStatus.SYNCING) return
        
        try {
            // Check network connectivity first
            if (!networkStatus.first()) {
                _syncStatus.value = SyncStatus.OFFLINE
                return
            }

            // Check if sync is enabled
            if (!userPreferences.cloudSyncEnabled.first()) {
                _syncStatus.value = SyncStatus.DISABLED
                return
            }

            _syncStatus.value = SyncStatus.SYNCING
            
            val userId = auth.currentUser?.uid ?: throw IllegalStateException("User not signed in")

            // Check storage limit
            val storageUsage = getStorageUsage().getOrThrow()
            val storageLimit = getStorageLimit().getOrThrow()
            if (storageUsage >= storageLimit) {
                _syncStatus.value = SyncStatus.STORAGE_FULL
                return
            }

            // Start sync worker
            val syncWorkRequest = OneTimeWorkRequestBuilder<SyncWorker>()
                .setExpedited(OutOfQuotaPolicy.RUN_AS_NON_EXPEDITED_WORK_REQUEST)
                .setConstraints(
                    Constraints.Builder()
                        .setRequiredNetworkType(NetworkType.CONNECTED)
                        .build()
                )
                .build()
            
            currentWorkId = syncWorkRequest.id
            workManager.enqueueUniqueWork(
                SYNC_WORK_NAME,
                ExistingWorkPolicy.REPLACE,
                syncWorkRequest
            )

            // Schedule periodic sync after immediate sync
            schedulePeriodicSync()

            // Update last sync time
            userPreferences.setLastSyncTime(System.currentTimeMillis())
        } catch (e: Exception) {
            when (e) {
                is CancellationException -> throw e
                else -> {
                    _syncStatus.value = SyncStatus.ERROR
                    _syncErrors.update { it + e.message.orEmpty() }
                    throw e
                }
            }
        }
    }

    override suspend fun cancelSync() {
        currentWorkId?.let { workId ->
            workManager.cancelWorkById(workId)
            currentWorkId = null
        }
        _syncStatus.value = SyncStatus.IDLE
    }

    override suspend fun uploadNote(note: Note): Result<Unit> = runCatching {
        if (_syncStatus.value == SyncStatus.OFFLINE) {
            addPendingNote(note.id)
            return@runCatching
        }

        val userId = auth.currentUser?.uid ?: throw IllegalStateException("User not signed in")

        // Upload note data
        firestoreService.saveNote(userId, note).getOrThrow()

        // Upload audio file if exists
        note.audioPath?.let { path ->
            val audioFile = File(path)
            if (audioFile.exists()) {
                cloudStorageService.uploadFile(
                    userId,
                    "$AUDIO_FOLDER/${note.id}",
                    audioFile
                ).getOrThrow()
            }
        }

        // Upload summary if exists
        note.summary?.let { summary ->
            val summaryPath = "$SUMMARY_FOLDER/${note.id}"
            val summaryData = NoteSummary(
                noteId = note.id,
                summary = summary
            )
            firestoreService.saveNoteSummary(userId, summaryPath, summaryData).getOrThrow()
        }
    }

    override suspend fun uploadNotes(notes: List<Note>): Result<Unit> = runCatching {
        if (notes.isEmpty()) return@runCatching

        val userId = auth.currentUser?.uid ?: throw IllegalStateException("User not signed in")
        firestoreService.saveNotes(userId, notes).getOrThrow()

        notes.forEach { note ->
            note.audioPath?.let { path ->
                try {
                    val audioFile = File(path)
                    if (audioFile.exists()) {
                        cloudStorageService.uploadFile(
                            userId,
                            "$AUDIO_FOLDER/${note.id}",
                            audioFile
                        ).getOrThrow()
                    }
                } catch (e: Exception) {
                    _syncErrors.update { it + "Failed to upload audio for note ${note.id}: ${e.message}" }
                }
            }
        }
    }

    override suspend fun uploadFolders(folders: List<Folder>): Result<Unit> = runCatching {
        if (folders.isEmpty()) return@runCatching

        val userId = auth.currentUser?.uid ?: throw IllegalStateException("User not signed in")
        firestoreService.saveFolders(userId, folders).getOrThrow()
    }

    override suspend fun deleteNote(noteId: String): Result<Unit> = runCatching {
        val userId = auth.currentUser?.uid ?: throw IllegalStateException("User not signed in")
        
        // Delete note data
        firestoreService.deleteNote(userId, noteId).getOrThrow()
        
        // Delete audio file
        cloudStorageService.deleteFile(userId, "$AUDIO_FOLDER/$noteId")
            .onFailure { e -> 
                _syncErrors.update { it + "Failed to delete audio for note $noteId: ${e.message}" }
            }
        
        // Delete summary
        firestoreService.deleteNoteSummary(userId, "$SUMMARY_FOLDER/$noteId")
            .onFailure { e ->
                _syncErrors.update { it + "Failed to delete summary for note $noteId: ${e.message}" }
            }
    }

    override suspend fun deleteFolder(folderId: String): Result<Unit> = runCatching {
        val userId = auth.currentUser?.uid ?: throw IllegalStateException("User not signed in")
        firestoreService.deleteFolder(userId, folderId).getOrThrow()
    }

    override suspend fun downloadNote(noteId: String): Result<Note> = runCatching {
        val userId = auth.currentUser?.uid ?: throw IllegalStateException("User not signed in")
        
        // Get note data
        val note = firestoreService.getNote(userId, noteId).getOrThrow()

        // Download audio if exists
        if (note.hasAudio) {
            val audioFile = cloudStorageService.downloadFile(
                userId,
                "$AUDIO_FOLDER/$noteId"
            ).getOrNull()
            
            audioFile?.let { file ->
                note.audioPath = file.absolutePath
            }
        }

        // Get summary if exists
        if (note.hasSummary) {
            val summary = firestoreService.getNoteSummary(userId, "$SUMMARY_FOLDER/$noteId").getOrNull()
            summary?.let {
                note.summary = it.summary
            }
        }

        note
    }

    override suspend fun syncPendingChanges() {
        if (_syncStatus.value == SyncStatus.SYNCING) return

        try {
            val pendingNotes = getPendingNotes()
            if (pendingNotes.isEmpty()) return

            _syncStatus.value = SyncStatus.SYNCING

            pendingNotes.forEach { noteId ->
                firestoreService.getNote(auth.currentUser?.uid ?: return, noteId)
                    .onSuccess { cloudNote ->
                        val localNote = firestoreService.getNote(auth.currentUser?.uid ?: return, noteId).getOrNull()
                        if (localNote != null && localNote.updatedAt > cloudNote.updatedAt) {
                            uploadNote(localNote)
                        }
                    }
            }

            clearPendingNotes()
            _syncStatus.value = SyncStatus.IDLE
        } catch (e: Exception) {
            _syncStatus.value = SyncStatus.ERROR
            _syncErrors.update { it + e.message.orEmpty() }
        }
    }

    override suspend fun schedulePeriodicSync() {
        val constraints = Constraints.Builder()
            .setRequiredNetworkType(NetworkType.CONNECTED)
            .build()

        val periodicWorkRequest = PeriodicWorkRequestBuilder<SyncWorker>(
            MIN_SYNC_INTERVAL,
            TimeUnit.MINUTES
        )
            .setConstraints(constraints)
            .build()

        workManager.enqueueUniquePeriodicWork(
            PERIODIC_SYNC_WORK_NAME,
            ExistingPeriodicWorkPolicy.REPLACE,
            periodicWorkRequest
        )
    }

    override suspend fun cancelPeriodicSync() {
        workManager.cancelUniqueWork(PERIODIC_SYNC_WORK_NAME)
    }

    private suspend fun addPendingNote(noteId: String) {
        userPreferences.addPendingNote(noteId)
    }

    private suspend fun getPendingNotes(): List<String> {
        return userPreferences.pendingNotes.first()
    }

    private suspend fun clearPendingNotes() {
        userPreferences.clearPendingNotes()
    }

    private suspend fun getStorageUsage(): Result<Long> = runCatching {
        cloudStorageService.getStorageUsage(auth.currentUser?.uid ?: throw IllegalStateException("User not signed in"))
    }

    private suspend fun getStorageLimit(): Result<Long> = runCatching {
        cloudStorageService.getStorageLimit(auth.currentUser?.uid ?: throw IllegalStateException("User not signed in"))
    }
} 