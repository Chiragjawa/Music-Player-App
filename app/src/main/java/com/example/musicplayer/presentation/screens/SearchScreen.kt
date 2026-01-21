package com.example.musicplayer.presentation.screens

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.example.musicplayer.data.model.Song
import com.example.musicplayer.presentation.components.SongItem
import com.example.musicplayer.presentation.viewmodel.SearchViewModel

enum class SearchTab { SUGGESTED, SONGS, ARTISTS, ALBUMS }

@Composable
fun SearchScreen(
    onNavigateToArtist: (String) -> Unit,
    onPlaySong: (Song, List<Song>) -> Unit,
    viewModel: SearchViewModel = hiltViewModel()
) {
    var query by remember { mutableStateOf("") }
    var selectedTab by remember { mutableStateOf(SearchTab.SUGGESTED) }

    // ✅ StateFlow collection
    val songs by viewModel.songs.collectAsState()
    val randomSongs by viewModel.randomSongs.collectAsState()
    val artistResults by viewModel.artistResults.collectAsState()
    val albumResults by viewModel.albumResults.collectAsState()

    val keyboardController = LocalSoftwareKeyboardController.current

    fun performSearch() {
        if (query.isBlank()) return

        when (selectedTab) {
            SearchTab.SONGS -> viewModel.searchSongs(query)
            SearchTab.ARTISTS -> viewModel.searchArtists(query)
            SearchTab.ALBUMS -> viewModel.searchAlbums(query)
            SearchTab.SUGGESTED -> Unit
        }
        keyboardController?.hide()
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {

        // Title
        Text(
            text = "MyPlay",
            style = MaterialTheme.typography.headlineMedium,
            fontWeight = FontWeight.Bold
        )

        Spacer(Modifier.height(12.dp))

        // Tabs
        Row {
            SearchTab.values().forEach { tab ->
                Text(
                    text = tab.name.lowercase().replaceFirstChar { it.uppercase() },
                    modifier = Modifier
                        .padding(end = 16.dp)
                        .clickable {
                            selectedTab = tab
                            query = ""
                            viewModel.clearResults()
                        },
                    fontWeight = if (selectedTab == tab) FontWeight.Bold else FontWeight.Normal,
                    color = if (selectedTab == tab)
                        MaterialTheme.colorScheme.primary
                    else Color.Gray
                )
            }
        }

        Spacer(Modifier.height(16.dp))

        // Search bar (not in Suggested)
        if (selectedTab != SearchTab.SUGGESTED) {
            OutlinedTextField(
                value = query,
                onValueChange = { query = it },
                modifier = Modifier.fillMaxWidth(),
                placeholder = { Text("Search ${selectedTab.name.lowercase()}") },
                singleLine = true,
                trailingIcon = {
                    IconButton(onClick = { performSearch() }) {
                        Icon(Icons.Default.Search, contentDescription = "Search")
                    }
                },
                keyboardOptions = KeyboardOptions(
                    imeAction = ImeAction.Search
                ),
                keyboardActions = KeyboardActions(
                    onSearch = { performSearch() }
                )
            )
            Spacer(Modifier.height(16.dp))
        }

        // Content
        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(bottom = 96.dp)
        ) {

            when (selectedTab) {

                SearchTab.SUGGESTED -> {
                    items(randomSongs, key = { it.id }) { song ->
                        SongItem(
                            song = song,
                            onClick = { onPlaySong(song, randomSongs) },
                            onArtistClick = {}
                        )
                        Spacer(Modifier.height(8.dp))
                    }
                }

                SearchTab.SONGS -> {
                    if (songs.isNotEmpty()) {
                        items(songs, key = { it.id }) { song ->
                            SongItem(
                                song = song,
                                onClick = { onPlaySong(song, songs) },
                                onArtistClick = {}
                            )
                            Spacer(Modifier.height(8.dp))
                        }
                    } else if (query.isNotBlank()) {
                        item { NoResult() }
                    }
                }

                SearchTab.ARTISTS -> {
                    if (artistResults.isNotEmpty()) {
                        items(artistResults, key = { it }) { artist ->
                            ArtistResultCard(
                                name = artist,
                                onClick = { onNavigateToArtist(artist) }
                            )
                            Spacer(Modifier.height(8.dp))
                        }
                    } else if (query.isNotBlank()) {
                        item { NoResult() }
                    }
                }

                SearchTab.ALBUMS -> {
                    if (albumResults.isNotEmpty()) {
                        items(albumResults, key = { it }) { album ->
                            Text(
                                text = album,
                                fontWeight = FontWeight.Bold,
                                modifier = Modifier.padding(12.dp)
                            )
                        }
                    } else if (query.isNotBlank()) {
                        item { NoResult() }
                    }
                }
            }
        }
    }
}

@Composable
private fun ArtistResultCard(
    name: String,
    onClick: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
    ) {
        Row(
            modifier = Modifier.padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(name, fontWeight = FontWeight.Bold)
            Spacer(Modifier.weight(1f))
            Text("Artist", color = Color.Gray)
        }
    }
}

@Composable
private fun NoResult() {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(top = 48.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text("No results found", fontWeight = FontWeight.Bold)
        Spacer(Modifier.height(4.dp))
        Text("Try another keyword", color = Color.Gray)
    }
}
