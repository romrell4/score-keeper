package com.romrell4.scorekeeper.ui.shared

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.Dp

@Composable
fun Table(
    columnCount: Int,
    rowCount: Int,
    cellWidth: (columnIndex: Int) -> Dp,
    headerContent: @Composable (columnIndex: Int) -> Unit,
    rowContent: @Composable (columnIndex: Int, rowIndex: Int) -> Unit,
    modifier: Modifier = Modifier
) {
    LazyRow(modifier = modifier.verticalScroll(rememberScrollState())) {
        items(columnCount) { columnIndex ->
            Column {
                // Include 0 for the header
                (0..rowCount).forEach { rowIndex ->
                    Box(
                        contentAlignment = Alignment.Center,
                        modifier = Modifier
                            .width(cellWidth(columnIndex))
                    ) {
                        when (rowIndex) {
                            // First row is always the header
                            0 -> headerContent(columnIndex)
                            // All other rows are content (subtract one for the header)
                            else -> rowContent(columnIndex, rowIndex - 1)
                        }
                    }
                }
            }
        }
    }
}