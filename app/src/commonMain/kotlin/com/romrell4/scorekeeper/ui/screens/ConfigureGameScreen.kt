package com.romrell4.scorekeeper.ui.screens

import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import com.romrell4.scorekeeper.data.Game
import com.romrell4.scorekeeper.ui.TopBar
import scorekeeper.app.generated.resources.Res
import scorekeeper.app.generated.resources.configure_game_app_bar_title

@Composable
fun ConfigureGameScreen(game: Game) {
    Scaffold(topBar = { TopBar(Res.string.configure_game_app_bar_title) }) { innerPadding ->
        Text("Configure Game: ${game.displayName}", modifier = Modifier.padding(innerPadding))
    }
}