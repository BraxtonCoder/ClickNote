package com.example.clicknote.data.repository

import com.example.clicknote.data.dao.NoteDao
import com.example.clicknote.data.entity.NoteEntity
import com.example.clicknote.domain.model.Note
import com.example.clicknote.domain.repository.NoteRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject
import javax.inject.Singleton
import java.util.UUID

@Singleton
class NoteRepositoryImpl @Inject constructor(
    private val noteDao: NoteDao
) : NoteRepository {

    override fun getAllNotes(): Flow<List<Note>> =
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

    override fun getNotesByDateRange(startTimestamp: Long, endTimestamp: Long): Flow<List<Note>> =
        noteDao.getNotesByDateRange(startTimestamp, endTimestamp).map { entities ->
            entities.map { it.toDomain() }
        }

    override suspend fun getNoteById(id: String): Note? =
        noteDao.getNoteById(id)?.toDomain()

    override suspend fun insertNote(note: Note): String {
        val entity = NoteEntity.fromDomain(note)
        noteDao.insertNote(entity)
        return entity.id
    }

    override suspend fun updateNote(note: Note) {
        noteDao.updateNote(NoteEntity.fromDomain(note))
    }

    override suspend fun delete(id: String) {
        noteDao.delete(id)
    }

    override suspend fun deleteBulk(ids: List<String>) {
        ids.forEach { noteDao.delete(it) }
    }

    override suspend fun moveToTrash(id: String) {
        val timestamp = System.currentTimeMillis()
        noteDao.moveToTrash(id, timestamp)
    }

    override suspend fun moveToTrashBulk(ids: List<String>) {
        val timestamp = System.currentTimeMillis()
        ids.forEach { noteDao.moveToTrash(it, timestamp) }
    }

    override suspend fun restoreFromTrash(id: String) {
        noteDao.restoreFromTrash(id)
    }

    override suspend fun restoreFromTrashBulk(ids: List<String>) {
        ids.forEach { noteDao.restoreFromTrash(it) }
    }

    override suspend fun moveToFolder(noteId: String, folderId: String?) {
        noteDao.updateNoteFolder(noteId, folderId)
    }

    override suspend fun moveToFolderBulk(noteIds: List<String>, folderId: String?) {
        noteDao.moveNotesToFolder(noteIds, folderId)
    }

    override suspend fun togglePin(id: String) {
        val note = noteDao.getNoteById(id) ?: return
        noteDao.updateNote(note.copy(
            isPinned = !note.isPinned,
            updatedAt = System.currentTimeMillis()
        ))
    }

    override suspend fun togglePinBulk(ids: List<String>) {
        ids.forEach { id ->
            val note = noteDao.getNoteById(id) ?: return@forEach
            noteDao.updateNote(note.copy(
                isPinned = !note.isPinned,
                updatedAt = System.currentTimeMillis()
            ))
        }
    }

    override suspend fun updateSummary(id: String, summary: String) {
        val note = noteDao.getNoteById(id) ?: return
        noteDao.updateNote(note.copy(
            summary = summary,
            updatedAt = System.currentTimeMillis()
        ))
    }

    override suspend fun updateKeyPoints(id: String, keyPoints: List<String>) {
        val note = noteDao.getNoteById(id) ?: return
        noteDao.updateNote(note.copy(
            keyPoints = keyPoints,
            updatedAt = System.currentTimeMillis()
        ))
    }

    override suspend fun updateSpeakers(id: String, speakers: List<String>) {
        val note = noteDao.getNoteById(id) ?: return
        noteDao.updateNote(note.copy(
            speakers = speakers,
            updatedAt = System.currentTimeMillis()
        ))
    }

    override suspend fun deleteExpiredNotes(expirationTime: Long) {
        noteDao.deleteExpiredNotes(expirationTime)
    }
} 