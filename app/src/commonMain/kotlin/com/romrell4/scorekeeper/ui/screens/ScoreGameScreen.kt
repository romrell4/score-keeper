package com.romrell4.scorekeeper.ui.screens

import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import com.romrell4.scorekeeper.data.ConfigOptionValue
import com.romrell4.scorekeeper.data.Game
import com.romrell4.scorekeeper.data.Player
import scorekeeper.app.generated.resources.Res
import scorekeeper.app.generated.resources.mormon_bridge_display_name

@Composable
fun ScoreGameScreen(game: Game, options: List<ConfigOptionValue>, players: List<Player>) {
    ScreenScaffold(title = Res.string.mormon_bridge_display_name) { innerPadding ->
        when (game) {
            Game.MORMON_BRIDGE -> MormonBridgeGameScreen(
                players = players,
                modifier = Modifier.padding(innerPadding)
            )
        }
    }
}
