package com.example.clicknote.domain.service

import com.example.clicknote.domain.model.Note
import com.example.clicknote.domain.model.Folder
import kotlinx.coroutines.flow.Flow

interface FirestoreService {
    suspend fun saveNote(userId: String, note: Note): Result<Unit>
    suspend fun saveNotes(userId: String, notes: List<Note>): Result<Unit>
    suspend fun deleteNote(userId: String, noteId: String): Result<Unit>
    suspend fun getNote(userId: String, noteId: String): Result<Note>
    suspend fun getNotes(userId: String): Result<List<Note>>
    suspend fun saveFolder(userId: String, folder: Folder): Result<Unit>
    suspend fun saveFolders(userId: String, folders: List<Folder>): Result<Unit>
    suspend fun deleteFolder(userId: String, folderId: String): Result<Unit>
    suspend fun getFolder(userId: String, folderId: String): Result<Folder>
    suspend fun getFolders(userId: String): Result<List<Folder>>
    suspend fun getUserStorageLimit(userId: String): Result<Long>
    suspend fun getUserStorageUsage(userId: String): Result<Long>
    fun getChanges(userId: String): Flow<List<String>>
    suspend fun runTransaction(action: suspend () -> Unit): Result<Unit>
    suspend fun runBatch(action: suspend () -> Unit): Result<Unit>
} 