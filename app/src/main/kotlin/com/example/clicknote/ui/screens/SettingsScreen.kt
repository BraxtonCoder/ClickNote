package com.example.clicknote.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.example.clicknote.ui.viewmodel.SettingsViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsScreen(
    onNavigateBack: () -> Unit,
    viewModel: SettingsViewModel = hiltViewModel()
) {
    val settings by viewModel.settings.collectAsState()
    var showStorageDialog by remember { mutableStateOf(false) }
    var showLanguageDialog by remember { mutableStateOf(false) }

    if (showStorageDialog) {
        StorageLocationDialog(
            currentLocation = settings.storageLocation,
            onLocationSelected = { viewModel.updateStorageLocation(it) },
            onDismiss = { showStorageDialog = false }
        )
    }

    if (showLanguageDialog) {
        LanguageSelectionDialog(
            currentLanguage = settings.language,
            onLanguageSelected = { viewModel.updateLanguage(it) },
            onDismiss = { showLanguageDialog = false }
        )
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
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
        ) {
            item {
                SettingsSection(title = "Recording") {
                    SettingsSwitch(
                        title = "Save Audio Files",
                        subtitle = "Keep audio recordings alongside transcriptions",
                        checked = settings.saveAudio,
                        onCheckedChange = { viewModel.updateSaveAudio(it) }
                    )
                    
                    SettingsSwitch(
                        title = "Auto-Delete Audio",
                        subtitle = "Delete audio files when note is deleted",
                        checked = settings.autoDeleteAudio,
                        onCheckedChange = { viewModel.updateAutoDeleteAudio(it) }
                    )
                }
            }

            item {
                SettingsSection(title = "Storage") {
                    SettingsItem(
                        title = "Storage Location",
                        subtitle = settings.storageLocation.displayName,
                        icon = Icons.Default.Storage,
                        onClick = { showStorageDialog = true }
                    )
                    
                    SettingsSwitch(
                        title = "Cloud Sync",
                        subtitle = "Sync notes across devices",
                        checked = settings.cloudSyncEnabled,
                        onCheckedChange = { viewModel.updateCloudSync(it) }
                    )
                }
            }

            item {
                SettingsSection(title = "Notifications") {
                    SettingsSwitch(
                        title = "Silent Notifications",
                        subtitle = "Show notifications for new recordings",
                        checked = settings.showNotifications,
                        onCheckedChange = { viewModel.updateShowNotifications(it) }
                    )
                }
            }

            item {
                SettingsSection(title = "Language") {
                    SettingsItem(
                        title = "App Language",
                        subtitle = settings.language,
                        icon = Icons.Default.Language,
                        onClick = { showLanguageDialog = true }
                    )
                }
            }

            item {
                SettingsSection(title = "Subscription") {
                    if (settings.isPremium) {
                        ListItem(
                            headlineContent = { Text("Premium Active") },
                            supportingContent = { Text("Unlimited transcriptions") },
                            leadingContent = {
                                Icon(
                                    Icons.Default.Star,
                                    contentDescription = null,
                                    tint = MaterialTheme.colorScheme.primary
                                )
                            }
                        )
                    } else {
                        SettingsItem(
                            title = "Upgrade to Premium",
                            subtitle = "Unlimited transcriptions and more",
                            icon = Icons.Default.StarBorder,
                            onClick = { viewModel.navigateToSubscription() }
                        )
                    }
                }
            }

            item {
                SettingsSection(title = "Account") {
                    settings.userEmail?.let { email ->
                        ListItem(
                            headlineContent = { Text(email) },
                            supportingContent = { Text("Signed in with Google") },
                            leadingContent = {
                                Icon(Icons.Default.AccountCircle, contentDescription = null)
                            },
                            trailingContent = {
                                TextButton(onClick = { viewModel.signOut() }) {
                                    Text("Sign Out")
                                }
                            }
                        )
                    }
                }
            }

            item {
                Spacer(modifier = Modifier.height(32.dp))
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    horizontalArrangement = Arrangement.Center
                ) {
                    Text(
                        text = "Version ${settings.appVersion}",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        }
    }
}

@Composable
private fun SettingsSection(
    title: String,
    content: @Composable () -> Unit
) {
    Column {
        Text(
            text = title,
            style = MaterialTheme.typography.titleSmall,
            color = MaterialTheme.colorScheme.primary,
            modifier = Modifier.padding(start = 16.dp, top = 16.dp, bottom = 8.dp)
        )
        content()
        Divider(modifier = Modifier.padding(vertical = 8.dp))
    }
}

@Composable
private fun SettingsItem(
    title: String,
    subtitle: String,
    icon: ImageVector,
    onClick: () -> Unit
) {
    ListItem(
        headlineContent = { Text(title) },
        supportingContent = { Text(subtitle) },
        leadingContent = { Icon(icon, contentDescription = null) },
        modifier = Modifier.clickable(onClick = onClick)
    )
}

@Composable
private fun SettingsSwitch(
    title: String,
    subtitle: String,
    checked: Boolean,
    onCheckedChange: (Boolean) -> Unit
) {
    ListItem(
        headlineContent = { Text(title) },
        supportingContent = { Text(subtitle) },
        trailingContent = {
            Switch(
                checked = checked,
                onCheckedChange = onCheckedChange
            )
        }
    )
} 