package com.example.clicknote.data.auth

import kotlinx.coroutines.flow.Flow

data class User(
    val id: String,
    val email: String?,
    val displayName: String?
)

interface AuthRepository {
    fun getCurrentUser(): Flow<User?>
    suspend fun signInWithGoogle()
    suspend fun signOut()
    suspend fun deleteAccount()
} 