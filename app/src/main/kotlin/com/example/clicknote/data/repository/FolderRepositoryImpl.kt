package com.example.clicknote.data.repository

import com.example.clicknote.data.dao.FolderDao
import com.example.clicknote.data.entity.FolderEntity
import com.example.clicknote.domain.model.Folder
import com.example.clicknote.domain.repository.FolderRepository
import kotlinx.coroutines.flow.*
import javax.inject.Inject
import javax.inject.Singleton
import java.util.UUID

@Singleton
class FolderRepositoryImpl @Inject constructor(
    private val folderDao: FolderDao
) : FolderRepository {

    override suspend fun getAllFolders(): Result<List<Folder>> = runCatching {
        folderDao.getAllFolders().first().map { it.toDomain() }
    }

    override suspend fun getFolderById(id: String): Result<Folder> = runCatching {
        folderDao.getFolderById(id)?.toDomain() ?: throw IllegalStateException("Folder not found")
    }

    override suspend fun insertFolder(folder: Folder): Result<Unit> = runCatching {
        val now = System.currentTimeMillis()
        val entity = FolderEntity(
            id = folder.id.ifEmpty { UUID.randomUUID().toString() },
            name = folder.name,
            color = folder.color,
            createdAt = now,
            modifiedAt = now,
            isDeleted = false,
            parentId = folder.parentId,
            position = folder.position
        )
        folderDao.insert(entity)
    }

    override suspend fun insertFolders(folders: List<Folder>): Result<Unit> = runCatching {
        val entities = folders.map { folder ->
            val now = System.currentTimeMillis()
            FolderEntity(
                id = folder.id.ifEmpty { UUID.randomUUID().toString() },
                name = folder.name,
                color = folder.color,
                createdAt = now,
                modifiedAt = now,
                isDeleted = false,
                parentId = folder.parentId,
                position = folder.position
            )
        }
        folderDao.insertAll(entities)
    }

    override suspend fun updateFolder(folder: Folder): Result<Unit> = runCatching {
        val now = System.currentTimeMillis()
        folderDao.update(
            FolderEntity(
                id = folder.id,
                name = folder.name,
                color = folder.color,
                createdAt = folder.createdAt,
                modifiedAt = now,
                isDeleted = folder.isDeleted,
                parentId = folder.parentId,
                position = folder.position
            )
        )
    }

    override suspend fun deleteFolder(id: String): Result<Unit> = runCatching {
        folderDao.delete(id)
    }

    override suspend fun createFolder(name: String, color: Int): Result<Unit> = runCatching {
        val folder = Folder.create(name, color)
        insertFolder(folder).getOrThrow()
    }

    override suspend fun renameFolder(id: String, newName: String): Result<Unit> = runCatching {
        val folder = getFolderById(id).getOrThrow()
        updateFolder(folder.copy(name = newName)).getOrThrow()
    }

    override fun observeFolders(): Flow<List<Folder>> =
        folderDao.getAllFolders().map { entities ->
            entities.map { it.toDomain() }
        }

    suspend fun folderNameExists(name: String): Boolean =
        folderDao.folderExists(name)

    private fun FolderEntity.toDomain() = Folder(
        id = id,
        name = name,
        color = color,
        createdAt = createdAt,
        modifiedAt = modifiedAt,
        isDeleted = isDeleted,
        parentId = parentId,
        position = position
    )
} 