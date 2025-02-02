package com.example.clicknote.ui.components

import androidx.compose.animation.core.*
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.*
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.*
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.unit.dp
import kotlin.math.*

@Composable
fun EnhancedWaveformVisualizer(
    amplitudes: List<Float>,
    isRecording: Boolean = false,
    modifier: Modifier = Modifier
) {
    val primaryColor = MaterialTheme.colorScheme.primary
    val surfaceColor = MaterialTheme.colorScheme.surfaceVariant
    
    // Animate the recording indicator
    val infiniteTransition = rememberInfiniteTransition()
    val recordingAlpha by infiniteTransition.animateFloat(
        initialValue = 0.3f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(1000, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        )
    )

    // Animate each amplitude value with spring animation
    val animatedAmplitudes = amplitudes.mapIndexed { index, amplitude ->
        var animatedValue by remember(amplitude) { mutableStateOf(0f) }
        LaunchedEffect(amplitude) {
            animate(
                initialValue = animatedValue,
                targetValue = amplitude,
                animationSpec = spring(
                    dampingRatio = Spring.DampingRatioMediumBouncy,
                    stiffness = Spring.StiffnessLow
                )
            ) { value, _ -> animatedValue = value }
        }
        animatedValue
    }

    Canvas(
        modifier = modifier
            .fillMaxWidth()
            .height(120.dp)
    ) {
        val width = size.width
        val height = size.height
        val centerY = height / 2
        
        // Draw background grid
        drawBackgroundGrid(
            width = width,
            height = height,
            color = surfaceColor.copy(alpha = 0.1f)
        )

        if (animatedAmplitudes.isNotEmpty()) {
            val path = Path()
            val barWidth = width / (animatedAmplitudes.size + 1)
            val maxAmplitude = animatedAmplitudes.maxOrNull()?.coerceAtLeast(0.01f) ?: 0.01f
            val scaleFactor = (height / 2) * 0.8f

            // Create smooth waveform path
            path.moveTo(0f, centerY)
            animatedAmplitudes.forEachIndexed { index, amplitude ->
                val normalizedAmplitude = (amplitude / maxAmplitude)
                val x = barWidth * (index + 1)
                val y = centerY - (normalizedAmplitude * scaleFactor)
                
                if (index == 0) {
                    path.lineTo(x, y)
                } else {
                    val prevX = barWidth * index
                    val prevY = centerY - ((animatedAmplitudes[index - 1] / maxAmplitude) * scaleFactor)
                    val controlX1 = prevX + (x - prevX) / 3
                    val controlX2 = prevX + (x - prevX) * 2 / 3
                    path.cubicTo(
                        controlX1, prevY,
                        controlX2, y,
                        x, y
                    )
                }
            }

            // Mirror the path for the bottom half
            for (i in animatedAmplitudes.size - 1 downTo 0) {
                val amplitude = animatedAmplitudes[i]
                val normalizedAmplitude = (amplitude / maxAmplitude)
                val x = barWidth * (i + 1)
                val y = centerY + (normalizedAmplitude * scaleFactor)
                
                if (i == animatedAmplitudes.size - 1) {
                    path.lineTo(x, y)
                } else {
                    val nextX = barWidth * (i + 2)
                    val nextY = centerY + ((animatedAmplitudes[i + 1] / maxAmplitude) * scaleFactor)
                    val controlX1 = nextX - (nextX - x) * 2 / 3
                    val controlX2 = nextX - (nextX - x) / 3
                    path.cubicTo(
                        controlX1, nextY,
                        controlX2, y,
                        x, y
                    )
                }
            }
            path.close()

            // Draw the waveform with gradient fill and animated alpha
            val alpha = if (isRecording) recordingAlpha else 0.8f
            val gradient = Brush.verticalGradient(
                colors = listOf(
                    primaryColor.copy(alpha = alpha),
                    primaryColor.copy(alpha = alpha * 0.5f)
                )
            )
            drawPath(
                path = path,
                brush = gradient,
                alpha = alpha
            )

            // Draw path stroke
            drawPath(
                path = path,
                color = primaryColor.copy(alpha = alpha),
                style = Stroke(
                    width = 2.dp.toPx(),
                    cap = StrokeCap.Round,
                    join = StrokeJoin.Round
                )
            )
        } else if (isRecording) {
            // Draw idle recording animation
            drawIdleRecordingAnimation(
                width = width,
                height = height,
                color = primaryColor.copy(alpha = recordingAlpha)
            )
        }
    }
}

