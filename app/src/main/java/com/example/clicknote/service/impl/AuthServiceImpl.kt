package com.example.clicknote.service.impl

import android.content.Context
import android.content.Intent
import com.example.clicknote.service.AuthService
import com.example.clicknote.domain.model.User
import com.google.android.gms.auth.api.signin.GoogleSignInClient
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.auth.GoogleAuthProvider
import com.google.firebase.auth.UserProfileChangeRequest
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.tasks.await
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class AuthServiceImpl @Inject constructor(
    @ApplicationContext private val context: Context,
    private val auth: FirebaseAuth,
    private val googleSignInClient: GoogleSignInClient
) : AuthService {

    private val _currentUser = MutableStateFlow<User?>(null)
    override val currentUser: StateFlow<User?> = _currentUser.asStateFlow()

    private val _isSignedIn = MutableStateFlow(false)
    override val isSignedIn: StateFlow<Boolean> = _isSignedIn.asStateFlow()

    private val _userId = MutableStateFlow<String?>(null)
    override val userId: StateFlow<String?> = _userId.asStateFlow()

    init {
        auth.addAuthStateListener { firebaseAuth ->
            val user = firebaseAuth.currentUser
            if (user != null) {
                _currentUser.value = User(
                    id = user.uid,
                    email = user.email ?: "",
                    displayName = user.displayName ?: "",
                    photoUrl = user.photoUrl?.toString()
                )
                _isSignedIn.value = true
                _userId.value = user.uid
            } else {
                _currentUser.value = null
                _isSignedIn.value = false
                _userId.value = null
            }
        }
    }

    override suspend fun signInWithGoogle(): Result<FirebaseUser> = try {
        val signInIntent = googleSignInClient.signInIntent
        Result.failure(GoogleSignInIntentException(signInIntent))
    } catch (e: Exception) {
        Result.failure(e)
    }

    override suspend fun handleSignInResult(idToken: String): Result<FirebaseUser> = try {
        val credential = GoogleAuthProvider.getCredential(idToken, null)
        val authResult = auth.signInWithCredential(credential).await()
        Result.success(authResult.user!!)
    } catch (e: Exception) {
        Result.failure(e)
    }

    override suspend fun signOut() {
        auth.signOut()
        googleSignInClient.signOut().await()
    }

    override fun isSignedIn(): Boolean = auth.currentUser != null

    override suspend fun signInWithEmail(email: String, password: String): Result<FirebaseUser> = try {
        val result = auth.signInWithEmailAndPassword(email, password).await()
        Result.success(result.user!!)
    } catch (e: Exception) {
        Result.failure(e)
    }

    override suspend fun createAccount(email: String, password: String): Result<FirebaseUser> = try {
        val result = auth.createUserWithEmailAndPassword(email, password).await()
        Result.success(result.user!!)
    } catch (e: Exception) {
        Result.failure(e)
    }

    override suspend fun sendPasswordResetEmail(email: String): Result<Unit> = try {
        auth.sendPasswordResetEmail(email).await()
        Result.success(Unit)
    } catch (e: Exception) {
        Result.failure(e)
    }

    override suspend fun deleteAccount(): Result<Unit> = try {
        auth.currentUser?.delete()?.await()
        Result.success(Unit)
    } catch (e: Exception) {
        Result.failure(e)
    }

    override suspend fun updateDisplayName(displayName: String): Result<Unit> = try {
        val profileUpdates = UserProfileChangeRequest.Builder()
            .setDisplayName(displayName)
            .build()
        auth.currentUser?.updateProfile(profileUpdates)?.await()
        Result.success(Unit)
    } catch (e: Exception) {
        Result.failure(e)
    }

    override suspend fun updateEmail(newEmail: String): Result<Unit> = try {
        auth.currentUser?.updateEmail(newEmail)?.await()
        Result.success(Unit)
    } catch (e: Exception) {
        Result.failure(e)
    }

    override suspend fun updatePassword(newPassword: String): Result<Unit> = try {
        auth.currentUser?.updatePassword(newPassword)?.await()
        Result.success(Unit)
    } catch (e: Exception) {
        Result.failure(e)
    }

    override suspend fun linkWithEmail(email: String, password: String): Result<FirebaseUser> = try {
        val credential = com.google.firebase.auth.EmailAuthProvider.getCredential(email, password)
        val result = auth.currentUser?.linkWithCredential(credential)?.await()
        Result.success(result?.user!!)
    } catch (e: Exception) {
        Result.failure(e)
    }

    override suspend fun linkWithGoogle(idToken: String): Result<FirebaseUser> = try {
        val credential = GoogleAuthProvider.getCredential(idToken, null)
        val result = auth.currentUser?.linkWithCredential(credential)?.await()
        Result.success(result?.user!!)
    } catch (e: Exception) {
        Result.failure(e)
    }

    override suspend fun getIdToken(forceRefresh: Boolean): Result<String> = try {
        val result = auth.currentUser?.getIdToken(forceRefresh)?.await()
        if (result?.token != null) {
            Result.success(result.token!!)
        } else {
            Result.failure(Exception("Failed to get ID token"))
        }
    } catch (e: Exception) {
        Result.failure(e)
    }

    override suspend fun signInWithEmailPassword(email: String, password: String) {
        auth.signInWithEmailAndPassword(email, password).await()
    }

    override suspend fun signUpWithEmailPassword(email: String, password: String) {
        auth.createUserWithEmailAndPassword(email, password).await()
    }

    override suspend fun resetPassword(email: String) {
        auth.sendPasswordResetEmail(email).await()
    }

    override fun getCurrentUserId(): String? = auth.currentUser?.uid

    override fun isUserSignedIn(): Boolean = auth.currentUser != null

    class GoogleSignInIntentException(val intent: Intent) : Exception("Sign in with Google must be initiated from an Activity")
} 