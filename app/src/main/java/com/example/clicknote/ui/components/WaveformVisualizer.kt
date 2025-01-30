package com.example.clicknote.ui.components

import androidx.compose.animation.core.*
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.*
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.StrokeJoin
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.unit.dp
import com.example.clicknote.domain.model.AudioAmplitude
import kotlin.math.abs
import kotlin.math.PI
import kotlin.math.sin
import kotlin.math.max
import kotlin.math.min

@Composable
fun WaveformVisualizer(
    amplitudes: List<Float>,
    modifier: Modifier = Modifier,
    activeColor: Color = MaterialTheme.colorScheme.primary,
    inactiveColor: Color = MaterialTheme.colorScheme.surfaceVariant,
    isRecording: Boolean = false
) {
    val transition = rememberInfiniteTransition()
    val animatedAlpha by transition.animateFloat(
        initialValue = 0.3f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(1000),
            repeatMode = RepeatMode.Reverse
        )
    )

    Canvas(
        modifier = modifier
            .fillMaxWidth()
            .height(60.dp)
    ) {
        val barWidth = 4.dp.toPx()
        val gap = 2.dp.toPx()
        val maxBars = (size.width / (barWidth + gap)).toInt()
        val usableWidth = maxBars * (barWidth + gap)
        val startX = (size.width - usableWidth) / 2

        val normalizedAmplitudes = amplitudes.map { amplitude ->
            val normalized = (amplitude + 1f) / 2f // Convert from [-1, 1] to [0, 1]
            normalized * size.height * 0.8f // Scale to 80% of height
        }

        val visibleAmplitudes = normalizedAmplitudes.takeLast(maxBars)
        
        visibleAmplitudes.forEachIndexed { index, amplitude ->
            val x = startX + index * (barWidth + gap)
            val barHeight = amplitude
            val yCenter = size.height / 2

            // Draw the bar
            drawLine(
                color = if (isRecording) activeColor.copy(alpha = animatedAlpha) else activeColor,
                start = Offset(x, yCenter - barHeight / 2),
                end = Offset(x, yCenter + barHeight / 2),
                strokeWidth = barWidth,
                cap = StrokeCap.Round
            )
        }

        // If we have fewer amplitudes than bars, draw inactive bars
        if (visibleAmplitudes.size < maxBars) {
            for (i in visibleAmplitudes.size until maxBars) {
                val x = startX + i * (barWidth + gap)
                val barHeight = 4.dp.toPx() // Minimal height for inactive bars
                val yCenter = size.height / 2

                drawLine(
                    color = inactiveColor,
                    start = Offset(x, yCenter - barHeight / 2),
                    end = Offset(x, yCenter + barHeight / 2),
                    strokeWidth = barWidth,
                    cap = StrokeCap.Round
                )
            }
        }
    }
}

@Composable
private fun animateAmplitudesAsState(
    targetAmplitudes: List<Float>
): State<List<Float>> {
    val animatedValues = remember { mutableStateListOf<Float>() }
    
    LaunchedEffect(targetAmplitudes) {
        // Initialize with zeros if needed
        while (animatedValues.size < targetAmplitudes.size) {
            animatedValues.add(0f)
        }
        // Remove excess values
        while (animatedValues.size > targetAmplitudes.size) {
            animatedValues.removeAt(animatedValues.lastIndex)
        }
        // Animate each value
        targetAmplitudes.forEachIndexed { index, target ->
            animate(
                initialValue = animatedValues.getOrNull(index) ?: 0f,
                targetValue = target,
                animationSpec = tween(
                    durationMillis = 100,
                    easing = FastOutSlowInEasing
                )
            ) { value, _ ->
                if (index < animatedValues.size) {
                    animatedValues[index] = value
                }
            }
        }
    }

    return remember { derivedStateOf { animatedValues.toList() } }
}

private fun DrawScope.drawLineWaveform(
    amplitudes: List<AudioAmplitude>,
    playedWidth: Float,
    centerY: Float,
    scaleFactor: Float,
    colors: WaveformColors
) {
    if (amplitudes.isEmpty()) return

    val playedPath = Path()
    val unplayedPath = Path()
    val pointSpacing = size.width / (amplitudes.size - 1)

    amplitudes.forEachIndexed { index, amplitude ->
        val x = index * pointSpacing
        val y = centerY * (1 - amplitude.value * scaleFactor)
        
        if (x <= playedWidth) {
            if (index == 0) playedPath.moveTo(x, y)
            else playedPath.lineTo(x, y)
        } else {
            if (x - pointSpacing <= playedWidth) {
                // Connect the paths at transition point
                val ratio = (playedWidth - (x - pointSpacing)) / pointSpacing
                val transitionX = x - pointSpacing + (ratio * pointSpacing)
                val prevAmplitude = amplitudes[index - 1].value
                val transitionY = centerY * (1 - (prevAmplitude + (amplitude.value - prevAmplitude) * ratio) * scaleFactor)
                playedPath.lineTo(transitionX, transitionY)
                unplayedPath.moveTo(transitionX, transitionY)
            }
            if (index == 0) unplayedPath.moveTo(x, y)
            else unplayedPath.lineTo(x, y)
        }
    }

    // Draw played path
    drawPath(
        path = playedPath,
        color = colors.playedColor,
        style = Stroke(width = 2.dp.toPx(), cap = StrokeCap.Round)
    )

    // Draw unplayed path
    drawPath(
        path = unplayedPath,
        color = colors.unplayedColor,
        style = Stroke(width = 2.dp.toPx(), cap = StrokeCap.Round)
    )
}

