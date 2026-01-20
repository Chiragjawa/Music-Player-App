package com.example.musicplayer.presentation.screens

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.example.musicplayer.viewmodel.PlayerViewModel

@Composable
fun PlayerScreen(
    playerViewModel: PlayerViewModel
) {

    val song by playerViewModel.currentSong.collectAsState()
    val isPlaying by playerViewModel.isPlaying.collectAsState()

    if (song == null) {
        Text("No Song Playing")
        return
    }

    Column(
        modifier = Modifier.padding(16.dp)
    ) {
        Text(text = song!!.name)
        Text(text = song!!.artists)

        Spacer(modifier = Modifier.height(20.dp))

        Button(onClick = {
            playerViewModel.togglePlayPause()
        }) {
            Text(if (isPlaying) "Pause" else "Play")
        }
    }
}
