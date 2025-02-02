package com.example.clicknote.sync

import com.example.clicknote.data.model.Note
import com.example.clicknote.data.model.Folder
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.SetOptions
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.tasks.await
import kotlinx.coroutines.flow.flow
import java.util.Date
import javax.inject.Inject
import javax.inject.Singleton
import com.example.clicknote.util.NetworkUtil
import com.example.clicknote.data.preferences.UserPreferencesDataStore
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

@Singleton
class SyncManager @Inject constructor(
    private val firestore: FirebaseFirestore,
    private val auth: FirebaseAuth,
    private val networkUtil: NetworkUtil,
    private val userPreferences: UserPreferencesDataStore
) {
    private val _syncState = MutableStateFlow<SyncState>(SyncState.Idle)
    val syncState: Flow<SyncState> = _syncState

    private val scope = CoroutineScope(Dispatchers.IO)

    suspend fun syncNotes(notes: List<Note>): Result<Unit> = withContext(Dispatchers.IO) {
        try {
            if (!networkUtil.isNetworkAvailable()) {
                return@withContext Result.failure(Exception("No network connection"))
            }

            val userId = auth.currentUser?.uid ?: return@withContext Result.failure(
                Exception("User not authenticated")
            )

            _syncState.value = SyncState.Syncing

            // Get server timestamps
            val serverNotes = getServerNotes(userId)
            
            // Resolve conflicts and prepare updates
            val (localUpdates, serverUpdates) = resolveConflicts(notes, serverNotes)

            // Batch update to Firestore
            val batch = firestore.batch()
            
            localUpdates.forEach { note ->
                val noteRef = firestore.collection("users")
                    .document(userId)
                    .collection("notes")
                    .document(note.id)
                
                batch.set(noteRef, note.toFirestoreMap(), SetOptions.merge())
            }
            
            batch.commit().await()

            // Update last sync timestamp
            userPreferences.setLastSyncTimestamp(Date().time)
            
            _syncState.value = SyncState.Success
            Result.success(Unit)
        } catch (e: Exception) {
            _syncState.value = SyncState.Error(e.message ?: "Sync failed")
            Result.failure(e)
        }
    }

    private suspend fun getServerNotes(userId: String): List<Note> {
        return firestore.collection("users")
            .document(userId)
            .collection("notes")
            .get()
            .await()
            .documents
            .mapNotNull { doc ->
                try {
                    Note.fromFirestore(doc)
                } catch (e: Exception) {
                    null
                }
            }
    }

    private fun resolveConflicts(
        localNotes: List<Note>,
        serverNotes: List<Note>
    ): Pair<List<Note>, List<Note>> {
        val localUpdates = mutableListOf<Note>()
        val serverUpdates = mutableListOf<Note>()

        val serverMap = serverNotes.associateBy { it.id }
        val localMap = localNotes.associateBy { it.id }

        // Handle local updates
        localNotes.forEach { localNote ->
            val serverNote = serverMap[localNote.id]
            if (serverNote == null || localNote.modifiedAt > serverNote.modifiedAt) {
                localUpdates.add(localNote)
            }
        }

        // Handle server updates
        serverNotes.forEach { serverNote ->
            val localNote = localMap[serverNote.id]
            if (localNote == null || serverNote.modifiedAt > localNote.modifiedAt) {
                serverUpdates.add(serverNote)
            }
        }

        return Pair(localUpdates, serverUpdates)
    }

    fun startAutoSync() {
        scope.launch {
            // Monitor network connectivity and trigger sync when online
            networkUtil.networkAvailable.collect { isAvailable ->
                if (isAvailable) {
                    val lastSync = userPreferences.getLastSyncTimestamp()
                    val currentTime = System.currentTimeMillis()
                    
                    // Sync if more than 15 minutes have passed
                    if (currentTime - lastSync > 15 * 60 * 1000) {
                        // TODO: Get notes from local database and sync
                    }
                }
            }
        }
    }
}

sealed class SyncState {
    data object Idle : SyncState()
    data object Syncing : SyncState()
    data object Success : SyncState()
    data class Error(val message: String) : SyncState()
} 