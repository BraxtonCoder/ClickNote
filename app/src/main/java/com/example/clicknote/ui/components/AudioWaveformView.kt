package com.example.clicknote.ui.components

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.unit.dp
import kotlin.math.abs

@Composable
fun AudioWaveformView(
    amplitudes: List<Float>,
    isRecording: Boolean,
    modifier: Modifier = Modifier,
    barWidth: Float = 4f,
    barSpacing: Float = 2f,
    minBarHeight: Float = 4f,
    color: Color = Color(0xFFE91E63)
) {
    // Calculate the number of bars that can fit in the view
    val maxBars = remember(barWidth, barSpacing) {
        (1000 / (barWidth + barSpacing)).toInt()
    }

    // Take only the most recent amplitudes that fit in the view
    val visibleAmplitudes = remember(amplitudes, maxBars) {
        amplitudes.takeLast(maxBars)
    }

    // Animate the alpha of the waveform based on recording state
    val alpha by animateFloatAsState(
        targetValue = if (isRecording) 1f else 0.6f,
        animationSpec = tween(durationMillis = 500),
        label = "alpha"
    )

    Canvas(
        modifier = modifier
            .fillMaxWidth()
            .height(120.dp)
    ) {
        val centerY = size.height / 2
        val maxAmplitude = visibleAmplitudes.maxOfOrNull { abs(it) } ?: 1f
        val scaleFactor = size.height / 3 / maxAmplitude

        visibleAmplitudes.forEachIndexed { index, amplitude ->
            val x = size.width - (visibleAmplitudes.size - index) * (barWidth + barSpacing)
            if (x >= 0) {
                val barHeight = (abs(amplitude) * scaleFactor).coerceAtLeast(minBarHeight)
                drawBar(
                    x = x,
                    centerY = centerY,
                    barWidth = barWidth,
                    barHeight = barHeight,
                    color = color.copy(alpha = alpha)
                )
            }
        }
    }
}

private fun DrawScope.drawBar(
    x: Float,
    centerY: Float,
    barWidth: Float,
    barHeight: Float,
    color: Color
) {
    drawLine(
        color = color,
        start = Offset(x, centerY - barHeight / 2),
        end = Offset(x, centerY + barHeight / 2),
        strokeWidth = barWidth,
        cap = StrokeCap.Round
    )
} 