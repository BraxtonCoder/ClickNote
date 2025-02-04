package com.example.clicknote.domain.service

interface BaseService {
    val id: String
    suspend fun cleanup()
    fun isInitialized(): Boolean
} 