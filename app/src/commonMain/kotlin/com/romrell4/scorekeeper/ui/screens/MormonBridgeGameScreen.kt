package com.romrell4.scorekeeper.ui.screens

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.snapping.SnapPosition
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.sizeIn
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material.icons.filled.KeyboardArrowUp
import androidx.compose.material.icons.outlined.ThumbUp
import androidx.compose.material3.Button
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.max
import androidx.compose.ui.window.Dialog
import androidx.lifecycle.viewmodel.compose.viewModel
import com.romrell4.scorekeeper.data.Player
import com.romrell4.scorekeeper.getScreenWidth
import com.romrell4.scorekeeper.ui.applyIfNotNull
import com.romrell4.scorekeeper.ui.shared.CenteredCard
import com.romrell4.scorekeeper.ui.shared.RoundBorderedBox
import com.romrell4.scorekeeper.ui.shared.Table
import com.romrell4.scorekeeper.ui.viewmodels.MormonBridgeGameViewModel
import com.romrell4.scorekeeper.ui.viewmodels.MormonBridgeGameViewState
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import scorekeeper.app.generated.resources.Res
import scorekeeper.app.generated.resources.mormon_bridge_display_name

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MormonBridgeGameScreen(
    players: List<Player>,
    viewModel: MormonBridgeGameViewModel = viewModel(
        factory = MormonBridgeGameViewModel.Factory(players)
    )
) {
    ScreenScaffold(
        title = Res.string.mormon_bridge_display_name,
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
            Spacer(modifier = Modifier.weight(1f))
            RoundBorderedBox {
                when (val vs = viewState.mainContent) {
                    is MormonBridgeGameViewState.SelectDealer -> SelectDealerSection(
                        viewState = vs,
                        onDealerSelected = { viewModel.onDealerSelected(it) }
                    )

                    is MormonBridgeGameViewState.SelectRoundStyle -> SelectRoundStyle(
                        viewState = vs,
                        onCardTapped = viewModel::onRoundStyleSelected
                    )

                    is MormonBridgeGameViewState.Bidding -> BiddingSection(
                        viewState = vs,
                        increaseBidTapped = viewModel::onIncreaseBidTapped,
                        decreaseBidTapped = viewModel::onDecreaseBidTapped,
                        startRoundTapped = viewModel::onStartRoundTapped
                    )

                    is MormonBridgeGameViewState.Scoring -> ScoringSection(
                        viewState = vs,
                        onPlayerCardTapped = viewModel::onScoringPlayerCardTapped,
                        onScoreRoundTapped = viewModel::onScoreRoundTapped,
                    )

                    is MormonBridgeGameViewState.ShowScores -> ShowScoresSection(
                        viewState = vs,
                        onNextRoundTapped = viewModel::onNextRoundTapped,
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
                ScoreboardSheetContent(it, onCellTapped = viewModel::onScoreboardCellTapped)
            }

            it.editDialogContent?.let {
                Dialog(onDismissRequest = { viewModel.onScoreboardEditDialogDismissed() }) {
                    ScoreboardEditDialogContent(
                        viewState = it,
                        onSaveTapped = viewModel::onScoreboardEditDialogSaved,
                    )
                }
            }
        }
    }
}

@Composable
private fun SelectDealerSection(
    viewState: MormonBridgeGameViewState.SelectDealer,
    onDealerSelected: (Player) -> Unit,
) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Text(
            "Welcome to Mormon Bridge!",
            style = MaterialTheme.typography.titleLarge
        )
        Text(
            "Please select the first dealer:",
            style = MaterialTheme.typography.bodyMedium
        )

        Spacer(modifier = Modifier.height(16.dp))

        LazyVerticalGrid(
            columns = GridCells.Fixed(count = 2),
        ) {
            items(viewState.gridPlayers) { player ->
                CenteredCard(modifier = Modifier.clickable { onDealerSelected(player) }) {
                    Text(
                        text = player.name,
                        style = MaterialTheme.typography.bodyLarge,
                    )
                }
            }
        }
    }
}

@Composable
private fun SelectRoundStyle(
    viewState: MormonBridgeGameViewState.SelectRoundStyle,
    onCardTapped: (index: Int) -> Unit,
) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Text(
            text = "Select a scoring option:",
            style = MaterialTheme.typography.titleLarge
        )
        viewState.cards.forEachIndexed { index, card ->
            CenteredCard(
                modifier = Modifier.clickable { onCardTapped(index) }
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text(
                        text = card.title,
                        style = MaterialTheme.typography.bodyLarge,
                        textAlign = TextAlign.Center,
                    )
                    Text(text = card.subtitle, style = MaterialTheme.typography.bodySmall)
                }
            }
        }
    }
}

