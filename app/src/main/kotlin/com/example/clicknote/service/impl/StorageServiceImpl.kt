package com.example.clicknote.service.impl

import android.content.Context
import com.example.clicknote.service.StorageService
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.File
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class StorageServiceImpl @Inject constructor(
    @ApplicationContext private val context: Context
) : StorageService {

    private val audioDirectory: File by lazy {
        File(context.filesDir, "audio").apply {
            if (!exists()) {
                mkdirs()
            }
        }
    }

    override suspend fun saveAudioFile(file: File): String = withContext(Dispatchers.IO) {
        val destinationFile = File(audioDirectory, "audio_${System.currentTimeMillis()}.m4a")
        file.copyTo(destinationFile, overwrite = true)
        destinationFile.absolutePath
    }

    override suspend fun getAudioFile(path: String): File = withContext(Dispatchers.IO) {
        File(path).also {
            if (!it.exists()) {
                throw IllegalStateException("Audio file not found: $path")
            }
        }
    }

    override suspend fun deleteAudioFile(path: String) = withContext(Dispatchers.IO) {
        File(path).delete()
    }

    override suspend fun getAudioFileSize(path: String): Long = withContext(Dispatchers.IO) {
        File(path).length()
    }

    override suspend fun cleanup() = withContext(Dispatchers.IO) {
        audioDirectory.listFiles()?.forEach { it.delete() }
    }
} 