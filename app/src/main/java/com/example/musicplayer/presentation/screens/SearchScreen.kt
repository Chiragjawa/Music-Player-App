package com.example.musicplayer.presentation.screens

import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import com.example.musicplayer.data.model.Song
import com.example.musicplayer.presentation.components.SongItem
import com.example.musicplayer.presentation.theme.*
import com.example.musicplayer.presentation.viewmodel.SearchViewModel

enum class SearchTab { SUGGESTED, SONGS, ARTISTS, ALBUMS }

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SearchScreen(
    onNavigateToArtist: (String) -> Unit,
    onNavigateToAlbum: (String) -> Unit = {},
    onPlaySong: (Song, List<Song>) -> Unit,
    viewModel: SearchViewModel = hiltViewModel(),
    isDarkTheme: Boolean,
    onThemeToggle: () -> Unit,
    playerViewModel: com.example.musicplayer.viewmodel.MusicPlayerViewModel? = null,
    onNavigateToPlayer: () -> Unit = {}
) {
    var query by remember { mutableStateOf("") }
    var selectedTab by remember { mutableStateOf(SearchTab.SUGGESTED) }
    var hasSearched by remember { mutableStateOf(false) }

    val songs by viewModel.songs.collectAsState()
    val randomSongs by viewModel.randomSongs.collectAsState()
    val artistResults by viewModel.artistResults.collectAsState()
    val albumResults by viewModel.albumResults.collectAsState()
    val isLoading by viewModel.isLoading.collectAsState()
    val hasMoreSongs by viewModel.hasMoreSongs.collectAsState()
    val hasMoreArtists by viewModel.hasMoreArtists.collectAsState()
    val hasMoreAlbums by viewModel.hasMoreAlbums.collectAsState()

    val keyboardController = LocalSoftwareKeyboardController.current

    // Animation states
    var isSearchBarFocused by remember { mutableStateOf(false) }
    val searchBarScale by animateFloatAsState(
        targetValue = if (isSearchBarFocused) 1.02f else 1f,
        animationSpec = spring(dampingRatio = Spring.DampingRatioMediumBouncy)
    )

    fun performSearch() {
        if (query.isBlank()) return

        hasSearched = true
        when (selectedTab) {
            SearchTab.SONGS -> viewModel.searchSongs(query)
            SearchTab.ARTISTS -> viewModel.searchArtists(query)
            SearchTab.ALBUMS -> viewModel.searchAlbums(query)
            SearchTab.SUGGESTED -> Unit
        }
        keyboardController?.hide()
    }

    val backgroundColor = if (isDarkTheme) DarkBackground else LightBackground
    val surfaceColor = if (isDarkTheme) DarkSurface else LightSurface
    val textPrimary = if (isDarkTheme) TextPrimaryDark else TextPrimaryLight
    val textSecondary = if (isDarkTheme) TextSecondaryDark else TextSecondaryLight

    val playerState = playerViewModel?.playerState?.collectAsState()


    Box(modifier = Modifier.fillMaxSize()) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .background(backgroundColor)
        ) {

            // ✨ GRADIENT HEADER
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(
                        Brush.verticalGradient(
                            colors = listOf(GradientStart, GradientEnd)
                        )
                    )
                    .padding(horizontal = 20.dp, vertical = 16.dp)
            ) {
                Column {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "Lokal Music Player",
                            fontSize = 32.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color.White
                        )

                        // ✨ THEME TOGGLE BUTTON
                        IconButton(
                            onClick = onThemeToggle,
                            modifier = Modifier
                                .size(48.dp)
                                .background(Color.White.copy(alpha = 0.2f), CircleShape)
                        ) {
                            Icon(
                                imageVector = if (isDarkTheme) Icons.Default.LightMode else Icons.Default.DarkMode,
                                contentDescription = "Toggle Theme",
                                tint = Color.White
                            )
                        }
                    }

                    Spacer(Modifier.height(8.dp))

                    Text(
                        text = "Discover your favorite music",
                        fontSize = 14.sp,
                        color = Color.White.copy(alpha = 0.9f)
                    )
                }
            }

            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(horizontal = 16.dp)
            ) {
                Spacer(Modifier.height(16.dp))

                // ✨ MODERN TABS
                ScrollableTabRow(
                    selectedTabIndex = selectedTab.ordinal,
                    containerColor = Color.Transparent,
                    contentColor = PrimaryOrange,
                    edgePadding = 0.dp,
                    indicator = { tabPositions ->
                        if (tabPositions.isNotEmpty() && selectedTab.ordinal < tabPositions.size) {
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .wrapContentSize(Alignment.BottomStart)
                                    .offset(x = tabPositions[selectedTab.ordinal].left)
                                    .width(tabPositions[selectedTab.ordinal].width)
                                    .height(3.dp)
                                    .background(PrimaryOrange, RoundedCornerShape(topStart = 3.dp, topEnd = 3.dp))
                            )
                        }
                    },
                    divider = {}
                ) {
                    SearchTab.values().forEach { tab ->
                        Tab(
                            selected = selectedTab == tab,
                            onClick = {
                                selectedTab = tab
                                query = ""
                                hasSearched = false
                                viewModel.clearResults()
                            },
                            modifier = Modifier.padding(vertical = 8.dp)
                        ) {
                            Text(
                                text = tab.name.lowercase().replaceFirstChar { it.uppercase() },
                                fontWeight = if (selectedTab == tab) FontWeight.Bold else FontWeight.Normal,
                                fontSize = 15.sp,
                                color = if (selectedTab == tab) PrimaryOrange else textSecondary,
                                modifier = Modifier.padding(bottom = 8.dp)
                            )
                        }
                    }
                }

                Spacer(Modifier.height(16.dp))

                // ✨ MODERN SEARCH BAR WITH SEARCH BUTTON
                if (selectedTab != SearchTab.SUGGESTED) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        OutlinedTextField(
                            value = query,
                            onValueChange = { query = it },
                            modifier = Modifier
                                .weight(1f)
                                .scale(searchBarScale),
                            placeholder = {
                                Text(
                                    "Search ${selectedTab.name.lowercase()}...",
                                    color = textSecondary
                                )
                            },
                            leadingIcon = {
                                Icon(
                                    Icons.Default.Search,
                                    contentDescription = null,
                                    tint = PrimaryOrange
                                )
                            },
                            trailingIcon = {
                                if (query.isNotEmpty()) {
                                    IconButton(onClick = {
                                        query = ""
                                        hasSearched = false
                                    }) {
                                        Icon(
                                            Icons.Default.Clear,
                                            contentDescription = "Clear",
                                            tint = textSecondary
                                        )
                                    }
                                }
                            },
                            singleLine = true,
                            shape = RoundedCornerShape(16.dp),
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedBorderColor = PrimaryOrange,
                                unfocusedBorderColor = if (isDarkTheme) Color(0xFF333333) else Color(0xFFE0E0E0),
                                focusedContainerColor = surfaceColor,
                                unfocusedContainerColor = surfaceColor,
                                focusedTextColor = textPrimary,
                                unfocusedTextColor = textPrimary
                            ),
                            keyboardOptions = KeyboardOptions(imeAction = ImeAction.Search),
                            keyboardActions = KeyboardActions(onSearch = { performSearch() })
                        )

                        Button(
                            onClick = { performSearch() },
                            enabled = query.isNotBlank(),
                            colors = ButtonDefaults.buttonColors(
                                containerColor = PrimaryOrange,
                                contentColor = Color.White,
                                disabledContainerColor = PrimaryOrange.copy(alpha = 0.5f),
                                disabledContentColor = Color.White.copy(alpha = 0.5f)
                            ),
                            shape = RoundedCornerShape(16.dp),
                            modifier = Modifier.height(56.dp)
                        ) {
                            Text("Search", fontWeight = FontWeight.Bold)
                        }
                    }
                    Spacer(Modifier.height(16.dp))
                }

                // ✨ CONTENT WITH ANIMATIONS
                val bottomPadding = if (playerState?.value?.currentSong != null) 156.dp else 96.dp

                LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    contentPadding = PaddingValues(bottom = bottomPadding),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    when (selectedTab) {
                        SearchTab.SUGGESTED -> {
                            items(randomSongs, key = { it.id }) { song ->
                                AnimatedVisibility(
                                    visible = true,
                                    enter = fadeIn() + slideInVertically()
                                ) {
                                    SongItem(
                                        song = song,
                                        onClick = { onPlaySong(song, randomSongs) },
                                        onArtistClick = {},
                                        onAddToQueue = { playerViewModel?.addToQueue(song) },
                                        isDarkTheme = isDarkTheme
                                    )
                                }
                            }
                        }

                        SearchTab.SONGS -> {
                            if (songs.isNotEmpty()) {
                                items(songs, key = { it.id }) { song ->
                                    AnimatedVisibility(
                                        visible = true,
                                        enter = fadeIn() + slideInVertically()
                                    ) {
                                        SongItem(
                                            song = song,
                                            onClick = { onPlaySong(song, songs) },
                                            onArtistClick = {},
                                            onAddToQueue = { playerViewModel?.addToQueue(song) },
                                            isDarkTheme = isDarkTheme
                                        )
                                    }
                                }

                                if (hasMoreSongs && !isLoading) {
                                    item {
                                        LoadMoreButton(
                                            onClick = { viewModel.searchSongs(query, loadMore = true) },
                                            isDarkTheme = isDarkTheme
                                        )
                                    }
                                }

                                if (isLoading) {
                                    item {
                                        Box(
                                            modifier = Modifier
                                                .fillMaxWidth()
                                                .padding(16.dp),
                                            contentAlignment = Alignment.Center
                                        ) {
                                            CircularProgressIndicator(color = PrimaryOrange)
                                        }
                                    }
                                }
                            } else if (hasSearched && !isLoading) {
                                item { NoResult(isDarkTheme) }
                            } else if (isLoading && songs.isEmpty()) {
                                item {
                                    Box(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .padding(32.dp),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        CircularProgressIndicator(color = PrimaryOrange)
                                    }
                                }
                            }
                        }

                        SearchTab.ARTISTS -> {
                            if (artistResults.isNotEmpty()) {
                                items(artistResults, key = { it }) { artist ->
                                    ArtistResultCard(
                                        name = artist,
                                        onClick = { onNavigateToArtist(artist) },
                                        isDarkTheme = isDarkTheme
                                    )
                                }

                                if (hasMoreArtists && !isLoading) {
                                    item {
                                        LoadMoreButton(
                                            onClick = { viewModel.searchArtists(query, loadMore = true) },
                                            isDarkTheme = isDarkTheme
                                        )
                                    }
                                }

                                if (isLoading) {
                                    item {
                                        Box(
                                            modifier = Modifier
                                                .fillMaxWidth()
                                                .padding(16.dp),
                                            contentAlignment = Alignment.Center
                                        ) {
                                            CircularProgressIndicator(color = PrimaryOrange)
                                        }
                                    }
                                }
                            } else if (hasSearched && !isLoading) {
                                item { NoResult(isDarkTheme) }
                            } else if (isLoading && artistResults.isEmpty()) {
                                item {
                                    Box(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .padding(32.dp),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        CircularProgressIndicator(color = PrimaryOrange)
                                    }
                                }
                            }
                        }

                        SearchTab.ALBUMS -> {
                            if (albumResults.isNotEmpty()) {
                                items(albumResults, key = { it.id }) { album ->
                                    AlbumResultCard(
                                        album = album,
                                        isDarkTheme = isDarkTheme,
                                        onClick = { onNavigateToAlbum(album.name) }
                                    )
                                }

                                if (hasMoreAlbums && !isLoading) {
                                    item {
                                        LoadMoreButton(
                                            onClick = { viewModel.searchAlbums(query, loadMore = true) },
                                            isDarkTheme = isDarkTheme
                                        )
                                    }
                                }

                                if (isLoading) {
                                    item {
                                        Box(
                                            modifier = Modifier
                                                .fillMaxWidth()
                                                .padding(16.dp),
                                            contentAlignment = Alignment.Center
                                        ) {
                                            CircularProgressIndicator(color = PrimaryOrange)
                                        }
                                    }
                                }
                            } else if (hasSearched && !isLoading) {
                                item { NoResult(isDarkTheme) }
                            } else if (isLoading && albumResults.isEmpty()) {
                                item {
                                    Box(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .padding(32.dp),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        CircularProgressIndicator(color = PrimaryOrange)
                                    }
                                }
                            }
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
                        currentPosition = playerState.value.currentPosition,  // ADD THIS
                        duration = playerState.value.duration,
                        onPlayPause = { vm.playPause() },
                        onClick = onNavigateToPlayer,
                        isDarkTheme = isDarkTheme
                    )
                }
            }
        }
    }
}

