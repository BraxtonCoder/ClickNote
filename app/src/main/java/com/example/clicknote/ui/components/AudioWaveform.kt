package com.example.clicknote.ui.components

import androidx.compose.animation.core.*
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.*
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.dp
import kotlin.math.abs
import kotlin.math.sin
import kotlin.random.Random

@Composable
fun AudioWaveform(
    amplitudes: List<Float>,
    isRecording: Boolean,
    isPlaying: Boolean,
    currentPosition: Float,
    modifier: Modifier = Modifier,
    activeColor: Color = MaterialTheme.colorScheme.primary,
    inactiveColor: Color = MaterialTheme.colorScheme.primary.copy(alpha = 0.3f)
) {
    val infiniteTransition = rememberInfiniteTransition()
    val wavePhase by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 2f * Math.PI.toFloat(),
        animationSpec = infiniteRepeatable(
            animation = tween(2000, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        )
    )

    val barWidth = 3.dp
    val barSpacing = 2.dp
    val maxBarHeight = 48.dp

    Canvas(modifier = modifier.fillMaxWidth().height(maxBarHeight)) {
        val canvasWidth = size.width
        val canvasHeight = size.height
        val totalBars = (canvasWidth / (barWidth.toPx() + barSpacing.toPx())).toInt()
        
        val barHeights = if (isRecording) {
            // Generate dynamic waveform for recording
            List(totalBars) { index ->
                val phase = wavePhase + index * 0.2f
                val amplitude = if (isRecording) {
                    val base = (sin(phase) + 1) / 2
                    val noise = Random.nextFloat() * 0.3f
                    (base + noise).coerceIn(0f, 1f)
                } else {
                    amplitudes.getOrNull(index)?.coerceIn(0f, 1f) ?: 0f
                }
                amplitude * canvasHeight
            }
        } else {
            // Use actual amplitudes for playback
            amplitudes.map { it * canvasHeight }
        }

        barHeights.forEachIndexed { index, height ->
            val x = index * (barWidth.toPx() + barSpacing.toPx())
            val normalizedPosition = currentPosition * totalBars
            
            val color = when {
                isRecording -> activeColor
                index <= normalizedPosition -> activeColor
                else -> inactiveColor
            }

            drawLine(
                color = color,
                start = Offset(x, canvasHeight / 2 + height / 2),
                end = Offset(x, canvasHeight / 2 - height / 2),
                strokeWidth = barWidth.toPx(),
                cap = StrokeCap.Round
            )
        }
    }
}

@Composable
fun AudioWaveformSmall(
    amplitudes: List<Float>,
    modifier: Modifier = Modifier,
    color: Color = Color.Red
) {
    Canvas(
        modifier = modifier
            .fillMaxWidth()
            .height(40.dp)
    ) {
        val availableWidth = size.width
        val centerY = size.height / 2
        
        if (amplitudes.isNotEmpty()) {
            val points = mutableListOf<Offset>()
            val step = amplitudes.size / (availableWidth / 2f)
            
            for (i in 0 until availableWidth.toInt() step 2) {
                val index = (i * step).toInt().coerceIn(0, amplitudes.size - 1)
                val amplitude = amplitudes[index]
                val x = i.toFloat()
                val y = centerY * (1 + amplitude)
                points.add(Offset(x, y))
            }
            
            // Draw waveform as a continuous line
            for (i in 0 until points.size - 1) {
                drawLine(
                    color = color.copy(alpha = 0.6f),
                    start = points[i],
                    end = points[i + 1],
                    strokeWidth = 1f
                )
            }
        }
    }
}

@Composable
fun AudioPlaybackWaveform(
    amplitudes: List<Float>,
    playbackPosition: Float,
    modifier: Modifier = Modifier,
    activeColor: Color = MaterialTheme.colorScheme.primary,
    inactiveColor: Color = MaterialTheme.colorScheme.surfaceVariant
) {
    Canvas(
        modifier = modifier
            .fillMaxWidth()
            .height(48.dp)
    ) {
        val width = size.width
        val height = size.height
        val lineSpacing = 2.dp.toPx()
        val lineWidth = 1.dp.toPx()
        val numLines = (width / (lineWidth + lineSpacing)).toInt()
        val centerY = height / 2
        val playbackX = width * playbackPosition

        for (i in 0 until numLines) {
            val x = i * (lineWidth + lineSpacing)
            val amplitude = if (i < amplitudes.size) {
                amplitudes[i]
            } else {
                0f
            }
            
            val lineHeight = height * amplitude * 0.8f
            val startY = centerY - lineHeight / 2
            val endY = centerY + lineHeight / 2

            drawLine(
                color = if (x <= playbackX) activeColor else inactiveColor,
                start = Offset(x, startY),
                end = Offset(x, endY),
                strokeWidth = lineWidth,
                cap = StrokeCap.Round
            )
        }
    }
}

@Composable
fun LiveAudioWaveform(
    isRecording: Boolean,
    modifier: Modifier = Modifier
) {
    var amplitudes by remember { mutableStateOf(listOf<Float>()) }
    val maxAmplitudes = 100

    LaunchedEffect(isRecording) {
        if (isRecording) {
            while (true) {
                val newAmplitude = Random.nextFloat()
                amplitudes = (amplitudes + newAmplitude).takeLast(maxAmplitudes)
                delay(50) // Update every 50ms
            }
        }
    }

    AudioWaveform(
        amplitudes = amplitudes,
        isRecording = isRecording,
        isPlaying = false,
        currentPosition = 0f,
        modifier = modifier
    )
}

private fun generateDummyWaveform(size: Int = 100): List<Float> {
    return List(size) {
        val base = sin(it * 0.1f).toFloat() * 0.3f + 0.5f
        val noise = Random.nextFloat() * 0.2f
        (base + noise).coerceIn(0.1f, 0.9f)
    }
} 