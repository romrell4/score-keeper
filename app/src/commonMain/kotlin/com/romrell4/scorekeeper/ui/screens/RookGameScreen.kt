package com.romrell4.scorekeeper.ui.screens

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.lazy.grid.itemsIndexed
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Slider
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import androidx.lifecycle.viewmodel.compose.viewModel
import com.romrell4.scorekeeper.data.Player
import com.romrell4.scorekeeper.getScreenWidth
import com.romrell4.scorekeeper.ui.applyIfNotNull
import com.romrell4.scorekeeper.ui.shared.CenteredCard
import com.romrell4.scorekeeper.ui.shared.RoundBorderedBox
import com.romrell4.scorekeeper.ui.shared.Table
import com.romrell4.scorekeeper.ui.viewmodels.RookGameViewModel
import com.romrell4.scorekeeper.ui.viewmodels.RookGameViewState
import scorekeeper.app.generated.resources.Res
import scorekeeper.app.generated.resources.rook_display_name
import kotlin.math.roundToInt

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun RookGameScreen(
    players: List<Player>,
    viewModel: RookGameViewModel = viewModel(factory = RookGameViewModel.Factory(players))
) {
    ScreenScaffold(
        title = Res.string.rook_display_name,
        menuActions = {
            OutlinedButton(onClick = { viewModel.updateSheetVisibility(true) }) {
                Text("Scoreboard", color = MaterialTheme.colorScheme.onPrimary)
            }
        },
    ) { innerPadding ->
        val viewState by viewModel.viewStateFlow.collectAsState()
        Column(
            modifier = Modifier
                .padding(innerPadding)
                .fillMaxSize()
        ) {
            viewState.currentScore?.let {
                RoundBorderedBox {
                    CurrentScoresSection(it)
                }
            }
            Spacer(modifier = Modifier.weight(1f))
            RoundBorderedBox {
                when (val vs = viewState.mainContent) {
                    is RookGameViewState.SelectTeams -> SelectTeamsSection(
                        viewState = vs,
                        onCardSelected = viewModel::onTeamSelectionCardSelected,
                        onSelectTeamsCtaTapped = viewModel::onSelectTeamsCtaTapped,
                    )

                    is RookGameViewState.EnterBid -> EnterBidSection(
                        viewState = vs,
                        onBidChanged = viewModel::onBidChanged,
                        onTeamCardTapped = viewModel::onBidderTeamCardTapped,
                        onBidFinalized = viewModel::onBidFinalized,
                    )

                    is RookGameViewState.CollectScore -> CollectScoreSection(
                        viewState = vs,
                        onSliderChanged = viewModel::onCollectScoreSliderChanged,
                        onCtaTapped = viewModel::onCollectScoreCtaTapped,
                    )
                }
            }
            Spacer(modifier = Modifier.weight(1f))
        }

        viewState.scoreboardSheetContent?.let {
            ModalBottomSheet(
                onDismissRequest = { viewModel.updateSheetVisibility(false) },
                dragHandle = {},
            ) {
                ScoreboardSheetContent(it, onRowTapped = viewModel::onScoreboardRowTapped)
            }

            it.editDialogContent?.let {
                Dialog(onDismissRequest = { viewModel.onScoreboardEditDialogDismissed() }) {
                    ScoreboardEditDialogContent()
                }
            }
        }
    }
}

@Composable
private fun CurrentScoresSection(viewState: RookGameViewState.CurrentScoreSection) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Text(
            text = "Round ${viewState.roundNumber}",
            style = MaterialTheme.typography.titleLarge
        )
        LazyVerticalGrid(columns = GridCells.Fixed(count = 2)) {
            items(viewState.cards) { card ->
                CurrentTeamScore(viewState = card)
            }
        }
    }
}

@Composable
private fun CurrentTeamScore(viewState: RookGameViewState.CurrentScoreSection.Card) {
    CenteredCard {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            Text(
                text = viewState.teamName,
                style = MaterialTheme.typography.titleMedium
            )
            Text(
                text = viewState.score.toString(),
                style = MaterialTheme.typography.displaySmall
            )
        }
    }
}

@Composable
private fun SelectTeamsSection(
    viewState: RookGameViewState.SelectTeams,
    onCardSelected: (id: String) -> Unit,
    onSelectTeamsCtaTapped: () -> Unit,
) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Text("Select the players on team 1", style = MaterialTheme.typography.titleLarge)
        Spacer(modifier = Modifier.height(8.dp))
        LazyVerticalGrid(
            columns = GridCells.Fixed(count = 2),
        ) {
            items(viewState.cards) { card ->
                CenteredCard(
                    modifier = Modifier.clickable { onCardSelected(card.id) },
                    border = card.isSelected,
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text(
                            text = card.name,
                            style = MaterialTheme.typography.titleLarge,
                        )
                    }
                }
            }
        }
        Button(
            onClick = onSelectTeamsCtaTapped,
            enabled = viewState.ctaEnabled,
            modifier = Modifier
                .padding(8.dp)
                .fillMaxWidth()
        ) {
            Text("Continue")
        }
    }
}

