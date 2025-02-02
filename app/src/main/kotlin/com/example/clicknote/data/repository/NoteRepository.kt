package com.example.clicknote.data.repository

import com.example.clicknote.data.model.Note
import kotlinx.coroutines.flow.Flow

interface NoteRepository {
    fun getAllNotes(): Flow<List<Note>>
    fun getNotesInFolder(folderId: String): Flow<List<Note>>
    fun searchNotes(query: String): Flow<List<Note>>
    fun getDeletedNotes(): Flow<List<Note>>
    suspend fun getNoteById(id: String): Note?
    suspend fun insertNote(note: Note)
    suspend fun updateNote(note: Note)
    suspend fun moveToTrash(id: String)
    suspend fun restoreFromTrash(id: String)
    suspend fun deleteNote(id: String)
    suspend fun deleteExpiredNotes(timestamp: Long)
    suspend fun moveNotesToFolder(noteIds: List<String>, folderId: String?)
    suspend fun updatePinned(id: String, isPinned: Boolean)
    fun getNotesByDateRange(startTimestamp: Long, endTimestamp: Long): Flow<List<Note>>
    fun getNotesCountInDateRange(startTimestamp: Long, endTimestamp: Long): Int
    fun getTotalNotesCount(): Int
}