package com.example.clicknote.domain.repository

import com.example.clicknote.domain.model.Folder
import kotlinx.coroutines.flow.Flow

interface FolderRepository {
    fun getAllFolders(): Flow<List<Folder>>
    suspend fun getFolderById(id: String): Folder?
    suspend fun insertFolder(folder: Folder): String
    suspend fun updateFolder(folder: Folder)
    suspend fun deleteFolder(folder: Folder)
    suspend fun permanentlyDeleteFolder(folder: Folder)
    fun searchFolders(query: String): Flow<List<Folder>>
    suspend fun incrementNoteCount(folderId: String)
    suspend fun decrementNoteCount(folderId: String)
    suspend fun folderNameExists(name: String): Boolean
} 