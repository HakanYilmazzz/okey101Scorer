package com.example.okey101scorer

import android.content.Context
import android.content.Intent
import android.graphics.Bitmap
import android.hardware.Sensor
import android.hardware.SensorEvent
import android.hardware.SensorEventListener
import android.hardware.SensorManager
import android.net.Uri
import androidx.compose.animation.*
import androidx.compose.animation.core.*
import com.google.zxing.BarcodeFormat
import com.google.zxing.qrcode.QRCodeWriter
import androidx.compose.foundation.Image
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.ui.text.input.KeyboardCapitalization
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Share
import androidx.compose.material.icons.filled.Podcasts
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.drawWithContent
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.asAndroidBitmap
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.graphics.lerp
import androidx.compose.ui.graphics.layer.drawLayer
import androidx.compose.ui.graphics.rememberGraphicsLayer
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import androidx.core.content.FileProvider
import com.example.okey101scorer.ui.theme.NegativeRed
import com.example.okey101scorer.ui.theme.PositiveGreen
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.File
import java.io.FileOutputStream
import kotlin.math.sqrt
import com.example.okey101scorer.engine.TableEvent
import com.example.okey101scorer.ActiveEventDialog
import com.example.okey101scorer.components.ActiveEventDialogHandler

@OptIn(ExperimentalMaterial3Api::class, ExperimentalFoundationApi::class)
@Composable
fun MainScreen(viewModel: ScoreViewModel) {
    val rounds by viewModel.rounds.collectAsState()
    val activeEvent by viewModel.activeEvent.collectAsState()
    val activeEventDialog by viewModel.activeEventDialog.collectAsState()
    val columnSums by viewModel.columnSums.collectAsState()
    val teamNames by viewModel.teamNames.collectAsState()
    val isSpectatorActive by viewModel.isSpectatorActive.collectAsState()
    val roomId by viewModel.roomId.collectAsState()
    var showSpectatorDialog by remember { mutableStateOf(false) }

    val activeParticles = remember { mutableStateListOf<ReactionParticle>() }
    val visibleChats = remember { mutableStateListOf<SpectatorChat>() }

    LaunchedEffect(Unit) {
        // Collect reactions and turn them into floating particles with profiles
        launch {
            viewModel.incomingReactions.collect { reaction ->
                if (activeParticles.size < 15) { // Cap at 15 active particles to eliminate lag and visual clutter
                    val newParticle = ReactionParticle(
                        id = System.nanoTime(),
                        emoji = reaction.emoji,
                        senderName = reaction.senderName,
                        senderAvatar = reaction.senderAvatar,
                        startX = 0.1f + (Math.random().toFloat() * 0.8f),
                        duration = 4000 + (Math.random() * 2000).toInt() // Slower float animation
                    )
                    activeParticles.add(newParticle)
                }
            }
        }
        // Collect incoming chats
        launch {
            viewModel.incomingChats.collect { chat ->
                if (visibleChats.size >= 4) {
                    visibleChats.removeAt(0)
                }
                visibleChats.add(chat)
                launch {
                    delay(5000)
                    visibleChats.remove(chat)
                }
            }
        }
    }

    val context = LocalContext.current
    val coroutineScope = rememberCoroutineScope()
    val snackbarHostState = remember { SnackbarHostState() }
    val graphicsLayer = rememberGraphicsLayer()
    val listState = rememberLazyListState()

    LaunchedEffect(rounds.size) {
        if (rounds.isNotEmpty()) {
            listState.animateScrollToItem(rounds.size - 1)
        }
    }

    var showNumpad by remember { mutableStateOf(false) }
    var editColIndex by remember { mutableIntStateOf(-1) }
    var editRoundId by remember { mutableStateOf("") }
    var editValue by remember { mutableStateOf("") }

    // Edit Team Name State
    var showRenameDialog by remember { mutableStateOf(false) }
    var editingTeamIndex by remember { mutableIntStateOf(-1) }
    var tempTeamName by remember { mutableStateOf("") }

    // Reset Game State
    var showResetDialog by remember { mutableStateOf(false) }
    var showEndGameConfirmDialog by remember { mutableStateOf(false) }

    // Shake State
    var showShakeGap by remember { mutableStateOf(false) }

    ShakeDetector(onShake = {
        if (!showShakeGap) {
            showShakeGap = true
        }
    })

    if (showShakeGap) {
        LaunchedEffect(Unit) {
            delay(4000) // 1 second calculation + 3 seconds showing result
            showShakeGap = false
        }
        Dialog(
            onDismissRequest = { showShakeGap = false },
            properties = DialogProperties(usePlatformDefaultWidth = false)
        ) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(Color.Black.copy(alpha = 0.85f)),
                contentAlignment = Alignment.Center
            ) {
                var isCalculating by remember { mutableStateOf(true) }
                
                LaunchedEffect(Unit) {
                    delay(1000)
                    isCalculating = false
                }

                AnimatedContent(
                    targetState = isCalculating,
                    transitionSpec = {
                        (fadeIn(animationSpec = tween(300)) + scaleIn(
                            animationSpec = spring(
                                dampingRatio = Spring.DampingRatioMediumBouncy,
                                stiffness = Spring.StiffnessLow
                            )
                        )).togetherWith(
                            fadeOut(animationSpec = tween(200)) + scaleOut(animationSpec = tween(200))
                        )
                    },
                    label = "shake_dialog_content"
                ) { calculating ->
                    if (calculating) {
                        Column(
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.Center
                        ) {
                            val infiniteTransition = rememberInfiniteTransition(label = "loading")
                            val pulseScale by infiniteTransition.animateFloat(
                                initialValue = 0.9f,
                                targetValue = 1.1f,
                                animationSpec = infiniteRepeatable(
                                    animation = tween(600),
                                    repeatMode = RepeatMode.Reverse
                                ),
                                label = "pulseScale"
                            )

                            Box(
                                modifier = Modifier
                                    .size(90.dp)
                                    .scale(pulseScale),
                                contentAlignment = Alignment.Center
                            ) {
                                CircularProgressIndicator(
                                    modifier = Modifier.fillMaxSize(),
                                    color = MaterialTheme.colorScheme.primary,
                                    strokeWidth = 6.dp,
                                    trackColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.2f)
                                )
                            }

                            Spacer(modifier = Modifier.height(24.dp))

                            Text(
                                text = "FARK HESAPLANIYOR...",
                                color = Color.White.copy(alpha = 0.9f),
                                fontSize = 24.sp,
                                fontWeight = FontWeight.Bold,
                                letterSpacing = 3.sp,
                                textAlign = TextAlign.Center
                            )
                        }
                    } else {
                        val sum1 = columnSums.getOrNull(0) ?: 0
                        val sum2 = columnSums.getOrNull(1) ?: 0
                        val diff = Math.abs(sum1 - sum2)
                        val name1 = teamNames.getOrNull(0) ?: "BİZ"
                        val name2 = teamNames.getOrNull(1) ?: "ONLAR"

                        val (statusText, statusEmoji, statusColor) = when {
                            sum1 < sum2 -> Triple("$name1 ÖNDE!", "😎", PositiveGreen)
                            sum2 < sum1 -> Triple("$name2 ÖNDE!", "🥶", NegativeRed)
                            else -> Triple("BERABERE!", "⚔️", Color(0xFFF1F5F9))
                        }

                        Card(
                            modifier = Modifier
                                .padding(24.dp)
                                .fillMaxWidth(0.9f)
                                .border(
                                    width = 2.dp,
                                    color = statusColor.copy(alpha = 0.5f),
                                    shape = RoundedCornerShape(32.dp)
                                ),
                            shape = RoundedCornerShape(32.dp),
                            colors = CardDefaults.cardColors(
                                containerColor = MaterialTheme.colorScheme.surfaceContainerHigh.copy(alpha = 0.95f)
                            ),
                            elevation = CardDefaults.cardElevation(defaultElevation = 24.dp)
                        ) {
                            Column(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(horizontal = 24.dp, vertical = 40.dp),
                                horizontalAlignment = Alignment.CenterHorizontally,
                                verticalArrangement = Arrangement.Center
                            ) {
                                Text(
                                    text = statusEmoji,
                                    fontSize = 72.sp,
                                    modifier = Modifier.padding(bottom = 16.dp)
                                )

                                Text(
                                    text = statusText.uppercase(),
                                    color = statusColor,
                                    fontSize = 32.sp,
                                    fontWeight = FontWeight.Black,
                                    textAlign = TextAlign.Center,
                                    letterSpacing = 2.sp
                                )

                                Spacer(modifier = Modifier.height(24.dp))

                                HorizontalDivider(
                                    modifier = Modifier.fillMaxWidth(0.6f),
                                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.1f)
                                )

                                Spacer(modifier = Modifier.height(24.dp))

                                Text(
                                    text = "PUAN FARKİ",
                                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                                    fontSize = 14.sp,
                                    fontWeight = FontWeight.Bold,
                                    letterSpacing = 4.sp
                                )

                                Spacer(modifier = Modifier.height(8.dp))

                                Text(
                                    text = diff.toString(),
                                    color = Color.White,
                                    fontSize = 72.sp,
                                    fontWeight = FontWeight.Black,
                                    fontFamily = FontFamily.Monospace,
                                    textAlign = TextAlign.Center
                                )
                            }
                        }
                    }
                }
            }
        }
    }
    val auraColors by remember {
        derivedStateOf {
            val sum1 = columnSums.getOrNull(0) ?: 0
            val sum2 = columnSums.getOrNull(1) ?: 0
            val diff = kotlin.math.abs(sum1 - sum2)

            val maxGap = 800f
            val intensity = (diff / maxGap).coerceIn(0f, 1f)

            val isBizWinning = sum1 < sum2
            val isOnlarWinning = sum2 < sum1

            val winningColor = Color(0xFF10B981) // Emerald Green
            val losingColor = Color(0xFFEF4444) // Red
            val neutralColor = Color(0xFF1E293B) // Slate 800

            val leftColor = when {
                isBizWinning -> winningColor.copy(alpha = 0.15f + 0.35f * intensity)
                isOnlarWinning -> losingColor.copy(alpha = 0.05f + 0.15f * intensity)
                else -> neutralColor.copy(alpha = 0.2f)
            }

            val rightColor = when {
                isOnlarWinning -> winningColor.copy(alpha = 0.15f + 0.35f * intensity)
                isBizWinning -> losingColor.copy(alpha = 0.05f + 0.15f * intensity)
                else -> neutralColor.copy(alpha = 0.2f)
            }
            Pair(leftColor, rightColor)
        }
    }

    Scaffold(
        snackbarHost = { SnackbarHost(hostState = snackbarHostState) },
        topBar = {
            CenterAlignedTopAppBar(
                title = { 
                    Text(
                        text = "OKEY 101", 
                        fontWeight = FontWeight.Bold,
                        style = MaterialTheme.typography.titleLarge,
                        letterSpacing = 2.sp,
                        color = Color(0xFFF1F5F9) // Sleek grayish-white
                    ) 
                },
                navigationIcon = {
                    IconButton(onClick = { showResetDialog = true }) {
                        Icon(
                            Icons.Default.Refresh, 
                            contentDescription = "Sıfırla",
                            tint = Color(0xFFF1F5F9)
                        )
                    }
                },
                actions = {
                    val broadcastTint = if (isSpectatorActive) PositiveGreen else Color(0xFFF1F5F9)

                    IconButton(onClick = { showSpectatorDialog = true }) {
                        Icon(
                            Icons.Default.Podcasts, 
                            contentDescription = "Seyirci Yayını",
                            tint = broadcastTint
                        )
                    }

                    IconButton(onClick = { 
                        coroutineScope.launch {
                            delay(200)
                            try {
                                val bitmap = graphicsLayer.toImageBitmap().asAndroidBitmap()
                                val sum1 = columnSums.getOrNull(0) ?: 0
                                val sum2 = columnSums.getOrNull(1) ?: 0
                                val name1 = teamNames.getOrNull(0) ?: "BİZ"
                                val name2 = teamNames.getOrNull(1) ?: "ONLAR"
                                val diff = Math.abs(sum1 - sum2)
                                val status = when {
                                    sum1 < sum2 -> "$name1 ÖNDE! 😎"
                                    sum2 < sum1 -> "$name2 ÖNDE! 🥶"
                                    else -> "BERABERE! ⚔️"
                                }
                                val shareText = "🎴 *OKEY 101 SKOR TABLOSU* 🎴\n\n" +
                                        "🟢 *$name1*: $sum1\n" +
                                        "🔴 *$name2*: $sum2\n\n" +
                                        "⚖️ *Puan Farkı*: $diff\n" +
                                        "🏆 *Durum*: $status"
                                shareBitmap(context, bitmap, shareText)
                            } catch (e: Exception) {
                                e.printStackTrace()
                            }
                        }
                    }) {
                        Icon(
                            Icons.Default.Share, 
                            contentDescription = "Paylaş",
                            tint = Color(0xFFF1F5F9)
                        )
                    }
                },
                colors = TopAppBarDefaults.centerAlignedTopAppBarColors(
                    containerColor = Color.Transparent,
                    scrolledContainerColor = Color.Transparent
                )
            )
        },
        floatingActionButton = {
            ExtendedFloatingActionButton(
                onClick = { viewModel.addRound() },
                containerColor = MaterialTheme.colorScheme.tertiaryContainer,
                contentColor = Color.White
            ) {
                Text(text = "+ Yeni El", fontWeight = FontWeight.Bold)
            }
        },
        bottomBar = {
            val playedHandsCount = rounds.count { it.score1 != 0 || it.score2 != 0 }
            com.example.okey101scorer.components.SumBar(sums = columnSums, roundCount = playedHandsCount)
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .background(MaterialTheme.colorScheme.surface)
                .drawBehind {
                    val w = size.width
                    val h = size.height
                    
                    // Left Aura
                    drawRect(
                        brush = Brush.radialGradient(
                            colors = listOf(auraColors.first, Color.Transparent),
                            center = Offset(0f, h / 3f),
                            radius = w * 0.8f
                        )
                    )
                    
                    // Right Aura
                    drawRect(
                        brush = Brush.radialGradient(
                            colors = listOf(auraColors.second, Color.Transparent),
                            center = Offset(w, h / 3f),
                            radius = w * 0.8f
                        )
                    )
                }
                .drawWithContent {
                    graphicsLayer.record { this@drawWithContent.drawContent() }
                    drawLayer(graphicsLayer)
                }
        ) {
            // Dynamic Column Tint Calculations based on Score Gap
            // In 101, lower score is better. Higher score = losing = red.
            val sum1 = columnSums.getOrNull(0) ?: 0
            val sum2 = columnSums.getOrNull(1) ?: 0
            
            val maxGap = 800f // Gap points at which background becomes fully red
            val fraction1 by remember { derivedStateOf { ((sum1 - sum2).coerceAtLeast(0) / maxGap).coerceIn(0f, 1f) } }
            val fraction2 by remember { derivedStateOf { ((sum2 - sum1).coerceAtLeast(0) / maxGap).coerceIn(0f, 1f) } }

            val calmGreen = Color.Transparent
            val alarmRed = Color(0x1AF87171)  // 10% Red

            val bgTint1 by animateColorAsState(
                targetValue = lerp(calmGreen, alarmRed, fraction1),
                animationSpec = tween(durationMillis = 800),
                label = "bgTint1_anim"
            )
            val bgTint2 by animateColorAsState(
                targetValue = lerp(calmGreen, alarmRed, fraction2),
                animationSpec = tween(durationMillis = 800),
                label = "bgTint2_anim"
            )

            // Event Banner
            AnimatedVisibility(
                visible = activeEvent != TableEvent.NONE,
                enter = expandVertically() + fadeIn(),
                exit = shrinkVertically() + fadeOut()
            ) {
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 8.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.errorContainer)
                ) {
                    Row(
                        modifier = Modifier.padding(16.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text("⚠️", fontSize = 24.sp, modifier = Modifier.padding(end = 12.dp))
                        Column {
                            Text(
                                text = "AKTİF ETKİNLİK: ${activeEvent.name}",
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onErrorContainer
                            )
                            
                            val eventDesc = when (activeEvent) {
                                TableEvent.MYSTERY_BOX -> "Rastgele bir takıma sürpriz bir ceza veya ödül vurur!"
                                TableEvent.YANCI_IHTILALI -> "Kader Çarkı şanssız takıma acımasızca +101 ceza verdi."
                                TableEvent.CIFTE_KUMAR -> "Eli kazanan takım risk alır! Yazı turaya göre skor x2 veya 0 olur."
                                TableEvent.GREAT_SWAP -> "Herkes ıstakasını sağındaki oyuncuya devretti!"
                                TableEvent.KAOS_ELI -> "Bu elde girilen tüm cezalar ve puanlar 2 katı hesaplanır!"
                                else -> "Bu elde özel kurallar geçerlidir!"
                            }
                            
                            Text(
                                text = eventDesc,
                                fontSize = 12.sp,
                                color = MaterialTheme.colorScheme.onErrorContainer.copy(alpha = 0.8f)
                            )
                        }
                    }
                }
            }

            // Sticky Team Headers (Elevated)
            com.example.okey101scorer.components.TeamHeader(
                teamNames = teamNames,
                onTeamClick = { index, name ->
                    editingTeamIndex = index
                    tempTeamName = name
                    showRenameDialog = true
                }
            )

            // Dynamic Rows with SwipeToDismiss
            com.example.okey101scorer.components.RoundList(
                listState = listState,
                rounds = rounds,
                bgTint1 = bgTint1,
                bgTint2 = bgTint2,
                snackbarHostState = snackbarHostState,
                modifier = Modifier.weight(1f),
                onDeleteRound = { id -> viewModel.deleteRound(id) },
                onUndoDelete = { viewModel.undoDelete() },
                onScoreClick = { colIndex, roundId, valueStr ->
                    editColIndex = colIndex
                    editRoundId = roundId
                    editValue = valueStr
                    showNumpad = true
                }
            )
        }
    }

    // Rename Dialog
    if (showRenameDialog) {
        AlertDialog(
            onDismissRequest = { showRenameDialog = false },
            title = { Text("Takım Adını Değiştir") },
            text = {
                OutlinedTextField(
                    value = tempTeamName,
                    onValueChange = { tempTeamName = it.uppercase() },
                    singleLine = true,
                    keyboardOptions = KeyboardOptions(capitalization = KeyboardCapitalization.Characters)
                )
            },
            confirmButton = {
                TextButton(onClick = {
                    viewModel.updateTeamName(editingTeamIndex, tempTeamName)
                    showRenameDialog = false
                }) {
                    Text("Kaydet")
                }
            },
            dismissButton = {
                TextButton(onClick = { showRenameDialog = false }) {
                    Text("İptal")
                }
            }
        )
    }

    // Reset Game Dialog
    if (showResetDialog) {
        val isEventsEnabled by viewModel.isEventsEnabled.collectAsState()
        
        AlertDialog(
            onDismissRequest = { showResetDialog = false },
            title = { Text("Oyunu Sıfırla") },
            text = { 
                Column {
                    Text("Tüm oyunu sıfırlamak istediğinize emin misiniz?")
                    Spacer(modifier = Modifier.height(24.dp))
                    
                    // Parti Modu (Etkinlikler) Toggle
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text("Parti Modu (Etkinlikler)", fontWeight = FontWeight.Bold, fontSize = 16.sp)
                            Text("Açık olduğunda oyun esnasında bomba, çark ve kutu gibi rastgele olaylar yaşanır.", fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                        }
                        Switch(
                            checked = isEventsEnabled,
                            onCheckedChange = { viewModel.setEventsEnabled(it) }
                        )
                    }

                    Spacer(modifier = Modifier.height(24.dp))
                    HorizontalDivider()
                    Spacer(modifier = Modifier.height(16.dp))

                    Button(
                        onClick = { 
                            showResetDialog = false
                            showEndGameConfirmDialog = true
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.error),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text("MAÇI BİTİR VE ODAYI KAPAT", fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onError)
                    }
                }
            },
            confirmButton = {
                TextButton(onClick = {
                    viewModel.resetGame()
                    showResetDialog = false
                }) {
                    Text("Sıfırla", color = MaterialTheme.colorScheme.error)
                }
            },
            dismissButton = {
                TextButton(onClick = { showResetDialog = false }) {
                    Text("İptal")
                }
            }
        )
    }

    if (showEndGameConfirmDialog) {
        AlertDialog(
            onDismissRequest = { showEndGameConfirmDialog = false },
            title = { Text("Maçı Bitir ve Kapat", color = MaterialTheme.colorScheme.error) },
            text = { Text("Oyun tamamen bitirilecek, veritabanı temizlenecek ve web izleyicilerinin bağlantısı kesilecek. Emin misiniz?") },
            confirmButton = {
                Button(
                    onClick = {
                        viewModel.endGameAndCloseRoom()
                        showEndGameConfirmDialog = false
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.error)
                ) {
                    Text("Evet, Bitir")
                }
            },
            dismissButton = {
                TextButton(onClick = { showEndGameConfirmDialog = false }) {
                    Text("İptal")
                }
            }
        )
    }

    ActiveEventDialogHandler(
        dialogState = activeEventDialog,
        onDismiss = { viewModel.dismissEventDialog() },
        onResolveCifteKumar = { acceptRisk ->
            if (activeEventDialog is ActiveEventDialog.CifteKumar) {
                val state = activeEventDialog as ActiveEventDialog.CifteKumar
                viewModel.resolveCifteKumar(acceptRisk, state.roundId, state.teamIndex, state.currentPenalty)
            }
        }
    )

    NumpadOverlay(
        showNumpad = showNumpad,
        initialValue = editValue,
        teamName = if (editColIndex != -1) teamNames.getOrNull(editColIndex) else null,
        onDismiss = { showNumpad = false },
        onDone = { newValue ->
            val currentRound = rounds.find { it.id == editRoundId }
            viewModel.updateCell(editRoundId, editColIndex, newValue)

            if (currentRound != null) {
                val otherColIndex = if (editColIndex == 0) 1 else 0
                val isOtherEntered = if (otherColIndex == 0) currentRound.isScore1Entered else currentRound.isScore2Entered
                
                if (!isOtherEntered) {
                    editColIndex = otherColIndex
                    editValue = ""
                } else {
                    showNumpad = false
                }
            } else {
                showNumpad = false
            }
        }
    )

    if (showSpectatorDialog) {
        Dialog(
            onDismissRequest = { showSpectatorDialog = false }
        ) {
            Card(
                modifier = Modifier
                    .padding(16.dp)
                    .fillMaxWidth()
                    .border(
                        width = 2.dp,
                        color = if (isSpectatorActive) PositiveGreen.copy(alpha = 0.5f) else Color.White.copy(alpha = 0.2f),
                        shape = RoundedCornerShape(24.dp)
                    ),
                shape = RoundedCornerShape(24.dp),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.surfaceContainerHigh.copy(alpha = 0.95f)
                ),
                elevation = CardDefaults.cardElevation(defaultElevation = 16.dp)
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(24.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(
                        text = "SEYİRCİ YAYINI",
                        fontWeight = FontWeight.Black,
                        fontSize = 20.sp,
                        letterSpacing = 2.sp,
                        color = Color.White
                    )

                    Spacer(modifier = Modifier.height(16.dp))

                    if (isSpectatorActive && roomId != null) {
                        val spectatorUrl = "https://hakanyilmazzz.github.io/okey101Scorer/?room=$roomId"
                        val qrBitmap = rememberQrCodeBitmap(spectatorUrl)

                        Box(
                            modifier = Modifier
                                .size(200.dp)
                                .background(Color.White, RoundedCornerShape(16.dp))
                                .padding(8.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            if (qrBitmap != null) {
                                Image(
                                    bitmap = qrBitmap.asImageBitmap(),
                                    contentDescription = "Yancı QR Kodu",
                                    modifier = Modifier.fillMaxSize()
                                )
                            } else {
                                CircularProgressIndicator(color = MaterialTheme.colorScheme.primary)
                            }
                        }

                        Spacer(modifier = Modifier.height(16.dp))

                        Text(
                            text = "ODA KODU",
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            letterSpacing = 2.sp
                        )

                        Text(
                            text = roomId ?: "",
                            fontSize = 36.sp,
                            fontWeight = FontWeight.Black,
                            fontFamily = FontFamily.Monospace,
                            color = PositiveGreen,
                            letterSpacing = 4.sp
                        )

                        Spacer(modifier = Modifier.height(12.dp))

                        Text(
                            text = "Yancılar bu QR kodu taratarak oyunu kendi tarayıcılarından canlı izleyebilir!",
                            fontSize = 13.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            textAlign = TextAlign.Center,
                            lineHeight = 18.sp
                        )

                        Spacer(modifier = Modifier.height(24.dp))

                        Button(
                            onClick = { viewModel.stopBroadcast() },
                            colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.error),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Text("YAYINI DURDUR", fontWeight = FontWeight.Bold)
                        }
                    } else {
                        Icon(
                            Icons.Default.Podcasts,
                            contentDescription = "Yayın Kapalı",
                            tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.4f),
                            modifier = Modifier.size(80.dp)
                        )

                        Spacer(modifier = Modifier.height(16.dp))

                        Text(
                            text = "Yayın Kapalı",
                            fontSize = 18.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color.White
                        )

                        Spacer(modifier = Modifier.height(8.dp))

                        Text(
                            text = "Yayını başlatarak yancıların oyunu kendi telefonlarından canlı izlemesini sağlayabilirsiniz.",
                            fontSize = 13.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            textAlign = TextAlign.Center,
                            lineHeight = 18.sp
                        )

                        Spacer(modifier = Modifier.height(24.dp))

                        Button(
                            onClick = { viewModel.startBroadcast() },
                            colors = ButtonDefaults.buttonColors(containerColor = PositiveGreen),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Text("YAYINI BAŞLAT", fontWeight = FontWeight.Bold, color = Color.Black)
                        }
                    }
                }
            }
        }
    }

    // Floating emoji particles layer
    activeParticles.forEach { particle ->
        key(particle.id) {
            FloatingEmoji(
                particle = particle,
                onAnimationFinished = {
                    activeParticles.remove(particle)
                }
            )
        }
    }

    // Banter Chat overlay stack positioned elegantly in bottom-left corner
    Box(
        modifier = Modifier
            .fillMaxSize()
            .padding(bottom = 85.dp, start = 16.dp, end = 16.dp), // Elevated above Bottom SumRow
        contentAlignment = Alignment.BottomStart
    ) {
        Column(
            verticalArrangement = Arrangement.spacedBy(8.dp),
            horizontalAlignment = Alignment.Start,
            modifier = Modifier.width(280.dp) // Premium narrow width for bubble stack
        ) {
            visibleChats.forEach { chat ->
                key(chat.id) {
                    var isVisible by remember { mutableStateOf(false) }
                    LaunchedEffect(Unit) {
                        isVisible = true
                    }
                    
                    AnimatedVisibility(
                        visible = isVisible,
                        enter = slideInHorizontally(
                            initialOffsetX = { -it },
                            animationSpec = tween(durationMillis = 350, easing = FastOutSlowInEasing)
                        ) + fadeIn(animationSpec = tween(durationMillis = 200)),
                        exit = slideOutHorizontally(
                            targetOffsetX = { -it },
                            animationSpec = tween(durationMillis = 300, easing = LinearOutSlowInEasing)
                        ) + fadeOut(animationSpec = tween(durationMillis = 150))
                    ) {
                        Surface(
                            shape = RoundedCornerShape(16.dp),
                            color = Color(0xE61E293B), // Sleek semi-transparent dark card-bg (1E293B)
                            modifier = Modifier
                                .fillMaxWidth()
                                .border(1.dp, Color(0x33FFFFFF), RoundedCornerShape(16.dp)),
                            shadowElevation = 8.dp
                        ) {
                            Row(
                                modifier = Modifier.padding(horizontal = 12.dp, vertical = 10.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                // Avatar Capsule
                                Surface(
                                    shape = RoundedCornerShape(8.dp),
                                    color = Color(0x1AFFFFFF),
                                    modifier = Modifier.size(28.dp)
                                ) {
                                    Box(contentAlignment = Alignment.Center) {
                                        Text(text = chat.senderAvatar, fontSize = 16.sp)
                                    }
                                }
                                Spacer(modifier = Modifier.width(8.dp))
                                Column {
                                    Text(
                                        text = chat.senderName.uppercase(),
                                        fontSize = 10.sp,
                                        fontWeight = FontWeight.Black,
                                        color = PositiveGreen, // Sleek neon accent green
                                        letterSpacing = 1.sp
                                    )
                                    Spacer(modifier = Modifier.height(1.dp))
                                    Text(
                                        text = chat.message,
                                        fontSize = 13.sp,
                                        fontWeight = FontWeight.Medium,
                                        color = Color.White,
                                        lineHeight = 16.sp
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}


@Composable
fun ShakeDetector(onShake: () -> Unit) {
    val context = LocalContext.current
    val currentOnShake by rememberUpdatedState(onShake)
    
    DisposableEffect(Unit) {
        val sensorManager = context.getSystemService(Context.SENSOR_SERVICE) as SensorManager
        val accelerometer = sensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER)

        var lastUpdate: Long = 0
        val shakeThreshold = 15f 

        val sensorEventListener = object : SensorEventListener {
            override fun onSensorChanged(event: SensorEvent) {
                val curTime = System.currentTimeMillis()
                if ((curTime - lastUpdate) > 200) { 
                    val gX = event.values[0]
                    val gY = event.values[1]
                    val gZ = event.values[2]

                    val gForce = sqrt((gX * gX + gY * gY + gZ * gZ).toDouble()).toFloat()
                    val acceleration = kotlin.math.abs(gForce - SensorManager.GRAVITY_EARTH)

                    if (acceleration > shakeThreshold) {
                        lastUpdate = curTime
                        currentOnShake()
                    }
                }
            }
            override fun onAccuracyChanged(sensor: Sensor?, accuracy: Int) {}
        }

        if (accelerometer != null) {
            sensorManager.registerListener(sensorEventListener, accelerometer, SensorManager.SENSOR_DELAY_UI)
        }
        onDispose {
            sensorManager.unregisterListener(sensorEventListener)
        }
    }
}

private fun shareBitmap(context: Context, bitmap: Bitmap, shareText: String) {
    try {
        val cacheDir = File(context.cacheDir, "images")
        cacheDir.mkdirs()
        val file = File(cacheDir, "skor.png")
        val fos = FileOutputStream(file)
        bitmap.compress(Bitmap.CompressFormat.PNG, 100, fos)
        fos.close()

        val uri: Uri = FileProvider.getUriForFile(
            context,
            "${context.packageName}.provider",
            file
        )

        val intent = Intent(Intent.ACTION_SEND).apply {
            type = "image/png"
            putExtra(Intent.EXTRA_STREAM, uri)
            putExtra(Intent.EXTRA_TEXT, shareText)
            addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
        }
        context.startActivity(Intent.createChooser(intent, "Skoru Paylaş"))
    } catch (e: Exception) {
        e.printStackTrace()
    }
}

@Composable
fun rememberQrCodeBitmap(data: String): Bitmap? {
    var bitmap by remember(data) { mutableStateOf<Bitmap?>(null) }
    LaunchedEffect(data) {
        if (data.isNotEmpty()) {
            withContext(Dispatchers.Default) {
                try {
                    val writer = QRCodeWriter()
                    val bitMatrix = writer.encode(data, BarcodeFormat.QR_CODE, 400, 400)
                    val width = bitMatrix.width
                    val height = bitMatrix.height
                    val bmp = Bitmap.createBitmap(width, height, Bitmap.Config.RGB_565)
                    for (x in 0 until width) {
                        for (y in 0 until height) {
                            bmp.setPixel(x, y, if (bitMatrix[x, y]) android.graphics.Color.BLACK else android.graphics.Color.WHITE)
                        }
                    }
                    bitmap = bmp
                } catch (e: Exception) {
                    e.printStackTrace()
                }
            }
        }
    }
    return bitmap
}

data class ReactionParticle(
    val id: Long,
    val emoji: String,
    val senderName: String,
    val senderAvatar: String,
    val startX: Float,
    val duration: Int
)

@Composable
fun FloatingEmoji(
    particle: ReactionParticle,
    onAnimationFinished: () -> Unit
) {
    val animY = remember { Animatable(1f) }
    val animAlpha = remember { Animatable(1f) }

    LaunchedEffect(Unit) {
        launch {
            animY.animateTo(
                targetValue = 0f,
                animationSpec = tween(durationMillis = particle.duration, easing = LinearEasing)
            )
            onAnimationFinished()
        }
        launch {
            delay((particle.duration * 0.5f).toLong())
            animAlpha.animateTo(
                targetValue = 0f,
                animationSpec = tween(durationMillis = (particle.duration * 0.5f).toInt(), easing = LinearEasing)
            )
        }
    }

    BoxWithConstraints(modifier = Modifier.fillMaxSize()) {
        val screenWidth = maxWidth
        val screenHeight = maxHeight

        val xOffset = screenWidth * particle.startX
        val yOffset = screenHeight * animY.value

        val sway = 24.dp * kotlin.math.sin(animY.value * 3 * Math.PI.toFloat())

        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier
                .offset(x = xOffset + sway, y = yOffset)
                .graphicsLayer { alpha = animAlpha.value }
        ) {
            Text(
                text = particle.emoji,
                fontSize = 44.sp
            )
            if (particle.senderName.isNotBlank() && particle.senderName != "Yancı") {
                Spacer(modifier = Modifier.height(2.dp))
                Surface(
                    shape = RoundedCornerShape(12.dp),
                    color = Color(0xCC1E293B), // Sleek semi-transparent dark card-bg (1E293B)
                    modifier = Modifier
                        .wrapContentSize()
                        .border(0.5.dp, Color(0x33FFFFFF), RoundedCornerShape(12.dp))
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = particle.senderAvatar,
                            fontSize = 10.sp
                        )
                        Spacer(modifier = Modifier.width(3.dp))
                        Text(
                            text = particle.senderName.uppercase(),
                            fontSize = 9.sp,
                            fontWeight = FontWeight.Black,
                            color = Color.White
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun NumpadOverlay(
    showNumpad: Boolean,
    initialValue: String,
    teamName: String? = null,
    onDismiss: () -> Unit,
    onDone: (Int) -> Unit
) {
    // Isolate the typing state so it DOES NOT recompose the massive MainScreen!
    var localValue by remember(showNumpad, initialValue, teamName) { mutableStateOf(initialValue) }
    
    val numpadOffset by animateFloatAsState(
        targetValue = if (showNumpad) 0f else 1f,
        animationSpec = tween(durationMillis = 300, easing = FastOutSlowInEasing),
        label = "numpadOffset"
    )
    val numpadAlpha by animateFloatAsState(
        targetValue = if (showNumpad) 1f else 0f,
        animationSpec = tween(durationMillis = 200),
        label = "numpadAlpha"
    )

    // Keep it in composition but invisible/offscreen
    if (numpadAlpha > 0f || showNumpad) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .graphicsLayer { alpha = numpadAlpha }
                .background(Color.Black.copy(alpha = 0.6f))
                .clickable(
                    interactionSource = remember { MutableInteractionSource() },
                    indication = null
                ) { onDismiss() },
            contentAlignment = Alignment.BottomCenter
        ) {
            Surface(
                modifier = Modifier
                    .fillMaxWidth()
                    .graphicsLayer {
                        translationY = size.height * numpadOffset
                    }
                    .clickable(
                        interactionSource = remember { MutableInteractionSource() },
                        indication = null
                    ) {},
                shape = RoundedCornerShape(topStart = 24.dp, topEnd = 24.dp),
                color = MaterialTheme.colorScheme.surfaceContainer
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Box(
                        modifier = Modifier
                            .padding(vertical = 22.dp)
                            .width(32.dp)
                            .height(4.dp)
                            .background(MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.4f), RoundedCornerShape(2.dp))
                    )
                    CustomNumpad(
                        currentValue = localValue,
                        teamName = teamName,
                        onValueChange = { localValue = it },
                        onDone = {
                            val newValue = localValue.toIntOrNull() ?: 0
                            onDone(newValue)
                        }
                    )
                }
            }
        }
    }
}


