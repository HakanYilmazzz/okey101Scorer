package com.example.okey101scorer.components

import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.example.okey101scorer.ActiveEventDialog
import kotlinx.coroutines.delay

@Composable
fun ActiveEventDialogHandler(
    dialogState: ActiveEventDialog,
    onDismiss: () -> Unit,
    onResolveCifteKumar: (Boolean) -> Unit
) {
    if (dialogState == ActiveEventDialog.None) return

    Dialog(
        onDismissRequest = { 
            // We want these dialogs to be mostly mandatory
        },
        properties = DialogProperties(dismissOnBackPress = false, dismissOnClickOutside = false)
    ) {
        when (dialogState) {
            is ActiveEventDialog.MysteryBox -> MysteryBoxDialog(dialogState, onDismiss)
            is ActiveEventDialog.YanciIhtilali -> YanciIhtilaliDialog(dialogState, onDismiss)
            is ActiveEventDialog.GreatSwap -> GreatSwapDialog(onDismiss)
            is ActiveEventDialog.CifteKumar -> CifteKumarDialog(dialogState, onResolveCifteKumar)
            is ActiveEventDialog.CifteKumarResult -> CifteKumarResultDialog(dialogState, onDismiss)
            else -> {}
        }
    }
}

@Composable
fun MysteryBoxDialog(state: ActiveEventDialog.MysteryBox, onDismiss: () -> Unit) {
    var isRevealed by remember { mutableStateOf(false) }

    LaunchedEffect(Unit) {
        delay(2000) // Suspense delay
        isRevealed = true
    }

    Card(
        modifier = Modifier.fillMaxWidth().padding(16.dp),
        shape = RoundedCornerShape(24.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceContainerHigh)
    ) {
        Column(
            modifier = Modifier.padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            val wobble by animateFloatAsState(
                targetValue = if (isRevealed) 0f else 15f,
                animationSpec = if (!isRevealed) infiniteRepeatable(
                    animation = tween(100, easing = LinearEasing),
                    repeatMode = RepeatMode.Reverse
                ) else tween(0),
                label = "box_wobble"
            )

            Text("🎁", fontSize = 64.sp, modifier = Modifier.rotate(wobble))
            Spacer(modifier = Modifier.height(16.dp))
            Text("GİZEMLİ KUTU...", fontWeight = FontWeight.Black, fontSize = 20.sp, color = MaterialTheme.colorScheme.primary)
            Spacer(modifier = Modifier.height(8.dp))
            
            if (!isRevealed) {
                Text("Kutu titriyor... İçinden ne çıkacak?", textAlign = TextAlign.Center, fontSize = 16.sp)
            } else {
                val text = if (state.amount > 0) {
                    "${state.teamName} takımına +${state.amount} CEZA çıktı! 🥶"
                } else if (state.amount < 0) {
                    "${state.teamName} takımına ${state.amount} ÖDÜL çıktı! 😎"
                } else {
                    "Kutu boş çıktı! Herkese geçmiş olsun. 😅"
                }
                Text(text, textAlign = TextAlign.Center, fontSize = 16.sp, fontWeight = FontWeight.Bold)
                Spacer(modifier = Modifier.height(24.dp))
                Button(onClick = onDismiss, modifier = Modifier.fillMaxWidth()) {
                    Text("Devam Et")
                }
            }
        }
    }
}

@Composable
fun GreatSwapDialog(onDismiss: () -> Unit) {
    var scale by remember { mutableStateOf(0.5f) }
    val animatedScale by animateFloatAsState(
        targetValue = scale,
        animationSpec = spring(dampingRatio = Spring.DampingRatioHighBouncy, stiffness = Spring.StiffnessLow),
        label = "bomb_scale"
    )

    LaunchedEffect(Unit) {
        scale = 1f
    }

    Card(
        modifier = Modifier.fillMaxWidth().padding(16.dp).scale(animatedScale),
        shape = RoundedCornerShape(24.dp),
        colors = CardDefaults.cardColors(containerColor = Color(0xFF991B1B))
    ) {
        Column(
            modifier = Modifier.padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text("💣", fontSize = 80.sp)
            Spacer(modifier = Modifier.height(16.dp))
            Text("BÜYÜK TAKAS!", fontWeight = FontWeight.Black, fontSize = 28.sp, color = Color.White)
            Spacer(modifier = Modifier.height(8.dp))
            Text("Herkes ıstakasını sağındaki oyuncuya devretsin! Adalet yeniden dağıtılıyor.", textAlign = TextAlign.Center, color = Color.White, fontSize = 16.sp)
            Spacer(modifier = Modifier.height(24.dp))
            Button(onClick = onDismiss, modifier = Modifier.fillMaxWidth(), colors = ButtonDefaults.buttonColors(containerColor = Color.White, contentColor = Color.Black)) {
                Text("Değiştik, Devam Et!")
            }
        }
    }
}

