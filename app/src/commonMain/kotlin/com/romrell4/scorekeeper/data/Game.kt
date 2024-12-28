package com.romrell4.scorekeeper.data

import kotlinx.serialization.Serializable
import org.jetbrains.compose.resources.StringResource
import scorekeeper.app.generated.resources.Res
import scorekeeper.app.generated.resources.mormon_bridge_display_name
import scorekeeper.app.generated.resources.rook_display_name

@Serializable
enum class Game(val displayValue: StringResource) {
    MORMON_BRIDGE(Res.string.mormon_bridge_display_name),
    ROOK(Res.string.rook_display_name),
}
