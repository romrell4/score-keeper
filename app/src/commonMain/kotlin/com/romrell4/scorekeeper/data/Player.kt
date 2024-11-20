package com.romrell4.scorekeeper.data

import kotlinx.serialization.Serializable

@Serializable
data class Player(
    val id: String,
    val name: String,
)