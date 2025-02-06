package com.example.clicknote.domain.repository

import com.example.clicknote.domain.model.User
import com.google.firebase.auth.PhoneAuthCredential
import com.google.firebase.auth.PhoneAuthProvider
import kotlinx.coroutines.flow.Flow

interface UserRepository {
    /**
     * Get the current signed-in user
     */
    suspend fun getCurrentUser(): User?

    /**
     * Observe the current user state
     */
    fun observeCurrentUser(): Flow<User?>

    /**
     * Sign in with email and password
     */
    suspend fun signInWithEmail(email: String, password: String): Result<User>

    /**
     * Sign in with Google
     */
    suspend fun signInWithGoogle(idToken: String): Result<User>

    /**
     * Sign in anonymously
     */
    suspend fun signInAnonymously(): Result<User>

    /**
     * Sign out the current user
     */
    suspend fun signOut()

    /**
     * Delete the current user account
     */
    suspend fun deleteAccount(): Result<Unit>

    /**
     * Update user profile
     */
    suspend fun updateProfile(displayName: String? = null, photoUrl: String? = null): Result<Unit>

    /**
     * Update user email
     */
    suspend fun updateEmail(newEmail: String): Result<Unit>

    /**
     * Send email verification
     */
    suspend fun sendEmailVerification(): Result<Unit>

    /**
     * Check if user is signed in
     */
    suspend fun isSignedIn(): Boolean

    /**
     * Get user ID or null if not signed in
     */
    suspend fun getUserId(): String?

    suspend fun signInWithPhoneNumber(
        phoneNumber: String,
        callbacks: PhoneAuthProvider.OnVerificationStateChangedCallbacks
    )
    
    suspend fun signInWithPhoneCredential(credential: PhoneAuthCredential): Result<User>
    
    suspend fun linkPhoneNumberWithCurrentUser(
        phoneNumber: String,
        callbacks: PhoneAuthProvider.OnVerificationStateChangedCallbacks
    )
    
    suspend fun linkPhoneCredentialWithCurrentUser(credential: PhoneAuthCredential): Result<User>
    
    suspend fun updatePassword(newPassword: String): Result<Unit>
    
    suspend fun sendPasswordResetEmail(email: String): Result<Unit>
    
    suspend fun reauthenticate(email: String, password: String): Result<Unit>

    // Testing utilities
    suspend fun verifyPhoneNumberForTesting(phoneNumber: String, smsCode: String)
    
    suspend fun disableAppVerificationForTesting()
} 