package com.example.clicknote.domain.service

import com.example.clicknote.domain.model.User
import kotlinx.coroutines.flow.Flow

interface AuthService {
    suspend fun signInWithGoogle(idToken: String): Result<User>
    suspend fun signOut(): Result<Unit>
    suspend fun getCurrentUser(): Result<User?>
    fun observeAuthState(): Flow<User?>
    suspend fun deleteAccount(): Result<Unit>
    suspend fun updateUserProfile(user: User): Result<Unit>
    fun isUserSignedIn(): Boolean
    fun getUserId(): String?
} 