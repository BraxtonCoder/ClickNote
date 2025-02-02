package com.example.clicknote.ui.components

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.example.clicknote.domain.model.SummaryTemplate
import com.example.clicknote.domain.model.TemplateCategory
import java.util.UUID

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CustomTemplateDialog(
    onDismiss: () -> Unit,
    onTemplateCreated: (SummaryTemplate) -> Unit
) {
    var name by remember { mutableStateOf("") }
    var description by remember { mutableStateOf("") }
    var prompt by remember { mutableStateOf("") }
    
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Create Custom Template") },
        text = {
            Column(
                modifier = Modifier.fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                OutlinedTextField(
                    value = name,
                    onValueChange = { name = it },
                    label = { Text("Template Name") },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true
                )
                
                OutlinedTextField(
                    value = description,
                    onValueChange = { description = it },
                    label = { Text("Description") },
                    modifier = Modifier.fillMaxWidth(),
                    minLines = 2,
                    maxLines = 3
                )
                
                OutlinedTextField(
                    value = prompt,
                    onValueChange = { prompt = it },
                    label = { Text("Summary Prompt") },
                    modifier = Modifier.fillMaxWidth(),
                    minLines = 3,
                    maxLines = 5,
                    supportingText = {
                        Text("Enter the prompt that will be used to generate the summary. Use {text} as a placeholder for the transcribed text.")
                    }
                )
            }
        },
        confirmButton = {
            TextButton(
                onClick = {
                    if (name.isNotBlank() && description.isNotBlank() && prompt.isNotBlank()) {
                        onTemplateCreated(
                            SummaryTemplate(
                                id = UUID.randomUUID().toString(),
                                name = name,
                                description = description,
                                category = TemplateCategory.CUSTOM,
                                prompt = prompt,
                                isCustom = true
                            )
                        )
                        onDismiss()
                    }
                },
                enabled = name.isNotBlank() && description.isNotBlank() && prompt.isNotBlank()
            ) {
                Text("Create")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancel")
            }
        }
    )
} 