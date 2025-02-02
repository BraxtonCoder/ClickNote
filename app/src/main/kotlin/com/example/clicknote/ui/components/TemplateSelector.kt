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
import com.example.clicknote.domain.model.SummaryTemplate
import com.example.clicknote.domain.model.TemplateCategory
import com.example.clicknote.domain.model.defaultTemplates

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TemplateSelector(
    selectedTemplate: SummaryTemplate?,
    onTemplateSelected: (SummaryTemplate) -> Unit,
    onCreateCustomTemplate: () -> Unit,
    modifier: Modifier = Modifier
) {
    var showDialog by remember { mutableStateOf(false) }
    var selectedCategory by remember { mutableStateOf<TemplateCategory?>(null) }
    var searchQuery by remember { mutableStateOf("") }
    
    // Template selection button
    OutlinedButton(
        onClick = { showDialog = true },
        modifier = modifier
    ) {
        Icon(
            imageVector = Icons.Default.AutoAwesome,
            contentDescription = null,
            modifier = Modifier.size(20.dp)
        )
        Spacer(modifier = Modifier.width(8.dp))
        Text(selectedTemplate?.name ?: "Choose Template")
    }
    
    // Template selection dialog
    if (showDialog) {
        AlertDialog(
            onDismissRequest = { showDialog = false },
            title = { Text("Choose Summary Template") },
            text = {
                Column {
                    // Search bar
                    OutlinedTextField(
                        value = searchQuery,
                        onValueChange = { searchQuery = it },
                        placeholder = { Text("Search templates") },
                        leadingIcon = {
                            Icon(Icons.Default.Search, contentDescription = null)
                        },
                        trailingIcon = if (searchQuery.isNotEmpty()) {
                            {
                                IconButton(onClick = { searchQuery = "" }) {
                                    Icon(Icons.Default.Clear, contentDescription = "Clear search")
                                }
                            }
                        } else null,
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(bottom = 16.dp),
                        singleLine = true
                    )
                    
                    // Category chips
                    SingleChoiceSegmentedButtonRow(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(bottom = 16.dp)
                    ) {
                        TemplateCategory.values().forEach { category ->
                            SegmentedButton(
                                selected = selectedCategory == category,
                                onClick = {
                                    selectedCategory = if (selectedCategory == category) null else category
                                },
                                shape = SegmentedButtonDefaults.shape(
                                    index = category.ordinal,
                                    count = TemplateCategory.values().size
                                )
                            ) {
                                Text(
                                    text = category.name.lowercase().capitalize(),
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis
                                )
                            }
                        }
                    }
                    
                    // Template list
                    LazyColumn(
                        modifier = Modifier.weight(1f),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        // Filter templates based on search and category
                        val filteredTemplates = defaultTemplates.filter { template ->
                            val matchesSearch = searchQuery.isEmpty() || 
                                template.name.contains(searchQuery, ignoreCase = true) ||
                                template.description.contains(searchQuery, ignoreCase = true)
                            val matchesCategory = selectedCategory == null || 
                                template.category == selectedCategory
                            matchesSearch && matchesCategory
                        }
                        
                        items(filteredTemplates) { template ->
                            TemplateItem(
                                template = template,
                                isSelected = template == selectedTemplate,
                                onClick = {
                                    onTemplateSelected(template)
                                    showDialog = false
                                }
                            )
                        }
                        
                        // Custom template option
                        item {
                            OutlinedButton(
                                onClick = {
                                    onCreateCustomTemplate()
                                    showDialog = false
                                },
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Icon(Icons.Default.Add, contentDescription = null)
                                Spacer(modifier = Modifier.width(8.dp))
                                Text("Create Custom Template")
                            }
                        }
                    }
                }
            },
            confirmButton = {
                TextButton(onClick = { showDialog = false }) {
                    Text("Close")
                }
            }
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun TemplateItem(
    template: SummaryTemplate,
    isSelected: Boolean,
    onClick: () -> Unit
) {
    Card(
        onClick = onClick,
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = if (isSelected) {
                MaterialTheme.colorScheme.primaryContainer
            } else {
                MaterialTheme.colorScheme.surface
            }
        )
    ) {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            Text(
                text = template.name,
                style = MaterialTheme.typography.titleMedium
            )
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = template.description,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Spacer(modifier = Modifier.height(8.dp))
            Row(
                verticalAlignment = Alignment.CenterVertically
            ) {
                AssistChip(
                    onClick = { },
                    label = { Text(template.category.name.lowercase().capitalize()) },
                    leadingIcon = {
                        Icon(
                            imageVector = when (template.category) {
                                TemplateCategory.GENERAL -> Icons.Default.Description
                                TemplateCategory.BUSINESS -> Icons.Default.Business
                                TemplateCategory.ACADEMIC -> Icons.Default.School
                                TemplateCategory.TECHNICAL -> Icons.Default.Code
                                TemplateCategory.CREATIVE -> Icons.Default.Brush
                                TemplateCategory.MEDICAL -> Icons.Default.LocalHospital
                                TemplateCategory.LEGAL -> Icons.Default.Gavel
                                TemplateCategory.CUSTOM -> Icons.Default.Edit
                            },
                            contentDescription = null,
                            modifier = Modifier.size(16.dp)
                        )
                    }
                )
                if (template.isCustom) {
                    Spacer(modifier = Modifier.width(8.dp))
                    AssistChip(
                        onClick = { },
                        label = { Text("Custom") },
                        leadingIcon = {
                            Icon(
                                imageVector = Icons.Default.Edit,
                                contentDescription = null,
                                modifier = Modifier.size(16.dp)
                            )
                        }
                    )
                }
            }
        }
    }
} 