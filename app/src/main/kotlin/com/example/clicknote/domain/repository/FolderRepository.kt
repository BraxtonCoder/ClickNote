package com.example.clicknote.domain.repository

import com.example.clicknote.domain.model.Folder
import kotlinx.coroutines.flow.Flow

interface FolderRepository {
    suspend fun getAllFolders(): Result<List<Folder>>
    suspend fun getFolderById(id: String): Result<Folder>
    suspend fun insertFolder(folder: Folder): Result<Unit>
    suspend fun insertFolders(folders: List<Folder>): Result<Unit>
    suspend fun updateFolder(folder: Folder): Result<Unit>
    suspend fun deleteFolder(id: String): Result<Unit>
    suspend fun createFolder(name: String, color: Int): Result<Unit>
    suspend fun renameFolder(id: String, newName: String): Result<Unit>
    fun observeFolders(): Flow<List<Folder>>
} 