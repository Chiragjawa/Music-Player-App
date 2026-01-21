package com.example.musicplayer.presentation.screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.example.musicplayer.presentation.components.MiniPlayer
import com.example.musicplayer.presentation.components.SongItem
import com.example.musicplayer.viewmodel.ArtistViewModel
import com.example.musicplayer.viewmodel.MusicPlayerViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ArtistScreen(
    artistName: String,
    artistViewModel: ArtistViewModel,
    playerViewModel: MusicPlayerViewModel,
    onBack: () -> Unit,
    onNavigateToPlayer: () -> Unit
) {
    val songs by artistViewModel.songs.collectAsState()
    val loading by artistViewModel.isLoading.collectAsState()
    val error by artistViewModel.error.collectAsState()
    val resolvedArtistName by artistViewModel.artistName.collectAsState()
    val playerState by playerViewModel.playerState.collectAsState()

    /**
     * Load artist data ONCE when screen opens
     */
    LaunchedEffect(artistName) {
        artistViewModel.loadArtistSongs(artistName)
    }

    /**
     * Cleanup when leaving screen
     */
    DisposableEffect(Unit) {
        onDispose {
            artistViewModel.clearData()
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = resolvedArtistName.ifEmpty { artistName },
                        color = Color.White,
                        fontWeight = FontWeight.Bold,
                        maxLines = 1
                    )
                },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Back",
                            tint = Color.White
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = Color(0xFF121212)
                )
            )
        },
        bottomBar = {
            if (playerState.currentSong != null) {
                MiniPlayer(
                    song = playerState.currentSong!!,
                    isPlaying = playerState.isPlaying,
                    onPlayPause = { playerViewModel.playPause() },
                    onClick = onNavigateToPlayer
                )
            }
        },
        containerColor = Color(0xFF121212)
    ) { padding ->

        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(16.dp)
        ) {

            when {
                loading -> {
                    Box(
                        modifier = Modifier.fillMaxSize(),
                        contentAlignment = Alignment.Center
                    ) {
                        Column(
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            CircularProgressIndicator(
                                color = Color(0xFF1DB954)
                            )
                            Spacer(modifier = Modifier.height(16.dp))
                            Text(
                                text = "Loading artist...",
                                color = Color.Gray,
                                fontSize = 14.sp
                            )
                        }
                    }
                }

                error != null -> {
                    Box(
                        modifier = Modifier.fillMaxSize(),
                        contentAlignment = Alignment.Center
                    ) {
                        Column(
                            horizontalAlignment = Alignment.CenterHorizontally,
                            modifier = Modifier.padding(32.dp)
                        ) {
                            Card(
                                colors = CardDefaults.cardColors(
                                    containerColor = Color(0xFF2A2A2A)
                                ),
                                shape = RoundedCornerShape(12.dp)
                            ) {
                                Column(
                                    modifier = Modifier.padding(24.dp),
                                    horizontalAlignment = Alignment.CenterHorizontally
                                ) {
                                    Text(
                                        text = "😕",
                                        fontSize = 48.sp
                                    )
                                    Spacer(modifier = Modifier.height(16.dp))
                                    Text(
                                        text = error!!,
                                        color = Color.White,
                                        fontSize = 16.sp,
                                        textAlign = TextAlign.Center,
                                        fontWeight = FontWeight.Medium
                                    )
                                    Spacer(modifier = Modifier.height(8.dp))
                                    Text(
                                        text = "Try searching for a different artist",
                                        color = Color.Gray,
                                        fontSize = 14.sp,
                                        textAlign = TextAlign.Center
                                    )
                                }
                            }

                            Spacer(modifier = Modifier.height(24.dp))

                            Button(
                                onClick = onBack,
                                colors = ButtonDefaults.buttonColors(
                                    containerColor = Color(0xFF1DB954)
                                ),
                                shape = RoundedCornerShape(12.dp)
                            ) {
                                Text(
                                    "Go Back",
                                    fontSize = 16.sp,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                        }
                    }
                }

                songs.isEmpty() && !loading -> {
                    Box(
                        modifier = Modifier.fillMaxSize(),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = "No songs available",
                            color = Color.Gray,
                            fontSize = 16.sp
                        )
                    }
                }

                else -> {
                    /**
                     * Artist artwork (uses first song image)
                     */
                    AsyncImage(
                        model = songs.firstOrNull()?.imageUrl ?: "",
                        contentDescription = "Artist Image",
                        modifier = Modifier
                            .size(200.dp)
                            .clip(RoundedCornerShape(16.dp))
                            .align(Alignment.CenterHorizontally)
                    )

                    Spacer(modifier = Modifier.height(8.dp))

                    /**
                     * Artist name (resolved from API)
                     */
                    Text(
                        text = resolvedArtistName.ifEmpty { artistName },
                        fontSize = 24.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color.White,
                        textAlign = TextAlign.Center,
                        modifier = Modifier.fillMaxWidth()
                    )

                    Spacer(modifier = Modifier.height(4.dp))

                    Text(
                        text = "${songs.size} songs",
                        fontSize = 14.sp,
                        color = Color.Gray,
                        textAlign = TextAlign.Center,
                        modifier = Modifier.fillMaxWidth()
                    )

                    Spacer(modifier = Modifier.height(24.dp))

                    Text(
                        text = "Popular Tracks",
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color.White,
                        modifier = Modifier.padding(bottom = 12.dp)
                    )

                    LazyColumn(
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        items(songs) { song ->
                            SongItem(
                                song = song,
                                isPlaying = playerState.currentSong?.id == song.id &&
                                        playerState.isPlaying,
                                onClick = {
                                    playerViewModel.playSong(song, songs)
                                }
                            )
                        }
                    }
                }
            }
        }
    }
}