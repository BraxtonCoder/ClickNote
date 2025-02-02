package com.example.clicknote.ui.components

import androidx.compose.animation.core.*
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.*
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.*
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.unit.dp
import kotlin.math.*

@Composable
fun AdvancedAudioWaveform(
    amplitudes: List<Float>,
    frequencies: List<Float>,
    isRecording: Boolean,
    isPlaying: Boolean,
    currentPosition: Float,
    modifier: Modifier = Modifier,
    activeColor: Color = MaterialTheme.colorScheme.primary,
    inactiveColor: Color = MaterialTheme.colorScheme.primary.copy(alpha = 0.3f),
    spectrumColor: Color = MaterialTheme.colorScheme.tertiary,
    showSpectrum: Boolean = true
) {
    val infiniteTransition = rememberInfiniteTransition()
    val wavePhase by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 2f * PI.toFloat(),
        animationSpec = infiniteRepeatable(
            animation = tween(2000, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        )
    )

    val spectrumHeight by infiniteTransition.animateFloat(
        initialValue = 0.7f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(1500, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        )
    )

    Canvas(modifier = modifier.fillMaxWidth().height(80.dp)) {
        val canvasWidth = size.width
        val canvasHeight = size.height
        val centerY = canvasHeight / 2

        if (showSpectrum && frequencies.isNotEmpty()) {
            drawFrequencySpectrum(
                frequencies = frequencies,
                color = spectrumColor,
                height = canvasHeight * spectrumHeight,
                phase = wavePhase
            )
        }

        // Draw waveform
        val barWidth = 2.dp.toPx()
        val barSpacing = 1.dp.toPx()
        val totalBars = (canvasWidth / (barWidth + barSpacing)).toInt()

        val normalizedPosition = (currentPosition * totalBars).toInt()

        val barHeights = if (isRecording) {
            generateRecordingBars(totalBars, wavePhase, canvasHeight)
        } else {
            normalizeAmplitudes(amplitudes, totalBars, canvasHeight)
        }

        // Draw bars with gradient
        val gradient = Brush.verticalGradient(
            colors = listOf(
                activeColor.copy(alpha = 0.8f),
                activeColor.copy(alpha = 0.3f)
            )
        )

        barHeights.forEachIndexed { index, height ->
            val x = index * (barWidth + barSpacing)
            val color = when {
                isRecording -> gradient
                index <= normalizedPosition -> gradient
                else -> Brush.verticalGradient(
                    colors = listOf(
                        inactiveColor.copy(alpha = 0.6f),
                        inactiveColor.copy(alpha = 0.2f)
                    )
                )
            }

            drawBar(
                x = x,
                centerY = centerY,
                width = barWidth,
                height = height,
                brush = color
            )
        }

        // Draw playback position indicator
        if (isPlaying && !isRecording) {
            drawPlaybackIndicator(
                x = normalizedPosition * (barWidth + barSpacing),
                height = canvasHeight,
                color = activeColor
            )
        }
    }
}

private fun DrawScope.drawFrequencySpectrum(
    frequencies: List<Float>,
    color: Color,
    height: Float,
    phase: Float
) {
    val width = size.width
    val centerY = size.height / 2
    val path = Path()
    val points = mutableListOf<Offset>()

    frequencies.forEachIndexed { index, frequency ->
        val x = (index.toFloat() / frequencies.size) * width
        val amplitude = frequency * height / 2
        val y = centerY + amplitude * sin(phase + index * 0.1f)
        points.add(Offset(x, y))
    }

    // Create smooth curve through points
    path.moveTo(points.first().x, points.first().y)
    for (i in 1 until points.size - 2) {
        val xControl = (points[i].x + points[i + 1].x) / 2
        val yControl = (points[i].y + points[i + 1].y) / 2
        path.quadraticBezierTo(
            points[i].x, points[i].y,
            xControl, yControl
        )
    }
    path.lineTo(points.last().x, points.last().y)

    drawPath(
        path = path,
        color = color,
        style = Stroke(
            width = 1.dp.toPx(),
            cap = StrokeCap.Round,
            join = StrokeJoin.Round
        )
    )
}

private fun DrawScope.drawBar(
    x: Float,
    centerY: Float,
    width: Float,
    height: Float,
    brush: Brush
) {
    drawRect(
        brush = brush,
        topLeft = Offset(x, centerY - height / 2),
        size = Size(width, height),
        alpha = 0.9f
    )
}

private fun DrawScope.drawPlaybackIndicator(
    x: Float,
    height: Float,
    color: Color
) {
    drawLine(
        color = color,
        start = Offset(x, 0f),
        end = Offset(x, height),
        strokeWidth = 2.dp.toPx(),
        pathEffect = PathEffect.dashPathEffect(
            intervals = floatArrayOf(4.dp.toPx(), 4.dp.toPx())
        )
    )
}

private fun generateRecordingBars(
    totalBars: Int,
    phase: Float,
    height: Float
): List<Float> {
    return List(totalBars) { index ->
        val baseAmplitude = (sin(phase + index * 0.1f) + 1) / 2
        val noise = (Random.nextFloat() - 0.5f) * 0.3f
        (baseAmplitude + noise).coerceIn(0.1f, 0.9f) * height * 0.8f
    }
}

private fun normalizeAmplitudes(
    amplitudes: List<Float>,
    totalBars: Int,
    height: Float
): List<Float> {
    if (amplitudes.isEmpty()) return List(totalBars) { 0f }
    
    val step = amplitudes.size.toFloat() / totalBars
    return List(totalBars) { index ->
        val pos = (index * step).toInt().coerceIn(0, amplitudes.size - 1)
        amplitudes[pos] * height * 0.8f
    }
} 