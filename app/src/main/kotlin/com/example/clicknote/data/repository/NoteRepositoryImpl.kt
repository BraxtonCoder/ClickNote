package com.example.clicknote.data.repository

import com.example.clicknote.data.dao.NoteDao
import com.example.clicknote.data.entity.NoteEntity
import com.example.clicknote.data.entity.NoteWithFolderEntity
import com.example.clicknote.domain.repository.NoteRepository
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

class NoteRepositoryImpl @Inject constructor(
    private val noteDao: NoteDao
) : NoteRepository {
    override fun getAllNotes(): Flow<List<NoteWithFolderEntity>> = noteDao.getAllNotesWithFolders()

    override fun getNotesInFolder(folderId: String): Flow<List<NoteWithFolderEntity>> = noteDao.getNotesInFolder(folderId)

    override fun searchNotes(query: String): Flow<List<NoteWithFolderEntity>> = noteDao.searchNotesWithFolders(query)

    override fun getDeletedNotes(): Flow<List<NoteWithFolderEntity>> = noteDao.getDeletedNotes()

    override suspend fun getNoteById(id: String): NoteEntity? = noteDao.getNoteById(id)

    override suspend fun insertNote(note: NoteEntity) = noteDao.insertNote(note)

    override suspend fun updateNote(note: NoteEntity) = noteDao.updateNote(note)

    override suspend fun moveToTrash(id: String) = noteDao.moveToTrash(id)

    override suspend fun restoreFromTrash(id: String) = noteDao.restoreFromTrash(id)

    override suspend fun deleteNote(id: String) = noteDao.delete(id)

    override suspend fun deleteExpiredNotes(timestamp: Long) = noteDao.deleteExpiredNotes(timestamp)

    override suspend fun moveNotesToFolder(noteIds: List<String>, folderId: String?) = noteDao.moveNotesToFolder(noteIds, folderId)

    override suspend fun updatePinned(id: String, isPinned: Boolean) = noteDao.updatePinned(id, isPinned)

    override fun getNotesByDateRange(startTimestamp: Long, endTimestamp: Long): Flow<List<NoteWithFolderEntity>> = 
        noteDao.getNotesByDateRange(startTimestamp, endTimestamp)

    override fun getNotesCountInDateRange(startTimestamp: Long, endTimestamp: Long): Int = 
        noteDao.getNotesCountInDateRange(startTimestamp, endTimestamp)

    override fun getTotalNotesCount(): Int = noteDao.getTotalNotesCount()
} 