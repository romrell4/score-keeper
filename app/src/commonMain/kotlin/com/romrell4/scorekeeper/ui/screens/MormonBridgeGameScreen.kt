package com.romrell4.scorekeeper.ui.screens

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material.icons.filled.KeyboardArrowUp
import androidx.compose.material.icons.filled.ThumbUp
import androidx.compose.material.icons.outlined.ThumbUp
import androidx.compose.material3.Button
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.romrell4.scorekeeper.data.Player
import com.romrell4.scorekeeper.utils.update
import scorekeeper.app.generated.resources.Res
import scorekeeper.app.generated.resources.mormon_bridge_display_name

private data class ReadOnlyRow(val cells: List<Int>, val isMathRow: Boolean)
private data class ActionRow(val cells: List<RoundData>)
private data class RoundData(val bid: Int = 0, val result: Boolean? = null)

@Composable
fun MormonBridgeGameScreen(players: List<Player>) {
    ScreenScaffold(title = Res.string.mormon_bridge_display_name) { innerPadding ->
        Column(
            modifier = Modifier.padding(innerPadding)
                .fillMaxSize()
                .padding(horizontal = 8.dp)
        ) {
            val displayRows = remember { mutableStateListOf<ReadOnlyRow>() }
            val actionRow =
                remember { mutableStateOf(ActionRow(List(players.size) { RoundData() })) }
            var preRoundPhase by remember { mutableStateOf(true) }

            fun updateAction(columnIndex: Int, newValue: RoundData) {
                actionRow.update {
                    it.copy(
                        cells = it.cells.mapIndexed { index, roundData ->
                            if (index == columnIndex) newValue else roundData
                        }
                    )
                }
            }

            Table(
                columnCount = players.size,
                // Add one for the header and one for the footer
                rowCount = displayRows.size + 2,
                cellWidth = { 80.dp },
                headerContent = { columnIndex ->
                    Row {
                        Text(
                            text = players[columnIndex].name,
                            style = MaterialTheme.typography.titleMedium,
                            modifier = Modifier.padding(end = 4.dp),
                        )
                    }
                },
                rowContent = { columnIndex, rowIndex ->
                    displayRows.getOrNull(rowIndex)?.let { row ->
                        row.cells.getOrNull(columnIndex)?.let {
                            if (row.isMathRow) {
                                Text(
                                    text = if (it >= 0) "+$it" else "$it",
                                    fontWeight = FontWeight.Light,
                                    style = MaterialTheme.typography.bodyMedium,
                                    textDecoration = TextDecoration.Underline,
                                )
                            } else {
                                Text(
                                    text = "$it",
                                    style = MaterialTheme.typography.bodyMedium,
                                )
                            }
                        }
                    }
                },
                footerContent = { columnIndex ->
                    actionRow.value.cells.getOrNull(columnIndex)?.let { roundData ->
                        ActionCell(
                            roundData = roundData,
                            preRoundPhase = preRoundPhase,
                            onDecreaseTapped = {
                                updateAction(
                                    columnIndex,
                                    roundData.copy(bid = maxOf(0, roundData.bid - 1))
                                )
                            },
                            onIncreaseTapped = {
                                updateAction(columnIndex, roundData.copy(bid = roundData.bid + 1))
                            },
                            onThumbsDownTapped = {
                                updateAction(
                                    columnIndex,
                                    roundData.copy(result = if (roundData.result == false) null else false)
                                )
                            },
                            onThumbsUpTapped = {
                                updateAction(
                                    columnIndex,
                                    roundData.copy(result = if (roundData.result == true) null else true)
                                )
                            },
                        )
                    }
                }
            )
            Spacer(modifier = Modifier.weight(1f))
            Button(
                onClick = {
                    if (!preRoundPhase) {
                        val isFirstRound = displayRows.isEmpty()

                        // Add a row with the results of this previous round
                        displayRows.add(
                            ReadOnlyRow(
                                cells = actionRow.value.cells.map {
                                    // Add 10 to the bid, and make negative if the result was a missed bid
                                    ((it.bid + 10) * if (it.result == false) -1 else 1)
                                },
                                // If it's the first round, this is the actual beginning score. Otherwise, it's a math row
                                isMathRow = !isFirstRound
                            )
                        )

                        // Add a new read-only row for the totalling (if there was a round already)
                        if (!isFirstRound) {
                            displayRows.add(
                                ReadOnlyRow(
                                    cells = displayRows[displayRows.lastIndex - 1].cells.zip(
                                        displayRows[displayRows.lastIndex].cells
                                    ).map { (last, current) -> last + current },
                                    isMathRow = false,
                                )
                            )
                        }

                        // Add new bid entries for the next round
                        actionRow.update { ActionRow(List(players.size) { RoundData() }) }
                    }
                    preRoundPhase = !preRoundPhase
                },
                modifier = Modifier.fillMaxWidth()
            ) {
                Text(
                    text = if (preRoundPhase) "Start Round" else "Next Round"
                )
            }
        }
    }
}

