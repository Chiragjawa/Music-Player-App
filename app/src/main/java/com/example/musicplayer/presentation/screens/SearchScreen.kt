package com.example.musicplayer.presentation.screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.example.musicplayer.viewmodel.PlayerViewModel
import com.example.musicplayer.viewmodel.SearchViewModel

@Composable
fun SearchScreen(
    viewModel: SearchViewModel,
    playerViewModel: PlayerViewModel
) {

    val songs by viewModel.songs.collectAsState()
    val loading by viewModel.isLoading.collectAsState()
    val error by viewModel.error.collectAsState()

    var query by remember { mutableStateOf("") }

    Column(modifier = Modifier.fillMaxSize().padding(16.dp)) {

        TextField(
            value = query,
            onValueChange = { query = it },
            modifier = Modifier.fillMaxWidth(),
            placeholder = { Text("Search songs...") }
        )

        Spacer(modifier = Modifier.height(8.dp))

        Button(
            onClick = { viewModel.search(query) },
            modifier = Modifier.fillMaxWidth()
        ) {
            Text("Search")
        }

        Spacer(modifier = Modifier.height(16.dp))

        when {
            loading -> CircularProgressIndicator()

            error != null -> Text("Error: $error")

            else -> {
                LazyColumn {
                    items(songs) { song ->
                        SongItem(song) {
                            playerViewModel.playSong(it)
                        }
                    }
                }
            }
        }
    }
}