@Composable
private fun BiddingSection(
    viewState: MormonBridgeGameViewState.Bidding,
    increaseBidTapped: (playerId: String) -> Unit,
    decreaseBidTapped: (playerId: String) -> Unit,
    startRoundTapped: () -> Unit
) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Text(
            text = viewState.dealerText,
            style = MaterialTheme.typography.titleLarge,
            modifier = Modifier.padding(8.dp)
        )
        TotalBidText(viewState.totalBid)
        val pagerState = rememberPagerState { viewState.cards.size }
        val onLastPage = pagerState.currentPage == pagerState.pageCount - 1
        HorizontalPager(
            modifier = Modifier.padding(8.dp),
            state = pagerState,
            snapPosition = SnapPosition.Center,
            contentPadding = PaddingValues(horizontal = 16.dp),
        ) {
            PlayerBidCard(
                viewState = viewState.cards[it],
                onIncreaseTapped = { increaseBidTapped(viewState.cards[it].player.id) },
                onDecreaseTapped = { decreaseBidTapped(viewState.cards[it].player.id) }
            )
        }
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.Center
        ) {
            repeat(pagerState.pageCount) {
                Box(
                    modifier = Modifier
                        .padding(8.dp)
                        .background(
                            color = if (pagerState.currentPage == it) Color.DarkGray else Color.LightGray,
                            shape = CircleShape
                        )
                        .size(8.dp)
                )
            }
        }
        val coroutineScope = rememberCoroutineScope()
        Button(
            onClick = {
                if (onLastPage) {
                    startRoundTapped()
                } else {
                    coroutineScope.launch {
                        pagerState.animateScrollToPage(pagerState.currentPage + 1)
                    }
                }
            },
            // Only disabled when on the last player and the bid equals the number of cards dealt
            enabled = !onLastPage || viewState.startRoundEnabled,
            modifier = Modifier
                .padding(8.dp)
                .fillMaxWidth()
        ) {
            Text(if (onLastPage) "Start Round" else "Next Bidder")
        }
    }
}

@Composable
private fun TotalBidText(viewState: MormonBridgeGameViewState.TotalBid) {
    Row(verticalAlignment = Alignment.CenterVertically) {
        Text(
            text = viewState.totalText,
            style = MaterialTheme.typography.bodyLarge,
            color = viewState.textColor,
        )
        Spacer(modifier = Modifier.width(8.dp))
        Text(
            text = viewState.overUnderText,
            style = MaterialTheme.typography.bodyMedium,
            fontStyle = FontStyle.Italic,
            color = viewState.textColor,
        )
    }
}


@Composable
private fun PlayerBidCard(
    viewState: MormonBridgeGameViewState.Bidding.Card,
    onIncreaseTapped: () -> Unit,
    onDecreaseTapped: () -> Unit,
) {
    CenteredCard {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Text(
                modifier = Modifier.padding(8.dp),
                text = viewState.player.name,
                style = MaterialTheme.typography.titleLarge,
            )
            Row(verticalAlignment = Alignment.CenterVertically) {
                val iconSize = 40.dp
                Box(
                    contentAlignment = Alignment.Center,
                    modifier = Modifier
                        .size(iconSize)
                        .clickable { onDecreaseTapped() },
                ) {
                    Icon(
                        imageVector = Icons.Filled.KeyboardArrowDown,
                        contentDescription = "Decrease bid",
                    )
                }
                Text(
                    modifier = Modifier.padding(8.dp),
                    text = "Bid: ${viewState.bid}",
                    style = MaterialTheme.typography.bodyLarge,
                )
                Box(
                    contentAlignment = Alignment.Center,
                    modifier = Modifier
                        .size(iconSize)
                        .clickable { onIncreaseTapped() },
                ) {
                    Icon(
                        imageVector = Icons.Filled.KeyboardArrowUp,
                        contentDescription = "Increase bid"
                    )
                }
            }
        }
    }
}

