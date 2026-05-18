package com.example.okey101scorer

import android.content.Context
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.hardware.Sensor
import android.hardware.SensorEvent
import android.hardware.SensorEventListener
import android.hardware.SensorManager
import android.net.Uri
import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.Image
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.ui.text.input.KeyboardCapitalization
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Delete
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
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import androidx.core.content.FileProvider
import com.example.okey101scorer.ui.theme.NegativeRed
import com.example.okey101scorer.ui.theme.PenaltyDarkRed
import com.example.okey101scorer.ui.theme.PenaltyLightRed
import com.example.okey101scorer.ui.theme.PositiveGreen
import com.example.okey101scorer.ui.theme.ZebraStripeTint
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.File
import java.io.FileOutputStream
import java.net.HttpURLConnection
import java.net.URL
import java.net.URLEncoder
import kotlin.math.sqrt

@OptIn(ExperimentalMaterial3Api::class, ExperimentalFoundationApi::class)
@Composable
fun MainScreen(viewModel: ScoreViewModel) {
    val rounds by viewModel.rounds.collectAsState()
    val columnSums by viewModel.columnSums.collectAsState()
    val teamNames by viewModel.teamNames.collectAsState()
    val isSpectatorActive by viewModel.isSpectatorActive.collectAsState()
    val roomId by viewModel.roomId.collectAsState()
    var showSpectatorDialog by remember { mutableStateOf(false) }

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
    var editColIndex by remember { mutableStateOf(-1) }
    var editRoundId by remember { mutableStateOf("") }
    var editValue by remember { mutableStateOf("") }
    
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)

    // Edit Team Name State
    var showRenameDialog by remember { mutableStateOf(false) }
    var editingTeamIndex by remember { mutableStateOf(-1) }
    var tempTeamName by remember { mutableStateOf("") }

    // Reset Game State
    var showResetDialog by remember { mutableStateOf(false) }

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
            SumRow(sums = columnSums, roundCount = rounds.size)
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .background(MaterialTheme.colorScheme.surface)
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
            val fraction1 = ((sum1 - sum2).coerceAtLeast(0) / maxGap).coerceIn(0f, 1f)
            val fraction2 = ((sum2 - sum1).coerceAtLeast(0) / maxGap).coerceIn(0f, 1f)

            val calmGreen = Color(0x1A4ADE80) // 10% Green
            val alarmRed = Color(0x33F87171)  // 20% Red

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

            // Sticky Team Headers (Elevated)
            Surface(
                color = MaterialTheme.colorScheme.surfaceContainerHighest,
                shadowElevation = 4.dp
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 16.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = teamNames[0],
                        modifier = Modifier
                            .weight(1f)
                            .clickable {
                                editingTeamIndex = 0
                                tempTeamName = teamNames[0]
                                showRenameDialog = true
                            },
                        textAlign = TextAlign.Center,
                        fontWeight = FontWeight.ExtraBold,
                        fontSize = 24.sp,
                        color = Color.White
                    )
                    
                    VerticalDivider(
                        modifier = Modifier.height(32.dp),
                        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.2f)
                    )
                    
                    Text(
                        text = teamNames[1],
                        modifier = Modifier
                            .weight(1f)
                            .clickable {
                                editingTeamIndex = 1
                                tempTeamName = teamNames[1]
                                showRenameDialog = true
                            },
                        textAlign = TextAlign.Center,
                        fontWeight = FontWeight.ExtraBold,
                        fontSize = 24.sp,
                        color = Color.White
                    )
                }
            }

            // Dynamic Rows with SwipeToDismiss
            LazyColumn(
                state = listState,
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f),
                contentPadding = PaddingValues(bottom = 88.dp)
            ) {
                itemsIndexed(items = rounds, key = { _, round -> round.id }) { index, round ->
                    
                    val isOdd = index % 2 != 0
                    val rowBackground = if (isOdd) ZebraStripeTint else Color.Transparent

                    @Composable
                    fun RoundRowContent() {
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
                                    editColIndex = 0
                                    editRoundId = round.id
                                    editValue = if (scoresArray[0] == 0) "" else scoresArray[0].toString()
                                    showNumpad = true
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
                                    editColIndex = 1
                                    editRoundId = round.id
                                    editValue = if (scoresArray[1] == 0) "" else scoresArray[1].toString()
                                    showNumpad = true
                                }
                            )
                        }
                    }

                    if (index == 0) {
                        Box(modifier = Modifier.animateItem()) {
                            RoundRowContent()
                        }
                    } else {
                        val dismissState = rememberSwipeToDismissBoxState(
                            confirmValueChange = {
                                if (it == SwipeToDismissBoxValue.EndToStart || it == SwipeToDismissBoxValue.StartToEnd) {
                                    viewModel.deleteRound(round.id)
                                    coroutineScope.launch {
                                        val result = snackbarHostState.showSnackbar(
                                            message = "El silindi",
                                            actionLabel = "Geri Al",
                                            duration = SnackbarDuration.Short
                                        )
                                        if (result == SnackbarResult.ActionPerformed) {
                                            viewModel.undoDelete()
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
                            RoundRowContent()
                        }
                    }
                }
            }
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
        AlertDialog(
            onDismissRequest = { showResetDialog = false },
            title = { Text("Oyunu Sıfırla") },
            text = { Text("Tüm oyunu sıfırlamak istediğinize emin misiniz?") },
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

    AnimatedVisibility(
        visible = showNumpad,
        enter = fadeIn(animationSpec = tween(600)),
        exit = fadeOut(animationSpec = tween(600))
    ) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(Color.Black.copy(alpha = 0.5f))
                .clickable(
                    interactionSource = remember { MutableInteractionSource() },
                    indication = null
                ) { showNumpad = false },
            contentAlignment = Alignment.BottomCenter
        ) {
            AnimatedVisibility(
                visible = showNumpad,
                enter = slideInVertically(
                    initialOffsetY = { it },
                    animationSpec = tween(durationMillis = 600, easing = FastOutSlowInEasing)
                ),
                exit = slideOutVertically(
                    targetOffsetY = { it },
                    animationSpec = tween(durationMillis = 600, easing = FastOutSlowInEasing)
                )
            ) {
                Surface(
                    modifier = Modifier
                        .fillMaxWidth()
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
                            currentValue = editValue,
                            onValueChange = { editValue = it },
                            onDone = {
                                val newValue = editValue.toIntOrNull() ?: 0
                                viewModel.updateCell(editRoundId, editColIndex, newValue)
                                showNumpad = false
                            }
                        )
                    }
                }
            }
        }
    }

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
                        val spectatorUrl = "https://fb-ha.github.io/okey101Scorer/?room=$roomId"
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
}

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

    val scale = remember { Animatable(1f) }

    LaunchedEffect(value) {
        if (value != 0) {
            scale.snapTo(1.4f)
            scale.animateTo(
                targetValue = 1f,
                animationSpec = spring(
                    dampingRatio = Spring.DampingRatioMediumBouncy,
                    stiffness = Spring.StiffnessMedium
                )
            )
        } else {
            scale.snapTo(1f)
        }
    }

    val displayValue = if (value == 0) "" else value.toString()
    val emoji = if (value == -202) " 🔥" else ""

    Box(
        modifier = modifier
            .fillMaxWidth()
            .height(72.dp)
            .clickable(onClick = onClick)
            .padding(4.dp)
            .scale(scale.value)
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
fun SumRow(sums: List<Int>, roundCount: Int) {
    val sum1 = sums.getOrNull(0) ?: 0
    val sum2 = sums.getOrNull(1) ?: 0
    val diff = Math.abs(sum1 - sum2)

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
                    val targetCrown1 = if (diff >= 500 && targetSum < sum2) " 👑" else ""
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
                    val targetCrown2 = if (diff >= 500 && targetSum < sum1) " 👑" else ""
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
                    val acceleration = Math.abs(gForce - SensorManager.GRAVITY_EARTH)

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
            withContext(Dispatchers.IO) {
                try {
                    val url = URL("https://api.qrserver.com/v1/create-qr-code/?size=400x400&margin=10&data=${URLEncoder.encode(data, "UTF-8")}")
                    val connection = url.openConnection() as HttpURLConnection
                    connection.doInput = true
                    connection.connect()
                    val input = connection.inputStream
                    bitmap = BitmapFactory.decodeStream(input)
                    connection.disconnect()
                } catch (e: Exception) {
                    e.printStackTrace()
                }
            }
        }
    }
    return bitmap
}
