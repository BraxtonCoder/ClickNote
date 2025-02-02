package com.example.clicknote.service.impl

import android.content.Context
import android.util.LruCache
import com.example.clicknote.domain.interfaces.AmplitudeCache
import dagger.hilt.android.qualifiers.ApplicationContext
import java.io.*
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class AmplitudeCacheImpl @Inject constructor(
    @ApplicationContext context: Context
) : AmplitudeCache {
    private val amplitudes = mutableListOf<Float>()
    private val maxSize = 100
    @Volatile private var lastAmplitude = 0f
    private val memoryCache = LruCache<String, List<Float>>(maxSize)
    private val cacheDir = File(context.cacheDir, "amplitude_cache").apply { mkdirs() }

    override fun cacheAmplitude(amplitude: Float) {
        synchronized(amplitudes) {
            amplitudes.add(amplitude)
            if (amplitudes.size > maxSize) {
                amplitudes.removeAt(0)
            }
        }
    }

    override fun get(): List<Float> {
        synchronized(amplitudes) {
            return amplitudes.toList()
        }
    }

    override fun getLastAmplitude(): Float = lastAmplitude

    override fun clear() {
        synchronized(amplitudes) {
            amplitudes.clear()
        }
        lastAmplitude = 0f
        memoryCache.evictAll()
        cacheDir.listFiles()?.forEach { it.delete() }
    }

    override fun put(values: List<Float>) {
        synchronized(amplitudes) {
            amplitudes.clear()
            amplitudes.addAll(values.takeLast(maxSize))
            if (values.isNotEmpty()) {
                lastAmplitude = values.last()
            }
        }
        val key = values.hashCode().toString()
        memoryCache.put(key, values)
        saveToDisk(key, values)
    }

    fun getFromCache(key: String): List<Float>? {
        return memoryCache.get(key) ?: loadFromDisk(key)?.also {
            memoryCache.put(key, it)
        }
    }

    private fun saveToDisk(key: String, values: List<Float>) {
        val file = File(cacheDir, key)
        ObjectOutputStream(FileOutputStream(file)).use { out ->
            out.writeObject(values)
        }
    }

    private fun loadFromDisk(key: String): List<Float>? {
        val file = File(cacheDir, key)
        if (!file.exists()) return null

        return try {
            ObjectInputStream(FileInputStream(file)).use { input ->
                @Suppress("UNCHECKED_CAST")
                input.readObject() as? List<Float>
            }
        } catch (e: Exception) {
            null
        }
    }

    fun generateKey(audioId: Long, windowSize: Int, hopSize: Int): String {
        return "audio_${audioId}_w${windowSize}_h${hopSize}"
    }
} 