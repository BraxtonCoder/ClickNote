package com.example.clicknote.ui.subscription

import androidx.compose.animation.*
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.AnimationSpec
import androidx.compose.animation.core.tween
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import com.example.clicknote.data.model.SubscriptionTier
import com.example.clicknote.ui.components.LoadingScreen
import com.stripe.android.model.PaymentMethod
import com.example.clicknote.viewmodel.SubscriptionViewModel
import com.example.clicknote.BuildConfig

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SubscriptionScreen(
    onNavigateBack: () -> Unit,
    viewModel: SubscriptionViewModel = hiltViewModel()
) {
    var selectedPlan by remember { mutableStateOf<SubscriptionPlan?>(null) }
    val isLoading by viewModel.isLoading.collectAsState()
    val error by viewModel.error.collectAsState()
    val showPaymentSheet by viewModel.showPaymentSheet.collectAsState()
    val clientSecret by viewModel.clientSecret.collectAsState()
    val subscriptionStatus by viewModel.subscriptionStatus.collectAsState()

    // Update tier comparisons
    val freeTier = SubscriptionTier.Free()
    val monthlyTier = SubscriptionTier.Monthly()
    val annualTier = SubscriptionTier.Annual()

    Scaffold(
        topBar = {
            SmallTopAppBar(
                title = { Text("Choose Your Plan") },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(
                            imageVector = Icons.Filled.ArrowBack,
                            contentDescription = "Back"
                        )
                    }
                },
                colors = TopAppBarDefaults.smallTopAppBarColors(
                    containerColor = MaterialTheme.colorScheme.primary,
                    titleContentColor = MaterialTheme.colorScheme.onPrimary,
                    navigationIconContentColor = MaterialTheme.colorScheme.onPrimary
                )
            )
        }
    ) { paddingValues ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .verticalScroll(rememberScrollState())
                    .padding(16.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                Text(
                    text = "Unlock Premium Features",
                    style = MaterialTheme.typography.headlineMedium,
                    fontWeight = FontWeight.Bold,
                    textAlign = TextAlign.Center
                )

                // Current Plan Status Card
                CurrentPlanStatusCard(subscriptionStatus = subscriptionStatus)

                Text(
                    text = "Choose the plan that works best for you",
                    style = MaterialTheme.typography.bodyLarge,
                    textAlign = TextAlign.Center,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )

                Spacer(modifier = Modifier.height(16.dp))

                // Free Plan Card
                SubscriptionPlanCard(
                    plan = SubscriptionPlan.Free,
                    isSelected = selectedPlan == SubscriptionPlan.Free,
                    isCurrentPlan = subscriptionStatus.tier == freeTier,
                    onSelect = { selectedPlan = SubscriptionPlan.Free }
                )

                // Monthly Plan Card
                SubscriptionPlanCard(
                    plan = SubscriptionPlan.Monthly,
                    isSelected = selectedPlan == SubscriptionPlan.Monthly,
                    isCurrentPlan = subscriptionStatus.tier == monthlyTier,
                    onSelect = { selectedPlan = SubscriptionPlan.Monthly }
                )

                // Annual Plan Card
                SubscriptionPlanCard(
                    plan = SubscriptionPlan.Annual,
                    isSelected = selectedPlan == SubscriptionPlan.Annual,
                    isCurrentPlan = subscriptionStatus.tier == annualTier,
                    onSelect = { selectedPlan = SubscriptionPlan.Annual }
                )

                Spacer(modifier = Modifier.height(16.dp))

                // Cancel Subscription Button (only show for paid plans)
                if (subscriptionStatus.tier != freeTier && subscriptionStatus.isActive) {
                    OutlinedButton(
                        onClick = { viewModel.cancelCurrentSubscription() },
                        modifier = Modifier.fillMaxWidth(),
                        colors = ButtonDefaults.outlinedButtonColors(
                            contentColor = MaterialTheme.colorScheme.error
                        )
                    ) {
                        Text("Cancel Current Subscription")
                    }
                }

                Spacer(modifier = Modifier.weight(1f))

                // Subscribe/Change Plan Button
                Button(
                    onClick = {
                        selectedPlan?.let { plan ->
                            viewModel.subscribeToPlan(plan)
                        }
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(56.dp),
                    enabled = selectedPlan != null && !isLoading && selectedPlan.toTier() != subscriptionStatus.tier,
                    colors = ButtonDefaults.buttonColors(
                        containerColor = MaterialTheme.colorScheme.primary
                    )
                ) {
                    if (isLoading) {
                        CircularProgressIndicator(
                            modifier = Modifier.size(24.dp),
                            color = MaterialTheme.colorScheme.onPrimary
                        )
                    } else {
                        Text(
                            text = when {
                                selectedPlan == null -> "Select a Plan"
                                selectedPlan.toTier() == subscriptionStatus.tier -> "Current Plan"
                                else -> when (selectedPlan) {
                                    SubscriptionPlan.Free -> "Switch to Free Plan"
                                    SubscriptionPlan.Monthly -> "Subscribe Monthly - £9.99"
                                    SubscriptionPlan.Annual -> "Subscribe Annually - £98"
                                }
                            }
                        )
                    }
                }
            }

            // Error Snackbar
            error?.let { errorMessage ->
                Snackbar(
                    modifier = Modifier
                        .align(Alignment.BottomCenter)
                        .padding(16.dp),
                    action = {
                        TextButton(onClick = { viewModel.dismissError() }) {
                            Text("Dismiss")
                        }
                    }
                ) {
                    Text(errorMessage)
                }
            }
        }
    }

    // Payment Sheet
    if (showPaymentSheet && clientSecret != null) {
        StripePaymentSheet(
            clientSecret = clientSecret!!,
            publishableKey = BuildConfig.STRIPE_PUBLISHABLE_KEY,
            onPaymentResult = { result ->
                viewModel.handlePaymentResult(result)
            }
        )
    }
}

