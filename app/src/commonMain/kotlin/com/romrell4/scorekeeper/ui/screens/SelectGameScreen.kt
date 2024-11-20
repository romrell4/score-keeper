package com.romrell4.scorekeeper.ui.screens

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.material3.Card
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.romrell4.scorekeeper.data.Game
import com.romrell4.scorekeeper.ui.TopBar
import scorekeeper.app.generated.resources.Res
import scorekeeper.app.generated.resources.pick_game_app_bar_title

@Composable
fun SelectGameScreen(onGameTapped: (Game) -> Unit) {
    Scaffold(topBar = { TopBar(Res.string.pick_game_app_bar_title) }) { innerPadding ->
        val minCardSize = 150.dp
        LazyVerticalGrid(
            columns = GridCells.Adaptive(minSize = minCardSize),
            modifier = Modifier.padding(innerPadding).fillMaxSize().padding(8.dp)
        ) {
            items(items = Game.entries) { game ->
                GameCard(
                    game = game,
                    onTapped = { onGameTapped(game) },
                    modifier = Modifier
                        .padding(8.dp)
                        .height(minCardSize),
                )
            }
        }
    }
}

@Composable
private fun GameCard(game: Game, onTapped: () -> Unit, modifier: Modifier = Modifier) {
    Card(modifier = modifier, onClick = onTapped) {
        Box(contentAlignment = Alignment.Center, modifier = Modifier.fillMaxSize()) {
            Text(game.displayName, style = MaterialTheme.typography.titleMedium)
        }
    }
}