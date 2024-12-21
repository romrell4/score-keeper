package com.romrell4.scorekeeper.ui

import androidx.compose.ui.Modifier

inline fun <T> Modifier.applyIfNotNull(
    argument: T?,
    ifNull: Modifier.() -> Modifier = { this },
    ifNotNull: Modifier.(T) -> Modifier,
): Modifier {
    return if (argument != null) {
        then(ifNotNull(Modifier, argument))
    } else {
        then(ifNull(Modifier))
    }
}

fun Int.withSign(): String {
    return if (this >= 0) {
        "+$this"
    } else {
        this.toString()
    }
}