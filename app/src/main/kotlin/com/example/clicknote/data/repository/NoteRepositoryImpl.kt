package com.example.clicknote.data.repository

import com.example.clicknote.data.dao.NoteDao
import com.example.clicknote.data.entity.NoteEntity
import com.example.clicknote.domain.model.Note
import com.example.clicknote.domain.repository.NoteRepository
import com.example.clicknote.util.DateTimeUtils
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import java.time.LocalDateTime
import java.util.UUID
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class NoteRepositoryImpl @Inject constructor(
    private val noteDao: NoteDao
) : NoteRepository {

    override fun getNotes(): Flow<List<Note>> =
        noteDao.getAllNotes().map { entities ->
            entities.map { it.toDomain() }
        }

    override fun getNotesInFolder(folderId: String): Flow<List<Note>> =
        noteDao.getNotesInFolder(folderId).map { entities ->
            entities.map { it.toDomain() }
        }

    override fun getDeletedNotes(): Flow<List<Note>> =
        noteDao.getDeletedNotes().map { entities ->
            entities.map { it.toDomain() }
        }

    override fun searchNotes(query: String): Flow<List<Note>> =
        noteDao.searchNotes(query).map { entities ->
            entities.map { it.toDomain() }
        }

    override fun getNotesByDateRange(startDate: LocalDateTime, endDate: LocalDateTime): Flow<List<Note>> =
        noteDao.getNotesByDateRange(
            DateTimeUtils.localDateTimeToTimestamp(startDate),
            DateTimeUtils.localDateTimeToTimestamp(endDate)
        ).map { entities ->
            entities.map { it.toDomain() }
        }

    override suspend fun getNoteById(id: String): Note? =
        noteDao.getNoteById(id)?.toDomain()

    override suspend fun insertNote(note: Note): String {
        val now = System.currentTimeMillis()
        val entity = NoteEntity(
            id = UUID.randomUUID().toString(),
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
        noteDao.insert(entity)
        return entity.id
    }

    override suspend fun updateNote(note: Note) {
        val now = System.currentTimeMillis()
        noteDao.update(
            NoteEntity(
                id = note.id,
                title = note.title,
                content = note.content,
                createdAt = DateTimeUtils.localDateTimeToTimestamp(note.createdAt),
                modifiedAt = now,
                isDeleted = note.isDeleted,
                deletedAt = note.deletedAt?.let { DateTimeUtils.localDateTimeToTimestamp(it) },
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

    override suspend fun deleteNote(note: Note) {
        val now = System.currentTimeMillis()
        noteDao.moveToTrash(note.id, now)
    }

    override suspend fun permanentlyDeleteNote(note: Note) {
        noteDao.delete(note.id)
    }

    override suspend fun restoreNote(note: Note) {
        noteDao.restoreFromTrash(note.id)
    }

    override suspend fun moveToFolder(noteId: String, folderId: String?) {
        noteDao.updateFolder(noteId, folderId)
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
        noteDao.deleteExpiredNotes(DateTimeUtils.localDateTimeToTimestamp(expirationDate))
    }

    override suspend fun noteExists(id: String): Boolean =
        noteDao.noteExists(id)

    private fun NoteEntity.toDomain() = Note(
        id = id,
        title = title,
        content = content,
        createdAt = DateTimeUtils.timestampToLocalDateTime(createdAt),
        modifiedAt = DateTimeUtils.timestampToLocalDateTime(modifiedAt),
        deletedAt = deletedAt?.let { DateTimeUtils.timestampToLocalDateTime(it) },
        isDeleted = isDeleted,
        isPinned = isPinned,
        isLongForm = isLongForm,
        hasAudio = hasAudio,
        audioPath = audioPath,
        duration = duration,
        source = com.example.clicknote.domain.model.NoteSource.valueOf(source),
        folderId = folderId,
        summary = summary,
        keyPoints = keyPoints,
        speakers = speakers,
        syncStatus = com.example.clicknote.domain.model.SyncStatus.valueOf(syncStatus)
    )
} 