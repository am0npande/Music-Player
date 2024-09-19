package com.example.musicplayercompose.ui.audioUI

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.material3.Card
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.core.net.toUri
import androidx.hilt.navigation.compose.hiltViewModel
import com.example.musicplayercompose.data.local.model.Audio
import com.example.musicplayercompose.ui.audioUI.common.bottomPlayer.BottomPlayer
import com.example.musicplayercompose.ui.audioUI.common.bottomPlayer.SpotifyBottomSheet
import com.example.musicplayercompose.ui.theme.MusicPlayerComposeTheme
import kotlin.math.floor


@Composable
fun MusicScreen(
    progress: Float,
    onProgressChange: (Float) -> Unit,
    isAudioPlaying: Boolean,
    currentPlayingAudio: Audio,
    audioList: List<Audio>,
    onStart: () -> Unit,
    onItemClick: (Int) -> Unit,
    onNext: () -> Unit,
    onPrevious: () -> Unit,
    padddingValue: PaddingValues,
    viewModel: AudioViewModel = hiltViewModel()
) {

    Scaffold(
        modifier = Modifier.padding(paddingValues = padddingValue),
        bottomBar = {
            BottomPlayer(
                onClick = { viewModel.showBottomSheet = true },
//                progress = progress,
//                onProgressChange = onProgressChange,
                isAudioPlaying = isAudioPlaying,
                audio = currentPlayingAudio,
                onStart = onStart,
                onNext = onNext,
                onPrevious = onPrevious

            )
        }
    ) {
        LazyColumn(
            contentPadding = it
        ) {
            itemsIndexed(audioList) { index, item ->
                AudioItem(
                    audio = item,
                    onItemClick = { onItemClick(index) }
                )
            }
        }

        SpotifyBottomSheet( progress = progress, onProgressChange =  onProgressChange,isAudioPlaying = isAudioPlaying,onNext = onNext,onStart = onStart,onPrevious = onPrevious)

    }
}

@Composable
fun AudioItem(
    audio: Audio,
    onItemClick: () -> Unit,
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(12.dp)
            .clickable { onItemClick() }
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.padding(8.dp)
        ) {
            Column(
                modifier = Modifier
                    .weight(1f)
                    .padding(8.dp),
                verticalArrangement = Arrangement.Center
            ) {
                Spacer(Modifier.size(4.dp))

                Text(
                    text = audio.name,
                    style = MaterialTheme.typography.titleLarge,
                    overflow = TextOverflow.Clip,
                    maxLines = 1
                )
                Spacer(Modifier.size(4.dp))

                Text(
                    text = audio.artist,
                    style = MaterialTheme.typography.bodySmall,
                    overflow = TextOverflow.Clip,
                    maxLines = 1
                )
                Spacer(Modifier.size(4.dp))

                Text(
                    text = timeStampToDuration(audio.duration),
                    maxLines = 1
                )
                Spacer(Modifier.size(8.dp))
            }
        }
    }
}


//don't know what this is

fun timeStampToDuration(position: Long): String {
    val totalSeconds = floor(position / 1E3).toInt()
    val minutes = totalSeconds / 60
    val remainingSeconds = totalSeconds - (minutes * 60)
    return if (position < 0) "--:--" else "%d:%02d".format(minutes, remainingSeconds)
}


@Preview(showSystemUi = true)
@Composable
fun oree() {
    MusicPlayerComposeTheme {
        MusicScreen(
            progress = 50f,
            onProgressChange = {},
            isAudioPlaying = false,
            currentPlayingAudio = Audio("".toUri(), "", 0L, "", 0L, "", "",""),
            audioList = listOf(
                Audio("".toUri(), "one", 0L, "said", 0L, "", "",""),
                Audio("".toUri(), "two", 0L, "caid", 0L, "", "",""),
                Audio("".toUri(), "three", 0L, "red", 0L, "", "",""),
                Audio("".toUri(), "four", 0L, "tailor swift", 0L, "", "",""),


                ),
            onStart = {},
            onItemClick = {},
            onNext = {},
            onPrevious = {},
            padddingValue = PaddingValues()
        )
    }
}