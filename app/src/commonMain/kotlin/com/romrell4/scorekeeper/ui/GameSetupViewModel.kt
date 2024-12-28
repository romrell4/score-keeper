package com.romrell4.scorekeeper.ui

import androidx.lifecycle.ViewModel
import com.romrell4.scorekeeper.data.Game
import com.romrell4.scorekeeper.data.Player

class GameSetupViewModel : ViewModel() {
    lateinit var selectedGame: Game
    var selectedPlayers: List<Player> = emptyList()
}