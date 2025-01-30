package com.example.clicknote.ui.components

import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Mic
import androidx.compose.material.icons.filled.Stop
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.example.clicknote.R
import kotlin.math.sin
import kotlin.random.Random
import kotlinx.coroutines.delay
import java.time.Duration
import java.time.LocalTime
import java.time.format.DateTimeFormatter

@Composable
fun RecordingOverlay(
    isRecording: Boolean,
    duration: String,
    amplitudes: List<Float>,
    modifier: Modifier = Modifier
) {
    val infiniteTransition = rememberInfiniteTransition()
    val alphaAnimation by infiniteTransition.animateFloat(
        initialValue = 1f,
        targetValue = 0.3f,
        animationSpec = infiniteRepeatable(
            animation = tween(1000),
            repeatMode = RepeatMode.Reverse
        )
    )

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.surface.copy(alpha = 0.9f)),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Text(
                text = stringResource(R.string.recording_in_progress),
                style = MaterialTheme.typography.titleLarge,
                color = MaterialTheme.colorScheme.error,
                modifier = Modifier.alpha(alphaAnimation)
            )
            
            Text(
                text = duration,
                style = MaterialTheme.typography.titleMedium,
                color = MaterialTheme.colorScheme.onSurface
            )

            WaveformVisualizer(
                amplitudes = amplitudes,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(100.dp)
                    .padding(horizontal = 16.dp),
                color = MaterialTheme.colorScheme.primary,
                style = WaveformStyle.Mirror
            )

            Text(
                text = stringResource(R.string.tap_volume_buttons_to_stop),
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                textAlign = TextAlign.Center,
                modifier = Modifier.padding(horizontal = 32.dp)
            )
        }
    }
}

private fun formatDuration(millis: Long): String {
    val duration = Duration.ofMillis(millis)
    val time = LocalTime.of(0, 0, 0).plus(duration)
    return time.format(DateTimeFormatter.ofPattern("mm:ss"))
}

@Composable
private fun AudioWaveform(
    isRecording: Boolean,
    amplitude: Float,
    modifier: Modifier = Modifier
) {
    val infiniteTransition = rememberInfiniteTransition()
    val phase by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 2f * Math.PI.toFloat(),
        animationSpec = infiniteRepeatable(
            animation = tween(1000, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        )
    )

    Canvas(modifier = modifier) {
        val width = size.width
        val height = size.height
        val centerY = height / 2
        val waveAmplitude = if (isRecording) height * 0.25f * amplitude else 0f

        val points = 100
        val spacing = width / points

        for (i in 0..points) {
            val x = i * spacing
            val normalizedX = i.toFloat() / points
            
            // Create a smooth wave pattern
            val y = centerY + waveAmplitude * sin(
                normalizedX * 10f + phase
            ).toFloat()

            // Add some randomness for a more natural look
            val randomOffset = if (isRecording) {
                Random.nextFloat() * waveAmplitude * 0.2f
            } else 0f

            drawLine(
                color = Color.White,
                start = Offset(x, centerY),
                end = Offset(x, y + randomOffset),
                strokeWidth = 3f,
                cap = StrokeCap.Round
            )
        }
    }
} 