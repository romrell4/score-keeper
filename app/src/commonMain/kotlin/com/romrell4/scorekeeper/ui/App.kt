package com.romrell4.scorekeeper.ui

import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.romrell4.scorekeeper.ui.screens.ScoreGameScreen
import com.romrell4.scorekeeper.ui.screens.SelectGameScreen
import com.romrell4.scorekeeper.ui.screens.SelectPlayersScreen
import kotlinx.serialization.Serializable

@Composable
fun App() {
    MyApplicationTheme {
        val navController = rememberNavController()
        val viewModel = viewModel { GameSetupViewModel() }

        NavHost(
            navController = navController,
            startDestination = Screen.SelectGame,
            modifier = Modifier.fillMaxSize()
        ) {
            composable<Screen.SelectGame> {
                SelectGameScreen(
                    onGameTapped = {
                        viewModel.selectedGame = it
                        navController.navigate(Screen.SelectPlayers)
                    }
                )
            }
            composable<Screen.SelectPlayers> {
                SelectPlayersScreen(
                    onCtaTapped = { players ->
                        viewModel.selectedPlayers = players
                        navController.navigate(Screen.ScoreGame)
                    }
                )
            }
            composable<Screen.ScoreGame> {
                ScoreGameScreen(
                    game = viewModel.selectedGame,
                    players = viewModel.selectedPlayers,
                )
            }
        }
    }
}

sealed interface Screen {
    @Serializable
    data object SelectGame : Screen

    @Serializable
    data object ConfigureGame : Screen

    @Serializable
    data object SelectPlayers : Screen

    @Serializable
    data object ScoreGame : Screen
}
