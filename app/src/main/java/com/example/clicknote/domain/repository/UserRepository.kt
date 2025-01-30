package com.example.clicknote.domain.repository

import com.example.clicknote.domain.model.User
import com.google.firebase.auth.PhoneAuthCredential
import com.google.firebase.auth.PhoneAuthProvider
import kotlinx.coroutines.flow.Flow

interface UserRepository {
    fun getCurrentUser(): Flow<User?>
    
    suspend fun signInWithGoogle(idToken: String): Result<User>
    
    suspend fun signInWithEmail(email: String, password: String): Result<User>
    
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
    
    suspend fun signOut(): Result<Unit>
    
    suspend fun deleteAccount(): Result<Unit>
    
    suspend fun updateProfile(displayName: String?, photoUrl: String?): Result<Unit>
    
    suspend fun updateEmail(newEmail: String): Result<Unit>
    
    suspend fun updatePassword(newPassword: String): Result<Unit>
    
    suspend fun sendPasswordResetEmail(email: String): Result<Unit>
    
    suspend fun reauthenticate(email: String, password: String): Result<Unit>

    // Testing utilities
    suspend fun verifyPhoneNumberForTesting(phoneNumber: String, smsCode: String)
    
    suspend fun disableAppVerificationForTesting()
} 