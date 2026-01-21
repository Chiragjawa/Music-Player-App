package com.example.musicplayer.presentation.screens

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.example.musicplayer.data.model.Song
import com.example.musicplayer.presentation.components.SongItem
import com.example.musicplayer.presentation.viewmodel.ArtistViewModel

@Composable
fun ArtistScreen(
    artistName: String,
    onPlaySong: (Song, List<Song>) -> Unit,
    viewModel: ArtistViewModel = hiltViewModel()
) {
    val songs by viewModel.songs.collectAsState()

    LaunchedEffect(artistName) {
        viewModel.loadArtist(artistName)
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {

        Text(
            text = artistName,
            style = MaterialTheme.typography.headlineMedium,
            fontWeight = FontWeight.Bold
        )

        Spacer(Modifier.height(4.dp))

        Text("${songs.size} Songs", color = Color.Gray)

        Spacer(Modifier.height(16.dp))

        Row {
            Button(
                onClick = {
                    if (songs.isNotEmpty()) onPlaySong(songs.first(), songs)
                }
            ) {
                Text("Play")
            }

            Spacer(Modifier.width(12.dp))

            OutlinedButton(
                onClick = {
                    if (songs.isNotEmpty()) {
                        val shuffled = songs.shuffled()
                        onPlaySong(shuffled.first(), shuffled)
                    }
                }
            ) {
                Text("Shuffle")
            }
        }

        Spacer(Modifier.height(24.dp))

        songs.forEach { song ->
            SongItem(
                song = song,
                onClick = { onPlaySong(song, songs) },
                onArtistClick = {}
            )
            Spacer(Modifier.height(8.dp))
        }
    }
}
