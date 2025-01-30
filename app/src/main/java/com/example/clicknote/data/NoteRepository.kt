package com.example.clicknote.data

import com.example.clicknote.domain.model.Note
import kotlinx.coroutines.flow.Flow

interface NoteRepository {
    fun getNotes(): Flow<List<Note>>
    fun getNotesInTrash(): Flow<List<Note>>
    fun searchNotes(query: String): Flow<List<Note>>
    fun getNotesByDateRange(startDate: Long, endDate: Long): Flow<List<Note>>
    suspend fun getNoteById(id: String): Result<Note>
    suspend fun insertNote(note: Note): Result<Unit>
    suspend fun updateNote(note: Note): Result<Unit>
    suspend fun deleteNote(id: String): Result<Unit>
    suspend fun deleteNotes(ids: List<String>): Result<Unit>
    suspend fun moveToTrash(id: String): Result<Unit>
    suspend fun moveToTrashBulk(ids: List<String>): Result<Unit>
    suspend fun restoreFromTrash(id: String): Result<Unit>
    suspend fun restoreFromTrash(noteIds: List<String>): Result<Unit>
    suspend fun moveToFolder(noteId: String, folderId: String?): Result<Unit>
    suspend fun moveToFolderBulk(noteIds: List<String>, folderId: String?): Result<Unit>
    suspend fun togglePin(noteId: String): Result<Unit>
    suspend fun togglePinBulk(noteIds: List<String>): Result<Unit>
    suspend fun updateSummary(noteId: String, summary: String): Result<Unit>
    suspend fun updateKeyPoints(noteId: String, keyPoints: List<String>): Result<Unit>
    suspend fun updateSpeakers(noteId: String, speakers: List<String>): Result<Unit>
    suspend fun deleteExpiredTrashNotes(): Result<Unit>
} 