@Composable
private fun ArtistResultCard(
    name: String,
    onClick: () -> Unit,
    isDarkTheme: Boolean
) {
    val cardColor = if (isDarkTheme) DarkCard else LightCard
    val textPrimary = if (isDarkTheme) TextPrimaryDark else TextPrimaryLight
    val textSecondary = if (isDarkTheme) TextSecondaryDark else TextSecondaryLight

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = cardColor),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(48.dp)
                    .background(
                        Brush.linearGradient(
                            colors = listOf(GradientStart, GradientEnd)
                        ),
                        CircleShape
                    ),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    Icons.Default.Person,
                    contentDescription = null,
                    tint = Color.White,
                    modifier = Modifier.size(28.dp)
                )
            }

            Spacer(Modifier.width(16.dp))

            Column(modifier = Modifier.weight(1f)) {
                Text(
                    name,
                    fontWeight = FontWeight.Bold,
                    fontSize = 16.sp,
                    color = textPrimary
                )
                Text(
                    "Artist",
                    color = textSecondary,
                    fontSize = 13.sp
                )
            }

            Icon(
                Icons.Default.ChevronRight,
                contentDescription = null,
                tint = textSecondary
            )
        }
    }
}

@Composable
private fun AlbumResultCard(
    album: com.example.musicplayer.data.model.Album,
    isDarkTheme: Boolean,
    onClick: () -> Unit = {}
) {
    val cardColor = if (isDarkTheme) DarkCard else LightCard
    val textPrimary = if (isDarkTheme) TextPrimaryDark else TextPrimaryLight
    val textSecondary = if (isDarkTheme) TextSecondaryDark else TextSecondaryLight

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = cardColor),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(48.dp)
                    .background(
                        Brush.linearGradient(
                            colors = listOf(GradientStart, GradientEnd)
                        ),
                        RoundedCornerShape(12.dp)
                    ),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    Icons.Default.Album,
                    contentDescription = null,
                    tint = Color.White,
                    modifier = Modifier.size(28.dp)
                )
            }

            Spacer(Modifier.width(16.dp))

            Column(modifier = Modifier.weight(1f)) {
                Text(
                    album.name,
                    fontWeight = FontWeight.Bold,
                    fontSize = 16.sp,
                    color = textPrimary
                )
                Text(
                    "Album",
                    color = textSecondary,
                    fontSize = 13.sp
                )
            }

            Icon(
                Icons.Default.ChevronRight,
                contentDescription = null,
                tint = textSecondary
            )
        }
    }
}

