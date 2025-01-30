package com.example.clicknote.ui.components

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.selection.selectable
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun FeedbackDialog(
    onDismiss: () -> Unit,
    onSubmit: (FeedbackData) -> Unit
) {
    var feedbackType by remember { mutableStateOf<FeedbackType?>(null) }
    var rating by remember { mutableStateOf<Int?>(null) }
    var description by remember { mutableStateOf("") }
    var contactEmail by remember { mutableStateOf("") }
    
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Send Feedback") },
        text = {
            Column(
                modifier = Modifier.fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                // Feedback type selection
                Text(
                    text = "What kind of feedback do you have?",
                    style = MaterialTheme.typography.titleSmall
                )
                Column {
                    FeedbackType.values().forEach { type ->
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .selectable(
                                    selected = feedbackType == type,
                                    onClick = { feedbackType = type }
                                )
                                .padding(vertical = 8.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            RadioButton(
                                selected = feedbackType == type,
                                onClick = { feedbackType = type }
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Column {
                                Text(type.title)
                                Text(
                                    text = type.description,
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        }
                    }
                }
                
                Divider()
                
                // Rating
                Text(
                    text = "How would you rate your experience?",
                    style = MaterialTheme.typography.titleSmall
                )
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceEvenly
                ) {
                    repeat(5) { index ->
                        IconButton(
                            onClick = { rating = index + 1 }
                        ) {
                            Icon(
                                imageVector = if (rating != null && index < rating!!) {
                                    Icons.Default.Star
                                } else {
                                    Icons.Default.StarOutline
                                },
                                contentDescription = "Rate ${index + 1} stars",
                                tint = if (rating != null && index < rating!!) {
                                    MaterialTheme.colorScheme.primary
                                } else {
                                    MaterialTheme.colorScheme.outline
                                }
                            )
                        }
                    }
                }
                
                Divider()
                
                // Description
                OutlinedTextField(
                    value = description,
                    onValueChange = { description = it },
                    label = { Text("Tell us more (optional)") },
                    modifier = Modifier.fillMaxWidth(),
                    minLines = 3,
                    maxLines = 5
                )
                
                // Contact email
                OutlinedTextField(
                    value = contactEmail,
                    onValueChange = { contactEmail = it },
                    label = { Text("Email (optional)") },
                    placeholder = { Text("For follow-up questions") },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true
                )
            }
        },
        confirmButton = {
            TextButton(
                onClick = {
                    feedbackType?.let { type ->
                        rating?.let { stars ->
                            onSubmit(
                                FeedbackData(
                                    type = type,
                                    rating = stars,
                                    description = description.takeIf { it.isNotBlank() },
                                    contactEmail = contactEmail.takeIf { it.isNotBlank() }
                                )
                            )
                            onDismiss()
                        }
                    }
                },
                enabled = feedbackType != null && rating != null
            ) {
                Text("Submit")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancel")
            }
        }
    )
}

enum class FeedbackType(
    val title: String,
    val description: String
) {
    BUG(
        title = "Report a Bug",
        description = "Something isn't working correctly"
    ),
    FEATURE(
        title = "Feature Request",
        description = "Suggest a new feature or improvement"
    ),
    USABILITY(
        title = "Usability Feedback",
        description = "Share your experience using the app"
    ),
    PERFORMANCE(
        title = "Performance Issue",
        description = "Report slow performance or crashes"
    ),
    OTHER(
        title = "Other",
        description = "Any other feedback"
    )
}

data class FeedbackData(
    val type: FeedbackType,
    val rating: Int,
    val description: String? = null,
    val contactEmail: String? = null,
    val timestamp: Long = System.currentTimeMillis(),
    val appVersion: String? = null,
    val deviceInfo: Map<String, String> = emptyMap()
) 