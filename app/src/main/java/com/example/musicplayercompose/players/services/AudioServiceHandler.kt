package com.example.musicplayercompose.players.services

import android.annotation.SuppressLint
import androidx.media3.common.MediaItem
import androidx.media3.common.Player
import androidx.media3.exoplayer.ExoPlayer
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.DelicateCoroutinesApi
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

class AudioServiceHandler @Inject constructor(
    private val exoPlayer: ExoPlayer,
) : Player.Listener {


    private val _audioState: MutableStateFlow<AudioState> = MutableStateFlow(AudioState.initial)
    val audioState: StateFlow<AudioState> = _audioState.asStateFlow() //read only flow/ only observe


    private var job: Job? = null

    init {
        exoPlayer.addListener(this)
    }


    fun setMediaItem(mediaItem: List<MediaItem>) {
        exoPlayer.setMediaItems(mediaItem)
        exoPlayer.prepare()
    }


    suspend fun onPlayerEvents(
        playerEvent: PlayerEvent,
        selectedAudioIndex: Int = -1,
        seekPosition: Long = 0,
    ) {
        when (playerEvent) {
            PlayerEvent.Backward -> exoPlayer.seekBack()
            PlayerEvent.Forward -> exoPlayer.seekForward()
            PlayerEvent.PlayerPause -> PlayOrPause()
            PlayerEvent.SeekToNext -> exoPlayer.seekToNext()
            PlayerEvent.SeekToPrevious -> exoPlayer.seekToPrevious()
            PlayerEvent.Stop -> stopProgressUpdate()

            PlayerEvent.SeekTo -> exoPlayer.seekTo(seekPosition)
            is PlayerEvent.UpdateProgress -> {
                exoPlayer.seekTo(
                    (exoPlayer.duration * playerEvent.newProgress).toLong()
                )
            }

            PlayerEvent.SelectedAudioChange -> {
                when (selectedAudioIndex) {
                    exoPlayer.currentMediaItemIndex -> {
                        PlayOrPause()
                    }

                    else -> {
                        exoPlayer.seekToDefaultPosition(selectedAudioIndex)
                        _audioState.value = AudioState.Playing(true)
                        exoPlayer.playWhenReady = true
                        startProgressUpdate()
                    }
                }
            }
        }
    }

    private suspend fun PlayOrPause() {
        if (exoPlayer.isPlaying) {
            exoPlayer.pause()
            stopProgressUpdate()
        } else {
            exoPlayer.play()
            _audioState.value = AudioState.Playing(isplaying = true)
            startProgressUpdate()
        }
    }

    private suspend fun startProgressUpdate() = job.run {
        while (true) {
            delay(500)
            _audioState.value = AudioState.Progress(exoPlayer.currentPosition)
            _formattedProgress.value = formatTime(exoPlayer.currentPosition)
        }
    }

    private fun stopProgressUpdate() {
        job?.cancel()
        _audioState.value = AudioState.Playing(false)
    }

    //ovverriding methods

    override fun onPlaybackStateChanged(playbackState: Int) {
        when (playbackState) {
            ExoPlayer.STATE_BUFFERING -> _audioState.value =
                AudioState.Buffering(exoPlayer.currentPosition)

            ExoPlayer.STATE_READY -> _audioState.value = AudioState.ready(exoPlayer.duration)
        }
    }

    @OptIn(DelicateCoroutinesApi::class)
    override fun onIsPlayingChanged(isPlaying: Boolean) {
        _audioState.value = AudioState.Playing(isPlaying)
        _audioState.value = AudioState.CurrentPlaying(exoPlayer.currentMediaItemIndex)

        if (isPlaying) {
            GlobalScope.launch(Dispatchers.Main) {
                startProgressUpdate()
                startProgressTimer()
            }
        } else {
            stopProgressUpdate()
            stopProgressTimer()
        }
    }


    // Flow to emit formatted progress as 00:00
    private val _formattedProgress = MutableStateFlow("00:00")
    val formattedProgress: StateFlow<String> = _formattedProgress

    // Job for progress tracking
    private var progressJob: Job? = null





    @SuppressLint("DefaultLocale")
    fun formatTime(milliseconds: Long): String {
        val minutes = (milliseconds / 1000) / 60
        val seconds = (milliseconds / 1000) % 60
        return String.format("%02d:%02d", minutes, seconds)
    }

    fun startProgressTimer() {
        // Cancel any previous job
        progressJob?.cancel()

        // Create a new coroutine job
        progressJob = CoroutineScope(Dispatchers.Main).launch {
            while (true) {
                delay(1000L) // Update every 1 second
                val currentPosition = exoPlayer.currentPosition
                _formattedProgress.value = formatTime(currentPosition)
            }
        }
    }

    // Stop progress timer
    fun stopProgressTimer() {
        progressJob?.cancel()
        _formattedProgress.value = "00:00" // Reset progress display
    }

}


sealed class AudioState {
    object initial : AudioState()
    data class ready(val duration: Long) : AudioState()
    data class Progress(val progress: Long) : AudioState()
    data class Buffering(val progress: Long) : AudioState()
    data class Playing(val isplaying: Boolean) : AudioState()
    data class CurrentPlaying(val mediaItemIndex: Int) : AudioState()
}

sealed class PlayerEvent {
    object PlayerPause : PlayerEvent()
    object SelectedAudioChange : PlayerEvent()
    object Backward : PlayerEvent()
    object Forward : PlayerEvent()
    object SeekTo : PlayerEvent()
    object SeekToNext : PlayerEvent()
    object SeekToPrevious:PlayerEvent()
    object Stop : PlayerEvent()
    data class UpdateProgress(val newProgress: Float) : PlayerEvent()
}