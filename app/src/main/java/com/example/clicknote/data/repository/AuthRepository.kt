package com.example.clicknote.data.repository

import kotlinx.coroutines.flow.Flow

interface AuthRepository {
    val isSignedIn: Flow<Boolean>
    
    suspend fun signIn()
    suspend fun signOut()
    suspend fun getCurrentUserId(): String?
    suspend fun isUserPremium(): Boolean
} 