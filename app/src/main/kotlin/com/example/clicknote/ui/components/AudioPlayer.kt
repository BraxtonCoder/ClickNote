package com.example.clicknote.ui.components

import androidx.compose.animation.core.*
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.example.clicknote.domain.model.AudioAmplitude
import com.example.clicknote.domain.model.PlaybackState
import com.example.clicknote.ui.theme.LocalSpacing
import kotlin.time.Duration.Companion.milliseconds
import kotlin.time.Duration.Companion.seconds

data class PlaybackSpeed(
    val label: String,
    val value: Float
)

val playbackSpeeds = listOf(
    PlaybackSpeed("0.5x", 0.5f),
    PlaybackSpeed("0.75x", 0.75f),
    PlaybackSpeed("1.0x", 1.0f),
    PlaybackSpeed("1.25x", 1.25f),
    PlaybackSpeed("1.5x", 1.5f),
    PlaybackSpeed("1.75x", 1.75f),
    PlaybackSpeed("2.0x", 2.0f)
)

@Composable
fun AudioPlayer(
    audioPath: String,
    amplitudes: List<AudioAmplitude>,
    currentPosition: Long = 0,
    duration: Long = 0,
    playbackState: PlaybackState = PlaybackState.STOPPED,
    playbackSpeed: Float = 1.0f,
    isLooping: Boolean = false,
    onPlay: () -> Unit = {},
    onPause: () -> Unit = {},
    onSeek: (Long) -> Unit = {},
    onSpeedChange: (Float) -> Unit = {},
    onLoopChange: (Boolean) -> Unit = {},
    onSkipForward: () -> Unit = {},
    onSkipBackward: () -> Unit = {},
    onExport: () -> Unit = {},
    modifier: Modifier = Modifier
) {
    val spacing = LocalSpacing.current
    var showSpeedDialog by remember { mutableStateOf(false) }
    val progress = (currentPosition.toFloat() / duration.toFloat()).coerceIn(0f, 1f)
    
    Column(
        modifier = modifier
            .fillMaxWidth()
            .padding(spacing.medium)
    ) {
        // Waveform visualization
        WaveformVisualizer(
            amplitudes = amplitudes,
            currentPosition = currentPosition,
            duration = duration,
            onPositionChange = onSeek,
            modifier = Modifier
                .fillMaxWidth()
                .height(80.dp)
                .padding(vertical = spacing.small)
        )
        
        // Playback controls
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Skip backward button
            IconButton(onClick = onSkipBackward) {
                Icon(
                    imageVector = Icons.Default.Replay10,
                    contentDescription = "Skip backward 10 seconds"
                )
            }
            
            // Play/Pause button
            IconButton(
                onClick = {
                    when (playbackState) {
                        PlaybackState.PLAYING -> onPause()
                        else -> onPlay()
                    }
                }
            ) {
                Icon(
                    imageVector = when (playbackState) {
                        PlaybackState.PLAYING -> Icons.Default.Pause
                        else -> Icons.Default.PlayArrow
                    },
                    contentDescription = when (playbackState) {
                        PlaybackState.PLAYING -> "Pause"
                        else -> "Play"
                    }
                )
            }
            
            // Skip forward button
            IconButton(onClick = onSkipForward) {
                Icon(
                    imageVector = Icons.Default.Forward10,
                    contentDescription = "Skip forward 10 seconds"
                )
            }
        }
        
        // Time and controls row
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(top = spacing.small),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Time display
            Text(
                text = "${formatDuration(currentPosition)} / ${formatDuration(duration)}",
                style = MaterialTheme.typography.bodySmall
            )
            
            // Loop button
            IconButton(
                onClick = { onLoopChange(!isLooping) }
            ) {
                Icon(
                    imageVector = Icons.Default.Loop,
                    contentDescription = "Toggle loop",
                    tint = if (isLooping) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
            
            // Playback speed button
            TextButton(
                onClick = { showSpeedDialog = true }
            ) {
                Text("${playbackSpeed}x")
            }
            
            // Export button
            IconButton(onClick = onExport) {
                Icon(
                    imageVector = Icons.Default.Download,
                    contentDescription = "Export audio"
                )
            }
        }
        
        // Seek bar
        Slider(
            value = progress,
            onValueChange = { onSeek((it * duration).toLong()) },
            valueRange = 0f..duration.toFloat(),
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = spacing.small)
        )
    }
    
    // Playback speed dialog
    if (showSpeedDialog) {
        AlertDialog(
            onDismissRequest = { showSpeedDialog = false },
            title = { Text("Playback speed") },
            text = {
                Column(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    playbackSpeeds.forEach { speed ->
                        TextButton(
                            onClick = {
                                onSpeedChange(speed.value)
                                showSpeedDialog = false
                            },
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Text(
                                text = speed.label,
                                color = if (speed.value == playbackSpeed) {
                                    MaterialTheme.colorScheme.primary
                                } else {
                                    MaterialTheme.colorScheme.onSurface
                                }
                            )
                        }
                    }
                }
            },
            confirmButton = {
                TextButton(onClick = { showSpeedDialog = false }) {
                    Text("Cancel")
                }
            }
        )
    }
}

@Composable
private fun formatDuration(durationMs: Long): String {
    val duration = durationMs.milliseconds
    val minutes = duration.inWholeMinutes
    val seconds = duration.inWholeSeconds % 60
    return "%d:%02d".format(minutes, seconds)
}

enum class PlaybackState {
    PLAYING,
    PAUSED,
    STOPPED
} 