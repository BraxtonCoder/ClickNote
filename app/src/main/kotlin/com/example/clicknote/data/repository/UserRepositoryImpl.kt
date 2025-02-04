package com.example.clicknote.data.repository

import android.app.Activity
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
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class UserRepositoryImpl @Inject constructor(
    private val auth: FirebaseAuth,
    private val googleSignInClient: GoogleSignInClient,
    private val phoneAuthOptionsProvider: (String, PhoneAuthProvider.OnVerificationStateChangedCallbacks) -> PhoneAuthOptions
) : UserRepository {

    override fun getCurrentUser(): Flow<User?> = callbackFlow {
        val listener = FirebaseAuth.AuthStateListener { auth ->
            val user = auth.currentUser
            if (user != null) {
                trySend(User(
                    id = user.uid,
                    email = user.email,
                    displayName = user.displayName,
                    photoUrl = user.photoUrl?.toString(),
                    phoneNumber = user.phoneNumber,
                    isEmailVerified = user.isEmailVerified
                ))
            } else {
                trySend(null)
            }
        }
        auth.addAuthStateListener(listener)
        awaitClose { auth.removeAuthStateListener(listener) }
    }

    private fun FirebaseUser.toUser() = User(
        id = uid,
        email = email,
        displayName = displayName,
        photoUrl = photoUrl?.toString(),
        phoneNumber = phoneNumber,
        isEmailVerified = isEmailVerified
    )

    override suspend fun signInWithGoogle(idToken: String): Result<User> = runCatching {
        val credential = GoogleAuthProvider.getCredential(idToken, null)
        val result = auth.signInWithCredential(credential).await()
        result.user?.let { user ->
            User(
                id = user.uid,
                email = user.email,
                displayName = user.displayName,
                photoUrl = user.photoUrl?.toString(),
                phoneNumber = user.phoneNumber,
                isEmailVerified = user.isEmailVerified
            )
        } ?: throw IllegalStateException("Sign in failed: No user returned")
    }

    override suspend fun signInWithEmail(email: String, password: String): Result<User> = runCatching {
        val result = auth.signInWithEmailAndPassword(email, password).await()
        result.user?.let { user ->
            User(
                id = user.uid,
                email = user.email,
                displayName = user.displayName,
                photoUrl = user.photoUrl?.toString(),
                phoneNumber = user.phoneNumber,
                isEmailVerified = user.isEmailVerified
            )
        } ?: throw IllegalStateException("Sign in failed: No user returned")
    }

    override suspend fun signInWithPhoneNumber(
        phoneNumber: String,
        callbacks: PhoneAuthProvider.OnVerificationStateChangedCallbacks
    ) {
        val options = PhoneAuthOptions.newBuilder(auth)
            .setPhoneNumber(phoneNumber)
            .setTimeout(60L, java.util.concurrent.TimeUnit.SECONDS)
            .setCallbacks(callbacks)
            .build()

        PhoneAuthProvider.verifyPhoneNumber(options)
    }

    override suspend fun signInWithPhoneCredential(credential: PhoneAuthCredential): Result<User> = runCatching {
        val result = auth.signInWithCredential(credential).await()
        result.user?.let { user ->
            User(
                id = user.uid,
                email = user.email,
                displayName = user.displayName,
                photoUrl = user.photoUrl?.toString(),
                phoneNumber = user.phoneNumber,
                isEmailVerified = user.isEmailVerified
            )
        } ?: throw IllegalStateException("Sign in failed: No user returned")
    }

    override suspend fun linkPhoneNumberWithCurrentUser(
        phoneNumber: String,
        callbacks: PhoneAuthProvider.OnVerificationStateChangedCallbacks
    ) {
        val options = PhoneAuthOptions.newBuilder(auth)
            .setPhoneNumber(phoneNumber)
            .setTimeout(60L, java.util.concurrent.TimeUnit.SECONDS)
            .setCallbacks(callbacks)
            .build()

        PhoneAuthProvider.verifyPhoneNumber(options)
    }

    override suspend fun linkPhoneCredentialWithCurrentUser(credential: PhoneAuthCredential): Result<User> = runCatching {
        val result = auth.currentUser?.linkWithCredential(credential)?.await()
            ?: throw IllegalStateException("No current user to link phone number with")
        
        result.user?.let { user ->
            User(
                id = user.uid,
                email = user.email,
                displayName = user.displayName,
                photoUrl = user.photoUrl?.toString(),
                phoneNumber = user.phoneNumber,
                isEmailVerified = user.isEmailVerified
            )
        } ?: throw IllegalStateException("Link failed: No user returned")
    }

    override suspend fun signOut(): Result<Unit> = runCatching {
        googleSignInClient.signOut().await()
        auth.signOut()
    }

    override suspend fun deleteAccount(): Result<Unit> = runCatching {
        auth.currentUser?.delete()?.await()
            ?: throw IllegalStateException("No user signed in")
    }

    override suspend fun updateProfile(displayName: String?, photoUrl: String?): Result<Unit> = runCatching {
        val user = auth.currentUser ?: throw IllegalStateException("No user signed in")
        user.updateProfile(
            UserProfileChangeRequest.Builder()
                .setDisplayName(displayName)
                .setPhotoUri(photoUrl?.let { android.net.Uri.parse(it) })
                .build()
        ).await()
    }

    override suspend fun updateEmail(newEmail: String): Result<Unit> = runCatching {
        val user = auth.currentUser ?: throw IllegalStateException("No user signed in")
        user.updateEmail(newEmail).await()
    }

    override suspend fun updatePassword(newPassword: String): Result<Unit> = runCatching {
        val user = auth.currentUser ?: throw IllegalStateException("No user signed in")
        user.updatePassword(newPassword).await()
    }

    override suspend fun sendPasswordResetEmail(email: String): Result<Unit> = runCatching {
        auth.sendPasswordResetEmail(email).await()
    }

    override suspend fun reauthenticate(email: String, password: String): Result<Unit> = runCatching {
        val user = auth.currentUser ?: throw IllegalStateException("No user signed in")
        val credential = EmailAuthProvider.getCredential(email, password)
        user.reauthenticate(credential).await()
    }

    override suspend fun verifyPhoneNumberForTesting(phoneNumber: String, smsCode: String) {
        auth.firebaseAuthSettings.setAutoRetrievedSmsCodeForPhoneNumber(phoneNumber, smsCode)
    }

    override suspend fun disableAppVerificationForTesting() {
        auth.firebaseAuthSettings.setAppVerificationDisabledForTesting(true)
    }
} 