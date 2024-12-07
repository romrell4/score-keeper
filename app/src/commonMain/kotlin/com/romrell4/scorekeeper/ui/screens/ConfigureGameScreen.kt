package com.romrell4.scorekeeper.ui.screens

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.RadioButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.romrell4.scorekeeper.data.ConfigOption
import com.romrell4.scorekeeper.data.ConfigOptionValue
import com.romrell4.scorekeeper.data.Game
import org.jetbrains.compose.resources.stringResource
import scorekeeper.app.generated.resources.Res
import scorekeeper.app.generated.resources.configure_game_app_bar_title
import scorekeeper.app.generated.resources.configure_game_cta

@Composable
fun ConfigureGameScreen(game: Game, onCtaTapped: (List<ConfigOptionValue>) -> Unit) {
    ScreenScaffold(title = Res.string.configure_game_app_bar_title) { innerPadding ->
        Column(
            modifier = Modifier
                .padding(innerPadding)
                .padding(8.dp)
        ) {
            // Start all the selected indexes at 0
            val selectedIndices = remember { mutableListOf(*Array(game.configOptions.size) { 0 }) }
            game.configOptions.zip(selectedIndices)
                .forEachIndexed { index, (option, selectedIndex) ->
                    ConfigOptionSection(
                        option = option,
                        selectedIndex = selectedIndex,
                        onRowTapped = { selectedIndices[index] = selectedIndex }
                    )
                }
            Spacer(modifier = Modifier.weight(1f))
            Button(
                onClick = {
                    onCtaTapped(
                        game.configOptions.zip(selectedIndices).map { (option, selectedIndex) ->
                            option.values[selectedIndex]
                        }
                    )
                },
                modifier = Modifier.fillMaxWidth()
            ) {
                Text(stringResource(Res.string.configure_game_cta))
            }
        }
    }
}

@Composable
private fun ConfigOptionSection(
    option: ConfigOption,
    selectedIndex: Int,
    onRowTapped: (index: Int) -> Unit
) {
    Column {
        Text(
            stringResource(option.displayValue),
            style = MaterialTheme.typography.titleMedium,
            modifier = Modifier.padding(vertical = 4.dp)
        )

        option.values.forEachIndexed { index, value ->
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                modifier = Modifier.clickable { onRowTapped(index) }.padding(vertical = 4.dp)
            ) {
                RadioButton(
                    selected = index == selectedIndex,
                    onClick = null,
                )
                Text(
                    stringResource(value.displayValue),
                    style = MaterialTheme.typography.bodyMedium
                )
            }
        }
    }
}