package com.example.clicknote

import android.content.Intent
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.viewModels
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.*
import androidx.lifecycle.lifecycleScope
import com.example.clicknote.domain.preferences.UserPreferencesDataStore
import com.example.clicknote.service.InternalAudioCaptureManager
import com.example.clicknote.ui.navigation.AppNavigation
import com.example.clicknote.ui.onboarding.OnboardingScreen
import com.example.clicknote.ui.theme.ClickNoteTheme
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInAccount
import com.google.android.gms.auth.api.signin.GoogleSignInClient
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.google.android.gms.common.api.ApiException
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.auth.GoogleAuthProvider
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import javax.inject.Inject

@AndroidEntryPoint
class MainActivity : ComponentActivity() {
    
    @Inject
    lateinit var userPreferences: UserPreferencesDataStore
    
    @Inject
    lateinit var internalAudioCaptureManager: InternalAudioCaptureManager

    private lateinit var auth: FirebaseAuth
    private lateinit var googleSignInClient: GoogleSignInClient
    private lateinit var signInLauncher: ActivityResultLauncher<Intent>

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        
        // Initialize Firebase Auth
        auth = FirebaseAuth.getInstance()
        
        // Configure Google Sign In
        val gso = GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
            .requestIdToken(getString(R.string.default_web_client_id))
            .requestEmail()
            .build()
        
        googleSignInClient = GoogleSignIn.getClient(this, gso)
        
        // Initialize sign-in launcher
        signInLauncher = registerForActivityResult(
            ActivityResultContracts.StartActivityForResult()
        ) { result ->
            val task = GoogleSignIn.getSignedInAccountFromIntent(result.data)
            try {
                val account = task.getResult(ApiException::class.java)
                firebaseAuthWithGoogle(account.idToken!!)
            } catch (e: ApiException) {
                // Handle sign in failure
                handleSignInError(e)
            }
        }

        // Check if first launch
        checkFirstLaunch()

        lifecycleScope.launch {
            val isFirstLaunch = userPreferences.isFirstLaunch.first()
            
            setContent {
                ClickNoteTheme {
                    Surface(color = MaterialTheme.colorScheme.background) {
                        if (isFirstLaunch) {
                            OnboardingScreen(
                                onOnboardingComplete = {
                                    lifecycleScope.launch {
                                        userPreferences.setFirstLaunch(false)
                                    }
                                }
                            )
                        } else {
                            AppNavigation(
                                onSignInClick = { signIn() },
                                onSignOutClick = { signOut() }
                            )
                        }
                    }
                }
            }
        }
    }

    private fun checkFirstLaunch() {
        CoroutineScope(Dispatchers.IO).launch {
            if (userPreferences.isFirstLaunch.first()) {
                userPreferences.setFirstLaunch(false)
                // Show onboarding or initial setup
                showOnboarding()
            }
        }
    }

    private fun showOnboarding() {
        // Implement onboarding UI
    }

    private fun signIn() {
        val signInIntent = googleSignInClient.signInIntent
        signInLauncher.launch(signInIntent)
    }

    private fun signOut() {
        // Sign out from Firebase
        auth.signOut()

        // Sign out from Google
        googleSignInClient.signOut().addOnCompleteListener(this) {
            // Handle sign out completion
            handleSignOutComplete()
        }
    }

    private fun firebaseAuthWithGoogle(idToken: String) {
        val credential = GoogleAuthProvider.getCredential(idToken, null)
        auth.signInWithCredential(credential)
            .addOnCompleteListener(this) { task ->
                if (task.isSuccessful) {
                    // Sign in success
                    val user = auth.currentUser
                    handleSignInSuccess(user)
                } else {
                    // Sign in failed
                    handleSignInError(task.exception)
                }
            }
    }

    private fun handleSignInSuccess(user: FirebaseUser?) {
        // Handle successful sign in
    }

    private fun handleSignInError(exception: Exception?) {
        // Handle sign in error
    }

    private fun handleSignOutComplete() {
        // Handle sign out completion
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        
        when (requestCode) {
            InternalAudioCaptureManager.REQUEST_MEDIA_PROJECTION -> {
                internalAudioCaptureManager.startRecording(resultCode, data)
            }
        }
    }
}
}