@Composable
private fun ScoringSection(
    viewState: MormonBridgeGameViewState.Scoring,
    onPlayerCardTapped: (playerId: String) -> Unit,
    onScoreRoundTapped: () -> Unit,
) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Text(
            text = "Play the Round!",
            style = MaterialTheme.typography.titleLarge,
            modifier = Modifier.padding(8.dp),
        )
        Text(text = viewState.firstPlayerText, style = MaterialTheme.typography.bodyLarge)
        Spacer(modifier = Modifier.height(4.dp))
        TotalBidText(viewState.totalBid)
        Text(
            text = "When finished, tap the players who missed their bid:",
            style = MaterialTheme.typography.titleMedium,
            textAlign = TextAlign.Center,
            modifier = Modifier.padding(8.dp),
        )
        LazyVerticalGrid(
            columns = GridCells.Fixed(count = 2),
        ) {
            items(viewState.gridPlayerCards) { card ->
                CenteredCard(
                    modifier = Modifier.clickable { onPlayerCardTapped(card.player.id) },
                    colors = CardDefaults.cardColors(containerColor = card.backgroundColor)
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text(
                            text = card.player.name,
                            style = MaterialTheme.typography.titleLarge,
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = "Current score: ${card.score}",
                            style = MaterialTheme.typography.bodyMedium
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text(
                                text = "Bid: ${card.bid}",
                                style = MaterialTheme.typography.titleMedium
                            )
                            Icon(
                                imageVector = Icons.Outlined.ThumbUp,
                                tint = MaterialTheme.colorScheme.onBackground,
                                contentDescription = "This player made their bid",
                                modifier = Modifier
                                    .padding(horizontal = 8.dp)
                                    .rotate(card.thumbRotation),
                            )
                        }
                    }
                }
            }
        }
        Button(
            onClick = { onScoreRoundTapped() },
            enabled = viewState.ctaEnabled,
            modifier = Modifier
                .padding(8.dp)
                .fillMaxWidth()
        ) {
            Text("Score Round")
        }
    }
}

