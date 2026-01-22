package com.example.musicplayer.presentation.components

import androidx.compose.animation.core.*
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Pause
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.example.musicplayer.data.model.Song
import com.example.musicplayer.presentation.theme.*

@Composable
fun SongItem(
    song: Song,
    isPlaying: Boolean = false,
    onClick: () -> Unit,
    onArtistClick: (String) -> Unit,
    isDarkTheme: Boolean = true
) {
    var isPressed by remember { mutableStateOf(false) }

    val scale by animateFloatAsState(
        targetValue = if (isPressed) 0.95f else 1f,
        animationSpec = spring(
            dampingRatio = Spring.DampingRatioMediumBouncy,
            stiffness = Spring.StiffnessLow
        )
    )

    val cardColor = when {
        isPlaying && isDarkTheme -> Color(0xFF2D1810)
        isPlaying && !isDarkTheme -> Color(0xFFFFE8D9)
        isDarkTheme -> DarkCard
        else -> LightCard
    }

    val textPrimary = if (isDarkTheme) TextPrimaryDark else TextPrimaryLight
    val textSecondary = if (isDarkTheme) TextSecondaryDark else TextSecondaryLight

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .scale(scale)
            .clickable {
                isPressed = true
                onClick()
            },
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = cardColor),
        elevation = CardDefaults.cardElevation(
            defaultElevation = if (isPlaying) 6.dp else 2.dp
        )
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {

            // ✨ ALBUM ART WITH SHADOW
            Card(
                modifier = Modifier.size(64.dp),
                shape = RoundedCornerShape(12.dp),
                elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
            ) {
                AsyncImage(
                    model = song.imageUrl,
                    contentDescription = "Song artwork",
                    modifier = Modifier.fillMaxSize()
                )
            }

            Spacer(modifier = Modifier.width(12.dp))

            // ✨ SONG DETAILS
            Column(
                modifier = Modifier.weight(1f)
            ) {
                Text(
                    text = song.name,
                    color = textPrimary,
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Bold,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )

                Spacer(modifier = Modifier.height(4.dp))

                Text(
                    text = song.artists,
                    color = PrimaryOrange,
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Medium,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                    modifier = Modifier.clickable {
                        onArtistClick(song.artists.split(",").first().trim())
                    }
                )

                Spacer(modifier = Modifier.height(4.dp))

                Text(
                    text = formatDuration(song.duration),
                    color = textSecondary,
                    fontSize = 12.sp
                )
            }

            Spacer(modifier = Modifier.width(8.dp))

            // ✨ ANIMATED PLAY ICON
            Surface(
                modifier = Modifier.size(40.dp),
                shape = RoundedCornerShape(12.dp),
                color = if (isPlaying) PrimaryOrange else Color.Transparent
            ) {
                Box(contentAlignment = Alignment.Center) {
                    Icon(
                        imageVector = if (isPlaying) Icons.Default.Pause else Icons.Default.PlayArrow,
                        contentDescription = if (isPlaying) "Pause" else "Play",
                        tint = if (isPlaying) Color.White else PrimaryOrange,
                        modifier = Modifier.size(24.dp)
                    )
                }
            }
        }
    }

    LaunchedEffect(isPressed) {
        if (isPressed) {
            kotlinx.coroutines.delay(100)
            isPressed = false
        }
    }
}

private fun formatDuration(seconds: Int): String {
    val minutes = seconds / 60
    val remainingSeconds = seconds % 60
    return String.format("%d:%02d", minutes, remainingSeconds)
}