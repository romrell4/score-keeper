package com.romrell4.scorekeeper.ui.shared

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Card
import androidx.compose.material3.CardColors
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.unit.dp

@Composable
fun CenteredCard(
    modifier: Modifier = Modifier,
    shape: Shape = CardDefaults.shape,
    colors: CardColors = CardDefaults.cardColors(),
    border: Boolean = false,
    content: @Composable () -> Unit,
) {
    Card(
        modifier = Modifier.padding(8.dp).then(modifier),
        shape = shape,
        colors = colors,
        border = if (border) BorderStroke(2.dp, MaterialTheme.colorScheme.primary) else null,
    ) {
        Box(
            contentAlignment = Alignment.Center,
            modifier = Modifier.padding(8.dp)
                .fillMaxWidth()

        ) {
            content()
        }
    }
}