private fun DrawScope.drawBackgroundGrid(
    width: Float,
    height: Float,
    color: Color
) {
    val horizontalLineCount = 6
    val horizontalSpacing = height / (horizontalLineCount + 1)
    for (i in 1..horizontalLineCount) {
        val y = horizontalSpacing * i
        drawLine(
            color = color,
            start = Offset(0f, y),
            end = Offset(width, y),
            strokeWidth = 1.dp.toPx()
        )
    }

    val verticalLineCount = 20
    val verticalSpacing = width / (verticalLineCount + 1)
    for (i in 1..verticalLineCount) {
        val x = verticalSpacing * i
        drawLine(
            color = color,
            start = Offset(x, 0f),
            end = Offset(x, height),
            strokeWidth = 1.dp.toPx()
        )
    }
}

private fun DrawScope.drawIdleRecordingAnimation(
    width: Float,
    height: Float,
    color: Color
) {
    val centerY = height / 2
    val path = Path()
    val amplitude = height / 4
    val frequency = 2f * PI / width

    path.moveTo(0f, centerY)
    var x = 0f
    while (x <= width) {
        val y = centerY + (amplitude * sin(x * frequency)).toFloat()
        if (x == 0f) {
            path.moveTo(x, y)
        } else {
            path.lineTo(x, y)
        }
        x += 2f
    }

    drawPath(
        path = path,
        color = color,
        style = Stroke(
            width = 2.dp.toPx(),
            cap = StrokeCap.Round
        )
    )
}

@Composable
fun AudioPlaybackVisualizer(
    amplitudes: List<Float>,
    playbackPosition: Float,
    isPlaying: Boolean,
    modifier: Modifier = Modifier
) {
    val animatedPosition by animateFloatAsState(
        targetValue = playbackPosition,
        animationSpec = tween(durationMillis = 100)
    )
    
    val waveColor = MaterialTheme.colorScheme.primary
    val backgroundColor = MaterialTheme.colorScheme.surfaceVariant
    
    Canvas(
        modifier = modifier
            .fillMaxWidth()
            .height(60.dp)
    ) {
        val width = size.width
        val height = size.height
        val centerY = height / 2
        
        // Draw waveform background
        val path = Path().apply {
            moveTo(0f, centerY)
            amplitudes.forEachIndexed { index, amplitude ->
                val x = width * index / amplitudes.size
                val y = centerY + (height * 0.4f * amplitude)
                lineTo(x, y)
            }
            lineTo(width, centerY)
        }
        
        drawPath(
            path = path,
            color = backgroundColor.copy(alpha = 0.3f),
            style = Stroke(width = 2.dp.toPx())
        )
        
        // Draw played portion with gradient
        val playedPath = Path().apply {
            moveTo(0f, centerY)
            amplitudes.forEachIndexed { index, amplitude ->
                val x = width * index / amplitudes.size
                if (x <= width * animatedPosition) {
                    val y = centerY + (height * 0.4f * amplitude)
                    lineTo(x, y)
                }
            }
            lineTo(width * animatedPosition, centerY)
            close()
        }
        
        drawPath(
            path = playedPath,
            brush = Brush.verticalGradient(
                colors = listOf(
                    waveColor.copy(alpha = 0.8f),
                    waveColor.copy(alpha = 0.4f)
                )
            )
        )
    }
} 