@Composable
private fun ShowScoresSection(
    viewState: MormonBridgeGameViewState.ShowScores,
    onNextRoundTapped: () -> Unit,
) {
    var cards by remember { mutableStateOf(viewState.cards.sortedByDescending { it.previousScore }) }
    var showingCalculation by remember { mutableStateOf(false) }
    var showingRoundScoreDelta by remember { mutableStateOf(false) }
    val roundScoreDeltaAlpha: Float by animateFloatAsState(
        targetValue = if (showingRoundScoreDelta) 1f else 0f,
        label = "Round Score Delta Alpha"
    )
    var showingNewScore by remember { mutableStateOf(false) }
    val newScoreAlpha: Float by animateFloatAsState(
        targetValue = if (showingNewScore) 1f else 0f,
        label = "New Score Alpha"
    )
    var ctaEnabled by remember { mutableStateOf(false) }

    LaunchedEffect(viewState) {
        delay(1000)
        showingCalculation = true
        delay(500)
        showingRoundScoreDelta = true
        delay(1000)
        showingNewScore = true
        delay(2000)
        ctaEnabled = true
        cards = viewState.cards.sortedByDescending { it.newScore }
    }

    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Text(text = viewState.title, style = MaterialTheme.typography.titleLarge)
        Text(
            text = "Here are the scores ${if (showingNewScore) "after" else "before"} this round:",
            style = MaterialTheme.typography.titleMedium,
            textAlign = TextAlign.Center,
            modifier = Modifier.padding(8.dp),
        )
        LazyColumn {
            items(cards, key = { it.player.id }) { card ->
                CenteredCard(
                    modifier = Modifier.animateItem()
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text(
                            text = card.player.name,
                            style = MaterialTheme.typography.titleLarge,
                        )
                        // Min width helps account for the not-yet visible scores (so that it doesn't look as glitchy)
                        Column(
                            horizontalAlignment = Alignment.End,
                            modifier = Modifier.sizeIn(minHeight = 64.dp)
                        ) {
                            // Min width helps account for the not-yet visible scores (so that it doesn't look as glitchy)
                            val minScoreWidth = 36.dp
                            Row {
                                Text(
                                    text = "Previous Score:",
                                    style = MaterialTheme.typography.bodyMedium
                                )
                                Text(
                                    text = "${card.previousScore}",
                                    style = MaterialTheme.typography.bodyMedium,
                                    textAlign = TextAlign.End,
                                    modifier = Modifier.sizeIn(minWidth = minScoreWidth)
                                )
                            }
                            AnimatedVisibility(showingCalculation) {
                                Column(horizontalAlignment = Alignment.End) {
                                    Text(
                                        text = card.scoreDelta,
                                        style = MaterialTheme.typography.bodyMedium,
                                        textDecoration = TextDecoration.Underline,
                                        modifier = Modifier
                                            .alpha(roundScoreDeltaAlpha)
                                    )
                                    Row {
                                        Text(
                                            text = "New Score:",
                                            style = MaterialTheme.typography.titleMedium,
                                            modifier = Modifier.alpha(newScoreAlpha),
                                        )
                                        Text(
                                            text = "${card.newScore}",
                                            style = MaterialTheme.typography.titleMedium,
                                            textAlign = TextAlign.End,
                                            modifier = Modifier
                                                .alpha(newScoreAlpha)
                                                .sizeIn(minWidth = minScoreWidth),
                                        )
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
        if (viewState.ctaEnabled) {
            Button(
                onClick = { onNextRoundTapped() },
                enabled = ctaEnabled,
                modifier = Modifier
                    .padding(8.dp)
                    .fillMaxWidth()
            ) {
                Text("Next Round")
            }
        }
    }
}

@Composable
private fun ScoreboardSheetContent(
    viewState: MormonBridgeGameViewState.Scoreboard,
    onCellTapped: (playerId: String, roundIndex: Int) -> Unit
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
        val screenWidth = getScreenWidth()
        Table(
            modifier = Modifier.padding(bottom = 16.dp),
            columnCount = viewState.headerCells.size,
            rowCount = viewState.rowCount,
            cellWidth = { max(80.dp, screenWidth / viewState.headerCells.size) },
            headerContent = { columnIndex ->
                Text(
                    text = viewState.headerCells[columnIndex],
                    style = MaterialTheme.typography.titleMedium,
                )
            },
            rowContent = { columnIndex, rowIndex ->
                val cell = viewState.columns[columnIndex][rowIndex]
                Text(
                    text = cell.text,
                    style = MaterialTheme.typography.bodyMedium,
                    modifier = Modifier.applyIfNotNull(cell.editableRoundInfo) {
                        clickable {
                            onCellTapped(it.playerId, it.roundIndex)
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
private fun ScoreboardEditDialogContent(
    viewState: MormonBridgeGameViewState.Scoreboard.EditDialogViewState,
    onSaveTapped: (bid: Int, madeBid: Boolean) -> Unit,
) {
    CenteredCard(shape = RoundedCornerShape(16.dp)) {
        var bid by remember { mutableIntStateOf(viewState.bid) }
        var madeBid by remember { mutableStateOf(viewState.madeBid) }
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier.fillMaxWidth()
        ) {
            Text(
                text = "Edit Score",
                style = MaterialTheme.typography.titleLarge,
                textAlign = TextAlign.Center,
            )
            Text(viewState.subtitle, style = MaterialTheme.typography.bodyMedium)
            Spacer(modifier = Modifier.height(16.dp))
            Row(verticalAlignment = Alignment.CenterVertically) {
                val iconSize = 40.dp
                Box(
                    contentAlignment = Alignment.Center,
                    modifier = Modifier
                        .size(iconSize)
                        .clickable { bid = (bid - 1).coerceAtLeast(0) },
                ) {
                    Icon(
                        imageVector = Icons.Filled.KeyboardArrowDown,
                        contentDescription = "Decrease bid",
                    )
                }
                Text(
                    modifier = Modifier.padding(8.dp),
                    text = "Bid: $bid",
                    style = MaterialTheme.typography.bodyLarge,
                )
                Box(
                    contentAlignment = Alignment.Center,
                    modifier = Modifier
                        .size(iconSize)
                        .clickable { bid++ },
                ) {
                    Icon(
                        imageVector = Icons.Filled.KeyboardArrowUp,
                        contentDescription = "Increase bid"
                    )
                }
            }
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text("Made Bid?")
                IconButton(onClick = { madeBid = !madeBid }) {
                    Icon(
                        imageVector = Icons.Outlined.ThumbUp,
                        tint = MaterialTheme.colorScheme.onBackground,
                        contentDescription = "This player made their bid",
                        modifier = Modifier.rotate(if (madeBid) 0f else 180f)
                    )
                }
            }
            Text("New round score: ${(bid + 10) * if (madeBid) 1 else -1}")
            Spacer(modifier = Modifier.height(16.dp))
            Button(
                onClick = { onSaveTapped(bid, madeBid) },
                modifier = Modifier
                    .fillMaxWidth()
            ) {
                Text("Save")
            }
        }
    }
}
