package com.example.clicknote.service.impl

import android.content.Context
import com.example.clicknote.domain.model.Note
import com.example.clicknote.domain.repository.NoteRepository
import com.example.clicknote.domain.repository.PreferencesRepository
import com.example.clicknote.service.CloudSyncService
import com.example.clicknote.service.SyncStatus
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.tasks.await
import kotlinx.coroutines.withContext
import javax.inject.Inject
import javax.inject.Singleton
import dagger.Lazy

@Singleton
class CloudSyncServiceImpl @Inject constructor(
    @ApplicationContext private val context: Context,
    private val firestore: Lazy<FirebaseFirestore>,
    private val auth: Lazy<FirebaseAuth>,
    private val noteRepository: Lazy<NoteRepository>,
    private val preferencesRepository: Lazy<PreferencesRepository>
) : CloudSyncService {

    private val _syncStatus = MutableStateFlow<SyncStatus>(SyncStatus.IDLE)
    private val notesCollection = firestore.get().collection("notes")

    override suspend fun syncNotes() = withContext(Dispatchers.IO) {
        if (auth.get().currentUser == null) {
            _syncStatus.value = SyncStatus.ERROR
            throw IllegalStateException("User not authenticated")
        }

        try {
            _syncStatus.value = SyncStatus.SYNCING
            val userId = auth.get().currentUser!!.uid

            // Get all remote notes for the user
            val remoteNotes = notesCollection
                .whereEqualTo("userId", userId)
                .get()
                .await()
                .mapNotNull { it.toNote() }

            // Get all local notes
            val localNotes = noteRepository.get().getAllNotes().getOrThrow()

            // Delete local notes that don't exist remotely
            localNotes.forEach { localNote ->
                if (remoteNotes.none { it.id == localNote.id }) {
                    noteRepository.get().deleteNote(localNote.id)
                }
            }

            // Update local notes with remote changes
            remoteNotes.forEach { remoteNote ->
                val localNote = localNotes.find { it.id == remoteNote.id }
                if (localNote == null || remoteNote.updatedAt > localNote.updatedAt) {
                    noteRepository.get().insertNote(remoteNote)
                }
            }

            // Upload local changes
            localNotes.forEach { localNote ->
                val remoteNote = remoteNotes.find { it.id == localNote.id }
                if (remoteNote == null || remoteNote.updatedAt < localNote.updatedAt) {
                    uploadNote(localNote)
                }
            }

            _syncStatus.value = SyncStatus.SUCCESS
        } catch (e: Exception) {
            _syncStatus.value = SyncStatus.ERROR
            throw e
        }
    }

    override suspend fun uploadNote(note: Note) = withContext(Dispatchers.IO) {
        val userId = auth.get().currentUser?.uid ?: return@withContext
        val noteMap = mapOf(
            "id" to note.id,
            "userId" to userId,
            "content" to note.content,
            "createdAt" to note.createdAt,
            "updatedAt" to note.updatedAt,
            "audioPath" to note.audioPath,
            "folderId" to note.folderId,
            "isPinned" to note.isPinned
        )
        notesCollection.document(note.id).set(noteMap).await()
    }

    override suspend fun downloadNote(noteId: String): Note? = withContext(Dispatchers.IO) {
        val userId = auth.get().currentUser?.uid ?: return@withContext null
        val doc = notesCollection
            .document(noteId)
            .get()
            .await()
        
        if (doc.exists() && doc.getString("userId") == userId) {
            doc.toNote()
        } else {
            null
        }
    }

    override suspend fun deleteNote(noteId: String) = withContext(Dispatchers.IO) {
        notesCollection.document(noteId).delete().await()
    }

    override fun getSyncStatus(): Flow<SyncStatus> = _syncStatus.asStateFlow()

    override suspend fun enableSync(enabled: Boolean) {
        preferencesRepository.get().setSyncEnabled(enabled)
    }

    override suspend fun cleanup() {
        _syncStatus.value = SyncStatus.IDLE
    }

    private fun com.google.firebase.firestore.DocumentSnapshot.toNote(): Note? {
        return try {
            Note(
                id = getString("id") ?: return null,
                content = getString("content") ?: return null,
                createdAt = getLong("createdAt") ?: return null,
                updatedAt = getLong("updatedAt") ?: return null,
                audioPath = getString("audioPath"),
                folderId = getString("folderId"),
                isPinned = getBoolean("isPinned") ?: false
            )
        } catch (e: Exception) {
            null
        }
    }
} 