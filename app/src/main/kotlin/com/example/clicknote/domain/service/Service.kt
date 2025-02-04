package com.example.clicknote.domain.service

import kotlinx.coroutines.flow.Flow

interface Service {
    val id: String
    val isInitialized: Flow<Boolean>

    suspend fun initialize()
    suspend fun cleanup()
} 