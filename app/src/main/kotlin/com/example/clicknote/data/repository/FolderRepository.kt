package com.example.clicknote.data.repository

import com.example.clicknote.data.dao.FolderDao
import com.example.clicknote.data.entity.FolderEntity
import com.example.clicknote.domain.model.Folder
import com.example.clicknote.domain.repository.FolderRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject
import javax.inject.Singleton
import java.util.UUID

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
        val now = System.currentTimeMillis()
        val entity = FolderEntity(
            id = UUID.randomUUID().toString(),
            name = folder.name,
            color = folder.color,
            noteCount = folder.noteCount,
            createdAt = now,
            modifiedAt = now,
            isDeleted = false,
            deletedAt = null
        )
        folderDao.insert(entity)
        return entity.id
    }

    override suspend fun updateFolder(folder: Folder) {
        val now = System.currentTimeMillis()
        folderDao.update(
            FolderEntity(
                id = folder.id,
                name = folder.name,
                color = folder.color,
                noteCount = folder.noteCount,
                createdAt = folder.createdAt,
                modifiedAt = now,
                isDeleted = folder.isDeleted,
                deletedAt = folder.deletedAt
            )
        )
    }

    override suspend fun deleteFolder(folder: Folder) {
        val now = System.currentTimeMillis()
        folderDao.moveToTrash(folder.id, now)
    }

    override suspend fun permanentlyDeleteFolder(folder: Folder) {
        folderDao.delete(folder.id)
    }

    override fun searchFolders(query: String): List<Folder> {
        return folderDao.getAllFolders().map { entities ->
            entities.filter { it.name.contains(query, ignoreCase = true) }
                .map { it.toDomain() }
        }.first()
    }

    override suspend fun incrementNoteCount(folderId: String) {
        val folder = folderDao.getFolderById(folderId) ?: return
        folderDao.update(folder.copy(noteCount = folder.noteCount + 1))
    }

    override suspend fun decrementNoteCount(folderId: String) {
        val folder = folderDao.getFolderById(folderId) ?: return
        folderDao.update(folder.copy(noteCount = (folder.noteCount - 1).coerceAtLeast(0)))
    }

    override suspend fun folderNameExists(name: String): Boolean =
        folderDao.folderExists(name)

    private fun FolderEntity.toDomain() = Folder(
        id = id,
        name = name,
        color = color,
        noteCount = noteCount,
        createdAt = createdAt,
        updatedAt = modifiedAt,
        isDeleted = isDeleted,
        deletedAt = deletedAt
    )
} 