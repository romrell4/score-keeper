package com.romrell4.scorekeeper.ui.screens

import androidx.compose.runtime.Composable
import com.romrell4.scorekeeper.data.Game
import com.romrell4.scorekeeper.data.Player

@Composable
fun ScoreGameScreen(game: Game, players: List<Player>) {
    when (game) {
        Game.MORMON_BRIDGE -> MormonBridgeGameScreen(players = players)
        Game.ROOK -> RookGameScreen(players = players)
    }
}
