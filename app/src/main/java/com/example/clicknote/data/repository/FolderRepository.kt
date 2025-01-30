package com.example.clicknote.data.repository

import com.example.clicknote.data.dao.FolderDao
import com.example.clicknote.data.entity.Folder
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class FolderRepository @Inject constructor(
    private val folderDao: FolderDao
) {
    fun getAllFolders(): Flow<List<Folder>> = folderDao.getAllFolders()

    suspend fun getFolderById(id: Long): Folder? = folderDao.getFolderById(id)

    suspend fun insertFolder(folder: Folder): Long = folderDao.insert(folder)

    suspend fun updateFolder(folder: Folder) = folderDao.update(folder)

    suspend fun deleteFolder(folder: Folder) = folderDao.delete(folder)

    fun searchFolders(query: String): Flow<List<Folder>> = folderDao.searchFolders(query)

    suspend fun updateNoteCount(folderId: Long, change: Int) = folderDao.updateNoteCount(folderId, change)

    suspend fun folderNameExists(name: String, excludeId: Long = 0): Boolean =
        folderDao.folderNameExists(name, excludeId)
} 