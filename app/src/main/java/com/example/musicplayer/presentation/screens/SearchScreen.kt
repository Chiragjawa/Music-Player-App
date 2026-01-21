package com.example.musicplayer.presentation.screens

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.musicplayer.presentation.components.MiniPlayer
import com.example.musicplayer.presentation.components.SongItem
import com.example.musicplayer.viewmodel.MusicPlayerViewModel
import com.example.musicplayer.viewmodel.SearchViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SearchScreen(
    searchViewModel: SearchViewModel,
    playerViewModel: MusicPlayerViewModel,
    onNavigateToPlayer: () -> Unit,
    onNavigateToArtist: (String) -> Unit
) {
    val focusManager = LocalFocusManager.current
    val keyboardController = LocalSoftwareKeyboardController.current

    val songs by searchViewModel.songs.collectAsState()
    val loading by searchViewModel.isLoading.collectAsState()
    val error by searchViewModel.error.collectAsState()
    val noResults by searchViewModel.noResults.collectAsState()
    val playerState by playerViewModel.playerState.collectAsState()

    var query by remember { mutableStateOf("") }
    var showArtistSuggestion by remember { mutableStateOf(false) }

    /**
     * Unified search trigger
     */
    fun performSearch() {
        if (query.isBlank()) return

        keyboardController?.hide()
        focusManager.clearFocus()
        showArtistSuggestion = false

        searchViewModel.search(query)
    }

    /**
     * Show artist suggestion when no song results found
     */
    LaunchedEffect(noResults, loading) {
        if (noResults && !loading && query.isNotBlank()) {
            showArtistSuggestion = true
        } else {
            showArtistSuggestion = false
        }
    }

    Scaffold(
        topBar = {
            CenterAlignedTopAppBar(
                title = {
                    Text(
                        "Music Player",
                        fontWeight = FontWeight.Bold,
                        fontSize = 22.sp,
                        color = Color.White
                    )
                },
                colors = TopAppBarDefaults.centerAlignedTopAppBarColors(
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

            /* 🔎 SEARCH FIELD */
            OutlinedTextField(
                value = query,
                onValueChange = {
                    query = it
                    showArtistSuggestion = false
                },
                modifier = Modifier.fillMaxWidth(),
                placeholder = {
                    Text("Search for songs, artists...", color = Color.Gray)
                },
                leadingIcon = {
                    Icon(
                        imageVector = Icons.Default.Search,
                        contentDescription = "Search",
                        tint = Color(0xFF1DB954)
                    )
                },
                keyboardOptions = KeyboardOptions(imeAction = ImeAction.Search),
                keyboardActions = KeyboardActions(
                    onSearch = { performSearch() }
                ),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = Color(0xFF1DB954),
                    unfocusedBorderColor = Color(0xFF2A2A2A),
                    focusedTextColor = Color.White,
                    unfocusedTextColor = Color.White,
                    cursorColor = Color(0xFF1DB954)
                ),
                shape = RoundedCornerShape(12.dp),
                singleLine = true
            )

            Spacer(modifier = Modifier.height(12.dp))

            /* 🔍 SEARCH BUTTON */
            Button(
                onClick = { performSearch() },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(50.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = Color(0xFF1DB954)
                ),
                shape = RoundedCornerShape(12.dp),
                enabled = query.isNotBlank()
            ) {
                Text(
                    "Search",
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color.White
                )
            }

            Spacer(modifier = Modifier.height(20.dp))

            /* 📄 CONTENT STATES */
            when {
                loading -> {
                    Box(Modifier.fillMaxSize(), Alignment.Center) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            CircularProgressIndicator(color = Color(0xFF1DB954))
                            Spacer(modifier = Modifier.height(16.dp))
                            Text(
                                "Searching...",
                                color = Color.Gray,
                                fontSize = 14.sp
                            )
                        }
                    }
                }

                error != null -> {
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        colors = CardDefaults.cardColors(
                            containerColor = Color(0xFF2A2A2A)
                        ),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Column(
                            modifier = Modifier.padding(16.dp),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Text(
                                text = "⚠️",
                                fontSize = 32.sp
                            )
                            Spacer(modifier = Modifier.height(8.dp))
                            Text(
                                text = "Error: $error",
                                color = Color(0xFFFF6B6B),
                                fontSize = 14.sp,
                                textAlign = TextAlign.Center
                            )
                        }
                    }
                }

                showArtistSuggestion && noResults -> {
                    Box(Modifier.fillMaxSize(), Alignment.Center) {
                        Column(
                            horizontalAlignment = Alignment.CenterHorizontally,
                            modifier = Modifier.padding(32.dp)
                        ) {
                            Text(
                                "No songs found",
                                color = Color.White,
                                fontSize = 20.sp,
                                fontWeight = FontWeight.Bold
                            )

                            Spacer(modifier = Modifier.height(8.dp))

                            Text(
                                "Try searching for the artist instead",
                                color = Color.Gray,
                                fontSize = 14.sp,
                                textAlign = TextAlign.Center
                            )

                            Spacer(modifier = Modifier.height(24.dp))

                            Card(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clickable { onNavigateToArtist(query) },
                                colors = CardDefaults.cardColors(
                                    containerColor = Color(0xFF1E1E1E)
                                ),
                                shape = RoundedCornerShape(12.dp)
                            ) {
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(16.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.Person,
                                        contentDescription = "Artist",
                                        tint = Color(0xFF1DB954),
                                        modifier = Modifier.size(48.dp)
                                    )

                                    Spacer(modifier = Modifier.width(16.dp))

                                    Column(modifier = Modifier.weight(1f)) {
                                        Text(
                                            "Search for Artist",
                                            color = Color.White,
                                            fontSize = 16.sp,
                                            fontWeight = FontWeight.Bold
                                        )
                                        Spacer(modifier = Modifier.height(4.dp))
                                        Text(
                                            "\"$query\"",
                                            color = Color(0xFF1DB954),
                                            fontSize = 14.sp
                                        )
                                    }

                                    Icon(
                                        imageVector = Icons.Default.Search,
                                        contentDescription = "Search",
                                        tint = Color(0xFF808080)
                                    )
                                }
                            }

                            Spacer(modifier = Modifier.height(16.dp))

                            TextButton(
                                onClick = {
                                    query = ""
                                    showArtistSuggestion = false
                                }
                            ) {
                                Text(
                                    "Clear Search",
                                    color = Color(0xFF808080)
                                )
                            }
                        }
                    }
                }

                songs.isEmpty() && !noResults -> {
                    Box(Modifier.fillMaxSize(), Alignment.Center) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Text(
                                "🎵",
                                fontSize = 64.sp
                            )
                            Spacer(modifier = Modifier.height(16.dp))
                            Text(
                                "Search for your favorite songs",
                                color = Color.Gray,
                                fontSize = 16.sp,
                                textAlign = TextAlign.Center
                            )
                        }
                    }
                }

                else -> {
                    Text(
                        "Results (${songs.size})",
                        color = Color.White,
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold,
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