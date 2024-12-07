package com.romrell4.scorekeeper.ui.screens

import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import com.romrell4.scorekeeper.ui.TopBar
import org.jetbrains.compose.resources.StringResource

@Composable
fun ScreenScaffold(title: StringResource, content: @Composable (PaddingValues) -> Unit) {
    Scaffold(topBar = { TopBar(title) }, content = content)
}