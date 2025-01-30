package com.example.clicknote.util

import android.media.MediaExtractor
import android.media.MediaFormat
import android.util.Log
import java.io.File
import java.nio.ByteBuffer
import javax.inject.Inject
import javax.inject.Singleton
import kotlin.math.*

@Singleton
class AudioFeatureExtractor @Inject constructor() {
    private val sampleRate = 16000
    private val windowSize = 512
    private val hopLength = 256
    private val melBands = 80
    private val fMin = 0f
    private val fMax = 8000f

    data class AudioSegment(
        val startTime: Double,
        val endTime: Double,
        val audioData: FloatArray
    ) {
        override fun equals(other: Any?): Boolean {
            if (this === other) return true
            if (javaClass != other?.javaClass) return false

            other as AudioSegment
            return startTime == other.startTime &&
                   endTime == other.endTime &&
                   audioData.contentEquals(other.audioData)
        }

        override fun hashCode(): Int {
            var result = startTime.hashCode()
            result = 31 * result + endTime.hashCode()
            result = 31 * result + audioData.contentHashCode()
            return result
        }
    }

    fun segmentAudio(audioFile: File, minSegmentDuration: Double): List<AudioSegment> {
        val segments = mutableListOf<AudioSegment>()
        val audioData = loadAudioData(audioFile)
        
        // Calculate energy for each window
        val energies = calculateEnergies(audioData)
        val silenceThreshold = calculateSilenceThreshold(energies)
        
        var segmentStart = 0
        var inSilence = true
        var silenceStart = 0
        
        for (i in energies.indices) {
            val timeInSeconds = i * hopLength / sampleRate.toDouble()
            val sampleIndex = i * hopLength
            
            if (energies[i] < silenceThreshold) {
                if (!inSilence) {
                    silenceStart = sampleIndex
                    inSilence = true
                }
            } else {
                if (inSilence) {
                    val silenceDuration = (sampleIndex - silenceStart) / sampleRate.toDouble()
                    if (silenceDuration >= minSegmentDuration && segmentStart < silenceStart) {
                        val segmentData = audioData.copyOfRange(segmentStart, silenceStart)
                        segments.add(
                            AudioSegment(
                                startTime = segmentStart / sampleRate.toDouble(),
                                endTime = silenceStart / sampleRate.toDouble(),
                                audioData = segmentData
                            )
                        )
                        segmentStart = sampleIndex
                    }
                    inSilence = false
                }
            }
        }
        
        // Add final segment if needed
        if (segmentStart < audioData.size) {
            val segmentData = audioData.copyOfRange(segmentStart, audioData.size)
            segments.add(
                AudioSegment(
                    startTime = segmentStart / sampleRate.toDouble(),
                    endTime = audioData.size / sampleRate.toDouble(),
                    audioData = segmentData
                )
            )
        }
        
        return segments
    }

    private fun loadAudioData(audioFile: File): FloatArray {
        val extractor = MediaExtractor()
        extractor.setDataSource(audioFile.path)
        extractor.selectTrack(0)
        
        val format = extractor.getTrackFormat(0)
        val duration = format.getLong(MediaFormat.KEY_DURATION)
        val buffer = ByteBuffer.allocate(duration.toInt())
        
        var offset = 0
        while (extractor.readSampleData(buffer, offset) >= 0) {
            offset += extractor.readSampleData(buffer, offset)
            extractor.advance()
        }
        
        buffer.rewind()
        val floatArray = FloatArray(buffer.remaining() / 2)
        buffer.asShortBuffer().get(ShortArray(floatArray.size)).forEachIndexed { index, value ->
            floatArray[index] = value / 32768f
        }
        
        extractor.release()
        return floatArray
    }

    private fun calculateEnergies(audioData: FloatArray): FloatArray {
        val numFrames = (audioData.size - windowSize) / hopLength + 1
        val energies = FloatArray(numFrames)
        
        for (i in 0 until numFrames) {
            val startIdx = i * hopLength
            val endIdx = minOf(startIdx + windowSize, audioData.size)
            var energy = 0f
            
            for (j in startIdx until endIdx) {
                energy += audioData[j] * audioData[j]
            }
            
            energies[i] = sqrt(energy / (endIdx - startIdx))
        }
        
        return energies
    }

