package com.example.clicknote.ui.recording

import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.example.clicknote.ui.components.EnhancedWaveformVisualizer
import com.example.clicknote.ui.components.AudioPlaybackVisualizer
import java.time.Duration
import java.time.LocalTime
import java.time.format.DateTimeFormatter
import com.example.clicknote.service.PremiumFeature
import com.example.clicknote.ui.components.UpgradePrompt

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun RecordingScreen(
    viewModel: RecordingViewModel = hiltViewModel(),
    onNavigateUp: () -> Unit,
    onNavigateToSubscription: () -> Unit
) {
    val uiState by viewModel.uiState.collectAsState()
    val scrollState = rememberScrollState()
    val snackbarHostState = remember { SnackbarHostState() }

    LaunchedEffect(Unit) {
        viewModel.checkMicrophonePermission()
    }

    // Handle error messages
    LaunchedEffect(uiState.error) {
        uiState.error?.let { error ->
            snackbarHostState.showSnackbar(
                message = error,
                duration = SnackbarDuration.Short
            )
            viewModel.clearError()
        }
    }

    // Show upgrade prompt if needed
    if (uiState.showUpgradePrompt) {
        UpgradePrompt(
            feature = PremiumFeature.TRANSCRIPTION,
            remainingCount = uiState.remainingTranscriptions,
            onUpgrade = {
                onNavigateToSubscription()
            },
            onDismiss = {
                viewModel.dismissUpgradePrompt()
            }
        )
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("New Recording") },
                navigationIcon = {
                    IconButton(onClick = onNavigateUp) {
                        Icon(
                            Icons.Default.ArrowBack,
                            contentDescription = "Back",
                            tint = MaterialTheme.colorScheme.onSurface
                        )
                    }
                },
                actions = {
                    AnimatedVisibility(
                        visible = uiState.recordingState != RecordingState.IDLE,
                        enter = fadeIn() + expandHorizontally(),
                        exit = fadeOut() + shrinkHorizontally()
                    ) {
                        IconButton(
                            onClick = { viewModel.saveRecording() },
                            enabled = uiState.recordingState == RecordingState.PAUSED
                        ) {
                            Icon(
                                Icons.Default.Save,
                                contentDescription = "Save",
                                tint = if (uiState.recordingState == RecordingState.PAUSED)
                                    MaterialTheme.colorScheme.primary
                                else
                                    MaterialTheme.colorScheme.onSurface.copy(alpha = 0.38f)
                            )
                        }
                    }
                }
            )
        },
        snackbarHost = { SnackbarHost(snackbarHostState) }
    ) { padding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(16.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                // Timer
                AnimatedContent(
                    targetState = uiState.recordingDuration,
                    transitionSpec = {
                        slideInVertically { height -> height } + fadeIn() togetherWith
                        slideOutVertically { height -> -height } + fadeOut()
                    }
                ) { duration ->
                    Text(
                        text = formatDuration(duration),
                        style = MaterialTheme.typography.displayMedium,
                        color = if (uiState.recordingState == RecordingState.RECORDING)
                            MaterialTheme.colorScheme.primary
                        else
                            MaterialTheme.colorScheme.onSurface
                    )
                }

                Spacer(modifier = Modifier.height(32.dp))

                // Waveform Card
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(160.dp),
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.surface
                    )
                ) {
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(8.dp)
                    ) {
                        EnhancedWaveformVisualizer(
                            amplitudes = uiState.amplitudeHistory.map { it.toFloat() / 100f },
                            isRecording = uiState.recordingState == RecordingState.RECORDING,
                            modifier = Modifier.fillMaxSize()
                        )

                        // Loading overlay
                        AnimatedVisibility(
                            visible = uiState.isLoading,
                            enter = fadeIn(),
                            exit = fadeOut()
                        ) {
                            Box(
                                modifier = Modifier
                                    .fillMaxSize()
                                    .padding(16.dp),
                                contentAlignment = Alignment.Center
                            ) {
                                CircularProgressIndicator()
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(32.dp))

                // Recording controls
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceEvenly,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    // Delete button
                    AnimatedVisibility(
                        visible = uiState.recordingState != RecordingState.IDLE,
                        enter = fadeIn() + expandHorizontally(),
                        exit = fadeOut() + shrinkHorizontally()
                    ) {
                        IconButton(
                            onClick = { viewModel.discardRecording() }
                        ) {
                            Icon(
                                Icons.Default.Delete,
                                contentDescription = "Discard recording",
                                tint = MaterialTheme.colorScheme.error
                            )
                        }
                    }

                    // Record button
                    FloatingActionButton(
                        onClick = {
                            when (uiState.recordingState) {
                                RecordingState.IDLE -> viewModel.startRecording()
                                RecordingState.RECORDING -> viewModel.pauseRecording()
                                RecordingState.PAUSED -> viewModel.resumeRecording()
                            }
                        },
                        containerColor = when (uiState.recordingState) {
                            RecordingState.RECORDING -> MaterialTheme.colorScheme.error
                            else -> MaterialTheme.colorScheme.primary
                        }
                    ) {
                        Icon(
                            when (uiState.recordingState) {
                                RecordingState.RECORDING -> Icons.Default.Stop
                                RecordingState.PAUSED -> Icons.Default.PlayArrow
                                RecordingState.IDLE -> Icons.Default.Mic
                            },
                            contentDescription = when (uiState.recordingState) {
                                RecordingState.RECORDING -> "Stop recording"
                                RecordingState.PAUSED -> "Resume recording"
                                RecordingState.IDLE -> "Start recording"
                            },
                            tint = MaterialTheme.colorScheme.onPrimary
                        )
                    }

                    // Save button (spacer for layout symmetry when hidden)
                    Box(modifier = Modifier.size(48.dp)) {
                        AnimatedVisibility(
                            visible = uiState.recordingState == RecordingState.PAUSED,
                            enter = fadeIn() + expandHorizontally(),
                            exit = fadeOut() + shrinkHorizontally()
                        ) {
                            IconButton(
                                onClick = { viewModel.saveRecording() }
                            ) {
                                Icon(
                                    Icons.Default.Check,
                                    contentDescription = "Save recording",
                                    tint = MaterialTheme.colorScheme.primary
                                )
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(32.dp))

                // Transcription Card
                AnimatedVisibility(
                    visible = uiState.recordingState != RecordingState.IDLE,
                    enter = fadeIn() + expandVertically(),
                    exit = fadeOut() + shrinkVertically()
                ) {
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .weight(1f)
                    ) {
                        Column(
                            modifier = Modifier
                                .fillMaxSize()
                                .padding(16.dp)
                        ) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    text = "Transcription",
                                    style = MaterialTheme.typography.titleSmall,
                                    color = MaterialTheme.colorScheme.primary
                                )
                                if (uiState.isTranscribing) {
                                    CircularProgressIndicator(
                                        modifier = Modifier.size(16.dp),
                                        strokeWidth = 2.dp
                                    )
                                }
                            }
                            Spacer(modifier = Modifier.height(8.dp))
                            Text(
                                text = uiState.transcription.ifEmpty {
                                    if (uiState.isTranscribing) "Transcribing..."
                                    else "Start speaking to see transcription"
                                },
                                style = MaterialTheme.typography.bodyMedium,
                                color = if (uiState.transcription.isEmpty())
                                    MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
                                else
                                    MaterialTheme.colorScheme.onSurface,
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .weight(1f)
                                    .verticalScroll(scrollState)
                            )
                        }
                    }
                }
            }

            // Permission request
            if (uiState.showPermissionRequest) {
                AlertDialog(
                    onDismissRequest = { },
                    title = { Text("Microphone Permission Required") },
                    text = {
                        Text(
                            "ClickNote needs access to your microphone to record audio. " +
                            "Please grant microphone permission to continue."
                        )
                    },
                    confirmButton = {
                        TextButton(
                            onClick = { viewModel.requestMicrophonePermission() }
                        ) {
                            Text("Grant Permission")
                        }
                    },
                    dismissButton = {
                        TextButton(
                            onClick = onNavigateUp
                        ) {
                            Text("Cancel")
                        }
                    }
                )
            }
        }
    }
}

private fun formatDuration(duration: Duration): String {
    val seconds = duration.seconds
    val minutes = seconds / 60
    val remainingSeconds = seconds % 60
    return String.format("%02d:%02d", minutes, remainingSeconds)
} 