package com.example.clicknote.ui.components

import androidx.compose.animation.*
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import java.time.Duration
import java.time.LocalDateTime

@Composable
fun CallRecordingUI(
    isRecording: Boolean,
    recordingStartTime: LocalDateTime?,
    phoneNumber: String?,
    currentSpeaker: Speaker?,
    speakerSegments: List<Pair<Speaker, String>>,
    amplitudes: List<Float>,
    frequencies: List<Float>,
    onStopRecording: () -> Unit,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .fillMaxWidth()
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        // Call info section
        CallInfoSection(
            isRecording = isRecording,
            recordingStartTime = recordingStartTime,
            phoneNumber = phoneNumber
        )

        Spacer(modifier = Modifier.height(24.dp))

        // Advanced waveform visualization
        AdvancedAudioWaveform(
            amplitudes = amplitudes,
            frequencies = frequencies,
            isRecording = isRecording,
            isPlaying = false,
            currentPosition = 0f,
            modifier = Modifier
                .fillMaxWidth()
                .height(120.dp)
        )

        Spacer(modifier = Modifier.height(24.dp))

        // Active speaker indicator
        ActiveSpeakerIndicator(
            currentSpeaker = currentSpeaker,
            isRecording = isRecording
        )

        Spacer(modifier = Modifier.height(16.dp))

        // Recent transcription segments
        RecentSegments(
            segments = speakerSegments.takeLast(3),
            modifier = Modifier.fillMaxWidth()
        )

        Spacer(modifier = Modifier.height(24.dp))

        // Stop recording button
        Button(
            onClick = onStopRecording,
            colors = ButtonDefaults.buttonColors(
                containerColor = MaterialTheme.colorScheme.error
            ),
            modifier = Modifier.height(48.dp)
        ) {
            Icon(
                imageVector = Icons.Default.Stop,
                contentDescription = "Stop recording"
            )
            Spacer(modifier = Modifier.width(8.dp))
            Text("Stop Recording")
        }
    }
}

@Composable
private fun CallInfoSection(
    isRecording: Boolean,
    recordingStartTime: LocalDateTime?,
    phoneNumber: String?
) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        // Recording indicator
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.Center
        ) {
            Box(
                modifier = Modifier
                    .size(12.dp)
                    .clip(CircleShape)
                    .background(
                        if (isRecording) MaterialTheme.colorScheme.error
                        else MaterialTheme.colorScheme.surfaceVariant
                    )
            )
            Spacer(modifier = Modifier.width(8.dp))
            Text(
                text = if (isRecording) "Recording" else "Call Ended",
                style = MaterialTheme.typography.titleMedium
            )
        }

        // Duration
        recordingStartTime?.let { startTime ->
            val duration by remember {
                derivedStateOf {
                    Duration.between(startTime, LocalDateTime.now())
                }
            }
            
            Text(
                text = formatDuration(duration),
                style = MaterialTheme.typography.titleLarge,
                color = MaterialTheme.colorScheme.primary
            )
        }

        // Phone number
        phoneNumber?.let { number ->
            Text(
                text = number,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

@Composable
private fun ActiveSpeakerIndicator(
    currentSpeaker: Speaker?,
    isRecording: Boolean
) {
    AnimatedContent(
        targetState = currentSpeaker,
        transitionSpec = {
            fadeIn() + slideInVertically() with 
            fadeOut() + slideOutVertically()
        }
    ) { speaker ->
        if (speaker != null && isRecording) {
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.secondaryContainer
                )
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector = Icons.Default.Person,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.secondary
                    )
                    Spacer(modifier = Modifier.width(12.dp))
                    Text(
                        text = speaker.label,
                        style = MaterialTheme.typography.titleMedium,
                        color = MaterialTheme.colorScheme.onSecondaryContainer
                    )
                    Spacer(modifier = Modifier.weight(1f))
                    Box(
                        modifier = Modifier
                            .size(8.dp)
                            .clip(CircleShape)
                            .background(MaterialTheme.colorScheme.secondary)
                    )
                }
            }
        }
    }
}

@Composable
private fun RecentSegments(
    segments: List<Pair<Speaker, String>>,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier,
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        segments.forEach { (speaker, text) ->
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(12.dp),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
                )
            ) {
                Column(
                    modifier = Modifier.padding(12.dp)
                ) {
                    Text(
                        text = speaker.label,
                        style = MaterialTheme.typography.labelMedium,
                        color = MaterialTheme.colorScheme.primary
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = text,
                        style = MaterialTheme.typography.bodyMedium
                    )
                }
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