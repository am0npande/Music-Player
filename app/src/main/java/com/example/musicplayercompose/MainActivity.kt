package com.example.musicplayercompose

import android.content.Intent
import android.os.Build
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.viewModels
import androidx.annotation.RequiresApi
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.basicMarquee
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalLifecycleOwner
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.TextUnit
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.media3.exoplayer.ExoPlayer
import com.example.musicplayercompose.players.services.AudioService
import com.example.musicplayercompose.ui.audioUI.AudioViewModel
import com.example.musicplayercompose.ui.audioUI.MusicScreen
import com.example.musicplayercompose.ui.audioUI.UIEvents
import com.example.musicplayercompose.ui.theme.MusicPlayerComposeTheme
import com.google.accompanist.permissions.ExperimentalPermissionsApi
import com.google.accompanist.permissions.rememberPermissionState
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject

@AndroidEntryPoint
class MainActivity : ComponentActivity() {

    private val viewModel: AudioViewModel by viewModels()
    private var isServiceRunning = false

    @Inject
    lateinit var exoPlayer: ExoPlayer

    override fun onDestroy() {
        exoPlayer.release()
        stopService(Intent(this, AudioService::class.java))
        super.onDestroy()
    }

    @RequiresApi(Build.VERSION_CODES.TIRAMISU)
    @OptIn(ExperimentalPermissionsApi::class, ExperimentalFoundationApi::class)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            MusicPlayerComposeTheme {
// Permission for audio files
                val audioPermissionState = rememberPermissionState(
                    permission = android.Manifest.permission.READ_MEDIA_AUDIO
                )

                // Permission for notifications (for Android 13+)
                val notificationPermissionState = rememberPermissionState(
                    permission = android.Manifest.permission.POST_NOTIFICATIONS
                )

                val lifeCycleOwner = LocalLifecycleOwner.current
                DisposableEffect(lifeCycleOwner) {
                    val observer = LifecycleEventObserver { _, event ->
                        if (event == Lifecycle.Event.ON_RESUME) {
                            // Request both audio and notification permissions
                            audioPermissionState.launchPermissionRequest()

                            notificationPermissionState.launchPermissionRequest()

                        }
                    }

                    lifeCycleOwner.lifecycle.addObserver(observer)
                    onDispose {
                        lifeCycleOwner.lifecycle.removeObserver(observer)
                    }
                }
                Scaffold(

                    modifier = Modifier.fillMaxSize(),
                    topBar = {
                        CubeLikeVerticalText(
                            text = viewModel.currentSelectedAudio.title,
                            modifier = Modifier.fillMaxWidth().basicMarquee(),
                            fontSize = 16.sp
                        )
                    }

                ) {
                    MusicScreen(
                        padddingValue = it,
                        progress = viewModel.progress,
                        onProgressChange = { viewModel.onUiEvents(UIEvents.SeekTo(it)) },
                        isAudioPlaying = viewModel.isPlaying,
                        currentPlayingAudio = viewModel.currentSelectedAudio,
                        audioList = viewModel.audioList,

                        onItemClick = {
                            viewModel.onUiEvents(UIEvents.SelectedAudioChange(it))
                            StartService()
                        },
                        onNext = { viewModel.onUiEvents(UIEvents.SeekToNext) },
                        onStart = { viewModel.onUiEvents(UIEvents.PlayPause) },
                        onPrevious = { viewModel.onUiEvents(UIEvents.SeekToPrev) }
                    )
                }
            }

        }
    }

    private fun StartService() {
        if (!isServiceRunning) {
            val intent = Intent(this, AudioService::class.java)
            startForegroundService(intent)

            isServiceRunning = true
        }
    }
}

@Composable
fun CubeLikeVerticalText(
    text: String,
    modifier: Modifier = Modifier,
    fontSize: TextUnit = 16.sp
) {
    // The composable to display animated content
    Box(
        modifier = modifier
            .fillMaxWidth()
            .padding(16.dp),
        contentAlignment = Alignment.Center
    ) {
        // Animate text with vertical slide effect
        AnimatedContent(
            targetState = text,
            transitionSpec = {
                (slideInVertically(initialOffsetY = { fullHeight -> -fullHeight }) + fadeIn()).togetherWith(
                    slideOutVertically(targetOffsetY = { fullHeight -> fullHeight }) + fadeOut()
                )
            }
        ) { targetText ->
            Text(
                text = targetText,
                fontSize = fontSize,
                fontWeight = FontWeight.Bold,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
        }
    }
}
