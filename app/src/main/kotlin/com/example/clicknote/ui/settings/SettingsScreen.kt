package com.example.clicknote.ui.settings

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.example.clicknote.R
import com.example.clicknote.domain.model.StorageLocation
import com.example.clicknote.domain.model.TranscriptionLanguage
import com.example.clicknote.ui.components.LoadingScreen

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsScreen(
    onNavigateBack: () -> Unit,
    onNavigateToSubscription: () -> Unit,
    onNavigateToPhoneAuth: () -> Unit,
    modifier: Modifier = Modifier,
    viewModel: SettingsViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()

    if (uiState.isLoading) {
        LoadingScreen()
        return
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Settings") },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Back")
                    }
                }
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .verticalScroll(rememberScrollState())
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Cloud Storage Section
            CloudStorageSection(
                isCloudSyncEnabled = uiState.isCloudSyncEnabled,
                isOfflineModeEnabled = uiState.isOfflineModeEnabled,
                storageUsage = uiState.storageUsage,
                storageLimit = uiState.storageLimit,
                onCloudSyncToggle = viewModel::toggleCloudSync,
                onOfflineModeToggle = viewModel::toggleOfflineMode,
                onBackupNow = viewModel::backupNow,
                onRestoreBackup = viewModel::restoreBackup,
                onManageStorage = { /* TODO: Navigate to storage management */ }
            )

            // Subscription Section
            SubscriptionSection(
                currentPlan = uiState.currentSubscriptionPlan,
                remainingFreeNotes = uiState.remainingFreeNotes,
                onUpgradePlan = { onNavigateToSubscription() },
                onManagePlan = { onNavigateToSubscription() }
            )

            // Recording Settings Section
            Card(
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    Text(
                        text = "Recording Settings",
                        style = MaterialTheme.typography.titleMedium
                    )

                    // Save audio toggle
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Column {
                            Text(
                                text = "Save Audio Files",
                                style = MaterialTheme.typography.bodyLarge
                            )
                            Text(
                                text = "Keep original audio recordings",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                        Switch(
                            checked = uiState.saveAudioWithNote,
                            onCheckedChange = { viewModel.updateSaveAudioSetting(it) }
                        )
                    }

                    // High quality audio toggle
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Column {
                            Text(
                                text = "High Quality Audio",
                                style = MaterialTheme.typography.bodyLarge
                            )
                            Text(
                                text = "Record in higher quality (uses more storage)",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                        Switch(
                            checked = uiState.highQualityAudio,
                            onCheckedChange = { viewModel.updateHighQualityAudioSetting(it) }
                        )
                    }
                }
            }

            // Language Settings Section
            Card(
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    Text(
                        text = "Language & Recognition",
                        style = MaterialTheme.typography.titleMedium
                    )

                    // Offline recognition toggle
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Column {
                            Text(
                                text = "Offline Recognition",
                                style = MaterialTheme.typography.bodyLarge
                            )
                            Text(
                                text = "Enable transcription without internet",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                        Switch(
                            checked = uiState.offlineRecognition,
                            onCheckedChange = { viewModel.updateOfflineRecognitionSetting(it) }
                        )
                    }

                    // Language selection
                    var showLanguageDialog by remember { mutableStateOf(false) }
                    
                    OutlinedButton(
                        onClick = { showLanguageDialog = true },
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Icon(Icons.Default.Language, contentDescription = null)
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("Recognition Language (${uiState.transcriptionLanguage.displayName})")
                    }

                    if (showLanguageDialog) {
                        LanguageSelectionDialog(
                            currentLanguage = uiState.transcriptionLanguage,
                            onLanguageSelected = { language ->
                                viewModel.updateTranscriptionLanguage(language)
                            },
                            onDismiss = { showLanguageDialog = false }
                        )
                    }
                }
            }

            // Accessibility Settings Section
            Card(
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    Text(
                        text = "Accessibility",
                        style = MaterialTheme.typography.titleMedium
                    )

                    // Vibration feedback toggle
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Column {
                            Text(
                                text = "Vibration Feedback",
                                style = MaterialTheme.typography.bodyLarge
                            )
                            Text(
                                text = "Vibrate when recording starts/stops",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                        Switch(
                            checked = uiState.vibrationFeedback,
                            onCheckedChange = { viewModel.updateVibrationFeedbackSetting(it) }
                        )
                    }

                    // High contrast toggle
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Column {
                            Text(
                                text = "High Contrast",
                                style = MaterialTheme.typography.bodyLarge
                            )
                            Text(
                                text = "Increase visual contrast",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                        Switch(
                            checked = uiState.highContrast,
                            onCheckedChange = { viewModel.updateHighContrastSetting(it) }
                        )
                    }
                }
            }

            SettingSection(title = "Account") {
                AccountCard(
                    email = uiState.email,
                    onSignOut = { viewModel.signOut() }
                )

                SignInCard(
                    onSignIn = { /* TODO: Implement sign-in */ }
                )

                PremiumCard(
                    isPremium = uiState.isPremium,
                    onManagePremium = { onNavigateToSubscription() }
                )

                SettingItem(
                    title = "Phone Authentication",
                    description = "Add phone number for additional security",
                    onClick = onNavigateToPhoneAuth
                )
            }
        }
    }

    // Error dialog
    if (uiState.error != null) {
        AlertDialog(
            onDismissRequest = { viewModel.clearError() },
            title = { Text("Error") },
            text = { Text(uiState.error) },
            confirmButton = {
                TextButton(onClick = { viewModel.clearError() }) {
                    Text("OK")
                }
            }
        )
    }
}

