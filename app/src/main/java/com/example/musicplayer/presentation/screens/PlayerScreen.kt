package com.example.musicplayer.presentation.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.List
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.example.musicplayer.viewmodel.MusicPlayerViewModel
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PlayerScreen(
    playerViewModel: MusicPlayerViewModel,
    onNavigateBack: () -> Unit,
    onNavigateToQueue: () -> Unit = {}
) {
    val playerState by playerViewModel.playerState.collectAsState()
    val song = playerState.currentSong

    // ✅ Track user interaction state
    var isSeeking by remember { mutableStateOf(false) }
    var seekPosition by remember { mutableStateOf(0f) }

    if (song == null) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(Color(0xFF121212)),
            contentAlignment = Alignment.Center
        ) {
            Text(
                "No song playing",
                color = Color.White,
                fontSize = 18.sp
            )
        }
        return
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        "Now Playing",
                        color = Color.White,
                        fontWeight = FontWeight.Bold
                    )
                },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(
                            imageVector = Icons.Default.KeyboardArrowDown,
                            contentDescription = "Back",
                            tint = Color.White,
                            modifier = Modifier.size(32.dp)
                        )
                    }
                },
                actions = {
                    // Queue button
                    IconButton(onClick = onNavigateToQueue) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.List,
                            contentDescription = "Queue",
                            tint = Color.White,
                            modifier = Modifier.size(28.dp)
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = Color(0xFF121212)
                )
            )
        },
        containerColor = Color(0xFF121212)
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Spacer(modifier = Modifier.height(20.dp))

            // Album Art
            AsyncImage(
                model = song.imageUrl,
                contentDescription = "Album Art",
                modifier = Modifier
                    .fillMaxWidth()
                    .aspectRatio(1f)
                    .clip(RoundedCornerShape(20.dp))
            )

            Spacer(modifier = Modifier.height(32.dp))

            // Song Title
            Text(
                text = song.name,
                fontSize = 24.sp,
                color = Color.White,
                fontWeight = FontWeight.Bold,
                textAlign = TextAlign.Center,
                maxLines = 2,
                overflow = TextOverflow.Ellipsis
            )

            Spacer(modifier = Modifier.height(8.dp))

            // Artist Name
            Text(
                text = song.artists,
                fontSize = 16.sp,
                color = Color(0xFFB3B3B3),
                textAlign = TextAlign.Center,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )

            Spacer(modifier = Modifier.height(32.dp))

            // ✅ FIXED Progress Bar - only seeks on user release
            val displayProgress = if (isSeeking) {
                seekPosition
            } else {
                if (playerState.duration > 0) {
                    (playerState.currentPosition.toFloat() / playerState.duration.toFloat())
                        .coerceIn(0f, 1f)
                } else 0f
            }

            Slider(
                value = displayProgress,
                onValueChange = { newValue ->
                    isSeeking = true
                    seekPosition = newValue
                },
                onValueChangeFinished = {
                    val newPosition = (seekPosition * playerState.duration).toLong()
                    playerViewModel.seekTo(newPosition)
                    isSeeking = false
                },
                colors = SliderDefaults.colors(
                    thumbColor = Color(0xFF1DB954),
                    activeTrackColor = Color(0xFF1DB954),
                    inactiveTrackColor = Color(0xFF404040)
                ),
                modifier = Modifier.fillMaxWidth()
            )

            // Time Labels
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    text = if (isSeeking) {
                        formatTime((seekPosition * playerState.duration).toLong())
                    } else {
                        formatTime(playerState.currentPosition)
                    },
                    color = Color(0xFFB3B3B3),
                    fontSize = 12.sp
                )
                Text(
                    text = formatTime(playerState.duration),
                    color = Color(0xFFB3B3B3),
                    fontSize = 12.sp
                )
            }

            Spacer(modifier = Modifier.height(24.dp))

            // Playback Controls
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceEvenly,
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Previous Button
                IconButton(
                    onClick = { playerViewModel.playPrevious() },
                    modifier = Modifier.size(56.dp),
                    enabled = playerState.currentIndex > 0
                ) {
                    Icon(
                        Icons.Default.SkipPrevious,
                        contentDescription = "Previous",
                        tint = if (playerState.currentIndex > 0) Color.White else Color(0xFF404040),
                        modifier = Modifier.size(40.dp)
                    )
                }

                // Play/Pause Button
                FloatingActionButton(
                    onClick = { playerViewModel.playPause() },
                    modifier = Modifier.size(72.dp),
                    containerColor = Color(0xFF1DB954),
                    shape = CircleShape
                ) {
                    Icon(
                        imageVector = if (playerState.isPlaying) Icons.Default.Pause else Icons.Default.PlayArrow,
                        contentDescription = if (playerState.isPlaying) "Pause" else "Play",
                        tint = Color.White,
                        modifier = Modifier.size(40.dp)
                    )
                }

                // Next Button
                IconButton(
                    onClick = { playerViewModel.playNext() },
                    modifier = Modifier.size(56.dp),
                    enabled = playerState.currentIndex < playerState.queue.size - 1
                ) {
                    Icon(
                        Icons.Default.SkipNext,
                        contentDescription = "Next",
                        tint = if (playerState.currentIndex < playerState.queue.size - 1)
                            Color.White else Color(0xFF404040),
                        modifier = Modifier.size(40.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(24.dp))

            // Queue position indicator
            if (playerState.queue.isNotEmpty()) {
                Text(
                    text = "Track ${playerState.currentIndex + 1} of ${playerState.queue.size}",
                    color = Color(0xFFB3B3B3),
                    fontSize = 14.sp,
                    textAlign = TextAlign.Center,
                    modifier = Modifier.fillMaxWidth()
                )
            }
        }
    }
}

private fun formatTime(millis: Long): String {
    if (millis < 0) return "0:00"
    val totalSeconds = millis / 1000
    val minutes = totalSeconds / 60
    val seconds = totalSeconds % 60
    return String.format(Locale.getDefault(), "%d:%02d", minutes, seconds)
}