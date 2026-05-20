package com.example.okey101scorer

import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shadow
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import kotlinx.coroutines.delay

@Composable
fun SplashNavHost(viewModel: ScoreViewModel) {
    val navController = rememberNavController()
    NavHost(navController = navController, startDestination = "splash") {
        composable("splash") {
            AnimatedSplashScreen(navController = navController, viewModel = viewModel)
        }
        composable("main") {
            MainScreen(viewModel = viewModel)
        }
    }
}

@Composable
fun AnimatedSplashScreen(navController: NavController, viewModel: ScoreViewModel) {
    val isReady by viewModel.isReady.collectAsState()

    // 1. DİNAMİK AURA ARKA PLAN ANİMASYONU
    val infiniteTransition = rememberInfiniteTransition(label = "aura")
    val pulseScale by infiniteTransition.animateFloat(
        initialValue = 0.7f,
        targetValue = 1.3f,
        animationSpec = infiniteRepeatable(
            animation = tween(4000, easing = EaseInOutSine),
            repeatMode = RepeatMode.Reverse
        ),
        label = "pulse_scale"
    )
    val pulseAlpha by infiniteTransition.animateFloat(
        initialValue = 0.15f,
        targetValue = 0.35f,
        animationSpec = infiniteRepeatable(
            animation = tween(3000, easing = EaseInOutSine),
            repeatMode = RepeatMode.Reverse
        ),
        label = "pulse_alpha"
    )

    // 2. NEON / FLICKER METİN ANİMASYONU
    var textAlpha by remember { mutableStateOf(0f) }
    LaunchedEffect(Unit) {
        // Neon tabela açılış kırpışması (Flicker efekti)
        delay(100)
        textAlpha = 0.3f
        delay(60)
        textAlpha = 0.0f
        delay(40)
        textAlpha = 0.7f
        delay(80)
        textAlpha = 0.1f
        delay(50)
        textAlpha = 1.0f // Tam parıldama seviyesi
    }

    // 3. SÜZÜLEN ALT METİN
    var startSubtextAnim by remember { mutableStateOf(false) }
    LaunchedEffect(Unit) {
        delay(550) // Neon tabelası tamamen açıldıktan sonra başlasın
        startSubtextAnim = true
    }

    val subtextAlpha by animateFloatAsState(
        targetValue = if (startSubtextAnim) 0.6f else 0.0f,
        animationSpec = tween(1200, easing = EaseOutCubic),
        label = "subtext_alpha"
    )
    val subtextOffsetY by animateDpAsState(
        targetValue = if (startSubtextAnim) 0.dp else 24.dp,
        animationSpec = tween(1200, easing = EaseOutCubic),
        label = "subtext_offset"
    )

    // 4. ZAMANLAYICI VE GEÇİŞ
    // Toplam minimum 2000ms bekletir ve ViewModel hazır olduğunda yönlendirir.
    LaunchedEffect(isReady) {
        if (isReady) {
            delay(2000)
            navController.navigate("main") {
                popUpTo("splash") { inclusive = true }
            }
        }
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFF0A0A0A)) // Koyu minimalist arka plan
            .drawBehind {
                val brush = Brush.radialGradient(
                    colors = listOf(
                        Color(0xFFEF4444).copy(alpha = pulseAlpha), // Neon Kırmızı Merkezli Aura
                        Color(0xFF6366F1).copy(alpha = pulseAlpha * 0.4f), // Geçişli İndigo
                        Color.Transparent
                    ),
                    center = center,
                    radius = size.minDimension * pulseScale
                )
                drawRect(brush = brush)
            },
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            // Neon Glow Shadow
            val neonShadow = if (textAlpha > 0.5f) {
                Shadow(
                    color = Color(0xFFEF4444).copy(alpha = 0.7f),
                    offset = Offset(0f, 0f),
                    blurRadius = 35f
                )
            } else null

            // Ana Başlık: 101 COCKPIT
            Text(
                text = "101 COCKPIT",
                fontSize = 42.sp,
                fontWeight = FontWeight.Black,
                color = Color.White.copy(alpha = textAlpha),
                letterSpacing = 4.sp,
                style = MaterialTheme.typography.headlineLarge.copy(shadow = neonShadow),
                textAlign = TextAlign.Center
            )

            Spacer(modifier = Modifier.height(16.dp))

            // Süzülen Alt Metin
            Text(
                text = "Eğlence Modu Başlatılıyor...",
                fontSize = 14.sp,
                fontWeight = FontWeight.Medium,
                color = Color(0xFF818CF8).copy(alpha = subtextAlpha), // Yumuşak Neon Mavi/İndigo tonda
                letterSpacing = 1.sp,
                textAlign = TextAlign.Center,
                modifier = Modifier
                    .offset { IntOffset(0, subtextOffsetY.roundToPx()) }
            )
        }
    }
}
