package com.example.clicknote.ui.components.audio

import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.example.clicknote.R
import kotlin.time.Duration.Companion.seconds

@Composable
fun AudioPlayer(
    isPlaying: Boolean,
    progress: Float,
    onProgressChange: (Float) -> Unit,
    onPlayPause: () -> Unit,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
    ) {
        // Progress bar
        Slider(
            value = progress,
            onValueChange = onProgressChange,
            modifier = Modifier.fillMaxWidth()
        )

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Time display
            Text(
                text = formatDuration((progress * 100).toInt().seconds),
                style = MaterialTheme.typography.bodySmall
            )

            // Playback controls
            Row(
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                IconButton(
                    onClick = { onProgressChange((progress - 0.1f).coerceAtLeast(0f)) }
                ) {
                    Icon(
                        imageVector = Icons.Default.Replay10,
                        contentDescription = stringResource(R.string.rewind_10)
                    )
                }

                IconButton(
                    onClick = onPlayPause,
                    modifier = Modifier.size(48.dp)
                ) {
                    Icon(
                        imageVector = if (isPlaying) {
                            Icons.Default.PauseCircleFilled
                        } else {
                            Icons.Default.PlayCircleFilled
                        },
                        contentDescription = if (isPlaying) {
                            stringResource(R.string.pause)
                        } else {
                            stringResource(R.string.play)
                        },
                        modifier = Modifier.fillMaxSize()
                    )
                }

                IconButton(
                    onClick = { onProgressChange((progress + 0.1f).coerceAtMost(1f)) }
                ) {
                    Icon(
                        imageVector = Icons.Default.Forward10,
                        contentDescription = stringResource(R.string.forward_10)
                    )
                }
            }

            // Speed control
            TextButton(
                onClick = { /* TODO: Implement speed control */ }
            ) {
                Text("1.0x")
            }
        }
    }
}

private fun formatDuration(duration: kotlin.time.Duration): String {
    val totalSeconds = duration.inWholeSeconds
    val minutes = totalSeconds / 60
    val seconds = totalSeconds % 60
    return "%d:%02d".format(minutes, seconds)
} 