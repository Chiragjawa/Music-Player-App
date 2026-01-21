package com.example.musicplayer.presentation.screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.example.musicplayer.data.model.Song
import com.example.musicplayer.presentation.components.SongItem
import com.example.musicplayer.presentation.viewmodel.AlbumViewModel

@Composable
fun AlbumScreen(
    albumName: String,
    onPlaySong: (Song, List<Song>) -> Unit,
    viewModel: AlbumViewModel = hiltViewModel()
) {
    val songs by viewModel.songs.collectAsState()
    val errorMessage by viewModel.errorMessage.collectAsState()
    val isLoading by viewModel.isLoading.collectAsState()

    val snackbarHostState = remember { SnackbarHostState() }

    LaunchedEffect(albumName) {
        viewModel.loadAlbum(albumName)
    }

    LaunchedEffect(errorMessage) {
        errorMessage?.let {
            snackbarHostState.showSnackbar(
                message = it,
                duration = SnackbarDuration.Short
            )
            viewModel.clearError()
        }
    }

    Scaffold(
        snackbarHost = { SnackbarHost(snackbarHostState) }
    ) { paddingValues ->
        if (isLoading) {
            Box(
                modifier = Modifier.fillMaxSize(),
                contentAlignment = Alignment.Center
            ) {
                CircularProgressIndicator()
            }
        } else {
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues)
                    .padding(16.dp),
                contentPadding = PaddingValues(bottom = 96.dp)
            ) {
                item {
                    Text(
                        text = albumName,
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
                }

                items(songs, key = { it.id }) { song ->
                    SongItem(
                        song = song,
                        onClick = { onPlaySong(song, songs) },
                        onArtistClick = {}
                    )
                    Spacer(Modifier.height(8.dp))
                }
            }
        }
    }
}
