package com.example.clicknote.presentation.settings

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import com.example.clicknote.navigation.Screen

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsScreen(
    navController: NavController,
    viewModel: SettingsViewModel = hiltViewModel()
) {
    val state by viewModel.state.collectAsState()

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Settings") },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.primaryContainer,
                    titleContentColor = MaterialTheme.colorScheme.onPrimaryContainer
                )
            )
        }
    ) { paddingValues ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {
            item {
                SubscriptionSection(
                    isSubscribed = state.isSubscribed,
                    currentPlan = state.currentPlan?.name ?: "Free Plan",
                    remainingFreeNotes = state.remainingFreeNotes,
                    onManageSubscriptionClick = {
                        navController.navigate(Screen.Premium.route)
                    }
                )
            }
            // Add other settings sections here
        }
    }
}

@Composable
private fun SubscriptionSection(
    isSubscribed: Boolean,
    currentPlan: String,
    remainingFreeNotes: Int,
    onManageSubscriptionClick: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(16.dp)
            .clickable(onClick = onManageSubscriptionClick)
    ) {
        Text(
            text = "Subscription",
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Bold
        )
        Spacer(modifier = Modifier.height(8.dp))
        Text(
            text = "Current Plan: $currentPlan",
            style = MaterialTheme.typography.bodyMedium
        )
        if (!isSubscribed) {
            Text(
                text = "Remaining Free Notes: $remainingFreeNotes",
                style = MaterialTheme.typography.bodyMedium
            )
        }
    }
} 
