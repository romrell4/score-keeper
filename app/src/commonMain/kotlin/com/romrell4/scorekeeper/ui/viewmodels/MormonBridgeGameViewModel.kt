package com.romrell4.scorekeeper.ui.viewmodels

import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextDecoration
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.CreationExtras
import com.romrell4.scorekeeper.data.Player
import com.romrell4.scorekeeper.ui.withSign
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlin.math.abs
import kotlin.reflect.KClass

data class MormonBridgeGameViewState(
    val mainContent: MainContent,
    val scoreboardSheetContent: Scoreboard? = null,
) {
    sealed interface MainContent

    data class SelectRoundStyle(val cards: List<Card>) : MainContent {
        data class Card(val title: String, val subtitle: String)
    }

    data class SelectDealer(val gridPlayers: List<Player>) : MainContent
    data class Bidding(
        val dealerText: String,
        val cards: List<Card>,
        val totalBid: TotalBid,
        val startRoundEnabled: Boolean,
    ) : MainContent {
        data class Card(val player: Player, val bid: Int)
    }

    data class TotalBid(
        val totalText: String,
        val overUnderText: String,
        val textColor: Color,
    )

    data class Scoring(
        val firstPlayerText: String,
        val totalBid: TotalBid,
        val gridPlayerCards: List<Card>,
        val ctaEnabled: Boolean,
    ) : MainContent {
        data class Card(
            val player: Player,
            val score: Int,
            val bid: Int,
            val backgroundColor: Color,
            val thumbRotation: Float,
        )
    }

    data class ShowScores(
        val title: String,
        val cards: List<Card>,
        val ctaEnabled: Boolean,
    ) : MainContent {
        data class Card(
            val player: Player,
            val previousScore: Int,
            val scoreDelta: String,
            val newScore: Int,
        )
    }

    data class Scoreboard(
        val headerCells: List<String>,
        val columns: List<List<Cell>>,
        val editDialogContent: EditDialogViewState?,
    ) {
        val rowCount: Int = columns.first().size

        data class Cell(
            val text: String,
            val editableRoundInfo: EditableRoundInfo? = null,
            val fontWeight: FontWeight? = null,
            val textDecoration: TextDecoration? = null,
        )

        data class EditableRoundInfo(val playerId: String, val roundIndex: Int)

        data class EditDialogViewState(
            val subtitle: String,
            val bid: Int,
            val madeBid: Boolean,
        )
    }
}

