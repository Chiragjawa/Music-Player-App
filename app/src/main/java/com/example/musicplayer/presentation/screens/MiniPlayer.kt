package com.example.musicplayer.presentation.screens

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.example.musicplayer.viewmodel.PlayerViewModel

@Composable
fun MiniPlayer(playerViewModel: PlayerViewModel) {

    val song by playerViewModel.currentSong.collectAsState()
    val isPlaying by playerViewModel.isPlaying.collectAsState()

    if (song == null) return

    Card(
        modifier = Modifier.fillMaxWidth()
    ) {
        Row(
            modifier = Modifier
                .padding(10.dp)
                .fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Column {
                Text(song!!.name)
                Text(song!!.artists)
            }

            Button(onClick = { playerViewModel.togglePlayPause() }) {
                Text(if (isPlaying) "Pause" else "Play")
            }
        }
    }
}
