package com.example.clicknote.data.repository

import android.content.Context
import com.example.clicknote.data.dao.NoteDao
import com.example.clicknote.data.entity.NoteEntity
import com.example.clicknote.domain.model.Note
import com.example.clicknote.domain.repository.AuthService
import com.example.clicknote.domain.repository.SyncRepository
import com.example.clicknote.domain.repository.SyncStatus
import com.example.clicknote.worker.SyncWorker
import com.google.firebase.firestore.FirebaseFirestore
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.tasks.await
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class SyncRepositoryImpl @Inject constructor(
    @ApplicationContext private val context: Context,
    private val noteDao: NoteDao,
    private val authService: AuthService,
    private val firestore: FirebaseFirestore
) : SyncRepository {

    private val _syncStatus = MutableStateFlow<SyncStatus>(SyncStatus.IDLE)
    override fun getSyncStatus(): Flow<SyncStatus> = _syncStatus.asStateFlow()

    private val _lastSyncTime = MutableStateFlow<Long>(0)
    override fun getLastSyncTime(): Flow<Long> = _lastSyncTime.asStateFlow()

    override fun getPendingNotes(): Flow<List<Note>> = noteDao.getNotesBySyncStatus(NoteEntity.SYNC_STATUS_PENDING)
        .map { notes -> notes.map { it.toDomain() } }

    override suspend fun syncNotes(): Result<Unit> = try {
        _syncStatus.value = SyncStatus.SYNCING
        
        val userId = authService.userId.first() ?: throw IllegalStateException("User not signed in")
        val pendingNotes = noteDao.getNotesBySyncStatusSuspend(NoteEntity.SYNC_STATUS_PENDING)
        
        pendingNotes.forEach { note ->
            try {
                firestore.collection("users")
                    .document(userId)
                    .collection("notes")
                    .document(note.id)
                    .set(note)
                    .await()
                
                noteDao.updateSyncStatus(note.id, NoteEntity.SYNC_STATUS_COMPLETED)
            } catch (e: Exception) {
                noteDao.updateSyncStatus(note.id, NoteEntity.SYNC_STATUS_FAILED)
            }
        }
        
        _lastSyncTime.value = System.currentTimeMillis()
        _syncStatus.value = SyncStatus.SUCCESS
        Result.success(Unit)
    } catch (e: Exception) {
        _syncStatus.value = SyncStatus.ERROR
        Result.failure(e)
    }

    override suspend fun syncNote(noteId: String): Result<Unit> = try {
        val userId = authService.userId.first() ?: throw IllegalStateException("User not signed in")
        val note = noteDao.getNoteById(noteId) ?: throw IllegalStateException("Note not found")
        
        firestore.collection("users")
            .document(userId)
            .collection("notes")
            .document(noteId)
            .set(note)
            .await()
        
        noteDao.updateSyncStatus(noteId, NoteEntity.SYNC_STATUS_COMPLETED)
        Result.success(Unit)
    } catch (e: Exception) {
        noteDao.updateSyncStatus(noteId, NoteEntity.SYNC_STATUS_FAILED)
        Result.failure(e)
    }

    override suspend fun pullNotes(): Result<Unit> = try {
        val userId = authService.userId.first() ?: throw IllegalStateException("User not signed in")
        
        val cloudNotes = firestore.collection("users")
            .document(userId)
            .collection("notes")
            .get()
            .await()
            .documents
            .mapNotNull { it.toObject(NoteEntity::class.java) }
        
        cloudNotes.forEach { note ->
            noteDao.insert(note.copy(syncStatus = NoteEntity.SYNC_STATUS_COMPLETED))
        }
        
        Result.success(Unit)
    } catch (e: Exception) {
        Result.failure(e)
    }

    override suspend fun updateNoteStatus(noteId: String, status: Int) {
        noteDao.updateSyncStatus(noteId, status)
    }

    override suspend fun schedulePeriodicSync() {
        SyncWorker.schedule(context)
    }

    override suspend fun cancelPeriodicSync() {
        SyncWorker.cancel(context)
    }
} 