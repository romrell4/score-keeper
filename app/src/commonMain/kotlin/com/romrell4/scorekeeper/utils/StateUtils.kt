package com.romrell4.scorekeeper.utils

import androidx.compose.runtime.MutableState

fun <T> MutableState<T>.update(block: (T) -> T) {
    value = block(value)
}