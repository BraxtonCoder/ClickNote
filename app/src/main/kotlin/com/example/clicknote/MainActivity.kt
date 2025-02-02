package com.example.clicknote

import android.content.Intent
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.viewModels
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Menu
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.example.clicknote.R
import com.example.clicknote.domain.preferences.UserPreferencesDataStore
import com.example.clicknote.service.InternalAudioCaptureManager
import com.example.clicknote.navigation.NavGraph
import com.example.clicknote.navigation.Screen
import com.example.clicknote.ui.components.NavigationDrawer
import com.example.clicknote.ui.theme.ClickNoteTheme
import dagger.hilt.android.AndroidEntryPoint
import androidx.lifecycle.lifecycleScope
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import javax.inject.Inject
import com.google.firebase.auth.FirebaseAuth
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInAccount
import com.google.android.gms.common.api.ApiException
import com.example.clicknote.service.AuthService
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers

@AndroidEntryPoint
class MainActivity : ComponentActivity() {
    
    @Inject
    lateinit var userPreferences: UserPreferencesDataStore
    
    @Inject
    lateinit var internalAudioCaptureManager: InternalAudioCaptureManager
    
    @Inject
    lateinit var authService: AuthService

    @OptIn(ExperimentalMaterial3Api::class)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        
        var startDestination by mutableStateOf<String?>(null)
        
        lifecycleScope.launch {
            val isFirstLaunch = userPreferences.isFirstLaunch().first()
            startDestination = if (isFirstLaunch) {
                Screen.Onboarding.route
            } else if (shouldShowPhoneAuth()) {
                Screen.PhoneAuth.route
            } else {
                Screen.Notes.route
            }
        }
        
        setContent {
            ClickNoteTheme {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    val navController = rememberNavController()
                    val drawerState = rememberDrawerState(initialValue = DrawerValue.Closed)
                    val scope = rememberCoroutineScope()
                    
                    val currentRoute = navController
                        .currentBackStackEntryAsState().value?.destination?.route ?: Screen.Notes.route

                    // Check authentication state when route changes
                    LaunchedEffect(currentRoute) {
                        if (shouldShowPhoneAuth() && currentRoute != Screen.PhoneAuth.route 
                            && currentRoute != Screen.Onboarding.route) {
                            navController.navigate(Screen.PhoneAuth.route) {
                                popUpTo(navController.graph.startDestinationId)
                                launchSingleTop = true
                            }
                        }
                    }

                    ModalNavigationDrawer(
                        drawerState = drawerState,
                        drawerContent = {
                            NavigationDrawer(
                                onNavigate = { screen ->
                                    navController.navigate(screen.route) {
                                        popUpTo(navController.graph.startDestinationId)
                                        launchSingleTop = true
                                    }
                                },
                                onCloseDrawer = {
                                    scope.launch { drawerState.close() }
                                },
                                currentRoute = currentRoute
                            )
                        }
                    ) {
                        Scaffold(
                            topBar = {
                                TopAppBar(
                                    title = { Text(stringResource(R.string.app_name)) },
                                    navigationIcon = {
                                        IconButton(onClick = {
                                            scope.launch { drawerState.open() }
                                        }) {
                                            Icon(
                                                imageVector = Icons.Default.Menu,
                                                contentDescription = "Open Menu"
                                            )
                                        }
                                    }
                                )
                            }
                        ) { paddingValues ->
                            Box(modifier = Modifier.padding(paddingValues)) {
                                NavGraph(
                                    navController = navController,
                                    startDestination = startDestination ?: Screen.Notes.route
                                )
                            }
                        }
                    }
                }
            }
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        
        when (requestCode) {
            InternalAudioCaptureManager.REQUEST_MEDIA_PROJECTION -> {
                internalAudioCaptureManager.startRecording(resultCode, data)
            }
            RC_SIGN_IN -> {
                val task = GoogleSignIn.getSignedInAccountFromIntent(data)
                try {
                    val account = task.getResult(ApiException::class.java)
                    handleGoogleSignInResult(account)
                } catch (e: ApiException) {
                    // Handle sign in failure
                }
            }
        }
    }

    private fun handleGoogleSignInResult(account: GoogleSignInAccount) {
        account.idToken?.let { idToken ->
            CoroutineScope(Dispatchers.Main).launch {
                authService.handleSignInResult(idToken)
            }
        }
    }

    private fun shouldShowPhoneAuth(): Boolean {
        val currentUser = FirebaseAuth.getInstance().currentUser
        return currentUser == null || (currentUser.phoneNumber.isNullOrEmpty() && !currentUser.isEmailVerified)
    }

    companion object {
        private const val RC_SIGN_IN = 9001
    }
}