package com.example.musicplayer.presentation.screens

import androidx.compose.animation.*
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.blur
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import coil.compose.AsyncImage
import com.example.musicplayer.data.model.Song
import com.example.musicplayer.presentation.components.SongItem
import com.example.musicplayer.presentation.theme.*
import com.example.musicplayer.presentation.viewmodel.AlbumViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AlbumScreen(
    albumName: String,
    onPlaySong: (Song, List<Song>) -> Unit,
    isDarkTheme: Boolean = true,
    viewModel: AlbumViewModel = hiltViewModel(),
    playerViewModel: com.example.musicplayer.viewmodel.MusicPlayerViewModel? = null,
    onNavigateToPlayer: () -> Unit = {}
) {
    val songs by viewModel.songs.collectAsState()

    LaunchedEffect(albumName) {
        viewModel.loadAlbum(albumName)
    }

    val backgroundColor = if (isDarkTheme) DarkBackground else LightBackground
    val textPrimary = if (isDarkTheme) TextPrimaryDark else TextPrimaryLight
    val textSecondary = if (isDarkTheme) TextSecondaryDark else TextSecondaryLight

    val playerState = playerViewModel?.playerState?.collectAsState()

    Box(modifier = Modifier.fillMaxSize()) {

        // ✨ BLURRED HEADER BACKGROUND
        if (songs.isNotEmpty()) {
            AsyncImage(
                model = songs.first().imageUrl,
                contentDescription = null,
                modifier = Modifier
                    .fillMaxWidth()
                    .fillMaxHeight()
                    .blur(50.dp),
                contentScale = ContentScale.Crop,
                alpha = 0.4f
            )
        }

        // Gradient overlay
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(300.dp)
                .background(
                    Brush.verticalGradient(
                        colors = listOf(
                            Color.Transparent,
                            backgroundColor
                        ),
                        startY = 0f,
                        endY = 800f
                    )
                )
        )

        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(
                bottom = if (playerState?.value?.currentSong != null) 156.dp else 96.dp
            )
        ) {

            // ✨ HEADER SECTION
            item {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(24.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {

                    Spacer(Modifier.height(40.dp))

                    // Album Cover
                    if (songs.isNotEmpty()) {
                        Card(
                            modifier = Modifier.size(180.dp),
                            elevation = CardDefaults.cardElevation(defaultElevation = 12.dp),
                            shape = RoundedCornerShape(16.dp)
                        ) {
                            AsyncImage(
                                model = songs.first().imageUrl,
                                contentDescription = "Album Cover",
                                modifier = Modifier.fillMaxSize(),
                                contentScale = ContentScale.Crop
                            )
                        }
                    }

                    Spacer(Modifier.height(20.dp))

                    // Album Name
                    Text(
                        text = albumName,
                        fontSize = 32.sp,
                        fontWeight = FontWeight.Bold,
                        color = textPrimary
                    )

                    Spacer(Modifier.height(8.dp))

                    // Song Count
                    Text(
                        text = "${songs.size} Songs",
                        fontSize = 16.sp,
                        color = textSecondary,
                        fontWeight = FontWeight.Medium
                    )

                    Spacer(Modifier.height(24.dp))

                    // ✨ ACTION BUTTONS
                    Row(
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        // Play Button
                        Button(
                            onClick = {
                                if (songs.isNotEmpty()) onPlaySong(songs.first(), songs)
                            },
                            modifier = Modifier.height(48.dp),
                            enabled = songs.isNotEmpty(),
                            colors = ButtonDefaults.buttonColors(
                                containerColor = PrimaryOrange,
                                contentColor = Color.White
                            ),
                            shape = RoundedCornerShape(24.dp)
                        ) {
                            Icon(
                                Icons.Default.PlayArrow,
                                contentDescription = null,
                                modifier = Modifier.size(20.dp)
                            )
                            Spacer(Modifier.width(8.dp))
                            Text("Play All", fontWeight = FontWeight.Bold)
                        }

                        // Shuffle Button
                        OutlinedButton(
                            onClick = {
                                if (songs.isNotEmpty()) {
                                    val shuffled = songs.shuffled()
                                    onPlaySong(shuffled.first(), shuffled)
                                }
                            },
                            modifier = Modifier.height(48.dp),
                            enabled = songs.isNotEmpty(),
                            colors = ButtonDefaults.outlinedButtonColors(
                                contentColor = PrimaryOrange
                            ),
                            border = androidx.compose.foundation.BorderStroke(
                                width = 2.dp,
                                brush = Brush.linearGradient(
                                    colors = listOf(GradientStart, GradientEnd)
                                )
                            ),
                            shape = RoundedCornerShape(24.dp)
                        ) {
                            Icon(
                                Icons.Default.Shuffle,
                                contentDescription = null,
                                modifier = Modifier.size(20.dp)
                            )
                            Spacer(Modifier.width(8.dp))
                            Text("Shuffle", fontWeight = FontWeight.Bold)
                        }
                    }

                    Spacer(Modifier.height(32.dp))

                    // Section Title
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            Icons.Default.MusicNote,
                            contentDescription = null,
                            tint = PrimaryOrange,
                            modifier = Modifier.size(20.dp)
                        )
                        Spacer(Modifier.width(8.dp))
                        Text(
                            text = "Tracks",
                            fontSize = 20.sp,
                            fontWeight = FontWeight.Bold,
                            color = textPrimary
                        )
                    }

                    Spacer(Modifier.height(16.dp))
                }
            }

            // ✨ SONGS LIST
            items(songs, key = { it.id }) { song ->
                AnimatedVisibility(
                    visible = true,
                    enter = fadeIn() + slideInVertically()
                ) {
                    Column {
                        SongItem(
                            song = song,
                            onClick = { onPlaySong(song, songs) },
                            onArtistClick = {},
                            isDarkTheme = isDarkTheme
                        )
                        Spacer(Modifier.height(8.dp))
                    }
                }
            }

            // Empty state
            if (songs.isEmpty()) {
                item {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(32.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Icon(
                            Icons.Default.MusicOff,
                            contentDescription = null,
                            modifier = Modifier.size(64.dp),
                            tint = textSecondary.copy(alpha = 0.4f)
                        )
                        Spacer(Modifier.height(16.dp))
                        Text(
                            "No songs found",
                            fontSize = 18.sp,
                            fontWeight = FontWeight.Bold,
                            color = textPrimary
                        )
                        Spacer(Modifier.height(8.dp))
                        Text(
                            "Try searching for another album",
                            fontSize = 14.sp,
                            color = textSecondary
                        )
                    }
                }
            }
        }
    }

    // ✨ MINI PLAYER AT BOTTOM
    playerViewModel?.let { vm ->
        playerState?.value?.currentSong?.let { song ->
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(bottom = 0.dp),
                contentAlignment = Alignment.BottomCenter
            ) {
                com.example.musicplayer.presentation.components.MiniPlayer(
                    song = song,
                    isPlaying = playerState.value.isPlaying,
                    onPlayPause = { vm.playPause() },
                    onClick = onNavigateToPlayer,
                    isDarkTheme = isDarkTheme
                )
            }
        }
    }
}
