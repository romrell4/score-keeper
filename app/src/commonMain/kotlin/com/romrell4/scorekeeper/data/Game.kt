package com.romrell4.scorekeeper.data

import kotlinx.serialization.Serializable
import org.jetbrains.compose.resources.StringResource
import scorekeeper.app.generated.resources.Res
import scorekeeper.app.generated.resources.mormon_bridge_display_name

@Serializable
enum class Game(override val displayValue: StringResource, val configOptions: List<ConfigOption>): Displayable {
    MORMON_BRIDGE(Res.string.mormon_bridge_display_name, emptyList()/*MormonBridge.Option.entries*/),
}

interface Displayable {
    val displayValue: StringResource
}

interface ConfigOption : Displayable {
    val values: List<ConfigOptionValue>
}

sealed interface ConfigOptionValue : Displayable

//data class MormonBridge(
//    val players: List<Player>,
//    val roundConfigValue: RoundConfigValue,
//) {
//    enum class Option(
//        override val values: List<ConfigOptionValue>
//    ) : ConfigOption {
//        ROUND_CONFIG(RoundConfigValue.entries),
//        ;
//
//        override val displayValue: StringResource
//            get() = when (this) {
//                ROUND_CONFIG -> Res.string.mormon_bridge_round_config_title
//            }
//    }
//
//    @Serializable
//    enum class RoundConfigValue : ConfigOptionValue {
//        DOWN_AND_UP_BY_ONES,
//        DOWN_ONLY,
//        ODDS_DOWN_EVENS_UP,
//        ;
//
//        override val displayValue: StringResource
//            get() = when (this) {
//                DOWN_AND_UP_BY_ONES -> Res.string.mormon_bridge_down_and_up_by_ones
//                DOWN_ONLY -> Res.string.mormon_bridge_down_only
//                ODDS_DOWN_EVENS_UP -> Res.string.mormon_bridge_odds_down_evens_up
//            }
//    }
//}
