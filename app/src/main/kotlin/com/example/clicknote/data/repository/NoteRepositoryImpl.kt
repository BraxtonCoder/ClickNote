package com.example.clicknote.data.repository

import com.example.clicknote.data.dao.NoteDao
import com.example.clicknote.data.entity.NoteEntity
import com.example.clicknote.domain.model.Note
import com.example.clicknote.domain.repository.NoteRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class NoteRepositoryImpl @Inject constructor(
    private val noteDao: NoteDao
) : NoteRepository {

    override fun getAllNotes(): Flow<List<Note>> =
        noteDao.getAllNotes().map { entities ->
            entities.map { it.toDomain() }
        }

    override fun getNotes(): Flow<List<Note>> =
        noteDao.getNotes().map { entities ->
            entities.map { it.toDomain() }
        }

    override fun getNotesInTrash(): Flow<List<Note>> =
        noteDao.getNotesInTrash().map { entities ->
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

    override suspend fun deleteNote(id: String) {
        noteDao.deleteNote(id)
    }

    override suspend fun deleteNotes(ids: List<String>) {
        noteDao.deleteNotes(ids)
    }

    override suspend fun moveToTrash(id: String) {
        noteDao.moveToTrash(id, System.currentTimeMillis())
    }

    override suspend fun moveToTrashBulk(ids: List<String>) {
        val timestamp = System.currentTimeMillis()
        noteDao.moveToTrashBulk(ids, timestamp)
    }

    override suspend fun restoreFromTrash(id: String) {
        noteDao.restoreFromTrash(id)
    }

    override suspend fun restoreFromTrashBulk(ids: List<String>) {
        noteDao.restoreFromTrashBulk(ids)
    }

    override suspend fun moveToFolder(noteId: String, folderId: String?) {
        noteDao.moveToFolder(noteId, folderId)
    }

    override suspend fun moveToFolderBulk(noteIds: List<String>, folderId: String?) {
        noteDao.moveToFolderBulk(noteIds, folderId)
    }

    override suspend fun togglePin(id: String) {
        val note = noteDao.getNoteById(id) ?: return
        noteDao.updateNote(note.copy(
            isPinned = !note.isPinned,
            updatedAt = System.currentTimeMillis()
        ))
    }

    override suspend fun togglePinBulk(ids: List<String>) {
        val notes = noteDao.getNotesByIds(ids)
        notes.forEach { note ->
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

    override suspend fun updateSpeakers(id: String, speakers: Map<String, String>) {
        val note = noteDao.getNoteById(id) ?: return
        noteDao.updateNote(note.copy(
            speakers = speakers,
            updatedAt = System.currentTimeMillis()
        ))
    }

    override suspend fun deleteExpiredTrashNotes(expirationTime: Long) {
        noteDao.deleteExpiredTrashNotes(expirationTime)
    }
} 