@Composable
fun CifteKumarDialog(state: ActiveEventDialog.CifteKumar, onResolve: (Boolean) -> Unit) {
    Card(
        modifier = Modifier.fillMaxWidth().padding(16.dp),
        shape = RoundedCornerShape(24.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceContainerHigh)
    ) {
        Column(
            modifier = Modifier.padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text("🎲", fontSize = 64.sp)
            Spacer(modifier = Modifier.height(16.dp))
            Text("ÇİFTE KUMAR!", fontWeight = FontWeight.Black, fontSize = 24.sp, color = MaterialTheme.colorScheme.primary)
            Spacer(modifier = Modifier.height(8.dp))
            Text("${state.teamName} takımı! Bu eli kazandınız ve ${state.currentPenalty} puan aldınız.", textAlign = TextAlign.Center, fontSize = 14.sp)
            Spacer(modifier = Modifier.height(8.dp))
            Text("Puanınızı x2 yapmak için yazı-tura riskine girer misiniz? (Kazanırsanız x2, kaybederseniz 0 puan!)", textAlign = TextAlign.Center, fontWeight = FontWeight.Bold, fontSize = 16.sp)
            
            Spacer(modifier = Modifier.height(24.dp))
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceEvenly) {
                OutlinedButton(onClick = { onResolve(false) }) {
                    Text("Hayır, Kalsın")
                }
                Button(onClick = { onResolve(true) }) {
                    Text("Risk Al! (x2)")
                }
            }
        }
    }
}

@Composable
fun CifteKumarResultDialog(state: ActiveEventDialog.CifteKumarResult, onDismiss: () -> Unit) {
    var isRevealed by remember { mutableStateOf(false) }

    LaunchedEffect(Unit) {
        delay(2500) // Suspense for the coin flip
        isRevealed = true
    }

    Card(
        modifier = Modifier.fillMaxWidth().padding(16.dp),
        shape = RoundedCornerShape(24.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceContainerHigh)
    ) {
        Column(
            modifier = Modifier.padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            if (!isRevealed) {
                // Spinning coin effect
                val rotation by animateFloatAsState(
                    targetValue = 360f * 5,
                    animationSpec = tween(2500, easing = FastOutSlowInEasing),
                    label = "coin_flip"
                )
                Text("🪙", fontSize = 64.sp, modifier = Modifier.rotate(rotation))
                Spacer(modifier = Modifier.height(16.dp))
                Text("YAZI-TURA ATILIYOR...", fontWeight = FontWeight.Black, fontSize = 20.sp, color = MaterialTheme.colorScheme.primary)
                Spacer(modifier = Modifier.height(8.dp))
                Text("Havada süzülüyor...", textAlign = TextAlign.Center, fontSize = 16.sp)
            } else {
                Text(if (state.win) "🎉" else "💀", fontSize = 64.sp)
                Spacer(modifier = Modifier.height(16.dp))
                Text(if (state.win) "KAZANDINIZ!" else "KAYBETTİNİZ!", fontWeight = FontWeight.Black, fontSize = 24.sp, color = if (state.win) Color(0xFF4ADE80) else Color(0xFFF87171))
                Spacer(modifier = Modifier.height(8.dp))
                Text(if (state.win) "Risk aldınız ve puanınız x2 oldu! Yeni skor: ${state.finalScore}" else "Maalesef risk patladı... Kazandığınız puan sıfırlandı: 0", textAlign = TextAlign.Center, fontSize = 16.sp, fontWeight = FontWeight.Bold)
                Spacer(modifier = Modifier.height(24.dp))
                Button(onClick = onDismiss, modifier = Modifier.fillMaxWidth()) {
                    Text("Devam Et")
                }
            }
        }
    }
}

@Composable
fun YanciIhtilaliDialog(state: ActiveEventDialog.YanciIhtilali, onDismiss: () -> Unit) {
    var isSpinning by remember { mutableStateOf(true) }
    var rotation by remember { mutableFloatStateOf(0f) }
    
    val animatedRotation by animateFloatAsState(
        targetValue = rotation,
        animationSpec = tween(durationMillis = 5000, easing = FastOutSlowInEasing), // Slower 5 seconds spin
        finishedListener = { isSpinning = false },
        label = "wheel_spin"
    )

    LaunchedEffect(Unit) {
        delay(500)
        // Spin multiple times and land unpredictably. 
        rotation = 360f * 8f + (Math.random() * 360).toFloat()
    }

    Card(
        modifier = Modifier.fillMaxWidth().padding(16.dp),
        shape = RoundedCornerShape(24.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceContainerHigh)
    ) {
        Column(
            modifier = Modifier.padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text("🎡", fontSize = 80.sp, modifier = Modifier.rotate(animatedRotation))
            Spacer(modifier = Modifier.height(24.dp))
            Text("YANCI İHTİLALİ", fontWeight = FontWeight.Black, fontSize = 24.sp, color = MaterialTheme.colorScheme.primary)
            Spacer(modifier = Modifier.height(8.dp))
            
            if (isSpinning) {
                Text("Kader çarkı dönüyor... Kime 101 ceza girecek?", textAlign = TextAlign.Center, fontSize = 16.sp)
            } else {
                Text("Çark Durdu!", fontWeight = FontWeight.Bold, fontSize = 18.sp, color = Color(0xFFF87171))
                Spacer(modifier = Modifier.height(8.dp))
                Text("Şanssız Takım: ${state.teamName}", fontWeight = FontWeight.Black, fontSize = 24.sp, color = Color.White, modifier = Modifier.background(Color(0xFF991B1B), RoundedCornerShape(8.dp)).padding(horizontal = 16.dp, vertical = 8.dp))
                Spacer(modifier = Modifier.height(8.dp))
                Text("Otomatik olarak +101 ceza eklendi.", textAlign = TextAlign.Center, fontSize = 14.sp)
                
                Spacer(modifier = Modifier.height(24.dp))
                Button(onClick = onDismiss, modifier = Modifier.fillMaxWidth()) {
                    Text("Acımasızmış, Devam Et")
                }
            }
        }
    }
}
