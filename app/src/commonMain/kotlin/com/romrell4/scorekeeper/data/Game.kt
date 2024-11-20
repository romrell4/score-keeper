package com.romrell4.scorekeeper.data

import kotlinx.serialization.Serializable

@Serializable
enum class Game(val displayName: String) {
    MORMON_BRIDGE("Mormon Bridge"),
    ROOK("Rook"),
}
