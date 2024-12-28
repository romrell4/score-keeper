package com.romrell4.scorekeeper.ui.screens

import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.lifecycle.viewmodel.compose.viewModel
import com.romrell4.scorekeeper.data.Player
import com.romrell4.scorekeeper.ui.viewmodels.RookGameViewModel
import scorekeeper.app.generated.resources.Res
import scorekeeper.app.generated.resources.rook_display_name

@Composable
fun RookGameScreen(
    players: List<Player>,
    viewModel: RookGameViewModel = viewModel(factory = RookGameViewModel.Factory(players))
) {
    ScreenScaffold(
        title = Res.string.rook_display_name,
    ) { innerPadding ->
        val viewState by viewModel.viewStateFlow.collectAsState()
    }
}