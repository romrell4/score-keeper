package com.romrell4.scorekeeper.data

enum class GameType {
    MORMON_BRIDGE,
}

sealed class Game

data class MormonBridge(
    val gameMode: GameMode,
): Game() {
    enum class GameMode {
        SEVEN_TO_ONE_AND_BACK,
        ODDS_DOWN_EVENS_UP,
        SEVEN_TO_ONE,
    }
}

