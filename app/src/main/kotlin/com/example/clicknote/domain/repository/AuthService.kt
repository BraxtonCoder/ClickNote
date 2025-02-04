package com.example.clicknote.domain.repository

import kotlinx.coroutines.flow.Flow

interface AuthService {
    /**
     * Get the current user ID as a flow
     */
    val userId: Flow<String?>

    /**
     * Check if user is signed in
     */
    val isSignedIn: Flow<Boolean>

    /**
     * Sign in with Google
     */
    suspend fun signInWithGoogle(): Result<Unit>

    /**
     * Sign out
     */
    suspend fun signOut(): Result<Unit>

    /**
     * Get current user's email
     */
    val userEmail: Flow<String?>

    /**
     * Get current user's display name
     */
    val displayName: Flow<String?>

    /**
     * Get current user's profile photo URL
     */
    val photoUrl: Flow<String?>
} 