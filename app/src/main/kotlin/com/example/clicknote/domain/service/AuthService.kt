package com.example.clicknote.domain.service

import com.example.clicknote.domain.model.User
import kotlinx.coroutines.flow.Flow

interface AuthService {
    val isAuthenticated: Flow<Boolean>
    val currentUser: Flow<User?>
    val userId: Flow<String?>

    suspend fun signInWithGoogle(idToken: String): Result<User>
    suspend fun signInWithEmailPassword(email: String, password: String): Result<User>
    suspend fun signUpWithEmailPassword(email: String, password: String): Result<User>
    suspend fun signOut(): Result<Unit>
    suspend fun deleteAccount(): Result<Unit>
    suspend fun resetPassword(email: String): Result<Unit>
    suspend fun updateProfile(displayName: String?, photoUrl: String?): Result<Unit>
    suspend fun reauthenticate(password: String): Result<Unit>
    suspend fun updateEmail(newEmail: String): Result<Unit>
    suspend fun updatePassword(newPassword: String): Result<Unit>
    suspend fun sendEmailVerification(): Result<Unit>
    suspend fun isEmailVerified(): Boolean
} 