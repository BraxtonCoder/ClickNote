package com.example.clicknote.data.service

import com.example.clicknote.domain.model.Note
import com.example.clicknote.domain.model.Folder
import com.example.clicknote.domain.service.FirestoreService
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import com.google.firebase.firestore.Transaction as FirebaseTransaction
import com.google.firebase.firestore.WriteBatch as FirebaseWriteBatch
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.tasks.await
import kotlinx.coroutines.withContext
import kotlinx.coroutines.Dispatchers
import javax.inject.Inject
import javax.inject.Singleton
import java.time.LocalDateTime

@Singleton
class FirestoreServiceImpl @Inject constructor(
    private val firestore: FirebaseFirestore
) : FirestoreService {

    override suspend fun saveNote(userId: String, note: Note): Result<Unit> = runCatching {
        val noteRef = firestore
            .collection("users")
            .document(userId)
            .collection("notes")
            .document(note.id)

        // Check for conflicts
        val existingNote = noteRef.get().await()
        if (existingNote.exists()) {
            val cloudNote = existingNote.toObject(Note::class.java)!!
            if (cloudNote.modifiedAt > note.modifiedAt) {
                throw IllegalStateException("Note conflict detected")
            }
        }

        noteRef.set(note).await()
    }

    override suspend fun saveNotes(userId: String, notes: List<Note>): Result<Unit> = runCatching {
        firestore.runBatch { batch ->
            notes.forEach { note ->
                val noteRef = firestore
                    .collection("users")
                    .document(userId)
                    .collection("notes")
                    .document(note.id)
                batch.set(noteRef, note)
            }
        }.await()
    }

    override suspend fun deleteNote(userId: String, noteId: String): Result<Unit> = runCatching {
        firestore
            .collection("users")
            .document(userId)
            .collection("notes")
            .document(noteId)
            .delete()
            .await()
    }

    override suspend fun getNote(userId: String, noteId: String): Result<Note> = runCatching {
        val noteDoc = firestore
            .collection("users")
            .document(userId)
            .collection("notes")
            .document(noteId)
            .get()
            .await()

        noteDoc.toObject(Note::class.java)
            ?: throw IllegalStateException("Note not found")
    }

    override suspend fun getNotes(userId: String): Result<List<Note>> = runCatching {
        val notesSnapshot = firestore
            .collection("users")
            .document(userId)
            .collection("notes")
            .orderBy("modifiedAt", Query.Direction.DESCENDING)
            .get()
            .await()

        notesSnapshot.documents.mapNotNull { it.toObject(Note::class.java) }
    }

    override suspend fun saveFolder(userId: String, folder: Folder): Result<Unit> = runCatching {
        firestore
            .collection("users")
            .document(userId)
            .collection("folders")
            .document(folder.id)
            .set(folder)
            .await()
    }

    override suspend fun saveFolders(userId: String, folders: List<Folder>): Result<Unit> = runCatching {
        firestore.runBatch { batch ->
            folders.forEach { folder ->
                val folderRef = firestore
                    .collection("users")
                    .document(userId)
                    .collection("folders")
                    .document(folder.id)
                batch.set(folderRef, folder)
            }
        }.await()
    }

    override suspend fun deleteFolder(userId: String, folderId: String): Result<Unit> = runCatching {
        // Check if folder has notes
        val notesInFolder = firestore
            .collection("users")
            .document(userId)
            .collection("notes")
            .whereEqualTo("folderId", folderId)
            .get()
            .await()

        if (!notesInFolder.isEmpty) {
            throw IllegalStateException("Cannot delete folder with notes")
        }

        firestore
            .collection("users")
            .document(userId)
            .collection("folders")
            .document(folderId)
            .delete()
            .await()
    }

    override suspend fun getFolder(userId: String, folderId: String): Result<Folder> = runCatching {
        val folderDoc = firestore
            .collection("users")
            .document(userId)
            .collection("folders")
            .document(folderId)
            .get()
            .await()

        folderDoc.toObject(Folder::class.java)
            ?: throw IllegalStateException("Folder not found")
    }

    override suspend fun getFolders(userId: String): Result<List<Folder>> = runCatching {
        val foldersSnapshot = firestore
            .collection("users")
            .document(userId)
            .collection("folders")
            .orderBy("name")
            .get()
            .await()

        foldersSnapshot.documents.mapNotNull { it.toObject(Folder::class.java) }
    }

    override suspend fun getUserStorageLimit(userId: String): Result<Long> = runCatching {
        val userDoc = firestore
            .collection("users")
            .document(userId)
            .get()
            .await()

        when (userDoc.getString("plan")) {
            "free" -> 100L * 1024 * 1024 // 100MB
            "monthly" -> 10L * 1024 * 1024 * 1024 // 10GB
            "annual" -> 20L * 1024 * 1024 * 1024 // 20GB
            else -> 5L * 1024 * 1024 * 1024 // 5GB default
        }
    }

    override suspend fun getUserStorageUsage(userId: String): Result<Long> = runCatching {
        val userDoc = firestore
            .collection("users")
            .document(userId)
            .get()
            .await()

        userDoc.getLong("storageUsage") ?: 0L
    }

    override fun getChanges(userId: String): Flow<List<String>> = callbackFlow {
        val notesListener = firestore
            .collection("users")
            .document(userId)
            .collection("notes")
            .addSnapshotListener { snapshot, error ->
                if (error != null) {
                    close(error)
                    return@addSnapshotListener
                }
                
                val changedIds = snapshot?.documentChanges?.map { it.document.id } ?: emptyList()
                trySend(changedIds)
            }

        awaitClose {
            notesListener.remove()
        }
    }

    override suspend fun <T> runTransaction(action: suspend (FirebaseTransaction) -> T): Result<T> = runCatching {
        withContext(Dispatchers.IO) {
            firestore.runTransaction { transaction ->
                action(transaction)
            }.await()
        }
    }

    override suspend fun runBatch(action: suspend (FirebaseWriteBatch) -> Unit): Result<Unit> = runCatching {
        withContext(Dispatchers.IO) {
            val batch = firestore.batch()
            action(batch)
            batch.commit().await()
        }
    }
} 