package com.example.clicknote.domain.service

import com.example.clicknote.domain.model.BackupInfo
import kotlinx.coroutines.flow.Flow

interface BackupSearchService {
    fun searchBackups(query: String): Flow<List<BackupInfo>>
    
    fun filterBackupsByDate(
        startDate: String? = null,
        endDate: String? = null
    ): Flow<List<BackupInfo>>
    
    fun filterBackupsBySize(
        minSize: Long? = null,
        maxSize: Long? = null
    ): Flow<List<BackupInfo>>
    
    fun filterBackupsByMetadata(
        key: String,
        value: String
    ): Flow<List<BackupInfo>>
} 