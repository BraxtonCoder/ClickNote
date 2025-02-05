package com.example.clicknote.data.repository

import com.example.clicknote.data.dao.NoteDao
import com.example.clicknote.data.entity.NoteEntity
import com.example.clicknote.domain.model.Note
import com.example.clicknote.domain.model.NoteSource
import com.example.clicknote.domain.model.SyncStatus
import com.example.clicknote.domain.repository.NoteRepository
import com.example.clicknote.util.DateTimeUtils
import kotlinx.coroutines.flow.*
import javax.inject.Inject
import javax.inject.Singleton
import java.time.LocalDateTime
import java.util.UUID

@Singleton
class NoteRepositoryImpl @Inject constructor(
    private val noteDao: NoteDao
) : NoteRepository {

    override suspend fun getAllNotes(): Result<List<Note>> = runCatching {
        noteDao.getAllNotes().first().map { it.toDomain() }
    }

    override fun getNotesInFolder(folderId: String): Flow<List<Note>> =
        noteDao.getNotesInFolder(folderId).map { entities ->
            entities.map { it.toDomain() }
        }

    override fun getDeletedNotes(): Flow<List<Note>> =
        noteDao.getDeletedNotes().map { entities ->
            entities.map { it.toDomain() }
        }

    override suspend fun searchNotes(query: String): Flow<List<Note>> =
        noteDao.searchNotes(query).map { entities ->
            entities.map { it.toDomain() }
        }

    override suspend fun getNoteById(id: String): Result<Note> = runCatching {
        noteDao.getNoteById(id)?.toDomain() ?: throw IllegalStateException("Note not found")
    }

    override suspend fun insertNote(note: Note): Result<Unit> = runCatching {
        val now = LocalDateTime.now()
        val entity = NoteEntity(
            id = note.id.ifEmpty { UUID.randomUUID().toString() },
            title = note.title,
            content = note.content,
            createdAt = now,
            modifiedAt = now,
            isDeleted = false,
            deletedAt = null,
            isPinned = note.isPinned,
            isLongForm = note.isLongForm,
            hasAudio = note.hasAudio,
            audioPath = note.audioPath,
            duration = note.duration,
            source = note.source.name,
            folderId = note.folderId,
            summary = note.summary,
            keyPoints = note.keyPoints,
            speakers = note.speakers,
            syncStatus = note.syncStatus.name
        )
        noteDao.insertNote(entity)
    }

    override suspend fun insertNotes(notes: List<Note>): Result<Unit> = runCatching {
        val entities = notes.map { note ->
            val now = LocalDateTime.now()
            NoteEntity(
                id = note.id.ifEmpty { UUID.randomUUID().toString() },
                title = note.title,
                content = note.content,
                createdAt = now,
                modifiedAt = now,
                isDeleted = false,
                deletedAt = null,
                isPinned = note.isPinned,
                isLongForm = note.isLongForm,
                hasAudio = note.hasAudio,
                audioPath = note.audioPath,
                duration = note.duration,
                source = note.source.name,
                folderId = note.folderId,
                summary = note.summary,
                keyPoints = note.keyPoints,
                speakers = note.speakers,
                syncStatus = note.syncStatus.name
            )
        }
        noteDao.insertAll(entities)
    }

    override suspend fun updateNote(note: Note): Result<Unit> = runCatching {
        val now = LocalDateTime.now()
        noteDao.updateNote(
            NoteEntity(
                id = note.id,
                title = note.title,
                content = note.content,
                createdAt = note.createdAt,
                modifiedAt = now,
                isDeleted = note.isDeleted,
                deletedAt = note.deletedAt,
                isPinned = note.isPinned,
                isLongForm = note.isLongForm,
                hasAudio = note.hasAudio,
                audioPath = note.audioPath,
                duration = note.duration,
                source = note.source.name,
                folderId = note.folderId,
                summary = note.summary,
                keyPoints = note.keyPoints,
                speakers = note.speakers,
                syncStatus = note.syncStatus.name
            )
        )
    }

    override suspend fun deleteNote(id: String): Result<Unit> = runCatching {
        noteDao.delete(id)
    }

    override suspend fun moveToTrash(noteIds: List<String>): Result<Unit> = runCatching {
        val now = LocalDateTime.now()
        noteIds.forEach { id ->
            noteDao.moveToTrash(id, now)
        }
    }

    override suspend fun restoreFromTrash(noteIds: List<String>): Result<Unit> = runCatching {
        noteIds.forEach { id ->
            noteDao.restoreFromTrash(id)
        }
    }

    override suspend fun updateNoteFolder(noteId: String, folderId: String?): Result<Unit> = runCatching {
        noteDao.updateNoteFolder(noteId, folderId)
    }

    override suspend fun moveToFolder(noteId: String, folderId: String?) {
        noteDao.updateNoteFolder(noteId, folderId)
    }

    override suspend fun pinNote(noteId: String, isPinned: Boolean) {
        noteDao.updatePinned(noteId, isPinned)
    }

    override suspend fun updateSpeakers(noteId: String, speakers: List<String>) {
        noteDao.updateSpeakers(noteId, speakers)
    }

    override suspend fun updateSummary(noteId: String, summary: String?) {
        noteDao.updateSummary(noteId, summary)
    }

    override suspend fun updateKeyPoints(noteId: String, keyPoints: List<String>) {
        noteDao.updateKeyPoints(noteId, keyPoints)
    }

    override suspend fun deleteExpiredNotes(expirationDate: LocalDateTime) {
        noteDao.deleteExpiredNotes(expirationDate)
    }

    override suspend fun noteExists(id: String): Boolean {
        return noteDao.getNoteById(id) != null
    }

    override suspend fun softDeleteNote(note: Note): Result<Unit> = runCatching {
        val now = LocalDateTime.now()
        noteDao.moveToTrash(note.id, now)
    }

    override suspend fun restoreNote(note: Note): Result<Unit> = runCatching {
        noteDao.restoreFromTrash(note.id)
    }

    override suspend fun restoreNote(id: String): Result<Unit> = runCatching {
        noteDao.restoreFromTrash(id)
    }

    override suspend fun permanentlyDeleteNote(id: String): Result<Unit> = runCatching {
        noteDao.delete(id)
    }

    private fun NoteEntity.toDomain() = Note(
        id = id,
        title = title,
        content = content,
        createdAt = createdAt,
        modifiedAt = modifiedAt,
        deletedAt = deletedAt,
        isDeleted = isDeleted,
        isPinned = isPinned,
        isLongForm = isLongForm,
        hasAudio = hasAudio,
        audioPath = audioPath,
        duration = duration,
        source = NoteSource.valueOf(source),
        folderId = folderId,
        summary = summary,
        keyPoints = keyPoints,
        speakers = speakers,
        syncStatus = SyncStatus.valueOf(syncStatus)
    )
} 