class MormonBridgeGameViewModel(
    players: List<Player>,
) : ViewModel() {
    private data class State(
        val players: List<Player>,
        val roundStyle: RoundStyle,
        val dealerIndex: Int,
        val currentRoundIndex: Int,
        val currentPhase: Phase,
        val playerRoundResults: Map<String, List<PlayerRoundResult>>,
        val showingBottomSheet: Boolean,
        val showingScoreboardEditDialogWithData: MormonBridgeGameViewState.Scoreboard.EditableRoundInfo?,
    ) {
        val currentRoundNumCards: Int
            get() = roundStyle.roundCardCounts[currentRoundIndex]

        fun toViewState(): MormonBridgeGameViewState = MormonBridgeGameViewState(
            mainContent = when (currentPhase) {
                Phase.SELECT_DEALER -> MormonBridgeGameViewState.SelectDealer(players.orderIntoGridCircle())
                Phase.SELECT_SCORE_STYLE -> MormonBridgeGameViewState.SelectRoundStyle(
                    cards = RoundStyle.entries.map {
                        MormonBridgeGameViewState.SelectRoundStyle.Card(
                            title = it.displayText,
                            subtitle = it.roundCardCounts.joinToString(", ")
                        )
                    }
                )

                Phase.BID -> {
                    val totalBid = playerRoundResults.values.sumOf { it.last().bid }
                    MormonBridgeGameViewState.Bidding(
                        dealerText = "${players[dealerIndex].name} is dealing ($currentRoundNumCards cards)",
                        cards = players.indexAfter(dealerIndex).let { firstBidderIndex ->
                            (players.drop(firstBidderIndex) + players.take(firstBidderIndex)).map {
                                MormonBridgeGameViewState.Bidding.Card(
                                    player = it,
                                    bid = playerRoundResults.getValue(it.id).last().bid
                                )
                            }
                        },
                        totalBid = totalBid(),
                        startRoundEnabled = totalBid != currentRoundNumCards,
                    )
                }

                Phase.SCORE -> MormonBridgeGameViewState.Scoring(
                    firstPlayerText = "${players[dealerIndex].name} leads the first trick",
                    totalBid = totalBid(),
                    gridPlayerCards = players.map { player ->
                        val roundResults = playerRoundResults.getValue(player.id)
                        val madeBid = roundResults.last().madeBid
                        MormonBridgeGameViewState.Scoring.Card(
                            player = player,
                            // Only include past rounds in the score
                            score = roundResults.dropLast(1).sumOf { it.score },
                            bid = roundResults.last().bid,
                            backgroundColor = (if (madeBid) Color.Green else Color.Red).copy(
                                alpha = 0.2f
                            ),
                            thumbRotation = if (madeBid) 0f else 180f,
                        )
                    },
                    ctaEnabled = playerRoundResults.values.any { !it.last().madeBid },
                )

                Phase.SHOW_SCORES -> {
                    val hasNextRound = currentRoundIndex < roundStyle.roundCardCounts.lastIndex
                    MormonBridgeGameViewState.ShowScores(
                        title = if (hasNextRound) "Round ${currentRoundIndex + 1} Scores" else "Final Scores",
                        cards = players.map { player ->
                            val roundResults = playerRoundResults.getValue(player.id)
                            val currentScore = roundResults.sumOf { it.score }
                            val previousScore = currentScore - roundResults.last().score
                            val delta = currentScore - previousScore
                            MormonBridgeGameViewState.ShowScores.Card(
                                player = player,
                                previousScore = previousScore,
                                scoreDelta = "${if (delta > 0) "+" else "-"} ${abs(delta)}",
                                newScore = currentScore,
                            )
                        },
                        ctaEnabled = hasNextRound
                    )
                }
            },
            scoreboardSheetContent = if (showingBottomSheet) {
                MormonBridgeGameViewState.Scoreboard(
                    headerCells = players.map { it.name },
                    columns = computeScoreboardColumns(),
                    editDialogContent = showingScoreboardEditDialogWithData?.let { (playerId, roundIndex) ->
                        val roundResults = playerRoundResults.getValue(playerId)
                        val result = roundResults[roundIndex]
                        MormonBridgeGameViewState.Scoreboard.EditDialogViewState(
                            subtitle = "Round ${roundIndex + 1} - ${roundStyle.roundCardCounts[roundIndex].let { if (it > 1) "$it cards" else "$it card"}} - ${players.first { it.id == playerId }.name}",
                            bid = result.bid,
                            madeBid = result.madeBid,
                        )
                    },
                )
            } else null,
        )

        private fun totalBid(): MormonBridgeGameViewState.TotalBid {
            val totalBid = playerRoundResults.values.sumOf { it.last().bid }
            return MormonBridgeGameViewState.TotalBid(
                totalText = "Total Bid: $totalBid / $currentRoundNumCards",
                overUnderText = (totalBid - currentRoundNumCards).let { diff ->
                    "(${if (diff == 0) "on-bid" else "${abs(diff)} ${if (diff > 0) "over" else "under"}"})"
                },
                textColor = if (totalBid == currentRoundNumCards) Color.Red else Color.Unspecified
            )
        }

        private fun computeScoreboardColumns(): List<List<MormonBridgeGameViewState.Scoreboard.Cell>> {
            return playerRoundResults.entries.map { (playerId, roundResults) ->
                roundResults.mapIndexed { roundIndex, result ->
                    val roundInProgress =
                        roundIndex == roundResults.lastIndex && currentPhase <= Phase.SCORE
                    if (roundIndex == 0) {
                        listOf(
                            MormonBridgeGameViewState.Scoreboard.Cell(
                                // If the round is still in progress, just show the bid
                                text = if (roundInProgress) result.bid.toString() else result.score.toString(),
                                // First round is editable, despite not being styled as a "math" row
                                editableRoundInfo = MormonBridgeGameViewState.Scoreboard.EditableRoundInfo(
                                    playerId = playerId,
                                    roundIndex = roundIndex,
                                ),
                            ),
                        )
                    } else {
                        listOfNotNull(
                            // Add a math cell before the end result cell
                            MormonBridgeGameViewState.Scoreboard.Cell(
                                // If the round is still in progress, don't show +/- yet. Just show the bid
                                text = if (roundInProgress) result.bid.toString() else result.score.withSign(),
                                editableRoundInfo = MormonBridgeGameViewState.Scoreboard.EditableRoundInfo(
                                    playerId = playerId,
                                    roundIndex = roundIndex,
                                ),
                                fontWeight = FontWeight.Light,
                                textDecoration = TextDecoration.Underline,
                            ),
                            MormonBridgeGameViewState.Scoreboard.Cell(
                                // Take score sum of all the rounds up to this point (including this one)
                                text = roundResults.take(roundIndex + 1).sumOf { it.score }
                                    .toString()
                            ).takeUnless { roundInProgress }
                        )
                    }
                }.flatten()
            }
        }
    }

    private enum class RoundStyle(val displayText: String, val roundCardCounts: List<Int>) {
        SEVEN_DOWN_AND_UP(
            displayText = "Seven Down & Up",
            roundCardCounts = listOf(7, 6, 5, 4, 3, 2, 1, 2, 3, 4, 5, 6, 7)
        ),
        SEVEN_DOWN(displayText = "Seven Down", roundCardCounts = listOf(7, 6, 5, 4, 3, 2, 1)),
        ODDS_DOWN_EVENS_UP(
            displayText = "Seven Down (odds), Back Up (evens)",
            roundCardCounts = listOf(7, 5, 3, 1, 2, 4, 6)
        ),
    }

    private data class PlayerRoundResult(val bid: Int, val madeBid: Boolean) {
        val score: Int
            get() = (10 + bid) * if (madeBid) 1 else -1
    }

    private enum class Phase {
        SELECT_DEALER, SELECT_SCORE_STYLE, BID, SCORE, SHOW_SCORES,
    }

    private val stateFlow = MutableStateFlow(
        State(
            players = players,
            roundStyle = RoundStyle.SEVEN_DOWN_AND_UP,
            dealerIndex = 0,
            currentRoundIndex = 0,
            currentPhase = Phase.SELECT_DEALER,
            playerRoundResults = players.associate { it.id to listOf() },
            showingBottomSheet = false,
            showingScoreboardEditDialogWithData = null,
        )
    )

    val viewStateFlow: StateFlow<MormonBridgeGameViewState> = stateFlow.map { it.toViewState() }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.Eagerly,
            initialValue = stateFlow.value.toViewState(),
        )

    fun onDealerSelected(dealer: Player) {
        stateFlow.update {
            it.copy(
                dealerIndex = stateFlow.value.players.indexOf(dealer),
                currentPhase = Phase.SELECT_SCORE_STYLE,
            )
        }
    }

    fun onRoundStyleSelected(index: Int) {
        stateFlow.update {
            val roundStyle = RoundStyle.entries[index]
            it.copy(
                roundStyle = roundStyle,
                currentPhase = Phase.BID,
                playerRoundResults = it.playerRoundResults.addNewRound(),
            )
        }
    }

    fun onIncreaseBidTapped(playerId: String) {
        adjustLastPlayerResult(playerId) {
            it.copy(bid = (it.bid + 1).coerceAtMost(stateFlow.value.currentRoundNumCards))
        }
    }

    fun onDecreaseBidTapped(playerId: String) {
        adjustLastPlayerResult(playerId) {
            it.copy(bid = (it.bid - 1).coerceAtLeast(0))
        }
    }

    fun onStartRoundTapped() {
        stateFlow.update {
            it.copy(currentPhase = Phase.SCORE)
        }
    }

    fun onScoringPlayerCardTapped(playerId: String) {
        adjustLastPlayerResult(playerId) {
            it.copy(madeBid = !it.madeBid)
        }
    }

    fun onScoreRoundTapped() {
        stateFlow.update {
            it.copy(
                currentPhase = Phase.SHOW_SCORES
            )
        }
    }

    fun onNextRoundTapped() {
        stateFlow.update {
            it.copy(
                dealerIndex = it.players.indexAfter(it.dealerIndex),
                playerRoundResults = it.playerRoundResults.addNewRound(),
                currentRoundIndex = it.currentRoundIndex + 1,
                currentPhase = Phase.BID,
            )
        }
    }

    private fun Map<String, List<PlayerRoundResult>>.addNewRound() = mapValues { (_, results) ->
        results + PlayerRoundResult(bid = 0, madeBid = true)
    }

    private fun adjustLastPlayerResult(
        playerId: String,
        newResult: (currentResult: PlayerRoundResult) -> PlayerRoundResult,
    ) {
        stateFlow.update {
            val playerRoundResults = it.playerRoundResults.toMutableMap()
            val playerResults = playerRoundResults.getValue(playerId).toMutableList()
            playerResults[playerResults.lastIndex] = newResult(playerResults.last())
            playerRoundResults[playerId] = playerResults
            it.copy(playerRoundResults = playerRoundResults)
        }
    }

    fun updateSheetVisibility(isVisible: Boolean) {
        stateFlow.update {
            it.copy(showingBottomSheet = isVisible)
        }
    }

    fun onScoreboardCellTapped(playerId: String, roundIndex: Int) {
        stateFlow.update {
            it.copy(
                showingScoreboardEditDialogWithData = MormonBridgeGameViewState.Scoreboard.EditableRoundInfo(
                    playerId = playerId,
                    roundIndex = roundIndex,
                )
            )
        }
    }

    fun onScoreboardEditDialogSaved(bid: Int, madeBid: Boolean) {
        val showingScoreboardEditDialogWithData =
            stateFlow.value.showingScoreboardEditDialogWithData ?: return
        stateFlow.update {
            it.copy(
                playerRoundResults = it.playerRoundResults.toMutableMap().apply {
                    val playerResults =
                        getValue(showingScoreboardEditDialogWithData.playerId).toMutableList()
                    playerResults[showingScoreboardEditDialogWithData.roundIndex] =
                        PlayerRoundResult(
                            bid = bid,
                            madeBid = madeBid,
                        )
                    this[showingScoreboardEditDialogWithData.playerId] = playerResults
                },
                showingScoreboardEditDialogWithData = null
            )
        }
    }

    fun onScoreboardEditDialogDismissed() {
        stateFlow.update {
            it.copy(showingScoreboardEditDialogWithData = null)
        }
    }

    data class Factory(val players: List<Player>) : ViewModelProvider.Factory {
        override fun <T : ViewModel> create(modelClass: KClass<T>, extras: CreationExtras): T {
            @Suppress("UNCHECKED_CAST")
            return MormonBridgeGameViewModel(players) as T
        }
    }
}

fun <T> List<T>.orderIntoGridCircle(): List<T> {
    var leftStart = 0
    var rightStart = 1
    val newList = mutableListOf<T>()

    fun addFromIndex(index: Int) {
        this.getOrNull(if (index < 0) this.size + index else index)?.let { newList.add(it) }
    }

    while (newList.size < size) {
        addFromIndex(leftStart--)
        if (newList.size < size) {
            addFromIndex(rightStart++)
        }
    }
    return newList
}

private fun List<*>.indexAfter(index: Int) = (index + 1) % size