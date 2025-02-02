package com.example.clicknote.ui.components

import androidx.compose.animation.*
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp

data class SpeakerSegment(
    val speakerId: String,
    val speakerName: String,
    val startTime: String,
    val endTime: String,
    val text: String
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MultiSpeakerUI(
    segments: List<SpeakerSegment>,
    onSpeakerNameChange: (String, String) -> Unit,
    onManageProfiles: () -> Unit,
    modifier: Modifier = Modifier
) {
    var expandedSegmentId by remember { mutableStateOf<String?>(null) }

    Column(
        modifier = modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // Header with Manage Profiles Button
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                "Speakers Timeline",
                style = MaterialTheme.typography.titleMedium
            )
            TextButton(
                onClick = onManageProfiles,
                colors = ButtonDefaults.textButtonColors(
                    contentColor = MaterialTheme.colorScheme.primary
                )
            ) {
                Icon(Icons.Default.Settings, contentDescription = null)
                Spacer(modifier = Modifier.width(4.dp))
                Text("Manage Profiles")
            }
        }

        // Speaker Segments Timeline
        LazyColumn(
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            items(segments) { segment ->
                val isExpanded = expandedSegmentId == segment.speakerId

                Card(
                    modifier = Modifier.fillMaxWidth(),
                    onClick = { 
                        expandedSegmentId = if (isExpanded) null else segment.speakerId 
                    }
                ) {
                    Column(
                        modifier = Modifier.padding(16.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        // Speaker Info Row
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Row(
                                horizontalArrangement = Arrangement.spacedBy(8.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Icon(
                                    Icons.Default.Person,
                                    contentDescription = null,
                                    tint = MaterialTheme.colorScheme.primary
                                )
                                Text(
                                    segment.speakerName,
                                    style = MaterialTheme.typography.titleSmall
                                )
                            }
                            Text(
                                "${segment.startTime} - ${segment.endTime}",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }

                        // Transcribed Text
                        Text(
                            text = segment.text,
                            maxLines = if (isExpanded) Int.MAX_VALUE else 2,
                            overflow = TextOverflow.Ellipsis,
                            style = MaterialTheme.typography.bodyMedium
                        )

                        // Edit Name Button (shown when expanded)
                        AnimatedVisibility(visible = isExpanded) {
                            OutlinedButton(
                                onClick = {
                                    // Show dialog to edit speaker name
                                    onSpeakerNameChange(segment.speakerId, segment.speakerName)
                                },
                                modifier = Modifier.align(Alignment.End)
                            ) {
                                Icon(Icons.Default.Edit, contentDescription = null)
                                Spacer(modifier = Modifier.width(4.dp))
                                Text("Edit Name")
                            }
                        }
                    }
                }
            }
        }
    }
} 