package com.example.musicplayercompose.ui.audioUI.common.bottomPlayer

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Pause
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.SkipNext
import androidx.compose.material.icons.filled.SkipPrevious
import androidx.compose.material3.Icon
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.unit.dp

@Composable
fun PlayBacks(
    isAudioPlaying: Boolean,
    onNext: () -> Unit,
    onStart: () -> Unit,
    onPrevious: () -> Unit,
) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = Modifier
            .height(70.dp) // Increased height for a larger touch area
            .padding(8.dp) // Added padding for better spacing around the Row
    ) {
        Icon(
            imageVector = Icons.Default.SkipPrevious,
            contentDescription = "Previous",
            modifier = Modifier
                .clickable { onPrevious() }
                .clip(CircleShape)
                .size(100.dp) // Increased size for better touch target
                .padding(12.dp) // Adjusted padding for visual appeal
        )

        Spacer(Modifier.size(32.dp)) // Increased space between icons

        Icon(
            imageVector = if (isAudioPlaying) Icons.Default.Pause else Icons.Default.PlayArrow,
            contentDescription = if (isAudioPlaying) "Pause" else "Play",
            modifier = Modifier
                .clickable { onStart() }
                .clip(CircleShape)
                .size(150.dp) // Increased size for better touch target
                .padding(12.dp)
        )

        Spacer(Modifier.size(32.dp)) // Increased space between icons

        Icon(
            imageVector = Icons.Default.SkipNext,
            contentDescription = "Next",
            modifier = Modifier
                .clickable { onNext() }
                .clip(CircleShape)
                .size(100.dp) // Increased size for better touch target
                .padding(12.dp)
        )
    }
}
