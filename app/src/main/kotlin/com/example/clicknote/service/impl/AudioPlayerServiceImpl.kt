package com.example.clicknote.service.impl

import android.media.MediaPlayer
import android.media.PlaybackParams
import android.os.Build
import com.example.clicknote.service.AudioPlayerService
import kotlinx.coroutines.*
import kotlinx.coroutines.flow.*
import java.io.File
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class AudioPlayerServiceImpl @Inject constructor() : AudioPlayerService {

    private var mediaPlayer: MediaPlayer? = null
    private var playbackJob: Job? = null

    private val _playbackPosition = MutableStateFlow(0L)
    override val playbackPosition: Flow<Long> = _playbackPosition.asStateFlow()

    private val _playbackSpeed = MutableStateFlow(1f)
    override val playbackSpeed: Flow<Float> = _playbackSpeed.asStateFlow()

    private val _duration = MutableStateFlow(0L)
    override val duration: Flow<Long> = _duration.asStateFlow()

    override suspend fun play(audioFile: File) {
        release()
        mediaPlayer = MediaPlayer().apply {
            setDataSource(audioFile.absolutePath)
            prepare()
            _duration.value = duration.toLong()
            start()
            startPositionTracking()
        }
    }

    override suspend fun pause() {
        mediaPlayer?.let { player ->
            if (player.isPlaying) {
                player.pause()
                playbackJob?.cancel()
            }
        }
    }

    override suspend fun resume() {
        mediaPlayer?.let { player ->
            if (!player.isPlaying) {
                player.start()
                startPositionTracking()
            }
        }
    }

    override suspend fun stop() {
        mediaPlayer?.let { player ->
            if (player.isPlaying) {
                player.stop()
                playbackJob?.cancel()
                _playbackPosition.value = 0
            }
        }
    }

    override suspend fun seekTo(position: Long) {
        mediaPlayer?.let { player ->
            player.seekTo(position.toInt())
            _playbackPosition.value = position
        }
    }

    override suspend fun setSpeed(speed: Float) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            mediaPlayer?.let { player ->
                player.playbackParams = PlaybackParams().setSpeed(speed)
                _playbackSpeed.value = speed
            }
        }
    }

    override fun release() {
        playbackJob?.cancel()
        mediaPlayer?.apply {
            if (isPlaying) {
                stop()
            }
            release()
        }
        mediaPlayer = null
        _playbackPosition.value = 0
        _playbackSpeed.value = 1f
        _duration.value = 0
    }

    private fun startPositionTracking() {
        playbackJob?.cancel()
        playbackJob = CoroutineScope(Dispatchers.IO).launch {
            while (isActive) {
                mediaPlayer?.let { player ->
                    if (player.isPlaying) {
                        _playbackPosition.value = player.currentPosition.toLong()
                    }
                }
                delay(100)
            }
        }
    }
} 