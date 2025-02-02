package com.example.clicknote.service.audio

import android.content.Context
import android.media.MediaPlayer
import dagger.hilt.android.qualifiers.ApplicationContext
import java.io.File
import javax.inject.Inject
import javax.inject.Singleton

interface AudioPlayer {
    suspend fun play(file: File)
    suspend fun pause()
    suspend fun resume()
    suspend fun stop()
    fun isPlaying(): Boolean
    fun getCurrentPosition(): Int
    fun getDuration(): Int
    fun seekTo(position: Int)
}

@Singleton
class AudioPlayerImpl @Inject constructor(
    @ApplicationContext private val context: Context
) : AudioPlayer {
    private var mediaPlayer: MediaPlayer? = null
    private var currentFile: File? = null

    override suspend fun play(file: File) {
        if (currentFile?.path == file.path && mediaPlayer?.isPlaying == true) {
            return
        }

        stop()
        mediaPlayer = MediaPlayer().apply {
            setDataSource(file.path)
            prepare()
            start()
        }
        currentFile = file
    }

    override suspend fun pause() {
        mediaPlayer?.let {
            if (it.isPlaying) {
                it.pause()
            }
        }
    }

    override suspend fun resume() {
        mediaPlayer?.let {
            if (!it.isPlaying) {
                it.start()
            }
        }
    }

    override suspend fun stop() {
        mediaPlayer?.let {
            if (it.isPlaying) {
                it.stop()
            }
            it.release()
        }
        mediaPlayer = null
        currentFile = null
    }

    override fun isPlaying(): Boolean = mediaPlayer?.isPlaying ?: false

    override fun getCurrentPosition(): Int = mediaPlayer?.currentPosition ?: 0

    override fun getDuration(): Int = mediaPlayer?.duration ?: 0

    override fun seekTo(position: Int) {
        mediaPlayer?.seekTo(position)
    }
} 