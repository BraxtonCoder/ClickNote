package com.example.clicknote.domain.repository

import com.example.clicknote.domain.model.Note
import kotlinx.coroutines.flow.Flow
import java.time.LocalDateTime

interface NoteRepository {
    suspend fun getAllNotes(): Result<List<Note>>
    suspend fun insertNote(note: Note): Result<Unit>
    suspend fun insertNotes(notes: List<Note>): Result<Unit>
    suspend fun updateNote(note: Note): Result<Unit>
    suspend fun deleteNote(id: String): Result<Unit>
    suspend fun getNoteById(id: String): Result<Note>
    suspend fun searchNotes(query: String): Flow<List<Note>>
    suspend fun moveToTrash(noteIds: List<String>): Result<Unit>
    suspend fun restoreFromTrash(noteIds: List<String>): Result<Unit>
    suspend fun updateNoteFolder(noteId: String, folderId: String?): Result<Unit>
    suspend fun getNotesInFolder(folderId: String): Flow<List<Note>>
    suspend fun getDeletedNotes(): Flow<List<Note>>
    suspend fun permanentlyDeleteNote(id: String): Result<Unit>
    suspend fun softDeleteNote(note: Note): Result<Unit>
    suspend fun restoreNote(note: Note): Result<Unit>
    suspend fun restoreNote(id: String): Result<Unit>
    
    suspend fun moveToFolder(noteId: String, folderId: String?)
    suspend fun pinNote(noteId: String, isPinned: Boolean)
    suspend fun updateSpeakers(noteId: String, speakers: List<String>)
    suspend fun updateSummary(noteId: String, summary: String?)
    suspend fun updateKeyPoints(noteId: String, keyPoints: List<String>)
    
    suspend fun deleteExpiredNotes(expirationDate: LocalDateTime)
    suspend fun noteExists(id: String): Boolean

    /**
     * Deletes notes that have been in the trash for more than the specified number of days
     * @param days The number of days after which trashed notes should be permanently deleted
     * @return Result indicating success or failure of the operation
     */
    suspend fun deleteExpiredNotes(days: Int): Result<Unit>
} 