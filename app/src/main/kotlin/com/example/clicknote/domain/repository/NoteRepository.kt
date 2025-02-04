package com.example.clicknote.domain.repository

import com.example.clicknote.domain.model.Note
import kotlinx.coroutines.flow.Flow

interface NoteRepository {
    fun getAllNotes(): Flow<List<Note>>
    fun getNotes(): Flow<List<Note>>
    fun getNotesInTrash(): Flow<List<Note>>
    fun searchNotes(query: String): Flow<List<Note>>
    fun getNotesByDateRange(startTimestamp: Long, endTimestamp: Long): Flow<List<Note>>
    suspend fun getNoteById(id: String): Note?
    suspend fun insertNote(note: Note): String
    suspend fun updateNote(note: Note)
    suspend fun deleteNote(id: String)
    suspend fun deleteNotes(ids: List<String>)
    suspend fun moveToTrash(id: String)
    suspend fun moveToTrashBulk(ids: List<String>)
    suspend fun restoreFromTrash(id: String)
    suspend fun restoreFromTrashBulk(ids: List<String>)
    suspend fun moveToFolder(noteId: String, folderId: String?)
    suspend fun moveToFolderBulk(noteIds: List<String>, folderId: String?)
    suspend fun togglePin(id: String)
    suspend fun togglePinBulk(ids: List<String>)
    suspend fun updateSummary(id: String, summary: String)
    suspend fun updateKeyPoints(id: String, keyPoints: List<String>)
    suspend fun updateSpeakers(id: String, speakers: Map<String, String>)
    suspend fun deleteExpiredTrashNotes(expirationTime: Long)
} 