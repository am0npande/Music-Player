package com.example.musicplayercompose.ui.audioUI.common.bottomPlayer

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.Image
import androidx.compose.foundation.basicMarquee
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import coil.compose.rememberAsyncImagePainter
import com.example.musicplayercompose.ui.audioUI.AudioViewModel

@OptIn(ExperimentalMaterial3Api::class, ExperimentalFoundationApi::class)
@Composable
fun SpotifyBottomSheet(
    progress: Float,
    onProgressChange: (Float) -> Unit,
    viewModel: AudioViewModel = hiltViewModel(),
    isAudioPlaying: Boolean,
    onNext: () -> Unit,
    onStart: () -> Unit,
    onPrevious: () -> Unit,
) {
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
    val audio = viewModel.currentSelectedAudio.artWork

    if (viewModel.showBottomSheet) {
        // Using Scaffold to manage layout and padding
        Scaffold(
            modifier = Modifier.fillMaxSize(),
            content = { paddingValues -> // Passing padding values from Scaffold
                ModalBottomSheet(
                    dragHandle = null,
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(paddingValues), // Applying the padding here to avoid overlap
                    //.padding(top = WindowInsets.statusBars.asPaddingValues().calculateTopPadding()),
                    sheetState = sheetState,
                    onDismissRequest = { viewModel.showBottomSheet = false }
                ) {
                    Spacer(Modifier.height(50.dp))
                    Text(
                        textAlign = TextAlign.Center,
                        maxLines = 1,
                        modifier = Modifier
                            .padding(16.dp)
                            .fillMaxWidth(),
                        text = "Music by ${viewModel.currentSelectedAudio.artist}"
                    )
                    Column(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(16.dp), // Inner padding for content
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.Center
                    ) {
                        // Load and display the artwork from ExoPlayer
                        Image(
                            painter = rememberAsyncImagePainter(model = audio),
                            contentDescription = null,
                            modifier = Modifier
                                .size(400.dp)
                                .padding(vertical = 10.dp)
                        )

                        // Display song title
                        Text(
                            text = viewModel.currentSelectedAudio.title,
                            style = MaterialTheme.typography.titleLarge,
                            modifier = Modifier
                                .basicMarquee()
                                .padding(vertical = 4.dp)
                        )

                        // Progress bar
                        ProgressBar(progress = progress, onProgressChange = onProgressChange)
                        Spacer(Modifier.height(10.dp))

                        // Playback controls
                        PlayBacks(
                            isAudioPlaying = isAudioPlaying,
                            onNext = onNext,
                            onStart = onStart,
                            onPrevious = onPrevious
                        )
                        Spacer(Modifier.height(16.dp))
                    }
                }
            }
        )
    }
}