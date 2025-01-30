package com.example.clicknote.service

import com.example.clicknote.domain.model.Note
import com.example.clicknote.domain.model.Folder
import kotlinx.coroutines.flow.Flow

interface FirestoreService {
    suspend fun saveNote(note: Note)
    suspend fun getNote(noteId: String): Note?
    suspend fun deleteNote(noteId: String)
    suspend fun getAllNotes(): List<Note>
    fun observeNotes(): Flow<List<Note>>
    
    suspend fun saveFolder(folder: Folder)
    suspend fun getFolder(folderId: String): Folder?
    suspend fun deleteFolder(folderId: String)
    suspend fun getAllFolders(): List<Folder>
    fun observeFolders(): Flow<List<Folder>>
    
    suspend fun cleanup()
} 