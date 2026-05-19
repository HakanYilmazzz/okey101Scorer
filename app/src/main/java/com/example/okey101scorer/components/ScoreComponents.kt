package com.example.okey101scorer.components

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.VerticalDivider
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.okey101scorer.Round
import com.example.okey101scorer.ui.theme.PenaltyDarkRed
import com.example.okey101scorer.ui.theme.PenaltyLightRed

@Composable
fun ScoreCell(
    value: Int,
    modifier: Modifier = Modifier,
    onClick: () -> Unit
) {
    val backgroundColor = when (value) {
        -101 -> PenaltyLightRed
        -202 -> PenaltyDarkRed
        else -> Color.Transparent
    }
    
    val textColor = when (value) {
        -101 -> Color.White
        -202 -> Color.White
        else -> MaterialTheme.colorScheme.onSurface
    }

    val displayValue = if (value == 0) "" else value.toString()
    val emoji = if (value == -202) " \uD83D\uDD25" else "" // Fire emoji

    Box(
        modifier = modifier
            .fillMaxWidth()
            .height(72.dp)
            .clickable(onClick = onClick)
            .padding(4.dp)
            .background(backgroundColor, RoundedCornerShape(8.dp)),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = "$displayValue$emoji",
            textAlign = TextAlign.Center,
            fontWeight = FontWeight.Black,
            fontSize = 32.sp,
            fontFamily = FontFamily.Monospace,
            color = textColor
        )
    }
}

@Composable
fun RoundRow(
    round: Round,
    rowBackground: Color,
    bgTint1: Color,
    bgTint2: Color,
    onScoreClick: (colIndex: Int, roundId: String, currentValue: String) -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .background(rowBackground)
            .height(IntrinsicSize.Min),
        verticalAlignment = Alignment.CenterVertically
    ) {
        val scoresArray = arrayOf(round.score1, round.score2)
        
        // Left Zone (Biz)
        ScoreCell(
            value = scoresArray[0],
            modifier = Modifier
                .weight(1f)
                .fillMaxHeight()
                .background(bgTint1),
            onClick = {
                onScoreClick(0, round.id, if (scoresArray[0] == 0) "" else scoresArray[0].toString())
            }
        )
        
        VerticalDivider(color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.1f))

        // Right Zone (Onlar)
        ScoreCell(
            value = scoresArray[1],
            modifier = Modifier
                .weight(1f)
                .fillMaxHeight()
                .background(bgTint2),
            onClick = {
                onScoreClick(1, round.id, if (scoresArray[1] == 0) "" else scoresArray[1].toString())
            }
        )
    }
}
