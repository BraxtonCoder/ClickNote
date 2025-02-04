package com.example.clicknote.data.repository

import com.example.clicknote.data.dao.FolderDao
import com.example.clicknote.data.entity.FolderEntity
import com.example.clicknote.domain.model.Folder
import com.example.clicknote.domain.repository.FolderRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class FolderRepositoryImpl @Inject constructor(
    private val folderDao: FolderDao
) : FolderRepository {

    override fun getAllFolders(): Flow<List<Folder>> =
        folderDao.getAllFolders().map { entities ->
            entities.map { it.toDomain() }
        }

    override suspend fun getFolderById(id: String): Folder? =
        folderDao.getFolderById(id)?.toDomain()

    override suspend fun insertFolder(folder: Folder): String {
        val entity = FolderEntity(
            name = folder.name,
            color = folder.color,
            noteCount = folder.noteCount,
            createdAt = folder.createdAt,
            updatedAt = folder.updatedAt,
            isDeleted = folder.isDeleted,
            deletedAt = folder.deletedAt
        )
        folderDao.insertFolder(entity)
        return entity.id
    }

    override suspend fun updateFolder(folder: Folder) {
        folderDao.updateFolder(
            FolderEntity(
                id = folder.id,
                name = folder.name,
                color = folder.color,
                noteCount = folder.noteCount,
                createdAt = folder.createdAt,
                updatedAt = System.currentTimeMillis(),
                isDeleted = folder.isDeleted,
                deletedAt = folder.deletedAt
            )
        )
    }

    override suspend fun deleteFolder(folder: Folder) {
        folderDao.deleteFolder(
            FolderEntity(
                id = folder.id,
                name = folder.name,
                color = folder.color,
                noteCount = folder.noteCount,
                createdAt = folder.createdAt,
                updatedAt = System.currentTimeMillis(),
                isDeleted = true,
                deletedAt = System.currentTimeMillis()
            )
        )
    }

    override suspend fun permanentlyDeleteFolder(folder: Folder) {
        folderDao.permanentlyDeleteFolder(folder.id)
    }

    override fun searchFolders(query: String): Flow<List<Folder>> =
        folderDao.searchFolders(query).map { entities ->
            entities.map { it.toDomain() }
        }

    override suspend fun incrementNoteCount(folderId: String) {
        folderDao.updateNoteCount(folderId, 1)
    }

    override suspend fun decrementNoteCount(folderId: String) {
        folderDao.updateNoteCount(folderId, -1)
    }

    override suspend fun folderNameExists(name: String): Boolean =
        folderDao.folderNameExists(name)

    private fun FolderEntity.toDomain() = Folder(
        id = id,
        name = name,
        color = color,
        noteCount = noteCount,
        createdAt = createdAt,
        updatedAt = updatedAt,
        isDeleted = isDeleted,
        deletedAt = deletedAt
    )
} 