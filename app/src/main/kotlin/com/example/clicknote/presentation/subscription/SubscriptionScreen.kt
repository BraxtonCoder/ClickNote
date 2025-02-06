package com.example.clicknote.presentation.subscription

import androidx.compose.animation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.example.clicknote.domain.model.SubscriptionPlan

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SubscriptionScreen(
    onNavigateBack: () -> Unit,
    viewModel: SubscriptionViewModel = hiltViewModel()
) {
    val state by viewModel.state.collectAsState()

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Choose Your Plan") },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Back")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.primaryContainer,
                    titleContentColor = MaterialTheme.colorScheme.onPrimaryContainer
                )
            )
        }
    ) { paddingValues ->
        if (state.isLoading) {
            Box(
                modifier = Modifier.fillMaxSize(),
                contentAlignment = Alignment.Center
            ) {
                CircularProgressIndicator()
            }
        } else {
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues)
                    .padding(horizontal = 16.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                item {
                    Text(
                        text = "Unlock Premium Features",
                        style = MaterialTheme.typography.headlineSmall,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.padding(top = 16.dp)
                    )
                }

                item {
                    PlanFeatures()
                }

                item {
                    Spacer(modifier = Modifier.height(8.dp))
                }

                item {
                    SubscriptionPlanCard(
                        title = "Free Plan",
                        price = "£0",
                        period = "",
                        features = listOf(
                            "3 transcription notes per week",
                            "Basic transcription quality",
                            "Local storage only",
                            "Standard support"
                        ),
                        isCurrentPlan = !state.isSubscribed,
                        onClick = { /* Free plan, no action needed */ }
                    )
                }

                item {
                    SubscriptionPlanCard(
                        title = "Monthly Premium",
                        price = "£9.99",
                        period = "/month",
                        features = listOf(
                            "Unlimited transcription notes",
                            "High-quality transcription",
                            "Cloud storage & sync",
                            "Priority support",
                            "AI-powered summaries",
                            "Multiple speaker detection",
                            "Call recording transcription"
                        ),
                        isCurrentPlan = state.currentPlan == SubscriptionPlan.Monthly(),
                        onClick = { viewModel.subscribe(SubscriptionPlan.Monthly()) }
                    )
                }

                item {
                    SubscriptionPlanCard(
                        title = "Annual Premium",
                        price = "£98",
                        period = "/year",
                        features = listOf(
                            "All Monthly Premium features",
                            "Save £21.88 per year",
                            "Extended cloud storage",
                            "Premium support",
                            "Early access to new features"
                        ),
                        isCurrentPlan = state.currentPlan == SubscriptionPlan.Annual(),
                        onClick = { viewModel.subscribe(SubscriptionPlan.Annual()) }
                    )
                }

                item {
                    Spacer(modifier = Modifier.height(16.dp))
                }

                state.error?.let { errorMessage ->
                    item {
                        Text(
                            text = errorMessage,
                            color = MaterialTheme.colorScheme.error,
                            style = MaterialTheme.typography.bodyMedium,
                            textAlign = TextAlign.Center,
                            modifier = Modifier.fillMaxWidth()
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun PlanFeatures() {
    Column(
        modifier = Modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        FeatureItem(
            icon = Icons.Default.RecordVoiceOver,
            title = "Unlimited Voice Notes",
            description = "Record and transcribe as many notes as you need"
        )
        FeatureItem(
            icon = Icons.Default.CloudSync,
            title = "Cloud Sync",
            description = "Access your notes across all your devices"
        )
        FeatureItem(
            icon = Icons.Default.Psychology,
            title = "AI-Powered Features",
            description = "Smart summaries and speaker detection"
        )
        FeatureItem(
            icon = Icons.Default.PhoneInTalk,
            title = "Call Recording",
            description = "Transcribe your phone calls automatically"
        )
    }
}

@Composable
private fun FeatureItem(
    icon: ImageVector,
    title: String,
    description: String
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(16.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(
            imageVector = icon,
            contentDescription = null,
            tint = MaterialTheme.colorScheme.primary
        )
        Column {
            Text(
                text = title,
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Medium
            )
            Text(
                text = description,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

@Composable
private fun SubscriptionPlanCard(
    title: String,
    price: String,
    period: String,
    features: List<String>,
    isCurrentPlan: Boolean,
    onClick: () -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = if (isCurrentPlan) {
                MaterialTheme.colorScheme.primaryContainer
            } else {
                MaterialTheme.colorScheme.surface
            }
        )
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Text(
                text = title,
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold
            )
            
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(
                    text = price,
                    style = MaterialTheme.typography.headlineMedium,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    text = period,
                    style = MaterialTheme.typography.titleMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            features.forEach { feature ->
                Row(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector = Icons.Default.Check,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.primary
                    )
                    Text(text = feature)
                }
            }

            if (!isCurrentPlan) {
                Button(
                    onClick = onClick,
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 8.dp)
                ) {
                    Text("Select Plan")
                }
            }
        }
    }
} 