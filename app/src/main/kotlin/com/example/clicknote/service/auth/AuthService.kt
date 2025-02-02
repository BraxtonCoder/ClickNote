package com.example.clicknote.service.auth

import kotlinx.coroutines.flow.Flow

interface AuthService {
    suspend fun signIn(email: String, password: String): AuthResult
    suspend fun signUp(email: String, password: String): AuthResult
    suspend fun signInWithGoogle(idToken: String): AuthResult
    suspend fun signOut()
    suspend fun resetPassword(email: String)
    suspend fun updatePassword(oldPassword: String, newPassword: String)
    suspend fun deleteAccount()
    fun getCurrentUser(): User?
    fun observeAuthState(): Flow<AuthState>
}

data class User(
    val id: String,
    val email: String,
    val displayName: String?,
    val photoUrl: String?,
    val isEmailVerified: Boolean
)

sealed class AuthResult {
    data class Success(val user: User) : AuthResult()
    data class Error(val message: String) : AuthResult()
}

sealed class AuthState {
    data class Authenticated(val user: User) : AuthState()
    object Unauthenticated : AuthState()
    object Loading : AuthState()
} 