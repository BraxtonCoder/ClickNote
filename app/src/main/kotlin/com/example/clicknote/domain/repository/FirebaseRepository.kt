package com.example.clicknote.domain.repository

import com.example.clicknote.domain.model.*
import kotlinx.coroutines.flow.Flow

interface FirebaseRepository {
    // Notes
    suspend fun syncNote(note: FirebaseNote)
    suspend fun syncNotes(notes: List<FirebaseNote>)
    suspend fun deleteNote(noteId: String)
    fun observeNotes(userId: String): Flow<List<FirebaseNote>>
    fun observeNote(noteId: String): Flow<FirebaseNote?>
    
    // Folders
    suspend fun syncFolder(folder: FirebaseFolder)
    suspend fun syncFolders(folders: List<FirebaseFolder>)
    suspend fun deleteFolder(folderId: String)
    fun observeFolders(userId: String): Flow<List<FirebaseFolder>>
    
    // User
    suspend fun syncUser(user: FirebaseUser)
    suspend fun getUser(userId: String): FirebaseUser?
    fun observeUser(userId: String): Flow<FirebaseUser?>
    
    // Audio
    suspend fun uploadAudio(noteId: String, audioBytes: ByteArray): String
    suspend fun downloadAudio(noteId: String): ByteArray?
    suspend fun deleteAudio(noteId: String)
    
    // Sync Status
    suspend fun updateSyncStatus(noteId: String, status: String)
    suspend fun getPendingSyncs(): List<String>
    
    // Cloud Storage
    suspend fun getStorageUsage(userId: String): Long
    suspend fun getStorageLimit(userId: String): Long
    
    // Offline Support
    suspend fun enableOfflineSupport()
    suspend fun disableOfflineSupport()
    suspend fun clearOfflineCache()
} 