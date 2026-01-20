package com.example.musicplayer.presentation.screens

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.material3.Card
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.example.musicplayer.data.model.Song

@Composable
fun SongItem(
    song: Song,
    onClick: (Song) -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(8.dp)
            .clickable { onClick(song) }
    ) {
        Column(modifier = Modifier.padding(12.dp)) {
            Text(text = song.name)
            Text(text = song.artists)
        }
    }
}
