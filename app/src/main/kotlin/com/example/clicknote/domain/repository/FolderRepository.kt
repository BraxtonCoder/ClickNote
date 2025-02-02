package com.example.clicknote.domain.repository

import com.example.clicknote.data.entity.FolderEntity
import kotlinx.coroutines.flow.Flow

interface FolderRepository {
    fun getAllFolders(): Flow<List<FolderEntity>>
    fun getDeletedFolders(): Flow<List<FolderEntity>>
    suspend fun getFolderById(id: String): FolderEntity?
    suspend fun insertFolder(folder: FolderEntity)
    suspend fun updateFolder(folder: FolderEntity)
    suspend fun moveToTrash(id: String)
    suspend fun restoreFromTrash(id: String)
    suspend fun deleteFolder(id: String)
    suspend fun deleteExpiredFolders(timestamp: Long)
    suspend fun folderExists(name: String): Boolean
    fun getTotalFoldersCount(): Int
    fun getDeletedFoldersCount(): Int
} 