package com.example.musicplayercompose.ui.audioUI.common.bottomPlayer

import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.Slider
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.example.musicplayercompose.ui.audioUI.AudioViewModel
import com.example.musicplayercompose.ui.audioUI.timeStampToDuration

@Composable
fun ProgressBar(
    progress: Float,
    onProgressChange: (Float) -> Unit,
    viewModel: AudioViewModel = hiltViewModel()
    ) {

    val initialString by viewModel.formattedProgress.collectAsState()

    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = Modifier
            .fillMaxWidth()
            .height(50.dp)
            .padding(5.dp)
    ) {

        Text(modifier = Modifier, text = initialString)

        Spacer(Modifier.size(5.dp))

        Slider(
            value = progress,
            onValueChange = { onProgressChange(it) },
            valueRange = 0f..100f,
            modifier = Modifier.weight(1f)
        )
        Spacer(Modifier.size(5.dp))

        Text(modifier = Modifier, text = timeStampToDuration(viewModel.duration))

    }

}