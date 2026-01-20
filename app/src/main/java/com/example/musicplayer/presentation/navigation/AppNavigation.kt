package com.example.musicplayer.presentation.navigation

import androidx.compose.foundation.layout.Column
import androidx.compose.runtime.Composable
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.example.musicplayer.presentation.screens.MiniPlayer
import com.example.musicplayer.presentation.screens.SearchScreen
import com.example.musicplayer.viewmodel.PlayerViewModel
import com.example.musicplayer.viewmodel.SearchViewModel

@Composable
fun AppNavigation(
    searchViewModel: SearchViewModel,
    playerViewModel: PlayerViewModel
) {

    val navController = rememberNavController()

    NavHost(
        navController = navController,
        startDestination = "search"
    ) {
        composable("search") {
            Column {
                SearchScreen(searchViewModel, playerViewModel)
                MiniPlayer(playerViewModel)
            }
        }
    }
}
