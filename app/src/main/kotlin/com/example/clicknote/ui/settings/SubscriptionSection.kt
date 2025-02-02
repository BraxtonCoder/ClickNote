package com.example.clicknote.ui.settings

import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.example.clicknote.domain.model.SubscriptionPlan

data class PlanDetails(
    val name: String,
    val price: String,
    val features: List<String>
)

@Composable
fun SubscriptionSection(
    currentPlan: SubscriptionPlan,
    remainingFreeNotes: Int,
    onUpgradePlan: (SubscriptionPlan) -> Unit,
    onManagePlan: () -> Unit,
    modifier: Modifier = Modifier
) {
    val plans = mapOf(
        SubscriptionPlan.FREE to PlanDetails(
            name = "Free Plan",
            price = "Free",
            features = listOf(
                "3 transcription notes per week",
                "Basic features",
                "Local storage only"
            )
        ),
        SubscriptionPlan.MONTHLY to PlanDetails(
            name = "Monthly Plan",
            price = "£9.99/month",
            features = listOf(
                "Unlimited transcriptions",
                "Cloud sync",
                "Advanced AI features",
                "Priority support"
            )
        ),
        SubscriptionPlan.ANNUAL to PlanDetails(
            name = "Annual Plan",
            price = "£98/year",
            features = listOf(
                "All Monthly Plan features",
                "Save 18%",
                "Extended cloud storage"
            )
        )
    )

    Card(
        modifier = modifier.fillMaxWidth()
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Text(
                text = "Subscription",
                style = MaterialTheme.typography.titleMedium
            )

            // Current plan info
            if (currentPlan == SubscriptionPlan.FREE) {
                ElevatedCard(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.elevatedCardColors(
                        containerColor = MaterialTheme.colorScheme.secondaryContainer
                    )
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text(
                            text = "$remainingFreeNotes notes remaining this week",
                            style = MaterialTheme.typography.bodyLarge
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = "Upgrade to unlock unlimited transcriptions",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSecondaryContainer
                        )
                    }
                }
            } else {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Text(
                            text = plans[currentPlan]?.name ?: "",
                            style = MaterialTheme.typography.bodyLarge
                        )
                        Text(
                            text = plans[currentPlan]?.price ?: "",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                    TextButton(onClick = onManagePlan) {
                        Text("Manage")
                    }
                }
            }

            Divider()

            // Available plans
            Text(
                text = "Available Plans",
                style = MaterialTheme.typography.titleSmall
            )

            plans.forEach { (plan, details) ->
                if (plan != currentPlan) {
                    OutlinedCard(
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(16.dp)
                        ) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    text = details.name,
                                    style = MaterialTheme.typography.titleSmall
                                )
                                Text(
                                    text = details.price,
                                    style = MaterialTheme.typography.bodyLarge,
                                    color = MaterialTheme.colorScheme.primary
                                )
                            }
                            Spacer(modifier = Modifier.height(8.dp))
                            details.features.forEach { feature ->
                                Row(
                                    modifier = Modifier.padding(vertical = 4.dp),
                                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Icon(
                                        Icons.Default.Check,
                                        contentDescription = null,
                                        modifier = Modifier.size(16.dp),
                                        tint = MaterialTheme.colorScheme.primary
                                    )
                                    Text(
                                        text = feature,
                                        style = MaterialTheme.typography.bodyMedium
                                    )
                                }
                            }
                            Spacer(modifier = Modifier.height(16.dp))
                            Button(
                                onClick = { onUpgradePlan(plan) },
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Text(
                                    text = if (currentPlan == SubscriptionPlan.FREE) "Upgrade" else "Switch Plan"
                                )
                            }
                        }
                    }
                    Spacer(modifier = Modifier.height(8.dp))
                }
            }
        }
    }
} 