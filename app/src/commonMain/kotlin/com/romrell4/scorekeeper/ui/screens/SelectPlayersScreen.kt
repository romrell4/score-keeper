package com.romrell4.scorekeeper.ui.screens

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.Button
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MenuAnchorType
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardCapitalization
import androidx.compose.ui.unit.dp
import com.romrell4.scorekeeper.data.Player
import org.jetbrains.compose.resources.stringResource
import scorekeeper.app.generated.resources.Res
import scorekeeper.app.generated.resources.select_players_add_player_cta
import scorekeeper.app.generated.resources.select_players_app_bar_title
import scorekeeper.app.generated.resources.select_players_cta
import scorekeeper.app.generated.resources.select_players_field_label

@Composable
fun SelectPlayersScreen(
    onCtaTapped: (List<Player>) -> Unit
) {
    ScreenScaffold(title = Res.string.select_players_app_bar_title) { innerPadding ->
        Column(modifier = Modifier.padding(innerPadding).padding(16.dp).fillMaxHeight()) {
            // TODO: Pull saved players from shared preferences

            val selectedPlayers = remember { mutableStateListOf<Player>() }

            EnterPlayerField(
                autoCompleteSuggestions = listOf(
                    Player(id = "1", name = "Eric"),
                    Player(id = "2", name = "Jess"),
                    Player(id = "3", name = "Katie"),
                    Player(id = "4", name = "Rochelle"),
                    Player(id = "5", name = "Mom"),
                    Player(id = "6", name = "Dad"),
                ).filterNot { it in selectedPlayers },
                onPlayerSelected = {
                    selectedPlayers.add(it)
                }
            )

            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .verticalScroll(rememberScrollState())
                    .weight(1f)
            ) {
                selectedPlayers.forEachIndexed { index, player ->
                    Row(
                        modifier = Modifier.padding(top = 8.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(player.name)
                        Spacer(modifier = Modifier.weight(1f))
                        IconButton(
                            onClick = {
                                selectedPlayers.removeAt(index)
                            }
                        ) {
                            Icon(Icons.Filled.Close, contentDescription = "Delete")
                        }
                    }
                }
            }

            Button(
                onClick = { onCtaTapped(selectedPlayers) },
                modifier = Modifier.fillMaxWidth().imePadding(),
                enabled = selectedPlayers.isNotEmpty(),
            ) {
                Text(stringResource(Res.string.select_players_cta))
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun EnterPlayerField(
    autoCompleteSuggestions: List<Player>,
    onPlayerSelected: (Player) -> Unit,
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        var newPlayerText by remember { mutableStateOf("") }
        var autoCompleteExpanded by remember { mutableStateOf(false) }

        fun addNewPlayer() {
            onPlayerSelected(Player(name = newPlayerText.trim()))
            newPlayerText = ""
            autoCompleteExpanded = false
        }

        ExposedDropdownMenuBox(
            expanded = autoCompleteExpanded,
            onExpandedChange = {
                autoCompleteExpanded = it
            }
        ) {
            OutlinedTextField(
                value = newPlayerText,
                onValueChange = {
                    autoCompleteExpanded = it.isNotEmpty()
                    newPlayerText = it
                },
                modifier = Modifier.menuAnchor(MenuAnchorType.PrimaryEditable),
                keyboardOptions = KeyboardOptions(
                    capitalization = KeyboardCapitalization.Words,
                    imeAction = ImeAction.Done
                ),
                keyboardActions = KeyboardActions(
                    onDone = { addNewPlayer() },
                ),
                label = { Text(stringResource(Res.string.select_players_field_label)) },
                trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = autoCompleteExpanded) },
                colors = ExposedDropdownMenuDefaults.textFieldColors()
            )

            // Only showing players that match the current text
            val suggestions = autoCompleteSuggestions.filter {
                it.name.contains(newPlayerText, ignoreCase = true)
            }
            if (suggestions.isNotEmpty()) {
                ExposedDropdownMenu(
                    expanded = autoCompleteExpanded,
                    onDismissRequest = { autoCompleteExpanded = false }
                ) {
                    suggestions.forEach { player ->
                        DropdownMenuItem(
                            onClick = {
                                onPlayerSelected(player)
                                newPlayerText = ""
                                autoCompleteExpanded = false
                            },
                            text = { Text(player.name) },
                        )
                    }
                }
            }
        }

        Button(enabled = newPlayerText.isNotBlank(), onClick = { addNewPlayer() }) {
            Text(stringResource(Res.string.select_players_add_player_cta))
        }
    }
}
