package com.example.okey101scorer

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Backspace
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

@Composable
fun CustomNumpad(
    currentValue: String,
    onValueChange: (String) -> Unit,
    onDone: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        // Display Current Value
        Text(
            text = if (currentValue.isEmpty()) "0" else currentValue,
            fontSize = 48.sp,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.primary,
            modifier = Modifier.padding(bottom = 16.dp)
        )

        // Penalty Quick Actions
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 16.dp),
            horizontalArrangement = Arrangement.SpaceEvenly
        ) {
            Button(
                onClick = {
                    onValueChange("-101")
                    onDone()
                },
                colors = ButtonDefaults.buttonColors(
                    containerColor = Color(0x33E53935), // PenaltyLightRed
                    contentColor = Color.White
                ),
                shape = RoundedCornerShape(12.dp),
                modifier = Modifier
                    .weight(1f)
                    .height(56.dp)
                    .padding(horizontal = 8.dp)
            ) {
                Text("Düz bitti", fontSize = 24.sp, fontWeight = FontWeight.Black)
            }

            Button(
                onClick = {
                    onValueChange("-202")
                    onDone()
                },
                colors = ButtonDefaults.buttonColors(
                    containerColor = Color(0xFFB71C1C), // PenaltyDarkRed
                    contentColor = Color.White
                ),
                shape = RoundedCornerShape(12.dp),
                modifier = Modifier
                    .weight(1f)
                    .height(56.dp)
                    .padding(horizontal = 8.dp)
            ) {
                Text("Elden bitti 🔥", fontSize = 24.sp, fontWeight = FontWeight.Black)
            }
        }

        // Numpad Grid
        val buttons = listOf(
            listOf("1", "2", "3"),
            listOf("4", "5", "6"),
            listOf("7", "8", "9"),
            listOf("Sil", "0", "Tamam")
        )

        for (row in buttons) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 8.dp),
                horizontalArrangement = Arrangement.SpaceEvenly
            ) {
                for (btn in row) {
                    NumpadButton(
                        text = btn,
                        modifier = Modifier
                            .weight(1f)
                            .aspectRatio(if (btn == "Sil" || btn == "Tamam") 1.5f else 1f)
                            .padding(horizontal = 8.dp),
                        onClick = {
                            when (btn) {
                                "Sil" -> {
                                    if (currentValue.isNotEmpty()) {
                                        onValueChange(currentValue.dropLast(1))
                                    }
                                }
                                "Tamam" -> onDone()
                                else -> {
                                    // Prevent leading zeros unless it's just "0"
                                    if (currentValue == "0") {
                                        onValueChange(btn)
                                    } else {
                                        // Limit length to prevent massive numbers
                                        if (currentValue.length < 5) {
                                            onValueChange(currentValue + btn)
                                        }
                                    }
                                }
                            }
                        }
                    )
                }
            }
        }
        Spacer(modifier = Modifier.height(24.dp))
    }
}

@Composable
fun NumpadButton(
    text: String,
    modifier: Modifier = Modifier,
    onClick: () -> Unit
) {
    val isAction = text == "Sil" || text == "Tamam"
    val containerColor = if (isAction) MaterialTheme.colorScheme.secondaryContainer else MaterialTheme.colorScheme.surfaceVariant
    val contentColor = if (isAction) MaterialTheme.colorScheme.onSecondaryContainer else MaterialTheme.colorScheme.onSurfaceVariant

    Box(
        modifier = modifier
            .clip(RoundedCornerShape(16.dp))
            .background(containerColor)
            .clickable { onClick() },
        contentAlignment = Alignment.Center
    ) {
        if (text == "Sil") {
            Icon(
                imageVector = Icons.AutoMirrored.Filled.Backspace,
                contentDescription = "Sil",
                tint = contentColor,
                modifier = Modifier.size(32.dp)
            )
        } else {
            Text(
                text = text,
                fontSize = if (isAction) 20.sp else 32.sp,
                fontWeight = if (isAction) FontWeight.SemiBold else FontWeight.Medium,
                color = contentColor,
                textAlign = TextAlign.Center
            )
        }
    }
}
