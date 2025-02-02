package com.example.clicknote.domain.repository

import com.google.firebase.auth.FirebaseUser
import kotlinx.coroutines.flow.Flow

interface AuthRepository {
    fun getCurrentUser(): Flow<FirebaseUser?>
    suspend fun signOut()
} 