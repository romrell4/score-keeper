package com.romrell4.scorekeeper.ui.shared

import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import com.romrell4.scorekeeper.ui.MyApplicationTheme

@Composable
fun ThemedSurface(content: @Composable () -> Unit) {
    MyApplicationTheme { Surface { content() } }
}
