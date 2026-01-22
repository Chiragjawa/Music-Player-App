package com.example.musicplayer.presentation.components

import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Pause
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.example.musicplayer.data.model.Song
import com.example.musicplayer.presentation.theme.*

@Composable
fun MiniPlayer(
    song: Song,
    isPlaying: Boolean,
    onPlayPause: () -> Unit,
    onClick: () -> Unit,
    isDarkTheme: Boolean = true
) {
    val infiniteTransition = rememberInfiniteTransition(label = "pulse")

    val pulseScale by infiniteTransition.animateFloat(
        initialValue = 1f,
        targetValue = 1.05f,
        animationSpec = infiniteRepeatable(
            animation = tween(1000, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "pulse"
    )

    val backgroundColor = if (isDarkTheme) {
        Brush.horizontalGradient(
            colors = listOf(
                Color(0xFF1A1A1A),
                Color(0xFF252525)
            )
        )
    } else {
        Brush.horizontalGradient(
            colors = listOf(
                Color(0xFFFFFFFF),
                Color(0xFFF5F5F5)
            )
        )
    }

    val textPrimary = if (isDarkTheme) TextPrimaryDark else TextPrimaryLight
    val textSecondary = if (isDarkTheme) TextSecondaryDark else TextSecondaryLight

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .height(72.dp)
            .shadow(12.dp, RoundedCornerShape(topStart = 16.dp, topEnd = 16.dp))
            .clickable { onClick() },
        shape = RoundedCornerShape(topStart = 16.dp, topEnd = 16.dp),
        colors = CardDefaults.cardColors(
            containerColor = Color.Transparent
        )
    ) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(backgroundColor)
        ) {
            Row(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(horizontal = 16.dp, vertical = 10.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {

                // ✨ ALBUM ART
                Card(
                    modifier = Modifier.size(52.dp),
                    shape = RoundedCornerShape(12.dp),
                    elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
                ) {
                    AsyncImage(
                        model = song.imageUrl,
                        contentDescription = "Album Art",
                        modifier = Modifier.fillMaxSize()
                    )
                }

                Spacer(modifier = Modifier.width(12.dp))

                // ✨ SONG INFO
                Column(
                    modifier = Modifier.weight(1f)
                ) {
                    Text(
                        text = song.name,
                        color = textPrimary,
                        fontSize = 15.sp,
                        fontWeight = FontWeight.Bold,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )

                    Spacer(modifier = Modifier.height(2.dp))

                    Text(
                        text = song.artists,
                        color = textSecondary,
                        fontSize = 13.sp,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }

                Spacer(modifier = Modifier.width(12.dp))

                // ✨ PLAY/PAUSE BUTTON
                Surface(
                    modifier = Modifier
                        .size(if (isPlaying) 48.dp * pulseScale else 48.dp)
                        .clip(CircleShape)
                        .clickable { onPlayPause() },
                    color = PrimaryOrange,
                    shadowElevation = 6.dp,
                    shape = CircleShape
                ) {
                    Box(
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = if (isPlaying) Icons.Default.Pause else Icons.Default.PlayArrow,
                            contentDescription = if (isPlaying) "Pause" else "Play",
                            tint = Color.White,
                            modifier = Modifier.size(28.dp)
                        )
                    }
                }
            }
        }
    }
}