package com.example.clicknote.ui.settings

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.example.clicknote.service.ModelInfo
import com.example.clicknote.service.ModelState
import kotlin.math.roundToInt

@Composable
fun OfflineTranscriptionSettings(
    viewModel: OfflineTranscriptionViewModel = hiltViewModel()
) {
    val modelState by viewModel.modelState.collectAsState()
    val availableModels by viewModel.availableModels.collectAsState()
    val currentModel by viewModel.currentModel.collectAsState()
    var showDownloadConfirmation by remember { mutableStateOf<ModelInfo?>(null) }

    Column(modifier = Modifier.fillMaxWidth()) {
        // Header
        Text(
            text = "Offline Transcription",
            style = MaterialTheme.typography.titleMedium,
            modifier = Modifier.padding(16.dp)
        )

        // Model status card
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp)
            ) {
                Text(
                    text = "Current Model: ${currentModel?.language ?: "None"}",
                    style = MaterialTheme.typography.bodyLarge
                )

                Spacer(modifier = Modifier.height(8.dp))

                when (val state = modelState) {
                    is ModelState.NotDownloaded -> {
                        Text(
                            text = "No model downloaded",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.error
                        )
                    }
                    is ModelState.Downloading -> {
                        Column {
                            Text(
                                text = "Downloading model...",
                                style = MaterialTheme.typography.bodyMedium
                            )
                            LinearProgressIndicator(
                                progress = state.progress,
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(vertical = 8.dp)
                            )
                            Text(
                                text = "${(state.progress * 100).roundToInt()}%",
                                style = MaterialTheme.typography.bodySmall,
                                modifier = Modifier.align(Alignment.End)
                            )
                        }
                    }
                    is ModelState.Ready -> {
                        Text(
                            text = "Model ready for offline use",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.primary
                        )
                    }
                    is ModelState.Error -> {
                        Text(
                            text = state.message,
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.error
                        )
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Available models
        Text(
            text = "Available Models",
            style = MaterialTheme.typography.titleSmall,
            modifier = Modifier.padding(horizontal = 16.dp)
        )

        LazyColumn(
            modifier = Modifier.fillMaxWidth(),
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            items(availableModels) { model ->
                ModelItem(
                    model = model,
                    isSelected = model.name == currentModel?.name,
                    onSelect = { viewModel.switchModel(model) },
                    onDownload = { showDownloadConfirmation = model }
                )
            }
        }

        if (showDownloadConfirmation != null) {
            AlertDialog(
                onDismissRequest = { showDownloadConfirmation = null },
                title = { Text("Download Model") },
                text = {
                    Text(
                        "Do you want to download the ${showDownloadConfirmation?.language} model? " +
                        "This will use approximately ${showDownloadConfirmation?.size?.div(1_000_000)} MB of storage."
                    )
                },
                confirmButton = {
                    TextButton(
                        onClick = {
                            showDownloadConfirmation?.let { viewModel.downloadModel(it) }
                            showDownloadConfirmation = null
                        }
                    ) {
                        Text("Download")
                    }
                },
                dismissButton = {
                    TextButton(onClick = { showDownloadConfirmation = null }) {
                        Text("Cancel")
                    }
                }
            )
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun ModelItem(
    model: ModelInfo,
    isSelected: Boolean,
    onSelect: () -> Unit,
    onDownload: () -> Unit
) {
    Card(
        onClick = { if (model.isDownloaded) onSelect() else onDownload() },
        modifier = Modifier.fillMaxWidth()
    ) {
        ListItem(
            headlineContent = { Text(model.language) },
            supportingContent = {
                Text(
                    text = if (model.isDownloaded) {
                        "Downloaded (${model.size / 1_000_000} MB)"
                    } else {
                        "Available for download (${model.size / 1_000_000} MB)"
                    }
                )
            },
            leadingContent = {
                RadioButton(
                    selected = isSelected,
                    onClick = null
                )
            },
            trailingContent = {
                if (!model.isDownloaded) {
                    IconButton(onClick = onDownload) {
                        Icon(
                            imageVector = Icons.Default.Download,
                            contentDescription = "Download model"
                        )
                    }
                }
            }
        )
    }
} 