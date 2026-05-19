package com.example.okey101scorer.components

import androidx.compose.animation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.VerticalDivider
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.okey101scorer.ui.theme.NegativeRed
import com.example.okey101scorer.ui.theme.PositiveGreen

@Composable
fun SumBar(sums: List<Int>, roundCount: Int) {
    val sum1 = sums.getOrNull(0) ?: 0
    val sum2 = sums.getOrNull(1) ?: 0
    val diff = kotlin.math.abs(sum1 - sum2)

    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .padding(12.dp),
        shape = RoundedCornerShape(24.dp),
        color = MaterialTheme.colorScheme.surfaceContainerHigh,
        shadowElevation = 24.dp 
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 12.dp),
        ) {
            Text(
                text = "TOPLAM ($roundCount El)",
                fontWeight = FontWeight.Black,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 8.dp),
                textAlign = TextAlign.Center,
                letterSpacing = 2.sp
            )
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Left Column Sum
                val color1 = if (sum1 < 0) NegativeRed else PositiveGreen
                AnimatedContent(
                    targetState = sum1,
                    transitionSpec = {
                        if (targetState > initialState) {
                            (slideInVertically { height -> height } + fadeIn()).togetherWith(slideOutVertically { height -> -height } + fadeOut())
                        } else {
                            (slideInVertically { height -> -height } + fadeIn()).togetherWith(slideOutVertically { height -> height } + fadeOut())
                        }
                    },
                    modifier = Modifier.weight(1f),
                    label = "sum1_anim"
                ) { targetSum ->
                    val targetCrown1 = if (diff >= 500 && targetSum < sum2) " \uD83D\uDC51" else ""
                    Text(
                        text = "$targetSum$targetCrown1",
                        textAlign = TextAlign.Center,
                        fontWeight = FontWeight.Black,
                        fontSize = 36.sp,
                        fontFamily = FontFamily.Monospace,
                        color = color1
                    )
                }

                VerticalDivider(
                    modifier = Modifier.height(40.dp),
                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.2f)
                )

                // Right Column Sum
                val color2 = if (sum2 < 0) NegativeRed else PositiveGreen
                AnimatedContent(
                    targetState = sum2,
                    transitionSpec = {
                        if (targetState > initialState) {
                            (slideInVertically { height -> height } + fadeIn()).togetherWith(slideOutVertically { height -> -height } + fadeOut())
                        } else {
                            (slideInVertically { height -> -height } + fadeIn()).togetherWith(slideOutVertically { height -> height } + fadeOut())
                        }
                    },
                    modifier = Modifier.weight(1f),
                    label = "sum2_anim"
                ) { targetSum ->
                    val targetCrown2 = if (diff >= 500 && targetSum < sum1) " \uD83D\uDC51" else ""
                    Text(
                        text = "$targetSum$targetCrown2",
                        textAlign = TextAlign.Center,
                        fontWeight = FontWeight.Black,
                        fontSize = 36.sp,
                        fontFamily = FontFamily.Monospace,
                        color = color2
                    )
                }
            }
        }
    }
}