@Composable
private fun SettingsSection(
    title: String,
    content: @Composable () -> Unit
) {
    Column(
        modifier = Modifier.padding(vertical = 8.dp)
    ) {
        Text(
            text = title,
            style = MaterialTheme.typography.titleMedium,
            color = MaterialTheme.colorScheme.primary,
            modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp)
        )
        content()
    }
}

@Composable
private fun SettingsCard(
    content: @Composable () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp)
    ) {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            content()
        }
    }
}

@Composable
private fun AccountCard(
    email: String?,
    onSignOut: () -> Unit
) {
    SettingsCard {
        Column {
            Text(
                text = email ?: "",
                style = MaterialTheme.typography.bodyLarge
            )
            Spacer(modifier = Modifier.height(16.dp))
            Button(
                onClick = onSignOut,
                modifier = Modifier.fillMaxWidth()
            ) {
                Text(stringResource(R.string.sign_out))
            }
        }
    }
}

@Composable
private fun SignInCard(
    onSignIn: () -> Unit
) {
    SettingsCard {
        Button(
            onClick = onSignIn,
            modifier = Modifier.fillMaxWidth()
        ) {
            Icon(
                imageVector = Icons.Default.Google,
                contentDescription = null
            )
            Spacer(modifier = Modifier.width(8.dp))
            Text(stringResource(R.string.sign_in_with_google))
        }
    }
}

