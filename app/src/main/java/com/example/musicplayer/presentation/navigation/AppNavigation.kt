package com.example.musicplayer.presentation.navigation

import androidx.compose.runtime.Composable
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.example.musicplayer.presentation.screens.PlayerScreen
import com.example.musicplayer.presentation.screens.SearchScreen
import com.example.musicplayer.viewmodel.MusicPlayerViewModel
import com.example.musicplayer.viewmodel.SearchViewModel

@Composable
fun AppNavigation(
    searchViewModel: SearchViewModel = hiltViewModel(),
    playerViewModel: MusicPlayerViewModel = hiltViewModel()
) {
    val navController = rememberNavController()

    NavHost(
        navController = navController,
        startDestination = "search"
    ) {
        composable("search") {
            SearchScreen(
                searchViewModel = searchViewModel,
                playerViewModel = playerViewModel,
                onNavigateToPlayer = {
                    navController.navigate("player")
                }
            )
        }

        composable("player") {
            PlayerScreen(
                playerViewModel = playerViewModel,
                onNavigateBack = {
                    navController.popBackStack()
                }
            )
        }
    }
}