private fun DrawScope.drawBarWaveform(
    amplitudes: List<AudioAmplitude>,
    playedWidth: Float,
    barWidth: Float,
    barSpacing: Float,
    centerY: Float,
    scaleFactor: Float,
    colors: WaveformColors
) {
    val barCount = (size.width / (barWidth + barSpacing)).toInt()
    val samplesPerBar = (amplitudes.size / barCount).coerceAtLeast(1)

    for (i in 0 until barCount) {
        val startX = i * (barWidth + barSpacing)
        if (startX > size.width) break

        val startIndex = (i * samplesPerBar).coerceIn(0, amplitudes.size - 1)
        val endIndex = ((i + 1) * samplesPerBar).coerceIn(0, amplitudes.size)
        val amplitude = amplitudes.subList(startIndex, endIndex)
            .map { abs(it.value) }
            .average()
            .toFloat()

        val height = (amplitude * scaleFactor).coerceIn(2f, centerY)
        val color = if (startX <= playedWidth) colors.playedColor else colors.unplayedColor

        // Draw top bar
        drawLine(
            color = color,
            start = Offset(startX, centerY - height),
            end = Offset(startX, centerY),
            strokeWidth = barWidth,
            cap = StrokeCap.Round
        )

        // Draw bottom bar (mirrored)
        drawLine(
            color = color,
            start = Offset(startX, centerY),
            end = Offset(startX, centerY + height),
            strokeWidth = barWidth,
            cap = StrokeCap.Round
        )
    }
}

private fun DrawScope.drawMirrorWaveform(
    amplitudes: List<AudioAmplitude>,
    playedWidth: Float,
    centerY: Float,
    scaleFactor: Float,
    colors: WaveformColors
) {
    if (amplitudes.isEmpty()) return

    val playedPath = Path()
    val unplayedPath = Path()
    val pointSpacing = size.width / (amplitudes.size - 1)

    // Draw top half
    amplitudes.forEachIndexed { index, amplitude ->
        val x = index * pointSpacing
        val y = centerY * (1 - amplitude.value * scaleFactor)
        
        if (x <= playedWidth) {
            if (index == 0) playedPath.moveTo(x, y)
            else playedPath.lineTo(x, y)
        } else {
            if (x - pointSpacing <= playedWidth) {
                // Connect the paths at transition point
                val ratio = (playedWidth - (x - pointSpacing)) / pointSpacing
                val transitionX = x - pointSpacing + (ratio * pointSpacing)
                val prevAmplitude = amplitudes[index - 1].value
                val transitionY = centerY * (1 - (prevAmplitude + (amplitude.value - prevAmplitude) * ratio) * scaleFactor)
                playedPath.lineTo(transitionX, transitionY)
                unplayedPath.moveTo(transitionX, transitionY)
            }
            if (index == 0) unplayedPath.moveTo(x, y)
            else unplayedPath.lineTo(x, y)
        }
    }

    // Draw bottom half (mirrored)
    amplitudes.asReversed().forEachIndexed { index, amplitude ->
        val x = size.width - (index * pointSpacing)
        val y = centerY * (1 + amplitude.value * scaleFactor)
        
        if (x <= playedWidth) {
            playedPath.lineTo(x, y)
        } else {
            unplayedPath.lineTo(x, y)
        }
    }

    // Close the paths
    playedPath.close()
    unplayedPath.close()

    // Draw played path
    drawPath(
        path = playedPath,
        color = colors.playedColor,
        style = Stroke(width = 2.dp.toPx(), cap = StrokeCap.Round, join = StrokeJoin.Round)
    )

    // Draw unplayed path
    drawPath(
        path = unplayedPath,
        color = colors.unplayedColor,
        style = Stroke(width = 2.dp.toPx(), cap = StrokeCap.Round, join = StrokeJoin.Round)
    )
}

enum class WaveformStyle {
    Bar,
    Line,
    Mirror
}

data class WaveformColors(
    val playedColor: Color,
    val unplayedColor: Color,
    val positionLineColor: Color
) {
    companion object {
        val Default = WaveformColors(
            playedColor = Color(0xFF2196F3),
            unplayedColor = Color(0xFFBBDEFB),
            positionLineColor = Color(0xFF2196F3)
        )
        
        val Dark = WaveformColors(
            playedColor = Color(0xFF82B1FF),
            unplayedColor = Color(0xFF304FFE),
            positionLineColor = Color(0xFF82B1FF)
        )
        
        val Minimal = WaveformColors(
            playedColor = Color(0xFF000000),
            unplayedColor = Color(0xFFAAAAAA),
            positionLineColor = Color(0xFF000000)
        )
        
        val Vibrant = WaveformColors(
            playedColor = Color(0xFFFF4081),
            unplayedColor = Color(0xFFFF80AB),
            positionLineColor = Color(0xFFFF4081)
        )
    }
} 