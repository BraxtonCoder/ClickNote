package com.example.clicknote.ui.components

import androidx.compose.animation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp

data class SummaryPoint(
    val text: String,
    val category: String? = null,
    val confidence: Float = 1.0f
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun NoteSummarySection(
    summary: String,
    keyPoints: List<SummaryPoint>,
    isLoading: Boolean,
    onRegenerateSummary: () -> Unit,
    onCopySummary: () -> Unit,
    modifier: Modifier = Modifier
) {
    var showFullSummary by remember { mutableStateOf(false) }
    var showKeyPoints by remember { mutableStateOf(true) }

    Column(
        modifier = modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // Summary Actions
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                "Summary",
                style = MaterialTheme.typography.titleMedium
            )
            Row(
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                IconButton(onClick = onRegenerateSummary) {
                    Icon(Icons.Default.Refresh, contentDescription = "Regenerate")
                }
                IconButton(onClick = onCopySummary) {
                    Icon(Icons.Default.ContentCopy, contentDescription = "Copy")
                }
            }
        }

        // Main Summary
        if (isLoading) {
            LinearProgressIndicator(modifier = Modifier.fillMaxWidth())
        } else {
            Card(
                modifier = Modifier.fillMaxWidth(),
                onClick = { showFullSummary = !showFullSummary }
            ) {
                Column(
                    modifier = Modifier.padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Text(
                        text = summary,
                        maxLines = if (showFullSummary) Int.MAX_VALUE else 3,
                        overflow = TextOverflow.Ellipsis,
                        style = MaterialTheme.typography.bodyMedium
                    )
                    if (!showFullSummary && summary.length > 150) {
                        TextButton(
                            onClick = { showFullSummary = true },
                            modifier = Modifier.align(Alignment.End)
                        ) {
                            Text("Show More")
                        }
                    }
                }
            }
        }

        // Key Points Section
        Column {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    "Key Points",
                    style = MaterialTheme.typography.titleMedium
                )
                IconButton(onClick = { showKeyPoints = !showKeyPoints }) {
                    Icon(
                        if (showKeyPoints) Icons.Default.ExpandLess else Icons.Default.ExpandMore,
                        contentDescription = if (showKeyPoints) "Hide" else "Show"
                    )
                }
            }

            AnimatedVisibility(
                visible = showKeyPoints,
                enter = expandVertically() + fadeIn(),
                exit = shrinkVertically() + fadeOut()
            ) {
                LazyColumn(
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    items(keyPoints) { point ->
                        ListItem(
                            headlineContent = { Text(point.text) },
                            supportingContent = point.category?.let { { Text(it) } },
                            leadingContent = {
                                Icon(
                                    Icons.Default.Circle,
                                    contentDescription = null,
                                    modifier = Modifier.size(8.dp)
                                )
                            }
                        )
                    }
                }
            }
        }
    }
} 