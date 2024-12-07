package com.romrell4.scorekeeper.data

import kotlinx.serialization.Serializable
import kotlin.uuid.ExperimentalUuidApi
import kotlin.uuid.Uuid

@Serializable
data class Player @OptIn(ExperimentalUuidApi::class) constructor(
    val name: String,
    val id: String = Uuid.random().toString(),
)