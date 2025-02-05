package com.example.clicknote.data.audio

import com.example.clicknote.domain.audio.AmplitudeProcessor
import java.nio.ByteBuffer
import java.nio.ByteOrder
import javax.inject.Inject
import javax.inject.Singleton
import kotlin.math.sqrt

@Singleton
class AmplitudeProcessorImpl @Inject constructor() : AmplitudeProcessor {
    private var maxAmplitude: Int = 0

    override fun processAmplitude(buffer: ByteArray, readSize: Int): Int {
        val shorts = ShortArray(readSize / 2)
        ByteBuffer.wrap(buffer, 0, readSize)
            .order(ByteOrder.LITTLE_ENDIAN)
            .asShortBuffer()
            .get(shorts)

        var sum = 0
        for (value in shorts) {
            sum += abs(value.toInt())
        }
        val amplitude = if (readSize > 0) sum / readSize else 0
        maxAmplitude = maxOf(maxAmplitude, amplitude)
        return amplitude
    }

    override fun calculateRms(buffer: ByteArray, readSize: Int): Float {
        val shorts = ShortArray(readSize / 2)
        ByteBuffer.wrap(buffer, 0, readSize)
            .order(ByteOrder.LITTLE_ENDIAN)
            .asShortBuffer()
            .get(shorts)

        var sum = 0.0f
        for (value in shorts) {
            sum += value * value
        }
        return sqrt(sum / shorts.size)
    }

    override fun getMaxAmplitude(): Int = maxAmplitude

    override fun reset() {
        maxAmplitude = 0
    }

    private fun abs(value: Int): Int = if (value < 0) -value else value
} 