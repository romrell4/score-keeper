package com.romrell4.scorekeeper

interface Platform {
    val name: String
}

expect fun getPlatform(): Platform