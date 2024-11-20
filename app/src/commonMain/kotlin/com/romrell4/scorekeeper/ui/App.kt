package com.romrell4.scorekeeper.ui

import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.toRoute
import com.romrell4.scorekeeper.data.Game
import com.romrell4.scorekeeper.ui.screens.ConfigureGameScreen
import com.romrell4.scorekeeper.ui.screens.SelectGameScreen
import kotlinx.serialization.Serializable

@Composable
fun App() {
    MyApplicationTheme {
        val navController = rememberNavController()

        NavHost(
            navController = navController,
            startDestination = Screen.SelectGame,
            modifier = Modifier.fillMaxSize()
        ) {
            composable<Screen.SelectGame> {
                SelectGameScreen(
                    onGameTapped = { navController.navigate(Screen.ConfigureGame(it)) }
                )
            }
            composable<Screen.ConfigureGame> {
                val route = it.toRoute<Screen.ConfigureGame>()
                ConfigureGameScreen(game = route.game)
            }
        }
    }
}

sealed interface Screen {
    @Serializable
    data object SelectGame : Screen

    @Serializable
    data class ConfigureGame(val game: Game) : Screen
}
