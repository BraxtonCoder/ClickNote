package com.example.clicknote.data.repository

import com.example.clicknote.data.dao.NoteDao
import com.example.clicknote.domain.model.Note
import com.example.clicknote.data.entity.NoteEntity
import com.example.clicknote.data.mapper.toNote
import com.example.clicknote.data.mapper.toNoteEntity
import com.example.clicknote.domain.repository.NoteRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import java.time.Instant
import java.time.LocalDateTime
import java.time.ZoneId
import javax.inject.Inject

class NoteRepositoryImpl @Inject constructor(
    private val noteDao: NoteDao
) : NoteRepository {
    override fun getNotes(): Flow<List<Note>> = 
        noteDao.getAllNotesWithFolders().map { notes -> notes.map { it.toNote() } }

    override fun getNotesInTrash(): Flow<List<Note>> = 
        noteDao.getDeletedNotes().map { notes -> notes.map { it.toNote() } }

    override fun searchNotes(query: String): Flow<List<Note>> = 
        noteDao.searchNotesWithFolders(query).map { notes -> notes.map { it.toNote() } }

    override fun getNotesByDateRange(startDate: Long, endDate: Long): Flow<List<Note>> {
        val startDateTime = LocalDateTime.ofInstant(
            Instant.ofEpochMilli(startDate),
            ZoneId.systemDefault()
        )
        val endDateTime = LocalDateTime.ofInstant(
            Instant.ofEpochMilli(endDate),
            ZoneId.systemDefault()
        )
        return noteDao.getNotesByDateRange(startDateTime, endDateTime)
            .map { notes -> notes.map { it.toNote() } }
    }

    override suspend fun getNoteById(id: String): Result<Note> = try {
        val note = noteDao.getNoteById(id)
        if (note != null) {
            Result.success(note.toNote())
        } else {
            Result.failure(NoSuchElementException("Note not found"))
        }
    } catch (e: Exception) {
        Result.failure(e)
    }

    override suspend fun insertNote(note: Note): Result<Unit> = try {
        noteDao.insertNote(note.toNoteEntity())
        Result.success(Unit)
    } catch (e: Exception) {
        Result.failure(e)
    }

    override suspend fun updateNote(note: Note): Result<Unit> = try {
        noteDao.updateNote(note.toNoteEntity())
        Result.success(Unit)
    } catch (e: Exception) {
        Result.failure(e)
    }

    override suspend fun deleteNote(id: String): Result<Unit> = try {
        noteDao.delete(id)
        Result.success(Unit)
    } catch (e: Exception) {
        Result.failure(e)
    }

    override suspend fun deleteNotes(ids: List<String>): Result<Unit> = try {
        ids.forEach { noteDao.delete(it) }
        Result.success(Unit)
    } catch (e: Exception) {
        Result.failure(e)
    }

    override suspend fun moveToTrash(id: String): Result<Unit> = try {
        noteDao.moveToTrash(id)
        Result.success(Unit)
    } catch (e: Exception) {
        Result.failure(e)
    }

    override suspend fun moveToTrashBulk(ids: List<String>): Result<Unit> = try {
        ids.forEach { noteDao.moveToTrash(it) }
        Result.success(Unit)
    } catch (e: Exception) {
        Result.failure(e)
    }

    override suspend fun restoreFromTrash(id: String): Result<Unit> = try {
        noteDao.restoreFromTrash(id)
        Result.success(Unit)
    } catch (e: Exception) {
        Result.failure(e)
    }

    override suspend fun restoreFromTrash(noteIds: List<String>): Result<Unit> = try {
        noteIds.forEach { noteDao.restoreFromTrash(it) }
        Result.success(Unit)
    } catch (e: Exception) {
        Result.failure(e)
    }

    override suspend fun moveToFolder(noteId: String, folderId: String?): Result<Unit> = try {
        noteDao.moveNotesToFolder(listOf(noteId), folderId)
        Result.success(Unit)
    } catch (e: Exception) {
        Result.failure(e)
    }

    override suspend fun moveToFolderBulk(noteIds: List<String>, folderId: String?): Result<Unit> = try {
        noteDao.moveNotesToFolder(noteIds, folderId)
        Result.success(Unit)
    } catch (e: Exception) {
        Result.failure(e)
    }

    override suspend fun togglePin(noteId: String): Result<Unit> = try {
        val note = noteDao.getNoteById(noteId)
        if (note != null) {
            noteDao.updatePinned(noteId, !note.isPinned)
            Result.success(Unit)
        } else {
            Result.failure(NoSuchElementException("Note not found"))
        }
    } catch (e: Exception) {
        Result.failure(e)
    }

    override suspend fun togglePinBulk(noteIds: List<String>): Result<Unit> = try {
        noteIds.forEach { noteId ->
            val note = noteDao.getNoteById(noteId)
            if (note != null) {
                noteDao.updatePinned(noteId, !note.isPinned)
            }
        }
        Result.success(Unit)
    } catch (e: Exception) {
        Result.failure(e)
    }

    override suspend fun updateSummary(noteId: String, summary: String): Result<Unit> = try {
        val note = noteDao.getNoteById(noteId)
        if (note != null) {
            noteDao.updateNote(note.copy(summary = summary))
            Result.success(Unit)
        } else {
            Result.failure(NoSuchElementException("Note not found"))
        }
    } catch (e: Exception) {
        Result.failure(e)
    }

    override suspend fun updateKeyPoints(noteId: String, keyPoints: List<String>): Result<Unit> = try {
        val note = noteDao.getNoteById(noteId)
        if (note != null) {
            noteDao.updateNote(note.copy(keyPoints = keyPoints))
            Result.success(Unit)
        } else {
            Result.failure(NoSuchElementException("Note not found"))
        }
    } catch (e: Exception) {
        Result.failure(e)
    }

    override suspend fun updateSpeakers(noteId: String, speakers: List<String>): Result<Unit> = try {
        val note = noteDao.getNoteById(noteId)
        if (note != null) {
            noteDao.updateNote(note.copy(speakers = speakers))
            Result.success(Unit)
        } else {
            Result.failure(NoSuchElementException("Note not found"))
        }
    } catch (e: Exception) {
        Result.failure(e)
    }

    override suspend fun deleteExpiredTrashNotes(): Result<Unit> = try {
        val thirtyDaysAgo = LocalDateTime.now().minusDays(30)
        noteDao.deleteExpiredNotes(thirtyDaysAgo)
        Result.success(Unit)
    } catch (e: Exception) {
        Result.failure(e)
    }
} 