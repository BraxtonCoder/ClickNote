package com.example.clicknote.service.impl

import android.content.Context
import android.media.MediaPlayer
import com.example.clicknote.service.AudioPlayer
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.*
import kotlinx.coroutines.flow.*
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class AudioPlayerImpl @Inject constructor(
    @ApplicationContext private val context: Context
) : AudioPlayer {

    private var mediaPlayer: MediaPlayer? = null
    private var progressJob: Job? = null

    private val _isPlaying = MutableStateFlow(false)
    override val isPlaying: Flow<Boolean> = _isPlaying.asStateFlow()

    private val _progress = MutableStateFlow(0f)
    override val progress: Flow<Float> = _progress.asStateFlow()

    private val _duration = MutableStateFlow(0L)
    override val duration: Flow<Long> = _duration.asStateFlow()

    override suspend fun loadAudio(path: String) {
        release()
        mediaPlayer = MediaPlayer().apply {
            setDataSource(path)
            prepare()
            _duration.value = duration.toLong()
        }
    }

    override suspend fun play() {
        mediaPlayer?.let { player ->
            player.start()
            _isPlaying.value = true
            startProgressTracking()
        }
    }

    override suspend fun pause() {
        mediaPlayer?.let { player ->
            if (player.isPlaying) {
                player.pause()
                _isPlaying.value = false
                progressJob?.cancel()
            }
        }
    }

    override suspend fun seekTo(progress: Float) {
        mediaPlayer?.let { player ->
            val position = (player.duration * progress).toInt()
            player.seekTo(position)
            _progress.value = progress
        }
    }

    override suspend fun release() {
        progressJob?.cancel()
        mediaPlayer?.apply {
            if (isPlaying) {
                stop()
            }
            release()
        }
        mediaPlayer = null
        _isPlaying.value = false
        _progress.value = 0f
        _duration.value = 0L
    }

    private fun startProgressTracking() {
        progressJob?.cancel()
        progressJob = CoroutineScope(Dispatchers.IO).launch {
            while (isActive) {
                mediaPlayer?.let { player ->
                    if (player.isPlaying) {
                        _progress.value = player.currentPosition.toFloat() / player.duration
                    }
                }
                delay(100)
            }
        }
    }
} 