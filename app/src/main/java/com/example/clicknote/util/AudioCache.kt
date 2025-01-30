package com.example.clicknote.util

import android.content.Context
import android.util.LruCache
import dagger.hilt.android.qualifiers.ApplicationContext
import java.io.File
import java.io.FileInputStream
import java.io.FileOutputStream
import java.nio.ByteBuffer
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class AudioCache @Inject constructor(
    @ApplicationContext private val context: Context
) {
    private val memoryCache = LruCache<String, FloatArray>(MAX_MEMORY_ITEMS)
    private val cacheDir = File(context.cacheDir, "audio_cache").apply { mkdirs() }

    fun get(key: String): FloatArray? {
        return memoryCache.get(key) ?: loadFromDisk(key)
    }

    fun put(key: String, audio: FloatArray) {
        memoryCache.put(key, audio)
        saveToDisk(key, audio)
    }

    fun clear() {
        memoryCache.evictAll()
        cacheDir.listFiles()?.forEach { it.delete() }
    }

    private fun loadFromDisk(key: String): FloatArray? {
        val file = File(cacheDir, key)
        if (!file.exists()) return null

        return try {
            FileInputStream(file).use { fis ->
                val size = fis.channel.size().toInt() / Float.SIZE_BYTES
                val buffer = ByteBuffer.allocate(size * Float.SIZE_BYTES)
                fis.channel.read(buffer)
                buffer.flip()
                FloatArray(size) { buffer.float }
            }
        } catch (e: Exception) {
            null
        }
    }

    private fun saveToDisk(key: String, audio: FloatArray) {
        val file = File(cacheDir, key)
        try {
            FileOutputStream(file).use { fos ->
                val buffer = ByteBuffer.allocate(audio.size * Float.SIZE_BYTES)
                audio.forEach { buffer.putFloat(it) }
                buffer.flip()
                fos.channel.write(buffer)
            }
        } catch (e: Exception) {
            // Handle error
        }
    }

    fun generateKey(file: File, sampleRate: Int): String {
        return "${file.name}_${file.lastModified()}_${sampleRate}"
    }

    companion object {
        private const val MAX_MEMORY_ITEMS = 10
    }
} 