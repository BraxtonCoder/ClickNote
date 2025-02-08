package com.example.clicknote.data.repository

import com.example.clicknote.data.dao.NoteDao
import com.example.clicknote.data.entity.NoteEntity
import com.example.clicknote.domain.model.Note
import com.example.clicknote.domain.model.NoteSource
import com.example.clicknote.domain.model.SyncStatus
import com.example.clicknote.domain.repository.NoteRepository
import kotlinx.coroutines.flow.*
import javax.inject.Inject
import javax.inject.Singleton
import java.time.LocalDateTime
import java.time.ZoneId

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

    override fun searchNotes(query: String): Flow<List<Note>> =
        noteDao.searchNotes(query).map { entities ->
            entities.map { it.toDomain() }
        }

    override suspend fun getNoteById(id: String): Result<Note> = runCatching {
        noteDao.getNoteById(id)?.toDomain() ?: throw IllegalStateException("Note not found")
    }

    override suspend fun insertNote(note: Note): Result<Unit> = runCatching {
        val now = System.currentTimeMillis()
        val entity = NoteEntity(
            id = note.id.ifEmpty { java.util.UUID.randomUUID().toString() },
            title = note.title,
            content = note.content,
            transcription = note.content,
            createdAt = now,
            modifiedAt = now,
            source = note.source.name,
            syncStatus = note.syncStatus.name,
            folderId = note.folderId,
            isArchived = note.isArchived,
            isPinned = note.isPinned,
            isDeleted = note.isDeleted,
            hasAudio = note.audioPath != null,
            audioPath = note.audioPath,
            duration = note.duration?.toLong(),
            transcriptionLanguage = note.transcriptionLanguage,
            speakerCount = note.speakerCount,
            metadata = note.metadata
        )
        noteDao.insertNote(entity)
    }

    override suspend fun insertNotes(notes: List<Note>): Result<Unit> = runCatching {
        val entities = notes.map { note ->
            val now = System.currentTimeMillis()
            NoteEntity(
                id = note.id.ifEmpty { java.util.UUID.randomUUID().toString() },
                title = note.title,
                content = note.content,
                transcription = note.content,
                createdAt = now,
                modifiedAt = now,
                source = note.source.name,
                syncStatus = note.syncStatus.name,
                folderId = note.folderId,
                isArchived = note.isArchived,
                isPinned = note.isPinned,
                isDeleted = note.isDeleted,
                hasAudio = note.audioPath != null,
                audioPath = note.audioPath,
                duration = note.duration?.toLong(),
                transcriptionLanguage = note.transcriptionLanguage,
                speakerCount = note.speakerCount,
                metadata = note.metadata
            )
        }
        noteDao.insertAll(entities)
    }

    override suspend fun updateNote(note: Note): Result<Unit> = runCatching {
        val now = System.currentTimeMillis()
        noteDao.updateNote(
            NoteEntity(
                id = note.id,
                title = note.title,
                content = note.content,
                transcription = note.content,
                createdAt = note.createdAt,
                modifiedAt = now,
                source = note.source.name,
                syncStatus = note.syncStatus.name,
                folderId = note.folderId,
                isArchived = note.isArchived,
                isPinned = note.isPinned,
                isDeleted = note.isDeleted,
                hasAudio = note.audioPath != null,
                audioPath = note.audioPath,
                duration = note.duration?.toLong(),
                transcriptionLanguage = note.transcriptionLanguage,
                speakerCount = note.speakerCount,
                metadata = note.metadata
            )
        )
    }

    override suspend fun deleteNote(id: String): Result<Unit> = runCatching {
        noteDao.delete(id)
    }

    override suspend fun moveToTrash(noteIds: List<String>): Result<Unit> = runCatching {
        val now = System.currentTimeMillis()
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
        val note = getNoteById(noteId).getOrNull() ?: return
        val updatedMetadata = note.metadata.toMutableMap().apply {
            put("speakers", speakers.joinToString(","))
        }
        updateNote(note.copy(metadata = updatedMetadata))
    }

    override suspend fun updateSummary(noteId: String, summary: String?) {
        val note = getNoteById(noteId).getOrNull() ?: return
        val updatedMetadata = note.metadata.toMutableMap().apply {
            if (summary != null) {
                put("summary", summary)
            } else {
                remove("summary")
            }
        }
        updateNote(note.copy(metadata = updatedMetadata))
    }

    override suspend fun updateKeyPoints(noteId: String, keyPoints: List<String>) {
        val note = getNoteById(noteId).getOrNull() ?: return
        val updatedMetadata = note.metadata.toMutableMap().apply {
            put("keyPoints", keyPoints.joinToString("\n"))
        }
        updateNote(note.copy(metadata = updatedMetadata))
    }

    override suspend fun deleteExpiredNotes(expirationDate: LocalDateTime) {
        val timestamp = expirationDate.atZone(ZoneId.systemDefault()).toInstant().toEpochMilli()
        noteDao.deleteExpiredNotes(timestamp)
    }

    override suspend fun noteExists(id: String): Boolean {
        return noteDao.getNoteById(id) != null
    }

    override suspend fun softDeleteNote(note: Note): Result<Unit> = runCatching {
        val now = System.currentTimeMillis()
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
        summary = summary,
        audioPath = audioPath,
        createdAt = createdAt,
        modifiedAt = modifiedAt,
        folderId = folderId,
        isPinned = isPinned,
        isDeleted = isDeleted,
        syncStatus = SyncStatus.valueOf(syncStatus),
        source = NoteSource.valueOf(source),
        isArchived = isArchived,
        duration = duration?.toInt(),
        transcriptionLanguage = transcriptionLanguage,
        speakerCount = speakerCount,
        metadata = metadata
    )
} 