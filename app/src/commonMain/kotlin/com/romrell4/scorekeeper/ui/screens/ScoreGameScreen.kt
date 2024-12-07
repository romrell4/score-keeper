package com.romrell4.scorekeeper.ui.screens

import androidx.compose.runtime.Composable
import com.romrell4.scorekeeper.data.ConfigOptionValue
import com.romrell4.scorekeeper.data.Game
import com.romrell4.scorekeeper.data.Player

@Composable
fun ScoreGameScreen(game: Game, options: List<ConfigOptionValue>, players: List<Player>) {
    when (game) {
        Game.MORMON_BRIDGE -> MormonBridgeGameScreen(players = players)
    }
}
