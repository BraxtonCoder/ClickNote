package com.example.clicknote.ui.components

import androidx.compose.animation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.example.clicknote.service.TranscriptionService.TranscriptionSegment
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun RealTimeTranscription(
    text: String,
    segments: List<TranscriptionSegment>,
    isRecording: Boolean,
    amplitudes: List<Float>,
    transcriptionProgress: Float,
    modifier: Modifier = Modifier
) {
    val listState = rememberLazyListState()
    val coroutineScope = rememberCoroutineScope()
    
    LaunchedEffect(segments.size) {
        if (segments.isNotEmpty()) {
            coroutineScope.launch {
                listState.animateScrollToItem(segments.size - 1)
            }
        }
    }

    Column(
        modifier = modifier.fillMaxSize(),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // Audio Waveform
        AudioWaveform(
            amplitudes = amplitudes,
            isRecording = isRecording,
            modifier = Modifier
                .fillMaxWidth()
                .height(64.dp)
        )

        // Progress Indicator
        if (!isRecording && transcriptionProgress > 0) {
            LinearProgressIndicator(
                progress = transcriptionProgress,
                modifier = Modifier.fillMaxWidth()
            )
        }

        // Transcription Text
        Card(
            modifier = Modifier.fillMaxWidth()
        ) {
            LazyColumn(
                state = listState,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                items(segments) { segment ->
                    TranscriptionSegmentItem(segment = segment)
                }

                // Current text being transcribed
                if (isRecording) {
                    item {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            CircularProgressIndicator(
                                modifier = Modifier.size(16.dp),
                                strokeWidth = 2.dp
                            )
                            Text(
                                text = text.takeLastWhile { it != '.' }.trim(),
                                style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun TranscriptionSegmentItem(
    segment: TranscriptionSegment,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(4.dp)
    ) {
        // Speaker and Timestamp
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            segment.speaker?.let { speaker ->
                Text(
                    text = speaker,
                    style = MaterialTheme.typography.labelMedium,
                    color = MaterialTheme.colorScheme.primary
                )
            }
            
            Text(
                text = formatTimestamp(segment.startTime),
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }

        // Segment Text
        Text(
            text = segment.text,
            style = MaterialTheme.typography.bodyMedium,
            modifier = Modifier.fillMaxWidth()
        )
    }
}

private fun formatTimestamp(seconds: Double): String {
    val minutes = (seconds / 60).toInt()
    val remainingSeconds = (seconds % 60).toInt()
    return "%02d:%02d".format(minutes, remainingSeconds)
} 