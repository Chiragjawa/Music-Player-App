package com.example.musicplayer.presentation.navigation

import androidx.compose.runtime.Composable
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.example.musicplayer.presentation.screens.ArtistScreen
import com.example.musicplayer.presentation.screens.PlayerScreen
import com.example.musicplayer.presentation.screens.SearchScreen
import com.example.musicplayer.viewmodel.ArtistViewModel
import com.example.musicplayer.viewmodel.MusicPlayerViewModel
import com.example.musicplayer.viewmodel.SearchViewModel

@Composable
fun AppNavigation() {

    val navController = rememberNavController()

    NavHost(
        navController = navController,
        startDestination = "search"
    ) {

        /* ---------------- SEARCH ---------------- */

        composable("search") {
            val searchViewModel: SearchViewModel = hiltViewModel()
            val playerViewModel: MusicPlayerViewModel = hiltViewModel()

            SearchScreen(
                searchViewModel = searchViewModel,
                playerViewModel = playerViewModel,
                onNavigateToPlayer = {
                    navController.navigate("player")
                },
                onNavigateToArtist = { artistName ->
                    navController.navigate("artist/$artistName")
                }
            )
        }

        /* ---------------- PLAYER ---------------- */

        composable("player") {
            val playerViewModel: MusicPlayerViewModel = hiltViewModel()

            PlayerScreen(
                playerViewModel = playerViewModel,
                onNavigateBack = {
                    navController.popBackStack()
                }
            )
        }

        /* ---------------- ARTIST ---------------- */

        composable(
            route = "artist/{artistName}",
            arguments = listOf(
                navArgument("artistName") {
                    type = NavType.StringType
                }
            )
        ) {
            val artistViewModel: ArtistViewModel = hiltViewModel()
            val playerViewModel: MusicPlayerViewModel = hiltViewModel()

            val artistName = it.arguments?.getString("artistName") ?: ""

            ArtistScreen(
                artistName = artistName,
                artistViewModel = artistViewModel,
                playerViewModel = playerViewModel,
                onBack = {
                    navController.popBackStack()
                },
                onNavigateToPlayer = {
                    navController.navigate("player")
                }
            )
        }
    }
}
