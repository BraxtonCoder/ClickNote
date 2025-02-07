package com.example.clicknote.util

import android.content.Context
import android.media.MediaExtractor
import android.media.MediaFormat
import dagger.hilt.android.qualifiers.ApplicationContext
import java.io.File
import java.nio.ByteBuffer
import javax.inject.Inject
import javax.inject.Singleton
import kotlin.math.*

@Singleton
class AudioFeatureExtractorImpl @Inject constructor(
    @ApplicationContext private val context: Context
) : AudioFeatureExtractor {
    private val sampleRate = 16000
    private val windowSize = 512
    private val hopLength = 256
    private val melBands = 80
    private val fMin = 0f
    private val fMax = 8000f

    override suspend fun extractFeatures(audioData: ByteArray): FloatArray {
        val floatData = convertByteArrayToFloat(audioData)
        val melSpectrogram = extractMelSpectrogram(floatData)
        return flattenMelSpectrogram(melSpectrogram)
    }

    override suspend fun compareFeatures(features1: FloatArray, features2: FloatArray): Float {
        return calculateCosineSimilarity(features1, features2)
    }

    private fun convertByteArrayToFloat(audioData: ByteArray): FloatArray {
        val shortArray = ShortArray(audioData.size / 2)
        ByteBuffer.wrap(audioData).asShortBuffer().get(shortArray)
        return FloatArray(shortArray.size) { i -> shortArray[i] / 32768f }
    }

    private fun extractMelSpectrogram(audioData: FloatArray): Array<FloatArray> {
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

    private fun flattenMelSpectrogram(melSpec: Array<FloatArray>): FloatArray {
        val result = FloatArray(melSpec.size * melSpec[0].size)
        var index = 0
        for (i in melSpec.indices) {
            for (j in melSpec[i].indices) {
                result[index++] = melSpec[i][j]
            }
        }
        return result
    }

    private fun computeSTFT(audioData: FloatArray): Array<FloatArray> {
        val numFrames = (audioData.size - windowSize) / hopLength + 1
        val fftSize = windowSize
        val stft = Array(numFrames) { FloatArray(fftSize / 2 + 1) }
        val window = createHannWindow()
        
        for (t in 0 until numFrames) {
            val startIdx = t * hopLength
            val frame = FloatArray(fftSize)
            
            for (n in 0 until minOf(windowSize, audioData.size - startIdx)) {
                frame[n] = audioData[startIdx + n] * window[n]
            }
            
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

    private fun calculateCosineSimilarity(v1: FloatArray, v2: FloatArray): Float {
        if (v1.isEmpty() || v2.isEmpty() || v1.size != v2.size) {
            return 0f
        }

        var dotProduct = 0f
        var norm1 = 0f
        var norm2 = 0f

        for (i in v1.indices) {
            dotProduct += v1[i] * v2[i]
            norm1 += v1[i] * v1[i]
            norm2 += v2[i] * v2[i]
        }

        if (norm1 <= 0 || norm2 <= 0) {
            return 0f
        }

        return dotProduct / (sqrt(norm1) * sqrt(norm2))
    }

    private fun hzToMel(hz: Float): Float = 
        2595 * log10(1 + hz / 700)

    private fun melToHz(mel: Float): Float = 
        700 * (pow(10f, mel / 2595) - 1)

    private fun computeFFT(input: FloatArray): FloatArray {
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