@Composable
private fun ActionCell(
    roundData: RoundData,
    preRoundPhase: Boolean,
    onDecreaseTapped: () -> Unit,
    onIncreaseTapped: () -> Unit,
    onThumbsDownTapped: () -> Unit,
    onThumbsUpTapped: () -> Unit,
) {
    Box(contentAlignment = Alignment.Center) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            val iconSize = 40.dp
            if (preRoundPhase) {
                Box(
                    contentAlignment = Alignment.Center,
                    modifier = Modifier
                        .size(iconSize)
                        .clickable { onDecreaseTapped() }
                ) {
                    Icon(
                        Icons.Filled.KeyboardArrowDown,
                        contentDescription = "Decrease bid",
                    )
                }
                Box(
                    contentAlignment = Alignment.Center,
                    modifier = Modifier
                        .size(iconSize)
                        .clickable { onIncreaseTapped() },
                ) {
                    Icon(
                        Icons.Filled.KeyboardArrowUp,
                        contentDescription = "Increase bid"
                    )
                }
            } else {
                val (positiveIcon, negativeIcon) = when (roundData.result) {
                    true -> Icons.Filled.ThumbUp to Icons.Outlined.ThumbUp
                    false -> Icons.Outlined.ThumbUp to Icons.Filled.ThumbUp
                    null -> Icons.Outlined.ThumbUp to Icons.Outlined.ThumbUp
                }
                Box(
                    contentAlignment = Alignment.Center,
                    modifier = Modifier
                        .size(iconSize)
                        .clickable { onThumbsDownTapped() }
                ) {
                    Icon(
                        negativeIcon,
                        contentDescription = "Player did not get their bid",
                        modifier = Modifier.rotate(180f)
                    )
                }
                Box(
                    contentAlignment = Alignment.Center,
                    modifier = Modifier
                        .size(iconSize)
                        .clickable { onThumbsUpTapped() }
                ) {
                    Icon(
                        positiveIcon,
                        contentDescription = "Player got their bid"
                    )
                }
            }
        }
        Text(roundData.bid.toString())
    }
}

@Composable
fun Table(
    columnCount: Int,
    rowCount: Int,
    cellWidth: (index: Int) -> Dp,
    headerContent: @Composable (columnIndex: Int) -> Unit,
    rowContent: @Composable (columnIndex: Int, rowIndex: Int) -> Unit,
    footerContent: @Composable (columnIndex: Int) -> Unit,
) {
    LazyRow(modifier = Modifier.verticalScroll(rememberScrollState())) {
        items(columnCount) { columnIndex ->
            Column {
                // Include 0 and data.size for the header and footer
                (0..rowCount).forEach { rowIndex ->
                    Box(
                        contentAlignment = Alignment.Center,
                        modifier = Modifier
                            .width(cellWidth(columnIndex))
                    ) {
                        when (rowIndex) {
                            // First row is always the header
                            0 -> headerContent(columnIndex)
                            // Last row is always the footer
                            rowCount -> footerContent(columnIndex)
                            // All other rows are content (subtract one for the header)
                            else -> rowContent(columnIndex, rowIndex - 1)
                        }
                    }
                }
            }
        }
    }
}