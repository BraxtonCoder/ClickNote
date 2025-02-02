package com.example.clicknote.data.repository

import com.example.clicknote.data.dao.FolderDao
import com.example.clicknote.data.entity.FolderEntity
import com.example.clicknote.domain.repository.FolderRepository
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class FolderRepositoryImpl @Inject constructor(
    private val folderDao: FolderDao
) : FolderRepository {
    override fun getAllFolders(): Flow<List<FolderEntity>> = folderDao.getAllFolders()

    override fun getDeletedFolders(): Flow<List<FolderEntity>> = folderDao.getDeletedFolders()

    override suspend fun getFolderById(id: String): FolderEntity? = folderDao.getFolderById(id)

    override suspend fun insertFolder(folder: FolderEntity) = folderDao.insert(folder)

    override suspend fun updateFolder(folder: FolderEntity) = folderDao.update(folder)

    override suspend fun moveToTrash(id: String) = folderDao.moveToTrash(id)

    override suspend fun restoreFromTrash(id: String) = folderDao.restoreFromTrash(id)

    override suspend fun deleteFolder(id: String) = folderDao.delete(id)

    override suspend fun deleteExpiredFolders(timestamp: Long) = folderDao.deleteExpiredFolders(timestamp)

    override suspend fun folderExists(name: String): Boolean = folderDao.folderExists(name)

    override fun getTotalFoldersCount(): Int = folderDao.getTotalFoldersCount()

    override fun getDeletedFoldersCount(): Int = folderDao.getDeletedFoldersCount()
} 