@Composable
fun CurrentPlanStatusCard(
    subscriptionStatus: SubscriptionStatus,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant
        )
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Text(
                text = "Current Plan: ${subscriptionStatus.tier.displayName}",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold
            )

            if (subscriptionStatus.tier == freeTier) {
                Text(
                    text = "${subscriptionStatus.weeklyUsageCount} of ${freeTier.weeklyLimit} weekly transcriptions used",
                    style = MaterialTheme.typography.bodyMedium
                )
                
                LinearProgressIndicator(
                    progress = subscriptionStatus.weeklyUsageCount.toFloat() / freeTier.weeklyLimit,
                    modifier = Modifier.fillMaxWidth(),
                    color = MaterialTheme.colorScheme.primary,
                    trackColor = MaterialTheme.colorScheme.surfaceVariant
                )

                if (subscriptionStatus.weeklyResetDate != null) {
                    Text(
                        text = "Resets on ${subscriptionStatus.weeklyResetDate.format(java.time.format.DateTimeFormatter.ofPattern("MMM dd"))}",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            } else {
                if (subscriptionStatus.subscriptionEndDate != null) {
                    Text(
                        text = "Subscription ${if (subscriptionStatus.isActive) "renews" else "ends"} on ${
                            subscriptionStatus.subscriptionEndDate.format(java.time.format.DateTimeFormatter.ofPattern("MMM dd, yyyy"))
                        }",
                        style = MaterialTheme.typography.bodyMedium
                    )
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SubscriptionPlanCard(
    plan: SubscriptionPlan,
    isSelected: Boolean,
    isCurrentPlan: Boolean,
    onSelect: () -> Unit,
    modifier: Modifier = Modifier
) {
    Card(
        onClick = onSelect,
        modifier = modifier
            .fillMaxWidth()
            .height(120.dp),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(
            containerColor = when {
                isSelected -> MaterialTheme.colorScheme.primaryContainer
                isCurrentPlan -> MaterialTheme.colorScheme.surfaceVariant
                else -> MaterialTheme.colorScheme.surface
            }
        )
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp),
            verticalArrangement = Arrangement.SpaceBetween
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = plan.title,
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold
                )
                if (isCurrentPlan) {
                    Text(
                        text = "Current Plan",
                        style = MaterialTheme.typography.labelMedium,
                        color = MaterialTheme.colorScheme.primary
                    )
                } else {
                    Text(
                        text = plan.price,
                        style = MaterialTheme.typography.titleMedium,
                        color = MaterialTheme.colorScheme.primary
                    )
                }
            }

            Text(
                text = plan.description,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

// Helper function to convert SubscriptionPlan to SubscriptionTier
private fun SubscriptionPlan.toTier(): SubscriptionTier = when (this) {
    SubscriptionPlan.Free -> SubscriptionTier.Free()
    SubscriptionPlan.Monthly -> SubscriptionTier.Monthly()
    SubscriptionPlan.Annual -> SubscriptionTier.Annual()
}

sealed class SubscriptionPlan(
    val title: String,
    val price: String,
    val description: String
) {
    object Free : SubscriptionPlan(
        title = "Free Plan",
        price = "Free",
        description = "3 transcription notes per week"
    )
    
    object Monthly : SubscriptionPlan(
        title = "Monthly Plan",
        price = "£9.99/mo",
        description = "Unlimited transcriptions with all premium features"
    )
    
    object Annual : SubscriptionPlan(
        title = "Annual Plan",
        price = "£98/yr",
        description = "Save 18% with annual billing"
    )
}

@Composable
private fun CurrentPlanCard(
    tier: SubscriptionTier,
    usageCount: Int,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant
        )
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Text(
                text = "Current Plan: ${tier.displayName}",
                style = MaterialTheme.typography.titleMedium
            )
            if (tier == freeTier) {
                Text(
                    text = "Used $usageCount of ${freeTier.weeklyLimit} weekly transcriptions",
                    style = MaterialTheme.typography.bodyMedium
                )
                LinearProgressIndicator(
                    progress = usageCount.toFloat() / freeTier.weeklyLimit,
                    modifier = Modifier.fillMaxWidth()
                )
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun SubscriptionTierCard(
    tier: SubscriptionTier,
    isSelected: Boolean,
    isCurrentTier: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Card(
        onClick = onClick,
        modifier = modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = if (isSelected) 
                MaterialTheme.colorScheme.primaryContainer 
            else 
                MaterialTheme.colorScheme.surface
        )
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = tier.displayName,
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold
                )
                if (isCurrentTier) {
                    Text(
                        text = "Current Plan",
                        style = MaterialTheme.typography.labelMedium,
                        color = MaterialTheme.colorScheme.primary
                    )
                }
            }

            if (tier != freeTier) {
                Text(
                    text = "£${tier.monthlyPrice}/month",
                    style = MaterialTheme.typography.headlineSmall
                )
                if (tier == annualTier) {
                    Text(
                        text = "Billed annually at £${tier.yearlyPrice}",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }

            Divider(modifier = Modifier.padding(vertical = 8.dp))

            tier.features.forEach { feature ->
                Row(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector = Icons.Default.Check,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.size(16.dp)
                    )
                    Text(
                        text = feature,
                        style = MaterialTheme.typography.bodyMedium
                    )
                }
            }
        }
    }
}

@Composable
private fun PaymentSheet(
    tier: SubscriptionTier,
    onDismiss: () -> Unit,
    onPaymentComplete: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    // TODO: Implement Stripe payment sheet
    // This is a placeholder for the actual Stripe payment integration
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Complete Payment") },
        text = {
            Column {
                Text("Selected plan: ${tier.displayName}")
                Text(
                    text = if (tier == annualTier)
                        "Total: £${tier.yearlyPrice}"
                    else
                        "Total: £${tier.monthlyPrice}/month"
                )
            }
        },
        confirmButton = {
            Button(onClick = { onPaymentComplete("dummy_payment_method_id") }) {
                Text("Pay Now")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancel")
            }
        }
    )
} 