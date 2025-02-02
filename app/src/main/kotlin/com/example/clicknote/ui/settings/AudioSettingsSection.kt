package com.example.clicknote.ui.settings

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.example.clicknote.domain.model.*

@Composable
fun AudioSettingsSection(
    settings: AudioSettings,
    onSettingsChanged: (AudioSettings) -> Unit,
    modifier: Modifier = Modifier
) {
    var showSkipDurationDialog by remember { mutableStateOf(false) }
    var showWaveformStyleDialog by remember { mutableStateOf(false) }
    var showWaveformColorDialog by remember { mutableStateOf(false) }
    var showExportFormatDialog by remember { mutableStateOf(false) }

    Column(
        modifier = modifier
            .fillMaxWidth()
            .padding(16.dp)
    ) {
        Text(
            text = "Audio Settings",
            style = MaterialTheme.typography.titleMedium,
            color = MaterialTheme.colorScheme.primary
        )

        Spacer(modifier = Modifier.height(16.dp))

        // Skip Duration Setting
        OutlinedCard(
            onClick = { showSkipDurationDialog = true },
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp)
            ) {
                Text(
                    text = "Skip Duration",
                    style = MaterialTheme.typography.titleSmall
                )
                Text(
                    text = "${settings.skipDuration} seconds",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }

        Spacer(modifier = Modifier.height(8.dp))

        // Waveform Style Setting
        OutlinedCard(
            onClick = { showWaveformStyleDialog = true },
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp)
            ) {
                Text(
                    text = "Waveform Style",
                    style = MaterialTheme.typography.titleSmall
                )
                Text(
                    text = settings.waveformStyle.name,
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }

        Spacer(modifier = Modifier.height(8.dp))

        // Waveform Color Setting
        OutlinedCard(
            onClick = { showWaveformColorDialog = true },
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp)
            ) {
                Text(
                    text = "Waveform Colors",
                    style = MaterialTheme.typography.titleSmall
                )
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    ColorPreview(settings.waveformColors.playedColor)
                    ColorPreview(settings.waveformColors.unplayedColor)
                    ColorPreview(settings.waveformColors.positionLineColor)
                }
            }
        }

        Spacer(modifier = Modifier.height(8.dp))

        // Export Format Setting
        OutlinedCard(
            onClick = { showExportFormatDialog = true },
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp)
            ) {
                Text(
                    text = "Export Format",
                    style = MaterialTheme.typography.titleSmall
                )
                Text(
                    text = settings.exportFormat.displayName,
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Save Audio Toggle
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "Save Audio with Note",
                style = MaterialTheme.typography.bodyLarge
            )
            Switch(
                checked = settings.saveAudioWithNote,
                onCheckedChange = { checked ->
                    onSettingsChanged(settings.copy(saveAudioWithNote = checked))
                }
            )
        }

        Spacer(modifier = Modifier.height(8.dp))

        // Enhance Audio Quality Toggle
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "Enhance Audio Quality",
                style = MaterialTheme.typography.bodyLarge
            )
            Switch(
                checked = settings.enhanceAudioQuality,
                onCheckedChange = { checked ->
                    onSettingsChanged(settings.copy(enhanceAudioQuality = checked))
                }
            )
        }
    }

    // Skip Duration Dialog
    if (showSkipDurationDialog) {
        var skipDuration by remember { mutableStateOf(settings.skipDuration.toString()) }
        AlertDialog(
            onDismissRequest = { showSkipDurationDialog = false },
            title = { Text("Skip Duration") },
            text = {
                OutlinedTextField(
                    value = skipDuration,
                    onValueChange = { skipDuration = it },
                    label = { Text("Seconds") },
                    singleLine = true,
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number)
                )
            },
            confirmButton = {
                TextButton(
                    onClick = {
                        skipDuration.toIntOrNull()?.let { duration ->
                            onSettingsChanged(settings.copy(skipDuration = duration))
                        }
                        showSkipDurationDialog = false
                    }
                ) {
                    Text("Save")
                }
            },
            dismissButton = {
                TextButton(onClick = { showSkipDurationDialog = false }) {
                    Text("Cancel")
                }
            }
        )
    }

    // Waveform Style Dialog
    if (showWaveformStyleDialog) {
        AlertDialog(
            onDismissRequest = { showWaveformStyleDialog = false },
            title = { Text("Waveform Style") },
            text = {
                Column {
                    WaveformStyle.values().forEach { style ->
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable {
                                    onSettingsChanged(settings.copy(waveformStyle = style))
                                    showWaveformStyleDialog = false
                                }
                                .padding(vertical = 12.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            RadioButton(
                                selected = settings.waveformStyle == style,
                                onClick = null
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(style.name)
                        }
                    }
                }
            },
            confirmButton = {}
        )
    }

    // Waveform Color Dialog
    if (showWaveformColorDialog) {
        AlertDialog(
            onDismissRequest = { showWaveformColorDialog = false },
            title = { Text("Waveform Colors") },
            text = {
                Column {
                    listOf(
                        WaveformColors.Default,
                        WaveformColors.Dark,
                        WaveformColors.Minimal,
                        WaveformColors.Vibrant
                    ).forEach { colors ->
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable {
                                    onSettingsChanged(settings.copy(waveformColors = colors))
                                    showWaveformColorDialog = false
                                }
                                .padding(vertical = 12.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            RadioButton(
                                selected = settings.waveformColors == colors,
                                onClick = null
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Row(
                                horizontalArrangement = Arrangement.spacedBy(4.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                ColorPreview(colors.playedColor)
                                ColorPreview(colors.unplayedColor)
                                ColorPreview(colors.positionLineColor)
                            }
                        }
                    }
                }
            },
            confirmButton = {}
        )
    }

    // Export Format Dialog
    if (showExportFormatDialog) {
        AlertDialog(
            onDismissRequest = { showExportFormatDialog = false },
            title = { Text("Export Format") },
            text = {
                Column {
                    AudioFormat.values().forEach { format ->
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable {
                                    onSettingsChanged(settings.copy(exportFormat = format))
                                    showExportFormatDialog = false
                                }
                                .padding(vertical = 12.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            RadioButton(
                                selected = settings.exportFormat == format,
                                onClick = null
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Column {
                                Text(format.displayName)
                                Text(
                                    text = format.extension.uppercase(),
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        }
                    }
                }
            },
            confirmButton = {}
        )
    }
}

@Composable
private fun ColorPreview(
    color: Color,
    modifier: Modifier = Modifier
) {
    Surface(
        modifier = modifier
            .size(24.dp)
            .border(
                width = 1.dp,
                color = MaterialTheme.colorScheme.outline,
                shape = CircleShape
            ),
        shape = CircleShape,
        color = color
    ) {}
} 