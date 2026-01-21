package com.example.musicplayer.presentation.navigation

import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.navigation.compose.*
import androidx.navigation.compose.currentBackStackEntryAsState
import com.example.musicplayer.presentation.components.MiniPlayer
import com.example.musicplayer.presentation.screens.*
import com.example.musicplayer.viewmodel.MusicPlayerViewModel
import androidx.hilt.navigation.compose.hiltViewModel

@Composable
fun AppNavigation() {
    val navController = rememberNavController()
    val playerViewModel: MusicPlayerViewModel = hiltViewModel()
    val playerState by playerViewModel.playerState.collectAsState()

    // Track current route to hide MiniPlayer on PlayerScreen
    val currentBackStackEntry by navController.currentBackStackEntryAsState()
    val currentRoute = currentBackStackEntry?.destination?.route

    Scaffold(
        bottomBar = {
            // Show MiniPlayer only when there's a current song AND not on player screen
            if (playerState.currentSong != null && currentRoute != "player") {
                MiniPlayer(
                    song = playerState.currentSong!!,
                    isPlaying = playerState.isPlaying,
                    onPlayPause = { playerViewModel.playPause() },
                    onClick = { navController.navigate("player") }
                )
            }
        }
    ) { paddingValues ->
        NavHost(
            navController,
            startDestination = "search",
            modifier = Modifier.padding(paddingValues)
        ) {
            composable("search") {
                SearchScreen(
                    onNavigateToArtist = {
                        navController.navigate("artist/$it")
                    },
                    onNavigateToAlbum = {
                        navController.navigate("album/$it")
                    },
                    onPlaySong = { song, queue ->
                        playerViewModel.playSong(song, queue)
                    }
                )
            }

            composable("artist/{artistName}") {
                val artistName = it.arguments?.getString("artistName") ?: ""
                ArtistScreen(
                    artistName = artistName,
                    onPlaySong = { song, queue ->
                        playerViewModel.playSong(song, queue)
                    }
                )
            }

            composable("album/{albumName}") {
                val albumName = it.arguments?.getString("albumName") ?: ""
                AlbumScreen(
                    albumName = albumName,
                    onPlaySong = { song, queue ->
                        playerViewModel.playSong(song, queue)
                    }
                )
            }

            // Full Player Screen
            composable("player") {
                PlayerScreen(
                    playerViewModel = playerViewModel,
                    onNavigateBack = { navController.popBackStack() },
                    onNavigateToQueue = { navController.navigate("queue") }
                )
            }

            // Queue Screen as dialog
            dialog("queue") {
                QueueScreen(
                    playerViewModel = playerViewModel,
                    onDismiss = { navController.popBackStack() }
                )
            }
        }
    }
}
