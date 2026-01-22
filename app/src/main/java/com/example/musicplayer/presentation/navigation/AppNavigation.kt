package com.example.musicplayer.presentation.navigation

import android.net.Uri
import androidx.compose.runtime.*
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.example.musicplayer.presentation.screens.*
import com.example.musicplayer.presentation.theme.MusicPlayerTheme
import com.example.musicplayer.viewmodel.MusicPlayerViewModel

@Composable
fun AppNavigation() {
    val navController = rememberNavController()

    // ✨ THEME STATE - Persists across navigation
    var isDarkTheme by remember { mutableStateOf(true) }

    // ✨ SHARED PLAYER VIEWMODEL - Single source of truth
    val playerViewModel: MusicPlayerViewModel = hiltViewModel()
    val playerState by playerViewModel.playerState.collectAsState()

    MusicPlayerTheme(darkTheme = isDarkTheme) {

        NavHost(navController, startDestination = "search") {

            composable("search") {
                SearchScreen(
                    onNavigateToArtist = { artist ->
                        navController.navigate("artist/${Uri.encode(artist)}")
                    },
                    onPlaySong = { song, queue ->
                        playerViewModel.playSong(song, queue)
                    },
                    isDarkTheme = isDarkTheme,
                    onThemeToggle = { isDarkTheme = !isDarkTheme },
                    playerViewModel = playerViewModel,
                    onNavigateToPlayer = { navController.navigate("player") }
                )
            }

            composable("player") {
                PlayerScreen(
                    playerViewModel = playerViewModel,
                    onNavigateBack = { navController.popBackStack() },
                    isDarkTheme = isDarkTheme
                )
            }

            composable(
                route = "artist/{artistName}",
                arguments = listOf(navArgument("artistName") {
                    type = NavType.StringType
                })
            ) {
                val artistName = Uri.decode(it.arguments?.getString("artistName") ?: "")

                ArtistScreen(
                    artistName = artistName,
                    onPlaySong = { song, queue ->
                        playerViewModel.playSong(song, queue)
                    },
                    isDarkTheme = isDarkTheme
                )

                // Mini player is handled inside ArtistScreen composable
            }

            composable("queue") {
                QueueScreen(
                    playerViewModel = playerViewModel,
                    onDismiss = { navController.popBackStack() },
                    isDarkTheme = isDarkTheme
                )
            }
        }
    }
}