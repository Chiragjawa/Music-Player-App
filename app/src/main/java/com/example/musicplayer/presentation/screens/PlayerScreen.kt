package com.example.musicplayer.presentation.screens

import androidx.compose.animation.core.*
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
import androidx.compose.ui.draw.blur
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.example.musicplayer.presentation.theme.*
import com.example.musicplayer.viewmodel.MusicPlayerViewModel
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PlayerScreen(
    playerViewModel: MusicPlayerViewModel,
    onNavigateBack: () -> Unit,
    isDarkTheme: Boolean = true
) {
    val playerState by playerViewModel.playerState.collectAsState()
    val song = playerState.currentSong

    var isSeeking by remember { mutableStateOf(false) }
    var seekPosition by remember { mutableStateOf(0f) }

    // Rotation animation for album art
    val infiniteTransition = rememberInfiniteTransition(label = "rotation")
    val rotation by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 360f,
        animationSpec = infiniteRepeatable(
            animation = tween(20000, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "rotation"
    )

    if (song == null) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(if (isDarkTheme) DarkBackground else LightBackground),
            contentAlignment = Alignment.Center
        ) {
            Text(
                "No song playing",
                color = if (isDarkTheme) TextPrimaryDark else TextPrimaryLight,
                fontSize = 18.sp
            )
        }
        return
    }

    val backgroundColor = if (isDarkTheme) DarkBackground else LightBackground
    val textPrimary = if (isDarkTheme) TextPrimaryDark else TextPrimaryLight
    val textSecondary = if (isDarkTheme) TextSecondaryDark else TextSecondaryLight

    Box(modifier = Modifier.fillMaxSize()) {

        // ✨ BLURRED BACKGROUND
        AsyncImage(
            model = song.imageUrl,
            contentDescription = null,
            modifier = Modifier
                .fillMaxSize()
                .blur(100.dp),
            contentScale = ContentScale.Crop,
            alpha = 0.3f
        )

        // Gradient overlay
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(
                    Brush.verticalGradient(
                        colors = listOf(
                            backgroundColor.copy(alpha = 0.8f),
                            backgroundColor.copy(alpha = 0.95f)
                        )
                    )
                )
        )

        Scaffold(
            topBar = {
                TopAppBar(
                    title = {
                        Text(
                            "Now Playing",
                            color = textPrimary,
                            fontWeight = FontWeight.Bold
                        )
                    },
                    navigationIcon = {
                        IconButton(onClick = onNavigateBack) {
                            Icon(
                                imageVector = Icons.Default.KeyboardArrowDown,
                                contentDescription = "Back",
                                tint = textPrimary,
                                modifier = Modifier.size(32.dp)
                            )
                        }
                    },
                    colors = TopAppBarDefaults.topAppBarColors(
                        containerColor = Color.Transparent
                    )
                )
            },
            containerColor = Color.Transparent
        ) { padding ->

            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(padding)
                    .padding(24.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {

                Spacer(modifier = Modifier.height(20.dp))

                // ✨ ALBUM ART WITH ROTATION
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .aspectRatio(1f)
                        .scale(if (playerState.isPlaying) 1f else 0.95f),
                    shape = RoundedCornerShape(24.dp),
                    elevation = CardDefaults.cardElevation(defaultElevation = 16.dp)
                ) {
                    AsyncImage(
                        model = song.imageUrl,
                        contentDescription = "Album Art",
                        modifier = Modifier.fillMaxSize(),
                        contentScale = ContentScale.Crop
                    )
                }

                Spacer(modifier = Modifier.height(40.dp))

                // ✨ SONG TITLE
                Text(
                    text = song.name,
                    fontSize = 26.sp,
                    color = textPrimary,
                    fontWeight = FontWeight.Bold,
                    textAlign = TextAlign.Center,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis
                )

                Spacer(modifier = Modifier.height(8.dp))

                // ✨ ARTIST NAME
                Text(
                    text = song.artists,
                    fontSize = 16.sp,
                    color = PrimaryOrange,
                    fontWeight = FontWeight.Medium,
                    textAlign = TextAlign.Center,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )

                Spacer(modifier = Modifier.height(32.dp))

                // ✨ PROGRESS BAR
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
                        thumbColor = PrimaryOrange,
                        activeTrackColor = PrimaryOrange,
                        inactiveTrackColor = textSecondary.copy(alpha = 0.3f)
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
                        color = textSecondary,
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Medium
                    )
                    Text(
                        text = formatTime(playerState.duration),
                        color = textSecondary,
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Medium
                    )
                }

                Spacer(modifier = Modifier.height(32.dp))

                // ✨ PLAYBACK CONTROLS
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceEvenly,
                    verticalAlignment = Alignment.CenterVertically
                ) {

                    // Previous Button
                    IconButton(
                        onClick = { playerViewModel.playPrevious() },
                        modifier = Modifier.size(64.dp),
                        enabled = playerState.currentIndex > 0
                    ) {
                        Icon(
                            Icons.Default.SkipPrevious,
                            contentDescription = "Previous",
                            tint = if (playerState.currentIndex > 0) textPrimary else textSecondary.copy(alpha = 0.3f),
                            modifier = Modifier.size(44.dp)
                        )
                    }

                    // Play/Pause Button
                    Surface(
                        modifier = Modifier.size(72.dp),
                        shape = CircleShape,
                        color = PrimaryOrange,
                        shadowElevation = 12.dp,
                        onClick = { playerViewModel.playPause() }
                    ) {
                        Box(contentAlignment = Alignment.Center) {
                            Icon(
                                imageVector = if (playerState.isPlaying) Icons.Default.Pause else Icons.Default.PlayArrow,
                                contentDescription = if (playerState.isPlaying) "Pause" else "Play",
                                tint = Color.White,
                                modifier = Modifier.size(40.dp)
                            )
                        }
                    }

                    // Next Button
                    IconButton(
                        onClick = { playerViewModel.playNext() },
                        modifier = Modifier.size(64.dp),
                        enabled = playerState.currentIndex < playerState.queue.size - 1
                    ) {
                        Icon(
                            Icons.Default.SkipNext,
                            contentDescription = "Next",
                            tint = if (playerState.currentIndex < playerState.queue.size - 1)
                                textPrimary else textSecondary.copy(alpha = 0.3f),
                            modifier = Modifier.size(44.dp)
                        )
                    }
                }

                Spacer(modifier = Modifier.height(24.dp))

                // ✨ QUEUE INFO
                if (playerState.queue.isNotEmpty()) {
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        colors = CardDefaults.cardColors(
                            containerColor = if (isDarkTheme)
                                DarkCard.copy(alpha = 0.8f)
                            else
                                LightCard.copy(alpha = 0.9f)
                        ),
                        shape = RoundedCornerShape(16.dp),
                        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(16.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column {
                                Text(
                                    "Queue",
                                    color = textPrimary,
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 14.sp
                                )
                                Text(
                                    "${playerState.currentIndex + 1} of ${playerState.queue.size}",
                                    color = textSecondary,
                                    fontSize = 12.sp
                                )
                            }
                            Icon(
                                imageVector = Icons.AutoMirrored.Filled.List,
                                contentDescription = "Queue",
                                tint = PrimaryOrange
                            )
                        }
                    }
                }
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