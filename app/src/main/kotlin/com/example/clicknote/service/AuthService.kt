package com.example.clicknote.service

import com.google.firebase.auth.FirebaseUser
import kotlinx.coroutines.flow.Flow

interface AuthService {
    /**
     * Current user state
     */
    val currentUser: Flow<FirebaseUser?>

    /**
     * Sign in with Google
     */
    suspend fun signInWithGoogle(): Result<FirebaseUser>

    /**
     * Handle sign in result
     */
    suspend fun handleSignInResult(idToken: String): Result<FirebaseUser>

    /**
     * Sign out
     */
    suspend fun signOut()

    /**
     * Check if user is signed in
     */
    fun isSignedIn(): Boolean

    /**
     * Sign in with email and password
     */
    suspend fun signInWithEmail(email: String, password: String): Result<FirebaseUser>

    /**
     * Create account with email and password
     */
    suspend fun createAccount(email: String, password: String): Result<FirebaseUser>

    /**
     * Send password reset email
     */
    suspend fun sendPasswordResetEmail(email: String): Result<Unit>

    /**
     * Delete current user's account
     */
    suspend fun deleteAccount(): Result<Unit>

    /**
     * Update user's display name
     */
    suspend fun updateDisplayName(displayName: String): Result<Unit>

    /**
     * Update user's email
     */
    suspend fun updateEmail(newEmail: String): Result<Unit>

    /**
     * Update user's password
     */
    suspend fun updatePassword(newPassword: String): Result<Unit>

    /**
     * Link anonymous account with email/password
     */
    suspend fun linkWithEmail(email: String, password: String): Result<FirebaseUser>

    /**
     * Link anonymous account with Google
     */
    suspend fun linkWithGoogle(idToken: String): Result<FirebaseUser>

    /**
     * Get current user's ID token
     */
    suspend fun getIdToken(forceRefresh: Boolean = false): Result<String>

    val isSignedIn: Flow<Boolean>
    val userId: Flow<String?>
    
    suspend fun signInWithEmailPassword(email: String, password: String)
    suspend fun signUpWithEmailPassword(email: String, password: String)
    suspend fun resetPassword(email: String)

    fun getCurrentUserId(): String?
    fun isUserSignedIn(): Boolean
} 