@Composable
private fun LoadMoreButton(
    onClick: () -> Unit,
    isDarkTheme: Boolean
) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 16.dp),
        contentAlignment = Alignment.Center
    ) {
        Button(
            onClick = onClick,
            colors = ButtonDefaults.buttonColors(
                containerColor = PrimaryOrange,
                contentColor = Color.White
            ),
            shape = RoundedCornerShape(12.dp),
            modifier = Modifier.padding(horizontal = 32.dp)
        ) {
            Icon(
                Icons.Default.ExpandMore,
                contentDescription = null,
                modifier = Modifier.size(20.dp)
            )
            Spacer(Modifier.width(8.dp))
            Text("Load More", fontWeight = FontWeight.Bold)
        }
    }
}

@Composable
private fun NoResult(isDarkTheme: Boolean) {
    val textPrimary = if (isDarkTheme) TextPrimaryDark else TextPrimaryLight
    val textSecondary = if (isDarkTheme) TextSecondaryDark else TextSecondaryLight

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(top = 64.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Icon(
            Icons.Default.SearchOff,
            contentDescription = null,
            modifier = Modifier.size(80.dp),
            tint = textSecondary.copy(alpha = 0.4f)
        )
        Spacer(Modifier.height(16.dp))
        Text(
            "No results found",
            fontWeight = FontWeight.Bold,
            fontSize = 18.sp,
            color = textPrimary
        )
        Spacer(Modifier.height(4.dp))
        Text(
            "Try another keyword",
            color = textSecondary,
            fontSize = 14.sp
        )
    }
}