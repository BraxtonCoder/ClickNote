package com.example.clicknote.data.repository

import android.content.Context
import android.net.ConnectivityManager
import android.net.Network
import android.net.NetworkCapabilities
import android.net.NetworkRequest
import android.util.Log
import com.example.clicknote.domain.model.SyncStatus
import com.example.clicknote.domain.model.SyncError
import com.example.clicknote.domain.model.SyncErrorType
import com.example.clicknote.domain.model.CloudStorageType
import com.example.clicknote.domain.model.Note
import com.example.clicknote.domain.preferences.UserPreferencesDataStore
import com.example.clicknote.domain.repository.CloudSyncRepository
import com.example.clicknote.domain.repository.NoteRepository
import com.example.clicknote.domain.service.FirebaseService
import com.example.clicknote.di.qualifiers.ApplicationScope
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.*
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import java.io.File
import java.io.IOException
import java.util.*
import java.util.concurrent.TimeoutException
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

    private val _syncStatus = MutableStateFlow<SyncStatus>(SyncStatus.PENDING)
    override val syncStatus: StateFlow<SyncStatus> = _syncStatus.asStateFlow()

    private val _isSyncing = MutableStateFlow(false)
    override val isSyncing: StateFlow<Boolean> = _isSyncing.asStateFlow()

    private val _syncProgress = MutableStateFlow(0f)
    override val syncProgress: StateFlow<Float> = _syncProgress.asStateFlow()

    private val _syncErrors = MutableStateFlow<List<SyncError>>(emptyList())
    override val syncErrors: StateFlow<List<SyncError>> = _syncErrors.asStateFlow()

    override val cloudStorageType: StateFlow<CloudStorageType> = preferences.cloudStorageType.stateIn(
        scope = scope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = CloudStorageType.NONE
    )

    private var syncJob: Job? = null
    private var periodicSyncJob: Job? = null
    private val pendingNotesLock = Mutex()
    private val pendingNotes = Collections.synchronizedList(mutableListOf<Note>())

    private val syncMutex = Mutex()

    private val localStorageDir: File by lazy {
        File(context.filesDir, "local_storage").apply {
            if (!exists() && !mkdirs()) {
                Log.e(TAG, "Failed to create local storage directory")
            }
        }
    }

    private val localCloudDir: File by lazy {
        File(context.filesDir, "local_cloud").apply {
            if (!exists() && !mkdirs()) {
                Log.e(TAG, "Failed to create local cloud directory")
            }
        }
    }

    private val audioDir: File by lazy {
        File(context.filesDir, "audio").apply {
            if (!exists() && !mkdirs()) {
                Log.e(TAG, "Failed to create audio directory")
            }
        }
    }

    private val connectivityManager = context.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
    private val _isOnline = MutableStateFlow(false)
    override val isOnline: StateFlow<Boolean> = _isOnline.asStateFlow()

    private val networkCallback = object : ConnectivityManager.NetworkCallback() {
        override fun onAvailable(network: Network) {
            super.onAvailable(network)
            scope.launch {
                handleNetworkStateChange(true)
            }
        }

        override fun onLost(network: Network) {
            super.onLost(network)
            scope.launch {
                handleNetworkStateChange(false)
            }
        }

        override fun onCapabilitiesChanged(
            network: Network,
            networkCapabilities: NetworkCapabilities
        ) {
            super.onCapabilitiesChanged(network, networkCapabilities)
            scope.launch {
                val isOnline = networkCapabilities.hasCapability(NetworkCapabilities.NET_CAPABILITY_INTERNET)
                handleNetworkStateChange(isOnline)
            }
        }
    }

    private var lifecycleJob: Job? = null
    private var networkCallbackRegistered = false
    private val networkCallbackLock = Mutex()

    private val syncOperationLock = Mutex()
    private var currentSyncOperation: Job? = null

    private val _lastSyncTime = MutableStateFlow(0L)
    override val lastSyncTime: StateFlow<Long> = _lastSyncTime.asStateFlow()

    init {
        lifecycleJob = scope.launch {
            try {
                // Clean up any temporary files on initialization
                cleanupTemporaryFiles()
                
                // Enable offline support
                enableOfflineSupport()
                
                // Register network callback
                registerNetworkCallback()
                
                // Check initial network state
                _isOnline.value = isNetworkAvailable()

                // Monitor cloud storage type changes
                cloudStorageType.collect { type ->
                    if (_isOnline.value && type != CloudStorageType.LOCAL && type != CloudStorageType.NONE) {
                        startSync()
                    }
                }
            } catch (e: Exception) {
                Log.e(TAG, "Error in initialization: ${e.message}")
            }
        }
    }

    private fun isNetworkAvailable(): Boolean {
        val network = connectivityManager.activeNetwork
        val capabilities = connectivityManager.getNetworkCapabilities(network)
        return capabilities?.hasCapability(NetworkCapabilities.NET_CAPABILITY_INTERNET) == true
    }

    private fun cleanup() {
        scope.launch {
            try {
                // Cancel all ongoing jobs
                syncJob?.cancel()
                periodicSyncJob?.cancel()
                lifecycleJob?.cancel()

                // Reset all state
                _isSyncing.value = false
                _syncProgress.value = 0f
                _syncErrors.value = emptyList()
                updateSyncStatus(SyncStatus.IDLE)

                // Unregister network callback
                unregisterNetworkCallback()

                // Clean up temporary files
                cleanupTemporaryFiles()
            } catch (e: Exception) {
                Log.e(TAG, "Error during cleanup: ${e.message}")
            }
        }
    }

    private suspend fun cleanupTemporaryFiles() = withContext(Dispatchers.IO) {
        try {
            val tempDir = File(context.cacheDir, "temp")
            if (tempDir.exists()) {
                tempDir.deleteRecursively()
            }
        } catch (e: Exception) {
            Log.e(TAG, "Failed to cleanup temporary files: ${e.message}")
        }
    }

    private suspend fun updateSyncStatus(newStatus: SyncStatus) {
        _syncStatus.value = newStatus
    }

    private suspend fun addSyncError(error: Throwable, noteId: String? = null, type: SyncErrorType = SyncErrorType.UNKNOWN) {
        _syncErrors.value = _syncErrors.value + SyncError(
            message = error.message ?: "Unknown error",
            type = type,
            noteId = noteId
        )
    }

    private suspend fun resetSyncState() {
        _isSyncing.value = false
        _syncProgress.value = 0f
        updateSyncStatus(SyncStatus.IDLE)
    }

    override suspend fun getCloudStoragePreference(): CloudStorageType {
        return preferences.cloudStorageType.first()
    }

    override suspend fun setCloudStoragePreference(type: CloudStorageType) {
        preferences.setCloudStorageType(type)
    }

    private suspend fun retryWithBackoff(
        maxAttempts: Int = 3,
        initialDelayMillis: Long = 1000,
        shouldRetry: (Exception) -> Boolean = { true },
        operation: suspend () -> Unit
    ) {
        var currentDelay = initialDelayMillis
        var lastException: Exception? = null

        repeat(maxAttempts) { attempt ->
            try {
                operation()
                return
            } catch (e: CancellationException) {
                throw e
            } catch (e: Exception) {
                lastException = e
                if (!shouldRetry(e) || attempt == maxAttempts - 1) {
                    throw e
                }
                Log.w(TAG, "Retry attempt ${attempt + 1} failed: ${e.message}")
                delay(currentDelay)
                currentDelay *= 2
            }
        }

        lastException?.let { throw it }
    }

    private fun shouldRetryError(error: Exception): Boolean {
        return when (error) {
            is IOException -> true // Network errors
            is SecurityException -> false // Permission errors
            is IllegalStateException -> false // State errors
            is IllegalArgumentException -> false // Input errors
            else -> true // Unknown errors
        }
    }

    private suspend fun recoverFromError(error: Exception, noteId: String? = null) {
        when (error) {
            is IOException -> {
                // Network error - add to pending notes for retry
                noteId?.let { id ->
                    noteRepository.getNoteById(id).onSuccess { note ->
                        addPendingNote(note)
                    }
                }
                updateSyncStatus(SyncStatus.OFFLINE)
            }
            is SecurityException -> {
                // Permission error - notify user
                addSyncError(error, noteId, SyncErrorType.PERMISSION)
                updateSyncStatus(SyncStatus.FAILED)
            }
            else -> {
                // Unknown error - log and notify
                addSyncError(error, noteId)
                updateSyncStatus(SyncStatus.FAILED)
            }
        }
    }

    private suspend fun startSyncOperation(operation: suspend () -> Unit) {
        syncOperationLock.withLock {
            try {
                currentSyncOperation?.cancelAndJoin()
                currentSyncOperation = scope.launch {
                    operation()
                }
                currentSyncOperation?.join()
            } catch (e: CancellationException) {
                throw e
            } catch (e: Exception) {
                Log.e(TAG, "Sync operation failed: ${e.message}")
                throw e
            } finally {
                currentSyncOperation = null
            }
        }
    }

    override suspend fun startSync() {
        if (_isSyncing.value) return

        scope.launch {
            startSyncOperation {
                try {
                    _isSyncing.value = true
                    _syncProgress.value = 0f
                    updateSyncStatus(SyncStatus.SYNCING)

                    val notes = noteRepository.getAllNotes().getOrNull() ?: emptyList()
                    if (notes.isEmpty()) {
                        updateSyncStatus(SyncStatus.SUCCESS)
                        return@startSyncOperation
                    }

                    val total = notes.size.toFloat()
                    var current = 0f
                    var hasErrors = false

                    notes.forEach { note ->
                        try {
                            retryWithBackoff(
                                shouldRetry = ::shouldRetryError
                            ) {
                                syncNote(note.id).onFailure { error ->
                                    throw error
                                }
                            }
                        } catch (e: CancellationException) {
                            throw e
                        } catch (e: Exception) {
                            hasErrors = true
                            addSyncError(e, note.id)
                        }
                        current++
                        _syncProgress.value = current / total
                    }

                    _syncProgress.value = 1f
                    updateSyncStatus(if (hasErrors) SyncStatus.COMPLETED_WITH_ERRORS else SyncStatus.SUCCESS)
                } catch (e: CancellationException) {
                    updateSyncStatus(SyncStatus.CANCELLED)
                    throw e
                } catch (e: Exception) {
                    addSyncError(e)
                    updateSyncStatus(SyncStatus.ERROR)
                } finally {
                    _isSyncing.value = false
                    _syncProgress.value = 0f
                }
            }
        }
    }

    override suspend fun syncNote(noteId: String): Result<Unit> = runCatching {
        scope.launch {
            startSyncOperation {
                updateSyncStatus(SyncStatus.SYNCING)
                try {
                    val note = noteRepository.getNoteById(noteId).getOrThrow()
                    
                    when (getCloudStoragePreference()) {
                        CloudStorageType.LOCAL -> {
                            retryWithBackoff(
                                shouldRetry = ::shouldRetryError
                            ) {
                                val noteDir = File(localStorageDir, note.id)
                                val noteFile = File(noteDir, "note.json")
                                val summaryFile = File(noteDir, "summary.txt")

                                writeNoteData(note, noteFile)
                                note.summary?.let { summary ->
                                    writeSummary(summary, summaryFile)
                                }

                                note.audioPath?.let { audioPath ->
                                    val sourceAudio = File(audioPath)
                                    val destAudio = File(noteDir, "audio.${sourceAudio.extension}")
                                    copyAudioFile(sourceAudio, destAudio)
                                }
                            }
                        }
                        CloudStorageType.LOCAL_CLOUD,
                        CloudStorageType.FIREBASE -> {
                            try {
                                retryWithBackoff(
                                    shouldRetry = ::shouldRetryError
                                ) {
                                    syncNoteToCloud(note).getOrThrow()
                                }
                            } catch (e: Exception) {
                                handleSyncConflict(note, e)
                            }
                        }
                        else -> {
                            // No sync needed
                        }
                    }
                    updateSyncStatus(SyncStatus.SUCCESS)
                } catch (e: Exception) {
                    recoverFromError(e, noteId)
                    throw e
                }
            }
        }.join()
    }

    override suspend fun syncAudio(audioFile: File): Result<String> = runCatching {
        updateSyncStatus(SyncStatus.SYNCING)
        try {
            val result = when (getCloudStoragePreference()) {
                CloudStorageType.LOCAL -> audioFile.absolutePath
                CloudStorageType.LOCAL_CLOUD -> syncAudioToLocalCloud(audioFile.absolutePath, UUID.randomUUID().toString()).getOrThrow()
                CloudStorageType.FIREBASE -> syncAudioToFirebase(audioFile.absolutePath, UUID.randomUUID().toString()).getOrThrow()
                CloudStorageType.GOOGLE_DRIVE,
                CloudStorageType.DROPBOX,
                CloudStorageType.ONEDRIVE,
                CloudStorageType.NONE -> audioFile.absolutePath
            }
            updateSyncStatus(SyncStatus.SUCCESS)
            result
        } catch (e: Exception) {
            updateSyncStatus(SyncStatus.FAILED)
            throw e
        }
    }

    private suspend fun copyAudioFile(sourceFile: File, destinationFile: File) {
        if (!sourceFile.exists()) {
            throw IllegalStateException("Source audio file does not exist: ${sourceFile.absolutePath}")
        }

        destinationFile.parentFile?.let { dir ->
            if (!dir.exists() && !dir.mkdirs()) {
                throw IOException("Failed to create directory: ${dir.absolutePath}")
            }
        }

        try {
            sourceFile.inputStream().use { input ->
                destinationFile.outputStream().use { output ->
                    input.copyTo(output)
                }
            }
        } catch (e: Exception) {
            throw IOException("Failed to copy audio file from ${sourceFile.absolutePath} to ${destinationFile.absolutePath}", e)
        }
    }

    private suspend fun writeNoteData(note: Note, file: File) {
        file.parentFile?.let { dir ->
            if (!dir.exists() && !dir.mkdirs()) {
                throw IOException("Failed to create directory: ${dir.absolutePath}")
            }
        }

        try {
            withContext(Dispatchers.IO) {
                file.writeText(Json.encodeToString(note))
            }
        } catch (e: Exception) {
            throw IOException("Failed to write note data to file: ${file.absolutePath}", e)
        }
    }

    private suspend fun writeSummary(summary: String, file: File) {
        file.parentFile?.let { dir ->
            if (!dir.exists() && !dir.mkdirs()) {
                throw IOException("Failed to create directory: ${dir.absolutePath}")
            }
        }

        try {
            withContext(Dispatchers.IO) {
                file.writeText(summary)
            }
        } catch (e: Exception) {
            throw IOException("Failed to write summary to file: ${file.absolutePath}", e)
        }
    }

    private suspend fun syncAudioToLocalCloud(path: String, noteId: String): Result<String> = runCatching {
        val sourceFile = File(path)
        if (!sourceFile.exists()) {
            throw IllegalStateException("Source audio file does not exist: $path")
        }

        val destDir = File(localCloudDir, "audio").also { dir ->
            if (!dir.exists() && !dir.mkdirs()) {
                throw IOException("Failed to create directory: ${dir.absolutePath}")
            }
        }

        val destFile = File(destDir, "$noteId.m4a")
        var tempFile: File? = null
        
        try {
            // Create temporary file for atomic copy
            tempFile = File(context.cacheDir, "temp_${System.currentTimeMillis()}_$noteId.m4a")
            sourceFile.copyTo(tempFile, overwrite = true)
            
            // Atomic move to final destination
            if (!tempFile.renameTo(destFile)) {
                throw IOException("Failed to move audio file to final destination")
            }
            
            destFile.absolutePath
        } catch (e: Exception) {
            tempFile?.delete()
            throw IOException("Failed to copy audio file: ${e.message}", e)
        }
    }

    private suspend fun syncNoteDataToLocalCloud(note: Note): Result<Unit> = runCatching {
        val destDir = File(localCloudDir, "notes").also { dir ->
            if (!dir.exists() && !dir.mkdirs()) {
                throw IOException("Failed to create directory: ${dir.absolutePath}")
            }
        }

        val destFile = File(destDir, "${note.id}.json")
        var tempFile: File? = null
        
        try {
            // Create temporary file for atomic write
            tempFile = File(context.cacheDir, "temp_${System.currentTimeMillis()}_${note.id}.json")
            tempFile.writeText(Json.encodeToString(note))
            
            // Atomic move to final destination
            if (!tempFile.renameTo(destFile)) {
                throw IOException("Failed to move note data to final destination")
            }
        } catch (e: Exception) {
            tempFile?.delete()
            throw IOException("Failed to write note data: ${e.message}", e)
        }
    }

    private suspend fun syncSummaryToLocalCloud(summary: String, noteId: String): Result<Unit> = runCatching {
        val destDir = File(localCloudDir, "summaries").also { dir ->
            if (!dir.exists() && !dir.mkdirs()) {
                throw IOException("Failed to create directory: ${dir.absolutePath}")
            }
        }

        val destFile = File(destDir, "$noteId.txt")
        var tempFile: File? = null
        
        try {
            // Create temporary file for atomic write
            tempFile = File(context.cacheDir, "temp_${System.currentTimeMillis()}_$noteId.txt")
            tempFile.writeText(summary)
            
            // Atomic move to final destination
            if (!tempFile.renameTo(destFile)) {
                throw IOException("Failed to move summary to final destination")
            }
        } catch (e: Exception) {
            tempFile?.delete()
            throw IOException("Failed to write summary: ${e.message}", e)
        }
    }

    private suspend fun syncAudioToFirebase(path: String, noteId: String): Result<String> = runCatching {
        firebaseService.uploadAudio(File(path), noteId).getOrThrow()
    }

    override suspend fun startPeriodicSync(intervalMillis: Long) {
        stopPeriodicSync() // Ensure any existing periodic sync is stopped

        periodicSyncJob = scope.launch {
            while (isActive) {
                try {
                    if (!_isOnline.value) {
                        Log.d(TAG, "Skipping periodic sync as device is offline")
                        delay(intervalMillis)
                        continue
                    }

                    syncMutex.withLock {
                        if (_isSyncing.value) {
                            Log.d(TAG, "Skipping periodic sync as sync is already in progress")
                            return@withLock
                        }

                        val pendingNotes = getPendingNotes()
                        if (pendingNotes.isEmpty()) {
                            Log.d(TAG, "No pending notes to sync")
                            return@withLock
                        }

                        startSyncOperation {
                            try {
                                _isSyncing.value = true
                                updateSyncStatus(SyncStatus.SYNCING)

                                var current = 0
                                val total = pendingNotes.size.toFloat()
                                var hasErrors = false

                                pendingNotes.forEach { note ->
                                    ensureActive()
                                    try {
                                        retryWithBackoff(
                                            shouldRetry = ::shouldRetryError
                                        ) {
                                            syncNote(note.id).onFailure { error ->
                                                throw error
                                            }
                                        }
                                    } catch (e: CancellationException) {
                                        throw e
                                    } catch (e: Exception) {
                                        hasErrors = true
                                        addSyncError(e, note.id, SyncErrorType.NETWORK)
                                        Log.e(TAG, "Failed to sync note ${note.id}: ${e.message}")
                                    }
                                    current++
                                    _syncProgress.value = current / total
                                }

                                clearPendingNotes()
                                _syncProgress.value = 1f
                                updateSyncStatus(if (hasErrors) SyncStatus.COMPLETED_WITH_ERRORS else SyncStatus.SUCCESS)
                                Log.d(TAG, "Periodic sync completed" + if (hasErrors) " with errors" else "")
                            } finally {
                                _isSyncing.value = false
                                _syncProgress.value = 0f
                            }
                        }
                    }

                    delay(intervalMillis)
                } catch (e: CancellationException) {
                    Log.d(TAG, "Periodic sync cancelled")
                    updateSyncStatus(SyncStatus.CANCELLED)
                    throw e
                } catch (e: Exception) {
                    Log.e(TAG, "Periodic sync failed: ${e.message}")
                    addSyncError(e)
                    updateSyncStatus(SyncStatus.FAILED)
                    delay(intervalMillis) // Wait before retrying after error
                }
            }
        }
    }

    override suspend fun stopPeriodicSync() {
        Log.d(TAG, "Stopping periodic sync")
        periodicSyncJob?.let { job ->
            job.cancel()
            try {
                // Wait for job to complete cancellation
                withTimeout(5000) {
                    job.join()
                }
            } catch (e: TimeoutException) {
                Log.w(TAG, "Timeout waiting for periodic sync to stop")
            }
        }
        periodicSyncJob = null
        resetSyncState()
    }

    override suspend fun addPendingNote(note: Note) {
        pendingNotesLock.withLock {
            pendingNotes.add(note)
        }
    }

    override suspend fun getPendingNotes(): List<Note> {
        return pendingNotesLock.withLock {
            pendingNotes.toList()
        }
    }

    override suspend fun clearPendingNotes() = pendingNotesLock.withLock {
        pendingNotes.clear()
    }

    private fun calculateDirectorySize(dir: File): Long {
        if (!dir.exists()) return 0L
        
        return try {
            dir.walkTopDown()
                .filter { it.isFile }
                .sumOf { it.length() }
        } catch (e: SecurityException) {
            Log.e(TAG, "Failed to calculate directory size: ${e.message}")
            0L
        }
    }

    override suspend fun getStorageUsage(): Result<Long> = runCatching {
        when (getCloudStoragePreference()) {
            CloudStorageType.LOCAL -> {
                if (!localStorageDir.exists() && !localStorageDir.mkdirs()) {
                    throw IOException("Failed to create local storage directory")
                }
                calculateDirectorySize(localStorageDir)
            }
            CloudStorageType.LOCAL_CLOUD -> {
                if (!localCloudDir.exists() && !localCloudDir.mkdirs()) {
                    throw IOException("Failed to create local cloud directory")
                }
                calculateDirectorySize(localCloudDir)
            }
            else -> 0L
        }
    }

    override suspend fun getStorageLimit(): Result<Long> = runCatching {
        when (getCloudStoragePreference()) {
            CloudStorageType.LOCAL -> {
                val freeSpace = localStorageDir.freeSpace
                maxOf(0L, minOf(freeSpace, LOCAL_STORAGE_LIMIT))
            }
            CloudStorageType.LOCAL_CLOUD -> {
                val freeSpace = localCloudDir.freeSpace
                maxOf(0L, minOf(freeSpace, LOCAL_CLOUD_STORAGE_LIMIT))
            }
            else -> 0L
        }
    }

    private suspend fun enableOfflineSupport() {
        when (getCloudStoragePreference()) {
            CloudStorageType.LOCAL_CLOUD,
            CloudStorageType.FIREBASE -> {
                try {
                    firebaseService.enableOfflineSupport().getOrThrow()
                } catch (e: Exception) {
                    Log.e(TAG, "Failed to enable offline support: ${e.message}")
                }
            }
            else -> {
                // No offline support needed for local storage
            }
        }
    }

    private suspend fun handleNetworkStateChange(isOnline: Boolean) {
        _isOnline.value = isOnline
        if (isOnline) {
            when (getCloudStoragePreference()) {
                CloudStorageType.LOCAL_CLOUD,
                CloudStorageType.FIREBASE -> {
                    // Sync pending changes when back online
                    val pendingNotes = getPendingNotes()
                    if (pendingNotes.isNotEmpty()) {
                        startSync()
                    }
                }
                else -> {
                    // No cloud sync needed
                }
            }
        } else {
            updateSyncStatus(SyncStatus.OFFLINE)
        }
    }

    private suspend fun registerNetworkCallback() = networkCallbackLock.withLock {
        if (!networkCallbackRegistered) {
            try {
                val networkRequest = NetworkRequest.Builder()
                    .addCapability(NetworkCapabilities.NET_CAPABILITY_INTERNET)
                    .build()
                connectivityManager.registerNetworkCallback(networkRequest, networkCallback)
                networkCallbackRegistered = true
                Log.d(TAG, "Network callback registered successfully")
            } catch (e: Exception) {
                Log.e(TAG, "Failed to register network callback: ${e.message}")
            }
        }
    }

    private suspend fun unregisterNetworkCallback() = networkCallbackLock.withLock {
        if (networkCallbackRegistered) {
            try {
                connectivityManager.unregisterNetworkCallback(networkCallback)
                networkCallbackRegistered = false
                Log.d(TAG, "Network callback unregistered successfully")
            } catch (e: IllegalArgumentException) {
                // Callback may already be unregistered
                Log.w(TAG, "Network callback already unregistered: ${e.message}")
            } catch (e: Exception) {
                Log.e(TAG, "Failed to unregister network callback: ${e.message}")
            }
        }
    }

    private suspend fun syncNoteToCloud(note: Note): Result<Unit> = runCatching {
        when (getCloudStoragePreference()) {
            CloudStorageType.LOCAL_CLOUD -> {
                syncNoteDataToLocalCloud(note).getOrThrow()
                note.audioPath?.let { path ->
                    syncAudioToLocalCloud(path, note.id).getOrThrow()
                }
                note.summary?.let { summary ->
                    syncSummaryToLocalCloud(summary, note.id).getOrThrow()
                }
            }
            CloudStorageType.FIREBASE -> {
                firebaseService.syncNote(note).getOrThrow()
            }
            else -> throw IllegalStateException("Cloud sync not supported for ${getCloudStoragePreference()}")
        }
    }

    private suspend fun handleSyncConflict(note: Note, error: Exception): Result<Unit> {
        updateSyncStatus(SyncStatus.CONFLICT)
        addSyncError(error, note.id, SyncErrorType.CONFLICT)
        
        return when (getCloudStoragePreference()) {
            CloudStorageType.LOCAL -> {
                // For local storage, local version always wins
                Result.success(Unit)
            }
            CloudStorageType.LOCAL_CLOUD -> {
                try {
                    // Get both versions
                    val localNote = noteRepository.getNoteById(note.id).getOrNull()
                    val cloudNote = firebaseService.getNote(note.id).getOrNull()

                    if (localNote == null || cloudNote == null) {
                        return Result.failure(error)
                    }

                    // Compare timestamps
                    if (localNote.modifiedAt > cloudNote.modifiedAt) {
                        // Local version is newer, force update
                        syncNoteToCloud(localNote)
                    } else {
                        // Cloud version is newer, update local
                        noteRepository.updateNote(cloudNote)
                    }
                    Result.success(Unit)
                } catch (e: Exception) {
                    Result.failure(e)
                }
            }
            else -> Result.failure(error)
        }
    }

    companion object {
        private const val TAG = "CloudSyncRepositoryImpl"
        private const val LOCAL_STORAGE_LIMIT = 10L * 1024 * 1024 * 1024 // 10GB
        private const val LOCAL_CLOUD_STORAGE_LIMIT = 50L * 1024 * 1024 * 1024 // 50GB
    }
} 