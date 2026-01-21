package com.example.musicplayer.presentation.navigation

import androidx.compose.runtime.Composable
import androidx.navigation.compose.*
import com.example.musicplayer.presentation.screens.*
import com.example.musicplayer.viewmodel.MusicPlayerViewModel
import androidx.hilt.navigation.compose.hiltViewModel

@Composable
fun AppNavigation() {
    val navController = rememberNavController()
    val playerViewModel: MusicPlayerViewModel = hiltViewModel()

    NavHost(navController, startDestination = "search") {

        composable("search") {
            SearchScreen(
                onNavigateToArtist = {
                    navController.navigate("artist/$it")
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
    }
}