@Composable
private fun EnterBidSection(
    viewState: RookGameViewState.EnterBid,
    onBidChanged: (bid: Int) -> Unit,
    onTeamCardTapped: (teamIndex: Int) -> Unit,
    onBidFinalized: () -> Unit,
) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Text("Enter the winning bid:", style = MaterialTheme.typography.titleLarge)
        Spacer(modifier = Modifier.height(8.dp))
        PointSlider(
            value = viewState.bid,
            range = 50..200,
            onValueChange = onBidChanged,
            modifier = Modifier
                .padding(8.dp)
                .fillMaxWidth()
        )
        Text("${viewState.bid}", style = MaterialTheme.typography.displayLarge)
        Spacer(modifier = Modifier.height(16.dp))
        Text(
            text = "Select the team that took the bid:",
            style = MaterialTheme.typography.titleLarge,
            modifier = Modifier.padding(bottom = 8.dp)
        )
        LazyVerticalGrid(columns = GridCells.Fixed(count = 2)) {
            itemsIndexed(viewState.teamCards) { index, card ->
                CenteredCard(
                    modifier = Modifier.clickable { onTeamCardTapped(index) },
                    border = card.isSelected,
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text(
                            text = card.name,
                            style = MaterialTheme.typography.bodyMedium,
                            textAlign = TextAlign.Center,
                            modifier = Modifier.padding(8.dp)
                        )
                    }
                }
            }
        }
        Button(
            onClick = onBidFinalized,
            enabled = viewState.ctaEnabled,
            modifier = Modifier
                .padding(8.dp)
                .fillMaxWidth()
        ) {
            Text("Continue")
        }
    }
}

@Composable
private fun CollectScoreSection(
    viewState: RookGameViewState.CollectScore,
    onSliderChanged: (newValue: Int) -> Unit,
    onCtaTapped: () -> Unit,
) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Text("Select the points won by each team", style = MaterialTheme.typography.titleLarge)
        Spacer(modifier = Modifier.height(8.dp))
        PointSlider(
            value = viewState.sliderValue,
            range = 0..200,
            onValueChange = onSliderChanged,
            modifier = Modifier
                .padding(8.dp)
                .fillMaxWidth()
        )
        LazyVerticalGrid(columns = GridCells.Fixed(count = 2)) {
            items(viewState.teamCards) { card ->
                CenteredCard {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text(
                            text = card.roundPointsWonText,
                            style = MaterialTheme.typography.headlineSmall,
                            textAlign = TextAlign.Center,
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = card.bidText,
                            style = MaterialTheme.typography.bodyMedium,
                            textAlign = TextAlign.Center,
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = "Round Score: ${card.proposedRoundScore}",
                            style = MaterialTheme.typography.bodyMedium,
                            textAlign = TextAlign.Center,
                        )
                    }
                }
            }
        }

        Button(
            onClick = onCtaTapped,
            enabled = viewState.ctaEnabled,
            modifier = Modifier
                .padding(8.dp)
                .fillMaxWidth()
        ) {
            Text("Continue")
        }
    }
}

@Composable
private fun PointSlider(
    value: Int,
    range: IntRange,
    onValueChange: (Int) -> Unit,
    modifier: Modifier = Modifier,
) {
    Slider(
        value = value.toFloat(),
        onValueChange = { onValueChange(it.roundToInt()) },
        valueRange = range.first.toFloat()..range.last.toFloat(),
        // Steps are exclusive (not including the ends of the range), hence the -1
        steps = (range.last - range.first) / 5 - 1,
        modifier = modifier
    )
}

@Composable
private fun ScoreboardSheetContent(
    viewState: RookGameViewState.Scoreboard,
    onRowTapped: (roundIndex: Int) -> Unit
) {
    Column {
        Text(
            text = "Scoreboard",
            style = MaterialTheme.typography.titleLarge,
            textAlign = TextAlign.Center,
            modifier = Modifier
                .padding(8.dp)
                .fillMaxWidth()
        )
        val bidColumnWidth = 80.dp
        val teamColumnWidth = (getScreenWidth() - bidColumnWidth) / 2
        Table(
            modifier = Modifier.padding(bottom = 16.dp),
            columnCount = viewState.headerCells.size,
            rowCount = viewState.rowCount,
            cellWidth = { if (it == 1) bidColumnWidth else teamColumnWidth },
            headerContent = { columnIndex ->
                Text(
                    text = viewState.headerCells[columnIndex],
                    style = MaterialTheme.typography.titleMedium,
                    textAlign = TextAlign.Center,
                )
            },
            rowContent = { columnIndex, rowIndex ->
                val cell = viewState.columns[columnIndex][rowIndex]
                Text(
                    text = cell.text,
                    style = MaterialTheme.typography.bodyMedium,
                    modifier = Modifier.applyIfNotNull(cell.editableRoundInfo) {
                        clickable {
                            onRowTapped(it.roundIndex)
                        }
                    },
                    fontWeight = cell.fontWeight,
                    textDecoration = cell.textDecoration,
                )
            },
        )
    }
}

@Composable
private fun ScoreboardEditDialogContent() {
    CenteredCard(shape = RoundedCornerShape(16.dp)) {
        Text("Coming Soon!")
    }
}
