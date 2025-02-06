package com.example.clicknote.data.repository

import android.content.Context
import com.example.clicknote.data.dao.NoteDao
import com.example.clicknote.data.entity.NoteEntity
import com.example.clicknote.domain.model.Note
import com.example.clicknote.domain.model.NoteSource
import com.example.clicknote.domain.model.SyncStatus as DomainSyncStatus
import com.example.clicknote.domain.preferences.UserPreferencesDataStore
import com.example.clicknote.domain.repository.AuthService
import com.example.clicknote.domain.repository.SyncRepository
import com.example.clicknote.domain.repository.SyncStatus
import com.example.clicknote.worker.SyncWorker
import com.google.firebase.firestore.FirebaseFirestore
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.tasks.await
import kotlinx.coroutines.withContext
import kotlinx.coroutines.Dispatchers
import java.time.LocalDateTime
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class SyncRepositoryImpl @Inject constructor(
    @ApplicationContext private val context: Context,
    private val noteDao: NoteDao,
    private val authService: AuthService,
    private val firestore: FirebaseFirestore,
    private val preferences: UserPreferencesDataStore
) : SyncRepository {

    private val _syncStatus = MutableStateFlow(SyncStatus.IDLE)
    override fun getSyncStatus(): Flow<SyncStatus> = _syncStatus.asStateFlow()

    private val _lastSyncTime = MutableStateFlow(0L)
    override fun getLastSyncTime(): Flow<Long> = _lastSyncTime.asStateFlow()

    override fun getPendingNotes(): Flow<List<Note>> = flow {
        val pendingNotes = noteDao.getNotesBySyncStatus(DomainSyncStatus.PENDING)
        emit(pendingNotes.map { it.toDomain() })
    }

    override suspend fun syncNotes(): Result<Unit> = runCatching {
        _syncStatus.value = SyncStatus.SYNCING
        try {
            val pendingNotes = noteDao.getNotesBySyncStatus(DomainSyncStatus.PENDING)
            noteDao.updateSyncStatus(pendingNotes.map { it.id }, DomainSyncStatus.SYNCING)

            val userId = authService.userId.first() ?: throw IllegalStateException("User not signed in")
            
            pendingNotes.forEach { note ->
                try {
                    firestore.collection("users")
                        .document(userId)
                        .collection("notes")
                        .document(note.id)
                        .set(note.toFirebaseMap())
                        .await()
                    
                    noteDao.updateSyncStatus(listOf(note.id), DomainSyncStatus.SYNCED)
                } catch (e: Exception) {
                    noteDao.updateSyncStatus(listOf(note.id), DomainSyncStatus.FAILED)
                }
            }
            
            _lastSyncTime.value = System.currentTimeMillis()
            _syncStatus.value = SyncStatus.IDLE
            Result.success(Unit)
        } catch (e: Exception) {
            _syncStatus.value = SyncStatus.ERROR
            throw e
        }
    }

    override suspend fun syncNote(noteId: String): Result<Unit> = runCatching {
        val note = noteDao.getNoteById(noteId) ?: throw IllegalStateException("Note not found")
        noteDao.updateSyncStatus(listOf(noteId), DomainSyncStatus.SYNCING)

        val userId = authService.userId.first() ?: throw IllegalStateException("User not signed in")
        
        try {
            firestore.collection("users")
                .document(userId)
                .collection("notes")
                .document(noteId)
                .set(note.toFirebaseMap())
                .await()
            
            noteDao.updateSyncStatus(listOf(noteId), DomainSyncStatus.SYNCED)
            Result.success(Unit)
        } catch (e: Exception) {
            noteDao.updateSyncStatus(listOf(noteId), DomainSyncStatus.FAILED)
            throw e
        }
    }

    override suspend fun pullNotes(): Result<Unit> = runCatching {
        _syncStatus.value = SyncStatus.SYNCING
        try {
            val userId = authService.userId.first() ?: throw IllegalStateException("User not signed in")
            
            val cloudNotes = firestore.collection("users")
                .document(userId)
                .collection("notes")
                .get()
                .await()
                .documents
                .mapNotNull { it.toObject(NoteEntity::class.java) }
            
            cloudNotes.forEach { note ->
                noteDao.insertNote(note.copy(syncStatus = DomainSyncStatus.SYNCED.name))
            }
            
            _syncStatus.value = SyncStatus.IDLE
            _lastSyncTime.value = System.currentTimeMillis()
            Result.success(Unit)
        } catch (e: Exception) {
            _syncStatus.value = SyncStatus.ERROR
            throw e
        }
    }

    override suspend fun updateNoteStatus(noteId: String, status: Int) {
        noteDao.updateSyncStatus(listOf(noteId), DomainSyncStatus.values()[status])
    }

    override suspend fun schedulePeriodicSync() {
        SyncWorker.schedule(context)
    }

    override suspend fun cancelPeriodicSync() {
        SyncWorker.cancel(context)
    }

    private fun NoteEntity.toDomain(): Note {
        return Note(
            id = id,
            title = title,
            content = content,
            createdAt = createdAt,
            modifiedAt = modifiedAt,
            source = NoteSource.valueOf(source),
            syncStatus = DomainSyncStatus.valueOf(syncStatus),
            folderId = folderId,
            isArchived = isArchived,
            isPinned = isPinned,
            isDeleted = isDeleted,
            hasAudio = hasAudio,
            audioPath = audioPath,
            duration = duration?.toInt(),
            transcriptionLanguage = transcriptionLanguage,
            speakerCount = speakerCount,
            metadata = metadata
        )
    }

    private fun NoteEntity.toFirebaseMap(): Map<String, Any?> = mapOf(
        "id" to id,
        "title" to title,
        "content" to content,
        "createdAt" to createdAt,
        "modifiedAt" to modifiedAt,
        "source" to source,
        "syncStatus" to syncStatus,
        "folderId" to folderId,
        "isArchived" to isArchived,
        "isPinned" to isPinned,
        "isDeleted" to isDeleted,
        "hasAudio" to hasAudio,
        "audioPath" to audioPath,
        "duration" to duration,
        "transcriptionLanguage" to transcriptionLanguage,
        "speakerCount" to speakerCount,
        "metadata" to metadata
    )
} 