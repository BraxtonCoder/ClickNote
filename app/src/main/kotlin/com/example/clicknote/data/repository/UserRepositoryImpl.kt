package com.example.clicknote.data.repository

import android.app.Activity
import android.net.Uri
import com.example.clicknote.BuildConfig
import com.example.clicknote.domain.model.User
import com.example.clicknote.domain.repository.UserRepository
import com.google.firebase.auth.*
import com.google.android.gms.auth.api.signin.GoogleSignInClient
import com.google.firebase.auth.ktx.auth
import com.google.firebase.ktx.Firebase
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.tasks.await
import java.util.concurrent.TimeUnit
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class UserRepositoryImpl @Inject constructor(
    private val auth: FirebaseAuth,
    private val googleSignInClient: GoogleSignInClient,
    private val activity: Activity
) : UserRepository {

    override fun observeCurrentUser(): Flow<User?> = callbackFlow {
        val listener = FirebaseAuth.AuthStateListener { auth ->
            trySend(auth.currentUser?.toUser())
        }
        auth.addAuthStateListener(listener)
        awaitClose { auth.removeAuthStateListener(listener) }
    }

    override suspend fun getCurrentUser(): User? {
        return auth.currentUser?.toUser()
    }

    override suspend fun signInWithEmail(email: String, password: String): Result<User> = runCatching {
        val result = auth.signInWithEmailAndPassword(email, password).await()
        result.user?.toUser() ?: throw IllegalStateException("Sign in failed")
    }

    override suspend fun signInWithGoogle(idToken: String): Result<User> = runCatching {
        val credential = GoogleAuthProvider.getCredential(idToken, null)
        val result = auth.signInWithCredential(credential).await()
        result.user?.toUser() ?: throw IllegalStateException("Google sign in failed")
    }

    override suspend fun signInAnonymously(): Result<User> = runCatching {
        val result = auth.signInAnonymously().await()
        result.user?.toUser() ?: throw IllegalStateException("Anonymous sign in failed")
    }

    override suspend fun signInWithPhoneNumber(
        phoneNumber: String,
        callbacks: PhoneAuthProvider.OnVerificationStateChangedCallbacks
    ) {
        val options = PhoneAuthOptions.newBuilder(auth)
            .setPhoneNumber(phoneNumber)
            .setTimeout(60L, TimeUnit.SECONDS)
            .setActivity(activity)
            .setCallbacks(callbacks)
            .build()
        PhoneAuthProvider.verifyPhoneNumber(options)
    }

    override suspend fun signInWithPhoneCredential(credential: PhoneAuthCredential): Result<User> = runCatching {
        val result = auth.signInWithCredential(credential).await()
        result.user?.toUser() ?: throw IllegalStateException("Phone sign in failed")
    }

    override suspend fun linkPhoneNumberWithCurrentUser(
        phoneNumber: String,
        callbacks: PhoneAuthProvider.OnVerificationStateChangedCallbacks
    ) {
        val user = auth.currentUser ?: throw IllegalStateException("No user signed in")
        val options = PhoneAuthOptions.newBuilder(auth)
            .setPhoneNumber(phoneNumber)
            .setTimeout(60L, TimeUnit.SECONDS)
            .setActivity(activity)
            .setCallbacks(callbacks)
            .build()
        PhoneAuthProvider.verifyPhoneNumber(options)
    }

    override suspend fun linkPhoneCredentialWithCurrentUser(credential: PhoneAuthCredential): Result<User> = runCatching {
        val user = auth.currentUser ?: throw IllegalStateException("No user signed in")
        val result = user.linkWithCredential(credential).await()
        result.user?.toUser() ?: throw IllegalStateException("Phone linking failed")
    }

    override suspend fun signOut() {
        auth.signOut()
    }

    override suspend fun deleteAccount(): Result<Unit> = runCatching {
        auth.currentUser?.delete()?.await()
    }

    override suspend fun updateProfile(displayName: String?, photoUrl: String?): Result<Unit> = runCatching {
        val user = auth.currentUser ?: throw IllegalStateException("No user signed in")
        val profileUpdates = UserProfileChangeRequest.Builder().apply {
            displayName?.let { setDisplayName(it) }
            photoUrl?.let { setPhotoUri(Uri.parse(it)) }
        }.build()
        user.updateProfile(profileUpdates).await()
    }

    override suspend fun updateEmail(newEmail: String): Result<Unit> = runCatching {
        val user = auth.currentUser ?: throw IllegalStateException("No user signed in")
        user.updateEmail(newEmail).await()
    }

    override suspend fun updatePassword(newPassword: String): Result<Unit> = runCatching {
        val user = auth.currentUser ?: throw IllegalStateException("No user signed in")
        user.updatePassword(newPassword).await()
    }

    override suspend fun sendEmailVerification(): Result<Unit> = runCatching {
        val user = auth.currentUser ?: throw IllegalStateException("No user signed in")
        user.sendEmailVerification().await()
    }

    override suspend fun sendPasswordResetEmail(email: String): Result<Unit> = runCatching {
        auth.sendPasswordResetEmail(email).await()
    }

    override suspend fun reauthenticate(email: String, password: String): Result<Unit> = runCatching {
        val user = auth.currentUser ?: throw IllegalStateException("No user signed in")
        val credential = EmailAuthProvider.getCredential(email, password)
        user.reauthenticate(credential).await()
    }

    override suspend fun isSignedIn(): Boolean {
        return auth.currentUser != null
    }

    override suspend fun getUserId(): String? {
        return auth.currentUser?.uid
    }

    override suspend fun verifyPhoneNumberForTesting(phoneNumber: String, smsCode: String) {
        // Only used for testing
    }

    override suspend fun disableAppVerificationForTesting() {
        // Only used for testing
    }

    private fun com.google.firebase.auth.FirebaseUser.toUser(): User {
        return User(
            id = uid,
            email = email ?: "",
            displayName = displayName,
            photoUrl = photoUrl?.toString(),
            isEmailVerified = isEmailVerified,
            isAnonymous = isAnonymous,
            createdAt = metadata?.creationTimestamp ?: System.currentTimeMillis(),
            lastSignInAt = metadata?.lastSignInTimestamp ?: System.currentTimeMillis()
        )
    }
} 