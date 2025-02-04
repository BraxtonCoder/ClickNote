package com.example.clicknote.domain.repository

import com.example.clicknote.domain.model.Note
import kotlinx.coroutines.flow.Flow
import java.time.LocalDateTime

interface NoteRepository {
    fun getNotes(): Flow<List<Note>>
    fun getNotesInFolder(folderId: String): Flow<List<Note>>
    fun getDeletedNotes(): Flow<List<Note>>
    fun searchNotes(query: String): Flow<List<Note>>
    fun getNotesByDateRange(startDate: LocalDateTime, endDate: LocalDateTime): Flow<List<Note>>
    
    suspend fun getNoteById(id: String): Note?
    suspend fun insertNote(note: Note): String
    suspend fun updateNote(note: Note)
    suspend fun deleteNote(note: Note)
    suspend fun permanentlyDeleteNote(note: Note)
    suspend fun restoreNote(note: Note)
    
    suspend fun moveToFolder(noteId: String, folderId: String?)
    suspend fun pinNote(noteId: String, isPinned: Boolean)
    suspend fun updateSpeakers(noteId: String, speakers: List<String>)
    suspend fun updateSummary(noteId: String, summary: String?)
    suspend fun updateKeyPoints(noteId: String, keyPoints: List<String>)
    
    suspend fun deleteExpiredNotes(expirationDate: LocalDateTime)
    suspend fun noteExists(id: String): Boolean
} 