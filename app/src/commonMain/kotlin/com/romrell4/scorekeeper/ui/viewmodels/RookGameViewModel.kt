package com.romrell4.scorekeeper.ui.viewmodels

import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextDecoration
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.CreationExtras
import com.romrell4.scorekeeper.data.Player
import com.romrell4.scorekeeper.ui.fillWith
import com.romrell4.scorekeeper.ui.withSign
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlin.math.max
import kotlin.reflect.KClass

data class RookGameViewState(
    val currentScore: CurrentScoreSection?,
    val mainContent: MainContent,
    val scoreboardSheetContent: Scoreboard?
) {
    data class CurrentScoreSection(
        val roundNumber: Int,
        val cards: List<Card>,
    ) {
        data class Card(val teamName: String, val score: Int)
    }

    sealed interface MainContent

    data class SelectTeams(val cards: List<Card>, val ctaEnabled: Boolean) : MainContent {
        data class Card(val id: String, val name: String, val isSelected: Boolean)
    }

    data class EnterBid(val bid: Int, val teamCards: List<Card>, val ctaEnabled: Boolean) :
        MainContent {
        data class Card(val name: String, val isSelected: Boolean)
    }

    data class CollectScore(
        val sliderValue: Int,
        val teamCards: List<Card>,
        val ctaEnabled: Boolean
    ) : MainContent {
        data class Card(
            val roundPointsWonText: String,
            val bidText: String,
            val proposedRoundScore: Int
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

        data class EditableRoundInfo(val roundIndex: Int)

        data class EditDialogViewState(
            val subtitle: String,
            val bidSliderValue: Int,
            val teamCards: List<TeamCard>,
            val pointsSliderValue: Int,
        ) {
            data class TeamCard(val name: String, val isSelected: Boolean)
        }
    }
}

class RookGameViewModel(
    players: List<Player>,
) : ViewModel() {
    private data class State(
        val players: List<Player>,
        val playerTeamMap: Map<String, Team>,
        val currentPhase: Phase,
        val roundData: List<RoundData>,
        val showingBottomSheet: Boolean,
        val showingScoreboardEditDialogWithData: RookGameViewState.Scoreboard.EditableRoundInfo?,
    ) {
        private val teams: Map<Team, List<Player>>
            get() = players.groupBy { playerTeamMap.getValue(it.id) }

        private val completedRounds = roundData.dropLast(1).filterIsInstance<RoundData.Ready>()

        private val List<Player>.teamName: String
            get() = joinToString(" & ") { it.name }

        private val List<Player>.abbreviatedTeamName: String
            get() = joinToString(" & ") {
                // Take the first letter from each word in the name and smash them together
                it.name.split(" ").joinToString("") { it.first().toString() }
            }

        fun toViewState(): RookGameViewState = RookGameViewState(
            currentScore = if (currentPhase > Phase.SELECT_TEAMS) {
                RookGameViewState.CurrentScoreSection(
                    roundNumber = completedRounds.size + 1,
                    cards = teams.map { (team, players) ->
                        RookGameViewState.CurrentScoreSection.Card(
                            teamName = players.teamName,
                            score = completedRounds.sumOf { it.score(team) }
                        )
                    }
                )
            } else null,
            mainContent = toMainContentViewState(),
            scoreboardSheetContent = if (showingBottomSheet) {
                RookGameViewState.Scoreboard(
                    headerCells = teams.entries.map { it.value.teamName }.fillWith("Bid"),
                    columns = teams.map { (team, _) ->
                        completedRounds.mapIndexed { roundIndex, result ->
                            val editableRoundInfo = RookGameViewState.Scoreboard.EditableRoundInfo(
                                roundIndex = roundIndex,
                            )
                            if (roundIndex == 0) {
                                listOf(
                                    RookGameViewState.Scoreboard.Cell(
                                        text = result.score(team).toString(),
                                        // First round is editable, despite not being styled as a "math" row
                                        editableRoundInfo = editableRoundInfo,
                                    ),
                                )
                            } else {
                                listOfNotNull(
                                    // Add a math cell before the end result cell
                                    RookGameViewState.Scoreboard.Cell(
                                        text = result.score(team).withSign(),
                                        editableRoundInfo = editableRoundInfo,
                                        fontWeight = FontWeight.Light,
                                        textDecoration = TextDecoration.Underline,
                                    ),
                                    RookGameViewState.Scoreboard.Cell(
                                        // Take score sum of all the rounds up to this point (including this one)
                                        text = completedRounds.take(roundIndex + 1)
                                            .sumOf { it.score(team) }.toString()
                                    )
                                )
                            }
                        }.flatten()
                    }.fillWith(
                        completedRounds.mapIndexed { roundIndex, result ->
                            val roundCell = RookGameViewState.Scoreboard.Cell(
                                text = "${if (result.biddingTeam == Team.TEAM1) "<-" else "   "} ${result.bid} ${if (result.biddingTeam == Team.TEAM2) "->" else "   "}",
                                editableRoundInfo = RookGameViewState.Scoreboard.EditableRoundInfo(
                                    roundIndex = roundIndex,
                                )
                            )
                            if (roundIndex == 0) {
                                listOf(roundCell)
                            } else {
                                listOf(roundCell, RookGameViewState.Scoreboard.Cell(""))
                            }
                        }.flatten()
                    ),
                    editDialogContent = showingScoreboardEditDialogWithData?.let {
                        RookGameViewState.Scoreboard.EditDialogViewState(
                            subtitle = "Edit round ${it.roundIndex + 1}",
                            bidSliderValue = completedRounds[it.roundIndex].bid,
                            teamCards = teams.map { (team, players) ->
                                RookGameViewState.Scoreboard.EditDialogViewState.TeamCard(
                                    name = players.teamName,
                                    isSelected = completedRounds[it.roundIndex].biddingTeam == team,
                                )
                            },
                            pointsSliderValue = completedRounds[it.roundIndex].biddingTeamPoints,
                        )
                    },
                )
            } else null,
        )

        private fun toMainContentViewState(): RookGameViewState.MainContent = when (currentPhase) {
            Phase.SELECT_TEAMS -> RookGameViewState.SelectTeams(
                cards = players.mapIndexed { index, player ->
                    RookGameViewState.SelectTeams.Card(
                        id = player.id,
                        name = player.name,
                        isSelected = playerTeamMap.getValue(player.id) == Team.TEAM1
                    )
                },
                // Can only proceed if both teams have 2 players
                ctaEnabled = playerTeamMap.values.filter { it == Team.TEAM1 }.size == 2
            )

            Phase.ENTER_BID -> {
                val currentRound = roundData.last() as RoundData.SettingUp
                RookGameViewState.EnterBid(
                    bid = currentRound.bid,
                    teamCards = teams.map { (team, players) ->
                        RookGameViewState.EnterBid.Card(
                            name = players.teamName,
                            isSelected = currentRound.biddingTeam == team,
                        )
                    },
                    ctaEnabled = currentRound.biddingTeam != null,
                )
            }

            Phase.COLLECT_SCORE -> {
                val currentRound = roundData.last() as RoundData.Ready
                RookGameViewState.CollectScore(
                    // Default the slider to start at the bidding team's score
                    sliderValue = currentRound.biddingTeamPoints,
                    teamCards = teams.entries
                        .sortedByDescending { currentRound.biddingTeam == it.key }
                        .map { (team, players) ->
                            RookGameViewState.CollectScore.Card(
                                roundPointsWonText = "${players.abbreviatedTeamName}: ${
                                    currentRound.points(
                                        team
                                    )
                                }",
                                bidText = if (currentRound.biddingTeam == team) "Took bid at: ${currentRound.bid}" else "",
                                proposedRoundScore = currentRound.score(team),
                            )
                        },
                    // Can only proceed if the bidding team has a valid score
                    ctaEnabled = currentRound.biddingTeamPoints in 0..180 || currentRound.biddingTeamPoints == 200,
                )
            }
        }
    }

    private enum class Team {
        TEAM1, TEAM2,
        ;

        fun switch(): Team = when (this) {
            TEAM1 -> TEAM2
            TEAM2 -> TEAM1
        }
    }

    private enum class Phase {
        SELECT_TEAMS,
        ENTER_BID,
        COLLECT_SCORE,
    }

    private sealed interface RoundData {
        val bid: Int

        data class SettingUp(override val bid: Int, val biddingTeam: Team?) : RoundData
        data class Ready(
            override val bid: Int,
            val biddingTeam: Team,
            val biddingTeamPoints: Int
        ) : RoundData {

            private val madeBid: Boolean = biddingTeamPoints >= bid

            // You earn all the points not won by the bidding team. If they got more than 180, you still just get 0
            private val nonBiddingTeamPoints get() = max(180 - biddingTeamPoints, 0)

            fun points(team: Team): Int =
                if (team == biddingTeam) biddingTeamPoints else nonBiddingTeamPoints

            fun score(team: Team): Int = if (team == biddingTeam) {
                // If you made your bid, you go up by that amount. If you don't, you go down by your bid amount
                if (madeBid) biddingTeamPoints else -bid
            } else {
                nonBiddingTeamPoints
            }
        }
    }

    private val stateFlow = MutableStateFlow(
        State(
            players = players,
            // By default, put the first two players on team1
            playerTeamMap = players.withIndex().associate {
                it.value.id to if (it.index < 2) Team.TEAM1 else Team.TEAM2
            },
            currentPhase = Phase.SELECT_TEAMS,
            roundData = emptyList(),
            showingBottomSheet = false,
            showingScoreboardEditDialogWithData = null,
        )
    )

    val viewStateFlow: StateFlow<RookGameViewState> = stateFlow.map { it.toViewState() }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.Eagerly,
            initialValue = stateFlow.value.toViewState(),
        )

    fun onTeamSelectionCardSelected(cardId: String) {
        stateFlow.update {
            it.copy(
                playerTeamMap = it.playerTeamMap.toMutableMap().apply {
                    this[cardId] = this.getValue(cardId).switch()
                }
            )
        }
    }

    fun onSelectTeamsCtaTapped() {
        startNewRound()
    }

    fun onBidChanged(bid: Int) {
        stateFlow.update { state ->
            state.updateLastRoundData<RoundData.SettingUp> { it.copy(bid = bid) }
        }
    }

    fun onBidderTeamCardTapped(teamIndex: Int) {
        stateFlow.update { state ->
            state.updateLastRoundData<RoundData.SettingUp> {
                it.copy(biddingTeam = if (teamIndex == 0) Team.TEAM1 else Team.TEAM2)
            }
        }
    }

    private fun <T : RoundData> State.updateLastRoundData(resultUpdater: (T) -> RoundData): State {
        val mutableRoundResults = roundData.toMutableList()
        val lastRound = mutableRoundResults.removeLast()
        @Suppress("UNCHECKED_CAST")
        return copy(
            // Call the resultUpdater with the lastRound (cast to the expected type), and add the result back to the end of the list
            roundData = mutableRoundResults + resultUpdater(lastRound as T),
        )
    }

    fun onBidFinalized() {
        stateFlow.update { state ->
            state.updateLastRoundData<RoundData.SettingUp> {
                // Default the bidding team score to the bid
                RoundData.Ready(
                    bid = it.bid,
                    biddingTeam = it.biddingTeam!!,
                    biddingTeamPoints = it.bid
                )
            }.copy(currentPhase = Phase.COLLECT_SCORE)
        }
    }

    fun onCollectScoreSliderChanged(newValue: Int) {
        stateFlow.update { state ->
            state.updateLastRoundData<RoundData.Ready> {
                it.copy(biddingTeamPoints = newValue)
            }
        }
    }

    fun onCollectScoreCtaTapped() {
        startNewRound()
    }

    private fun startNewRound() {
        stateFlow.update {
            it.copy(
                currentPhase = Phase.ENTER_BID,
                // Default the bid to 100, which will require less swiping
                roundData = it.roundData + RoundData.SettingUp(bid = 100, biddingTeam = null)
            )
        }
    }

    fun updateSheetVisibility(isVisible: Boolean) {
        stateFlow.update { it.copy(showingBottomSheet = isVisible) }
    }

    fun onScoreboardRowTapped(roundIndex: Int) {
        stateFlow.update {
            it.copy(
                showingScoreboardEditDialogWithData = RookGameViewState.Scoreboard.EditableRoundInfo(
                    roundIndex
                )
            )
        }
    }

    fun onScoreboardEditDialogDismissed() {
        stateFlow.update { it.copy(showingScoreboardEditDialogWithData = null) }
    }

    data class Factory(val players: List<Player>) : ViewModelProvider.Factory {
        override fun <T : ViewModel> create(modelClass: KClass<T>, extras: CreationExtras): T {
            @Suppress("UNCHECKED_CAST")
            return RookGameViewModel(players) as T
        }
    }
}