@Composable
private fun PremiumCard(
    isPremium: Boolean,
    onManagePremium: () -> Unit
) {
    SettingsCard {
        Column {
            Text(
                text = if (isPremium) "Premium Active" else "Free Plan",
                style = MaterialTheme.typography.titleMedium
            )
            Text(
                text = if (isPremium) {
                    "You have unlimited access to all features"
                } else {
                    "Upgrade to unlock unlimited transcriptions"
                },
                style = MaterialTheme.typography.bodyMedium
            )
            Spacer(modifier = Modifier.height(16.dp))
            Button(
                onClick = onManagePremium,
                modifier = Modifier.fillMaxWidth()
            ) {
                Text(
                    text = if (isPremium) {
                        "Manage Subscription"
                    } else {
                        "Upgrade to Premium"
                    }
                )
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun StorageLocationPreference(
    currentLocation: StorageLocation,
    onLocationSelected: (StorageLocation) -> Unit
) {
    var expanded by remember { mutableStateOf(false) }
    
    Column {
        Text(
            text = "Storage Location",
            style = MaterialTheme.typography.bodyLarge
        )
        Text(
            text = "Choose where to store your notes and recordings",
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        Spacer(modifier = Modifier.height(8.dp))
        ExposedDropdownMenuBox(
            expanded = expanded,
            onExpandedChange = { expanded = it }
        ) {
            OutlinedTextField(
                value = currentLocation.displayName,
                onValueChange = {},
                readOnly = true,
                trailingIcon = {
                    ExposedDropdownMenuDefaults.TrailingIcon(expanded = expanded)
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .menuAnchor()
            )
            ExposedDropdownMenu(
                expanded = expanded,
                onDismissRequest = { expanded = false }
            ) {
                StorageLocation.values().forEach { location ->
                    DropdownMenuItem(
                        text = { Text(location.displayName) },
                        onClick = {
                            onLocationSelected(location)
                            expanded = false
                        }
                    )
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun LanguagePreference(
    currentLanguage: TranscriptionLanguage,
    onLanguageSelected: (TranscriptionLanguage) -> Unit
) {
    var expanded by remember { mutableStateOf(false) }
    
    Column {
        Text(
            text = "Transcription Language",
            style = MaterialTheme.typography.bodyLarge
        )
        Text(
            text = "Select the primary language for transcription",
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        Spacer(modifier = Modifier.height(8.dp))
        ExposedDropdownMenuBox(
            expanded = expanded,
            onExpandedChange = { expanded = it }
        ) {
            OutlinedTextField(
                value = currentLanguage.displayName,
                onValueChange = {},
                readOnly = true,
                trailingIcon = {
                    ExposedDropdownMenuDefaults.TrailingIcon(expanded = expanded)
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .menuAnchor()
            )
            ExposedDropdownMenu(
                expanded = expanded,
                onDismissRequest = { expanded = false }
            ) {
                TranscriptionLanguage.values().forEach { language ->
                    DropdownMenuItem(
                        text = { Text(language.displayName) },
                        onClick = {
                            onLanguageSelected(language)
                            expanded = false
                        }
                    )
                }
            }
        }
    }
}

@Composable
private fun AboutCard(
    version: String,
    onPrivacyPolicy: () -> Unit,
    onTermsOfService: () -> Unit
) {
    SettingsCard {
        Column {
            Text(
                text = "Version $version",
                style = MaterialTheme.typography.bodyLarge
            )
            Spacer(modifier = Modifier.height(16.dp))
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceEvenly
            ) {
                TextButton(onClick = onPrivacyPolicy) {
                    Text("Privacy Policy")
                }
                TextButton(onClick = onTermsOfService) {
                    Text("Terms of Service")
                }
            }
        }
    }
}

@Composable
private fun ErrorMessage(
    message: String,
    onDismiss: () -> Unit
) {
    Snackbar(
        modifier = Modifier.padding(16.dp),
        action = {
            TextButton(onClick = onDismiss) {
                Text("Dismiss")
            }
        }
    ) {
        Text(message)
    }
}

@Composable
fun SettingsSwitch(
    title: String,
    description: String,
    checked: Boolean,
    onCheckedChange: (Boolean) -> Unit,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = title,
                style = MaterialTheme.typography.bodyLarge
            )
            Text(
                text = description,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
        Switch(
            checked = checked,
            onCheckedChange = onCheckedChange
        )
    }
}

@Composable
fun SettingsItem(
    title: String,
    description: String,
    icon: ImageVector,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Surface(
        onClick = onClick,
        modifier = modifier.fillMaxWidth()
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 8.dp),
            horizontalArrangement = Arrangement.spacedBy(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.primary
            )
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = title,
                    style = MaterialTheme.typography.bodyLarge
                )
                Text(
                    text = description,
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }
}

@Composable
private fun SettingItem(
    title: String,
    description: String? = null,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Surface(
        onClick = onClick,
        modifier = modifier.fillMaxWidth()
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
        ) {
            Text(
                text = title,
                style = MaterialTheme.typography.titleMedium
            )
            if (description != null) {
                Text(
                    text = description,
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }
}

@Composable
private fun SettingSection(
    title: String,
    content: @Composable () -> Unit
) {
    Column(
        modifier = Modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        Text(
            text = title,
            style = MaterialTheme.typography.titleSmall,
            color = MaterialTheme.colorScheme.primary,
            modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp)
        )
        content()
    }
} 