package com.example.clicknote.service

import kotlinx.coroutines.flow.Flow
import java.io.File

interface AudioPlayerService {
    /**
     * Current playback position in milliseconds
     */
    val playbackPosition: Flow<Long>

    /**
     * Current playback speed
     */
    val playbackSpeed: Flow<Float>

    /**
     * Duration of the audio file in milliseconds
     */
    val duration: Flow<Long>

    /**
     * Start playing audio file
     */
    suspend fun play(audioFile: File)

    /**
     * Pause playback
     */
    suspend fun pause()

    /**
     * Resume playback
     */
    suspend fun resume()

    /**
     * Stop playback
     */
    suspend fun stop()

    /**
     * Seek to position
     */
    suspend fun seekTo(position: Long)

    /**
     * Set playback speed
     */
    suspend fun setSpeed(speed: Float)

    /**
     * Release resources
     */
    fun release()
}

enum class PlaybackState {
    IDLE,
    PLAYING,
    PAUSED,
    ERROR
}

data class PlaybackError(
    val code: Int,
    val message: String
) 