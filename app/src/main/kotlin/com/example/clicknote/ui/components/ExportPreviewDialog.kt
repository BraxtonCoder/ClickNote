package com.example.clicknote.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.clicknote.R
import com.example.clicknote.domain.model.ExportSettings
import com.example.clicknote.domain.model.Note
import com.example.clicknote.domain.model.TranscriptionSegment
import java.text.SimpleDateFormat
import java.util.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ExportPreviewDialog(
    notes: List<Pair<Note, List<TranscriptionSegment>>>,
    settings: ExportSettings,
    onSettingsChange: (ExportSettings) -> Unit,
    onConfirm: () -> Unit,
    onDismiss: () -> Unit
) {
    val dateFormat = SimpleDateFormat("yyyy-MM-dd HH:mm", Locale.getDefault())
    
    AlertDialog(
        onDismissRequest = onDismiss,
        modifier = Modifier.fillMaxHeight(0.8f)
    ) {
        Surface(
            modifier = Modifier
                .fillMaxWidth()
                .wrapContentHeight(),
            shape = MaterialTheme.shapes.large,
            tonalElevation = 2.dp
        ) {
            Column(modifier = Modifier.fillMaxWidth()) {
                // Title bar
                SmallTopAppBar(
                    title = { Text(stringResource(R.string.export_preview)) },
                    actions = {
                        IconButton(onClick = onConfirm) {
                            Icon(Icons.Default.Check, contentDescription = null)
                        }
                    }
                )
                
                // Settings section
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    // Font size
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(Icons.Default.FormatSize, contentDescription = null)
                        Spacer(Modifier.width(8.dp))
                        Text(stringResource(R.string.font_size))
                        Spacer(Modifier.weight(1f))
                        SegmentedButtons(
                            items = ExportSettings.FontSize.values().toList(),
                            selectedItem = settings.fontSize,
                            onItemSelect = { onSettingsChange(settings.copy(fontSize = it)) }
                        ) { item ->
                            when (item) {
                                ExportSettings.FontSize.SMALL -> "S"
                                ExportSettings.FontSize.MEDIUM -> "M"
                                ExportSettings.FontSize.LARGE -> "L"
                            }
                        }
                    }
                    
                    // Spacing
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(Icons.Default.Height, contentDescription = null)
                        Spacer(Modifier.width(8.dp))
                        Text(stringResource(R.string.spacing))
                        Spacer(Modifier.weight(1f))
                        SegmentedButtons(
                            items = ExportSettings.Spacing.values().toList(),
                            selectedItem = settings.spacing,
                            onItemSelect = { onSettingsChange(settings.copy(spacing = it)) }
                        ) { item ->
                            when (item) {
                                ExportSettings.Spacing.COMPACT -> stringResource(R.string.spacing_compact)
                                ExportSettings.Spacing.NORMAL -> stringResource(R.string.spacing_normal)
                                ExportSettings.Spacing.RELAXED -> stringResource(R.string.spacing_relaxed)
                            }
                        }
                    }
                    
                    // Include options
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        FilterChip(
                            selected = settings.includeTitle,
                            onClick = { onSettingsChange(settings.copy(includeTitle = !settings.includeTitle)) },
                            label = { Text(stringResource(R.string.include_title)) }
                        )
                        FilterChip(
                            selected = settings.includeDate,
                            onClick = { onSettingsChange(settings.copy(includeDate = !settings.includeDate)) },
                            label = { Text(stringResource(R.string.include_date)) }
                        )
                        FilterChip(
                            selected = settings.includeTimestamps,
                            onClick = { onSettingsChange(settings.copy(includeTimestamps = !settings.includeTimestamps)) },
                            label = { Text(stringResource(R.string.include_timestamps)) }
                        )
                        FilterChip(
                            selected = settings.includeSummary,
                            onClick = { onSettingsChange(settings.copy(includeSummary = !settings.includeSummary)) },
                            label = { Text(stringResource(R.string.include_summary)) }
                        )
                    }
                }
                
                Divider()
                
                // Preview section
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(1f)
                        .verticalScroll(rememberScrollState())
                        .padding(16.dp)
                ) {
                    notes.forEachIndexed { index, (note, segments) ->
                        if (index > 0) {
                            Spacer(modifier = Modifier.height(settings.spacing.paragraphSpacing.dp))
                            Divider()
                            Spacer(modifier = Modifier.height(settings.spacing.paragraphSpacing.dp))
                        }
                        
                        if (settings.includeTitle) {
                            Text(
                                text = note.title ?: "Untitled Note",
                                fontSize = settings.fontSize.titleSize.sp,
                                textAlign = TextAlign.Center,
                                modifier = Modifier.fillMaxWidth()
                            )
                        }
                        
                        if (settings.includeDate) {
                            Text(
                                text = "Created: ${dateFormat.format(note.createdAt)}",
                                fontSize = settings.fontSize.timestampSize.sp,
                                fontStyle = FontStyle.Italic
                            )
                        }
                        
                        Spacer(modifier = Modifier.height(8.dp))
                        
                        segments.forEach { segment ->
                            if (settings.includeTimestamps) {
                                Text(
                                    text = formatTimestamp(segment.startTime),
                                    fontSize = settings.fontSize.timestampSize.sp,
                                    fontStyle = FontStyle.Italic
                                )
                            }
                            
                            Text(
                                text = segment.text,
                                fontSize = settings.fontSize.textSize.sp,
                                lineHeight = settings.fontSize.textSize.sp * settings.spacing.lineSpacing
                            )
                            
                            Spacer(modifier = Modifier.height(8.dp))
                        }
                        
                        if (settings.includeSummary && note.summary != null) {
                            Text(
                                text = "Summary",
                                fontSize = settings.fontSize.titleSize.sp,
                                modifier = Modifier.padding(top = 8.dp)
                            )
                            Text(
                                text = note.summary,
                                fontSize = settings.fontSize.textSize.sp,
                                lineHeight = settings.fontSize.textSize.sp * settings.spacing.lineSpacing
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun <T> SegmentedButtons(
    items: List<T>,
    selectedItem: T,
    onItemSelect: (T) -> Unit,
    itemLabel: (T) -> String
) {
    Row(
        modifier = Modifier
            .background(
                MaterialTheme.colorScheme.surfaceVariant,
                MaterialTheme.shapes.small
            )
    ) {
        items.forEachIndexed { index, item ->
            TextButton(
                onClick = { onItemSelect(item) },
                colors = ButtonDefaults.textButtonColors(
                    containerColor = if (item == selectedItem) {
                        MaterialTheme.colorScheme.primaryContainer
                    } else {
                        MaterialTheme.colorScheme.surfaceVariant
                    }
                ),
                modifier = Modifier.height(32.dp)
            ) {
                Text(
                    text = itemLabel(item),
                    color = if (item == selectedItem) {
                        MaterialTheme.colorScheme.onPrimaryContainer
                    } else {
                        MaterialTheme.colorScheme.onSurfaceVariant
                    }
                )
            }
        }
    }
}

private fun formatTimestamp(milliseconds: Long): String {
    val seconds = milliseconds / 1000
    val minutes = seconds / 60
    val remainingSeconds = seconds % 60
    return String.format("%02d:%02d", minutes, remainingSeconds)
} 