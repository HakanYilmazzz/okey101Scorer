package com.example.okey101scorer.components

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import com.example.okey101scorer.Round
import com.example.okey101scorer.ui.theme.PenaltyDarkRed
import com.example.okey101scorer.ui.theme.ZebraStripeTint
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class, ExperimentalFoundationApi::class)
@Composable
fun RoundList(
    listState: LazyListState,
    rounds: List<Round>,
    bgTint1: Color,
    bgTint2: Color,
    snackbarHostState: SnackbarHostState,
    modifier: Modifier = Modifier,
    onDeleteRound: (String) -> Unit,
    onUndoDelete: () -> Unit,
    onScoreClick: (colIndex: Int, roundId: String, valueStr: String) -> Unit
) {
    val coroutineScope = rememberCoroutineScope()

    LazyColumn(
        state = listState,
        modifier = modifier.fillMaxWidth(),
        contentPadding = PaddingValues(bottom = 88.dp)
    ) {
        itemsIndexed(items = rounds, key = { _, round -> round.id }) { index, round ->
            
            val isOdd = index % 2 != 0
            val rowBackground = if (isOdd) ZebraStripeTint else Color.Transparent

            if (index == 0) {
                Box(modifier = Modifier.animateItem()) {
                    RoundRow(
                        round = round,
                        rowBackground = rowBackground,
                        bgTint1 = bgTint1,
                        bgTint2 = bgTint2,
                        onScoreClick = onScoreClick
                    )
                }
            } else {
                val dismissState = rememberSwipeToDismissBoxState(
                    confirmValueChange = {
                        if (it == SwipeToDismissBoxValue.EndToStart || it == SwipeToDismissBoxValue.StartToEnd) {
                            onDeleteRound(round.id)
                            coroutineScope.launch {
                                val result = snackbarHostState.showSnackbar(
                                    message = "El silindi",
                                    actionLabel = "Geri Al",
                                    duration = SnackbarDuration.Short
                                )
                                if (result == SnackbarResult.ActionPerformed) {
                                    onUndoDelete()
                                }
                            }
                            true
                        } else {
                            false
                        }
                    }
                )

                SwipeToDismissBox(
                    state = dismissState,
                    enableDismissFromStartToEnd = false,
                    modifier = Modifier.animateItem(),
                    backgroundContent = {
                        if (dismissState.targetValue != SwipeToDismissBoxValue.Settled) {
                            val color = PenaltyDarkRed
                            Box(
                                modifier = Modifier
                                    .fillMaxSize()
                                    .background(color)
                                    .padding(horizontal = 20.dp),
                                contentAlignment = Alignment.CenterEnd
                            ) {
                                Icon(
                                    Icons.Default.Delete,
                                    contentDescription = "Sil",
                                    tint = Color.White
                                )
                            }
                        }
                    }
                ) {
                    RoundRow(
                        round = round,
                        rowBackground = rowBackground,
                        bgTint1 = bgTint1,
                        bgTint2 = bgTint2,
                        onScoreClick = onScoreClick
                    )
                }
            }
        }
    }
}
