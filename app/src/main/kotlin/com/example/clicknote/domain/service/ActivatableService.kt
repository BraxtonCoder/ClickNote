package com.example.clicknote.domain.service

import kotlinx.coroutines.flow.Flow

interface ActivatableService : Service {
    val isActive: Flow<Boolean>

    suspend fun activate()
    suspend fun deactivate()
} 