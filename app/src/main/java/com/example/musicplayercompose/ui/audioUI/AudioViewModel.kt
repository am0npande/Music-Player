package com.example.musicplayercompose.ui.audioUI

import android.annotation.SuppressLint
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableLongStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.core.net.toUri
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.compose.SavedStateHandleSaveableApi
import androidx.lifecycle.viewmodel.compose.saveable
import androidx.media3.common.MediaItem
import androidx.media3.common.MediaMetadata
import com.example.musicplayercompose.data.local.model.Audio
import com.example.musicplayercompose.data.repository
import com.example.musicplayercompose.players.services.AudioServiceHandler
import com.example.musicplayercompose.players.services.AudioState
import com.example.musicplayercompose.players.services.PlayerEvent
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch
import java.util.concurrent.TimeUnit
import javax.inject.Inject

private val audioDummy = Audio("".toUri(), "", 0L, "", 0L, "", "", "")

@OptIn(SavedStateHandleSaveableApi::class)
@HiltViewModel
class AudioViewModel @Inject constructor(
    private val audioServiceHandler: AudioServiceHandler,
    private val repository: repository,
    saveStateHandle: SavedStateHandle,
) : ViewModel() {
    // Expose the formatted progress from the AudioServiceHandler to the UI
    val formattedProgress: StateFlow<String> = audioServiceHandler.formattedProgress

    var showBottomSheet by saveStateHandle.saveable { mutableStateOf(false) }

    var duration by saveStateHandle.saveable { mutableLongStateOf(0L) }
    var progress by saveStateHandle.saveable { mutableFloatStateOf(0f) }

    var progressString by saveStateHandle.saveable { mutableStateOf("00:00") }
    var isPlaying by saveStateHandle.saveable { mutableStateOf(false) }
    var currentSelectedAudio by saveStateHandle.saveable { mutableStateOf(audioDummy) }
    var audioList by saveStateHandle.saveable { mutableStateOf(listOf<Audio>()) }

    private val _uiState = MutableStateFlow<UIState>(UIState.Initial)
    val uiState = _uiState.asStateFlow()

    init {
        loadAudioData()
    }

    init {
        viewModelScope.launch {
            audioServiceHandler.audioState.collectLatest {
                when (it) {
                    is AudioState.Buffering -> calculateProgressValue(it.progress)
                    is AudioState.CurrentPlaying -> {
                        currentSelectedAudio = audioList[it.mediaItemIndex]
                    }

                    is AudioState.Playing -> isPlaying = it.isplaying
                    is AudioState.Progress -> calculateProgressValue(it.progress)

                    AudioState.initial -> _uiState.value = UIState.Initial

                    is AudioState.ready -> {
                        duration = it.duration
                        _uiState.value = UIState.Ready
                    }
                }
            }
        }
    }

    fun onUiEvents(uiEvent: UIEvents) = viewModelScope.launch {
        when (uiEvent) {
            UIEvents.BackWard -> audioServiceHandler.onPlayerEvents(PlayerEvent.Backward)
            UIEvents.ForWard -> audioServiceHandler.onPlayerEvents(PlayerEvent.Forward)
            UIEvents.PlayPause -> audioServiceHandler.onPlayerEvents(PlayerEvent.PlayerPause)
            is UIEvents.SeekTo -> {
                audioServiceHandler.onPlayerEvents(
                    PlayerEvent.SeekTo,
                    seekPosition = ((duration * uiEvent.position) / 100f).toLong()
                )
            }

            UIEvents.SeekToNext -> audioServiceHandler.onPlayerEvents(PlayerEvent.SeekToNext)
            is UIEvents.SelectedAudioChange -> {
                audioServiceHandler.onPlayerEvents(
                    PlayerEvent.SelectedAudioChange,
                    selectedAudioIndex = uiEvent.index
                )
            }

            is UIEvents.UpdateProgress -> {
                audioServiceHandler.onPlayerEvents(PlayerEvent.UpdateProgress(uiEvent.newProgress))
                progress = uiEvent.newProgress
            }

            UIEvents.SeekToPrev -> audioServiceHandler.onPlayerEvents(PlayerEvent.SeekToPrevious)
        }
    }

    private fun loadAudioData() {
        viewModelScope.launch {
            val audio = repository.getAudioDataa()
            audioList = audio
            setMediaItems()
        }
    }

    private fun setMediaItems() {
        viewModelScope.launch {
            audioList.map {
                MediaItem.Builder()
                    .setUri(it.uri)
                    .setMediaMetadata(
                        MediaMetadata.Builder()
                            .setAlbumArtist(it.artist)
                            .setDisplayTitle(it.title)
                            .setSubtitle(it.name)
                            .setArtworkUri(it.artWork.toUri())
                            .build()

                    ).build()
            }.also {
                audioServiceHandler.setMediaItem(it)
            }
        }
    }

    private fun calculateProgressValue(currentProgress: Long) {
        progress = if (currentProgress > 0) (currentProgress.toFloat() / duration.toFloat()) * 100f
        else 0f

        progressString = formatDuration(currentProgress)
    }

    @SuppressLint("DefaultLocale")
    private fun formatDuration(duration: Long): String {
        val minute = TimeUnit.MINUTES.convert(duration, TimeUnit.MILLISECONDS)
        val seconds = (minute) - minute * TimeUnit.SECONDS.convert(1, TimeUnit.MINUTES)

        return String.format("%02d:%02d", minute, seconds)
    }


    override fun onCleared() {
        viewModelScope.launch {
            audioServiceHandler.onPlayerEvents(PlayerEvent.Stop)
        }
        super.onCleared()
    }

}


sealed class UIEvents {
    object PlayPause : UIEvents()
    data class SelectedAudioChange(val index: Int) : UIEvents()
    data class SeekTo(val position: Float) : UIEvents()
    object SeekToNext : UIEvents()
    object SeekToPrev : UIEvents()
    object BackWard : UIEvents()
    object ForWard : UIEvents()
    data class UpdateProgress(val newProgress: Float) : UIEvents()

}


sealed class UIState {
    object Initial : UIState()
    object Ready : UIState()
}
