package com.example.clicknote.ui.components.note

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.*
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.unit.dp

@Composable
fun NoteContent(
    content: String,
    searchQuery: String,
    isEditing: Boolean,
    onContentChange: (String) -> Unit,
    onEditClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Box(modifier = modifier) {
        if (isEditing) {
            BasicTextField(
                value = content,
                onValueChange = onContentChange,
                textStyle = MaterialTheme.typography.bodyLarge.copy(
                    color = MaterialTheme.colorScheme.onSurface
                ),
                modifier = Modifier
                    .fillMaxSize()
                    .verticalScroll(rememberScrollState())
            )
        } else {
            Text(
                text = buildAnnotatedString {
                    if (searchQuery.isBlank()) {
                        append(content)
                    } else {
                        var lastIndex = 0
                        val pattern = searchQuery.lowercase()
                        val contentLower = content.lowercase()
                        var index = contentLower.indexOf(pattern)
                        
                        while (index != -1) {
                            // Add text before match
                            append(content.substring(lastIndex, index))
                            
                            // Add highlighted match
                            withStyle(
                                SpanStyle(
                                    background = MaterialTheme.colorScheme.primary.copy(alpha = 0.3f),
                                    textDecoration = TextDecoration.Underline
                                )
                            ) {
                                append(content.substring(index, index + pattern.length))
                            }
                            
                            lastIndex = index + pattern.length
                            index = contentLower.indexOf(pattern, lastIndex)
                        }
                        
                        // Add remaining text
                        if (lastIndex < content.length) {
                            append(content.substring(lastIndex))
                        }
                    }
                },
                style = MaterialTheme.typography.bodyLarge,
                modifier = Modifier
                    .fillMaxSize()
                    .verticalScroll(rememberScrollState())
            )
        }

        FloatingActionButton(
            onClick = onEditClick,
            modifier = Modifier
                .align(Alignment.BottomEnd)
                .padding(16.dp),
            containerColor = MaterialTheme.colorScheme.secondaryContainer
        ) {
            Icon(
                imageVector = Icons.Default.Edit,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.onSecondaryContainer
            )
        }
    }
} 