    private fun calculateSilenceThreshold(energies: FloatArray): Float {
        val sortedEnergies = energies.sorted()
        val percentile15 = sortedEnergies[(sortedEnergies.size * 0.15).toInt()]
        val percentile85 = sortedEnergies[(sortedEnergies.size * 0.85).toInt()]
        
        return percentile15 + (percentile85 - percentile15) * 0.1f
    }

    fun extractMelSpectrogram(audioData: FloatArray): Array<FloatArray> {
        val stft = computeSTFT(audioData)
        val melFilterbank = createMelFilterbank()
        val melSpec = Array(stft.size) { FloatArray(melBands) }
        
        for (t in stft.indices) {
            for (m in 0 until melBands) {
                var sum = 0f
                for (f in melFilterbank[m].indices) {
                    sum += stft[t][f] * melFilterbank[m][f]
                }
                melSpec[t][m] = if (sum > 0) ln(sum) else 0f
            }
        }
        
        return melSpec
    }

    private fun computeSTFT(audioData: FloatArray): Array<FloatArray> {
        val numFrames = (audioData.size - windowSize) / hopLength + 1
        val fftSize = windowSize
        val stft = Array(numFrames) { FloatArray(fftSize / 2 + 1) }
        val window = createHannWindow()
        
        for (t in 0 until numFrames) {
            val startIdx = t * hopLength
            val frame = FloatArray(fftSize)
            
            // Apply window function
            for (n in 0 until minOf(windowSize, audioData.size - startIdx)) {
                frame[n] = audioData[startIdx + n] * window[n]
            }
            
            // Compute magnitude spectrum
            val spectrum = computeFFT(frame)
            System.arraycopy(spectrum, 0, stft[t], 0, spectrum.size)
        }
        
        return stft
    }

    private fun createHannWindow(): FloatArray {
        return FloatArray(windowSize) { i ->
            0.5f * (1 - cos(2 * PI * i / (windowSize - 1)))
        }
    }

    private fun createMelFilterbank(): Array<FloatArray> {
        val fftSize = windowSize
        val numBins = fftSize / 2 + 1
        val filterbank = Array(melBands) { FloatArray(numBins) }
        
        val melMin = hzToMel(fMin)
        val melMax = hzToMel(fMax)
        val melPoints = FloatArray(melBands + 2)
        
        for (i in melPoints.indices) {
            melPoints[i] = melMin + i * (melMax - melMin) / (melBands + 1)
        }
        
        val freqPoints = melPoints.map { melToHz(it) }
        val bins = freqPoints.map { freq -> 
            ((fftSize + 1) * freq / sampleRate).roundToInt()
        }
        
        for (m in 0 until melBands) {
            for (k in bins[m] until bins[m + 2]) {
                if (k < numBins) {
                    if (k < bins[m + 1]) {
                        filterbank[m][k] = (k - bins[m]).toFloat() / (bins[m + 1] - bins[m])
                    } else {
                        filterbank[m][k] = (bins[m + 2] - k).toFloat() / (bins[m + 2] - bins[m + 1])
                    }
                }
            }
        }
        
        return filterbank
    }

    private fun hzToMel(hz: Float): Float = 
        2595 * log10(1 + hz / 700)

    private fun melToHz(mel: Float): Float = 
        700 * (pow(10f, mel / 2595) - 1)

    private fun computeFFT(input: FloatArray): FloatArray {
        // Simple magnitude spectrum calculation
        // In a real implementation, you would use a proper FFT library
        val spectrum = FloatArray(input.size / 2 + 1)
        for (k in spectrum.indices) {
            var realSum = 0f
            var imagSum = 0f
            val omega = 2 * PI * k / input.size
            
            for (n in input.indices) {
                realSum += input[n] * cos(omega * n)
                imagSum -= input[n] * sin(omega * n)
            }
            
            spectrum[k] = sqrt(realSum * realSum + imagSum * imagSum)
        }
        return spectrum
    }

    companion object {
        private const val PI = Math.PI.toFloat()
    }
} 