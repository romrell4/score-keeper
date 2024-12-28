package com.romrell4.scorekeeper.ui.viewmodels

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.CreationExtras
import com.romrell4.scorekeeper.data.Player
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlin.reflect.KClass

data object RookGameViewState

class RookGameViewModel(
    players: List<Player>,
) : ViewModel() {
    private data class State(
        val players: List<Player>,
    ) {
        fun toViewState(): RookGameViewState = RookGameViewState
    }

    private val stateFlow = MutableStateFlow(
        State(
            players = players,
        )
    )

    val viewStateFlow: StateFlow<RookGameViewState> = stateFlow.map { it.toViewState() }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.Eagerly,
            initialValue = stateFlow.value.toViewState(),
        )

    data class Factory(val players: List<Player>) : ViewModelProvider.Factory {
        override fun <T : ViewModel> create(modelClass: KClass<T>, extras: CreationExtras): T {
            @Suppress("UNCHECKED_CAST")
            return RookGameViewModel(players) as T
        }
    }
}
