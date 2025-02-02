package com.example.clicknote.ui.components

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import com.example.clicknote.service.PremiumFeature

@Composable
fun UpgradePrompt(
    feature: PremiumFeature,
    remainingCount: Int? = null,
    onUpgrade: () -> Unit,
    onDismiss: () -> Unit
) {
    Dialog(onDismissRequest = onDismiss) {
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            shape = MaterialTheme.shapes.large
        ) {
            Column(
                modifier = Modifier
                    .padding(24.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                Text(
                    text = when (feature) {
                        PremiumFeature.TRANSCRIPTION -> {
                            if (remainingCount == 0) {
                                "Weekly Limit Reached"
                            } else {
                                "Upgrade to Premium"
                            }
                        }
                        PremiumFeature.CLOUD_SYNC -> "Enable Cloud Sync"
                        PremiumFeature.CALL_RECORDING -> "Unlock Call Recording"
                        PremiumFeature.MULTI_SPEAKER -> "Enable Speaker Detection"
                        PremiumFeature.AI_SUMMARY -> "Unlock AI Summaries"
                        PremiumFeature.AUDIO_ENHANCEMENT -> "Enable Audio Enhancement"
                        PremiumFeature.OFFLINE_MODE -> "Enable Offline Mode"
                    },
                    style = MaterialTheme.typography.titleLarge
                )

                Text(
                    text = when (feature) {
                        PremiumFeature.TRANSCRIPTION -> {
                            if (remainingCount == 0) {
                                "You've used all your free transcriptions for this week. Upgrade to Premium for unlimited transcriptions."
                            } else {
                                "You have $remainingCount free transcriptions left this week. Get unlimited transcriptions with Premium."
                            }
                        }
                        PremiumFeature.CLOUD_SYNC -> 
                            "Keep your notes synced across all your devices with cloud backup."
                        PremiumFeature.CALL_RECORDING -> 
                            "Record and transcribe your calls automatically."
                        PremiumFeature.MULTI_SPEAKER -> 
                            "Automatically detect and label different speakers in your recordings."
                        PremiumFeature.AI_SUMMARY -> 
                            "Get AI-powered summaries of your transcriptions."
                        PremiumFeature.AUDIO_ENHANCEMENT -> 
                            "Enhance your audio quality for better transcription accuracy."
                        PremiumFeature.OFFLINE_MODE -> 
                            "Use transcription features without an internet connection."
                    },
                    style = MaterialTheme.typography.bodyLarge,
                    textAlign = TextAlign.Center,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    TextButton(
                        onClick = onDismiss,
                        modifier = Modifier.weight(1f)
                    ) {
                        Text("Not Now")
                    }
                    
                    Button(
                        onClick = onUpgrade,
                        modifier = Modifier.weight(1f)
                    ) {
                        Text("Upgrade")
                    }
                }
            }
        }
    }
} 