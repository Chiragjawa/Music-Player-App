package com.example.musicplayer.presentation.screens

import androidx.compose.animation.*
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.example.musicplayer.data.model.Song
import com.example.musicplayer.presentation.theme.*
import com.example.musicplayer.viewmodel.MusicPlayerViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun QueueScreen(
    playerViewModel: MusicPlayerViewModel,
    onDismiss: () -> Unit,
    isDarkTheme: Boolean = true
) {
    val playerState by playerViewModel.playerState.collectAsState()

    val backgroundColor = if (isDarkTheme) DarkBackground else LightBackground
    val surfaceColor = if (isDarkTheme) DarkSurface else LightSurface
    val textPrimary = if (isDarkTheme) TextPrimaryDark else TextPrimaryLight
    val textSecondary = if (isDarkTheme) TextSecondaryDark else TextSecondaryLight

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Column {
                        Text(
                            "Queue",
                            color = textPrimary,
                            fontWeight = FontWeight.Bold,
                            fontSize = 20.sp
                        )
                        Text(
                            "${playerState.queue.size} songs",
                            color = textSecondary,
                            fontSize = 12.sp
                        )
                    }
                },
                navigationIcon = {
                    IconButton(onClick = onDismiss) {
                        Icon(
                            Icons.Default.Close,
                            contentDescription = "Close",
                            tint = textPrimary
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = surfaceColor
                )
            )
        },
        containerColor = backgroundColor
    ) { padding ->

        if (playerState.queue.isEmpty()) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(padding),
                contentAlignment = Alignment.Center
            ) {
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Icon(
                        Icons.Default.QueueMusic,
                        contentDescription = null,
                        modifier = Modifier.size(80.dp),
                        tint = textSecondary.copy(alpha = 0.4f)
                    )
                    Spacer(Modifier.height(16.dp))
                    Text(
                        "Queue is empty",
                        color = textPrimary,
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold
                    )
                    Spacer(Modifier.height(8.dp))
                    Text(
                        "Add songs to get started",
                        color = textSecondary,
                        fontSize = 14.sp
                    )
                }
            }
        } else {
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(padding)
                    .padding(horizontal = 16.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp),
                contentPadding = PaddingValues(vertical = 16.dp)
            ) {
                itemsIndexed(
                    items = playerState.queue,
                    key = { _, song -> song.id }
                ) { index, song ->
                    AnimatedVisibility(
                        visible = true,
                        enter = fadeIn() + slideInHorizontally()
                    ) {
                        QueueItem(
                            song = song,
                            isCurrentSong = index == playerState.currentIndex,
                            onRemove = { playerViewModel.removeFromQueue(song) },
                            isDarkTheme = isDarkTheme,
                            position = index + 1
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun QueueItem(
    song: Song,
    isCurrentSong: Boolean,
    onRemove: () -> Unit,
    isDarkTheme: Boolean,
    position: Int
) {
    val cardColor = when {
        isCurrentSong && isDarkTheme -> Color(0xFF2D1810)
        isCurrentSong && !isDarkTheme -> Color(0xFFFFE8D9)
        isDarkTheme -> DarkCard
        else -> LightCard
    }

    val textPrimary = if (isDarkTheme) TextPrimaryDark else TextPrimaryLight
    val textSecondary = if (isDarkTheme) TextSecondaryDark else TextSecondaryLight

    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = cardColor),
        elevation = CardDefaults.cardElevation(
            defaultElevation = if (isCurrentSong) 6.dp else 2.dp
        )
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {

            // Position Number
            Box(
                modifier = Modifier
                    .size(32.dp)
                    .background(
                        if (isCurrentSong) PrimaryOrange else textSecondary.copy(alpha = 0.2f),
                        RoundedCornerShape(8.dp)
                    ),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = position.toString(),
                    color = if (isCurrentSong) Color.White else textSecondary,
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Bold
                )
            }

            Spacer(modifier = Modifier.width(12.dp))

            // Album Art
            Card(
                modifier = Modifier.size(50.dp),
                shape = RoundedCornerShape(10.dp),
                elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
            ) {
                AsyncImage(
                    model = song.imageUrl,
                    contentDescription = null,
                    modifier = Modifier.fillMaxSize()
                )
            }

            Spacer(modifier = Modifier.width(12.dp))

            // Song Info
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = song.name,
                    color = if (isCurrentSong) PrimaryOrange else textPrimary,
                    fontSize = 15.sp,
                    fontWeight = if (isCurrentSong) FontWeight.Bold else FontWeight.Medium,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )

                Spacer(modifier = Modifier.height(4.dp))

                Text(
                    text = song.artists,
                    color = textSecondary,
                    fontSize = 13.sp,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            }

            Spacer(modifier = Modifier.width(8.dp))

            // Remove Button
            IconButton(
                onClick = onRemove,
                modifier = Modifier.size(40.dp)
            ) {
                Icon(
                    Icons.Default.Close,
                    contentDescription = "Remove",
                    tint = textSecondary,
                    modifier = Modifier.size(20.dp)
                )
            }
        }
    }
}