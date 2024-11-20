package com.romrell4.scorekeeper.ui

import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarColors
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import kotlinx.serialization.Serializable

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun App() {
    MyApplicationTheme {
        val navController = rememberNavController()

        Scaffold(
            topBar = {
                TopAppBar(
                    title = { Text("Score Keeper") },
                    colors = TopAppBarDefaults.topAppBarColors(
                        containerColor = MaterialTheme.colorScheme.primary,
                        titleContentColor = MaterialTheme.colorScheme.onPrimary,
                    )
                )
            }
        ) { innerPadding ->
            NavHost(
                navController = navController,
                startDestination = Screen.SelectGame,
                modifier = Modifier.fillMaxSize().padding(innerPadding)
            ) {
                composable<Screen.SelectGame> {
                    Text("Select Game")
                }
            }
        }
    }
}

sealed interface Screen {
    @Serializable
    data object SelectGame : Screen
}
