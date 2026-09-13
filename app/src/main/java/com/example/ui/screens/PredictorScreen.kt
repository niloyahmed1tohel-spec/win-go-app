package com.example.ui.screens

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Error
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Send
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.Stop
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalClipboardManager
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.Prediction
import com.example.data.model.PredictionStatus
import com.example.ui.theme.BackgroundDark
import com.example.ui.theme.CardBorder
import com.example.ui.theme.SurfaceDark
import com.example.ui.theme.SurfaceHighlight
import com.example.ui.theme.SurfaceVariantDark
import com.example.ui.theme.TextMuted
import com.example.ui.theme.TextPrimary
import com.example.ui.theme.TextSecondary
import com.example.ui.theme.WinGoCyan
import com.example.ui.theme.WinGoGold
import com.example.ui.theme.WinGoGreen
import com.example.ui.theme.WinGoGreenContainer
import com.example.ui.theme.WinGoRed
import com.example.ui.theme.WinGoRedContainer
import com.example.ui.theme.WinGoViolet
import com.example.ui.theme.WinGoVioletContainer
import com.example.ui.viewmodel.BotConnectionState
import com.example.ui.viewmodel.PredictorViewModel
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PredictorScreen(
    viewModel: PredictorViewModel,
    modifier: Modifier = Modifier
) {
    val isRunning by viewModel.isRunning.collectAsState()
    val currentPeriod by viewModel.currentPeriod.collectAsState()
    val lastPostedPeriod by viewModel.lastPostedPeriod.collectAsState()
    val countdownSeconds by viewModel.countdownSeconds.collectAsState()
    val isBusy by viewModel.isBusy.collectAsState()
    val botConnectionState by viewModel.botConnectionState.collectAsState()
    val predictions by viewModel.predictions.collectAsState()

    val botToken by viewModel.botToken.collectAsState()
    val chatId by viewModel.chatId.collectAsState()
    val officialTag by viewModel.officialTag.collectAsState()

    val snackbarHostState = remember { SnackbarHostState() }
    val stats = remember(predictions) { viewModel.computeStats(predictions) }

    var showPeriodDialog by remember { mutableStateOf(false) }
    var showSettingsDialog by remember { mutableStateOf(false) }
    var showClearHistoryDialog by remember { mutableStateOf(false) }

    LaunchedEffect(key1 = true) {
        viewModel.snackbarEvent.collect { message ->
            snackbarHostState.showSnackbar(message)
        }
    }

    Scaffold(
        modifier = modifier.fillMaxSize(),
        containerColor = BackgroundDark,
        snackbarHost = { SnackbarHost(hostState = snackbarHostState) },
        topBar = {
            TopAppBar(
                title = {
                    Column {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text(
                                text = "Win Go Predictor",
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold,
                                color = TextPrimary
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Surface(
                                shape = RoundedCornerShape(4.dp),
                                color = WinGoVioletContainer,
                                border = BorderStroke(1.dp, WinGoViolet.copy(alpha = 0.5f))
                            ) {
                                Text(
                                    text = "ADMIN",
                                    fontSize = 10.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = Color(0xFFE0AAFF),
                                    modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                                )
                            }
                        }
                        BotStatusChip(botConnectionState)
                    }
                },
                actions = {
                    IconButton(
                        onClick = { showSettingsDialog = true },
                        modifier = Modifier.testTag("btn_settings")
                    ) {
                        Icon(
                            imageVector = Icons.Default.Settings,
                            contentDescription = "Bot Settings",
                            tint = TextSecondary
                        )
                    }
                    if (predictions.isNotEmpty()) {
                        IconButton(
                            onClick = { showClearHistoryDialog = true },
                            modifier = Modifier.testTag("btn_clear_history")
                        ) {
                            Icon(
                                imageVector = Icons.Default.Clear,
                                contentDescription = "Clear History",
                                tint = TextMuted
                            )
                        }
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = SurfaceDark
                )
            )
        }
    ) { innerPadding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding),
            contentPadding = PaddingValues(horizontal = 16.dp, vertical = 12.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            // Section 1: Live Status & Countdown Timer Banner
            item {
                StatusCountdownBanner(
                    isRunning = isRunning,
                    countdownSeconds = countdownSeconds,
                    currentPeriod = currentPeriod
                )
            }

            // Section 2: Period Control Card
            item {
                PeriodControlCard(
                    currentPeriod = currentPeriod,
                    lastPostedPeriod = lastPostedPeriod,
                    onEditPeriodClick = { showPeriodDialog = true },
                    onIncrementPeriod = { viewModel.incrementPeriod() }
                )
            }

            // Section 3: Live Signal Preview Box (Recreates the Telegram Card format)
            item {
                val latestPrediction = predictions.firstOrNull()
                LiveSignalPreviewCard(
                    prediction = latestPrediction,
                    officialTag = officialTag,
                    fallbackPeriod = currentPeriod
                )
            }

            // Section 4: Primary Admin Control Buttons Grid
            item {
                AdminButtonsGrid(
                    isRunning = isRunning,
                    isBusy = isBusy,
                    onStartClick = { viewModel.startAutoSignal() },
                    onStopClick = { viewModel.stopAutoSignal() },
                    onSendNowClick = { viewModel.sendSingleSignal() },
                    onWinClick = { viewModel.markWin() },
                    onLossClick = { viewModel.markLoss() },
                    onSetPeriodClick = { showPeriodDialog = true }
                )
            }

            // Section 5: Stats Overview Bar
            item {
                StatsOverviewCard(stats = stats)
            }

            // Section 6: Prediction History Header
            item {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "Broadcast History (${predictions.size})",
                        style = MaterialTheme.typography.titleSmall,
                        fontWeight = FontWeight.Bold,
                        color = TextPrimary
                    )
                    Text(
                        text = "Channel: $chatId",
                        fontSize = 11.sp,
                        color = TextMuted
                    )
                }
            }

            // Section 7: History List
            if (predictions.isEmpty()) {
                item {
                    EmptyHistoryCard()
                }
            } else {
                items(predictions, key = { it.id }) { prediction ->
                    HistoryItemCard(
                        prediction = prediction,
                        onMarkStatus = { status ->
                            viewModel.markPredictionStatus(prediction.period, status)
                        }
                    )
                }
            }
        }
    }

    // Dialog 1: Set Period Dialog
    if (showPeriodDialog) {
        SetPeriodDialog(
            currentPeriod = currentPeriod,
            onDismiss = { showPeriodDialog = false },
            onConfirm = { newPeriod ->
                viewModel.setPeriod(newPeriod)
                showPeriodDialog = false
            }
        )
    }

    // Dialog 2: Bot Settings Dialog
    if (showSettingsDialog) {
        BotSettingsDialog(
            currentBotToken = botToken,
            currentChatId = chatId,
            currentOfficialTag = officialTag,
            botConnectionState = botConnectionState,
            onTestConnection = { viewModel.testBotConnection() },
            onDismiss = { showSettingsDialog = false },
            onSave = { token, chat, tag ->
                viewModel.updateConfig(token, chat, tag)
                showSettingsDialog = false
            }
        )
    }

    // Dialog 3: Clear History Dialog
    if (showClearHistoryDialog) {
        AlertDialog(
            onDismissRequest = { showClearHistoryDialog = false },
            title = { Text("Clear History?", color = TextPrimary) },
            text = { Text("Are you sure you want to clear all recorded prediction signals?", color = TextSecondary) },
            confirmButton = {
                Button(
                    onClick = {
                        viewModel.clearHistory()
                        showClearHistoryDialog = false
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = WinGoRed)
                ) {
                    Text("Clear All")
                }
            },
            dismissButton = {
                TextButton(onClick = { showClearHistoryDialog = false }) {
                    Text("Cancel", color = TextSecondary)
                }
            },
            containerColor = SurfaceDark
        )
    }
}

@Composable
fun BotStatusChip(state: BotConnectionState) {
    Row(verticalAlignment = Alignment.CenterVertically) {
        val (indicatorColor, statusText) = when (state) {
            is BotConnectionState.Connected -> Pair(WinGoGreen, "Bot: @${state.username ?: state.botName}")
            is BotConnectionState.Checking -> Pair(WinGoGold, "Connecting to Telegram...")
            is BotConnectionState.Error -> Pair(WinGoRed, "Bot Error")
            is BotConnectionState.Idle -> Pair(TextMuted, "Bot Standby")
        }

        Box(
            modifier = Modifier
                .size(7.dp)
                .clip(CircleShape)
                .background(indicatorColor)
        )
        Spacer(modifier = Modifier.width(5.dp))
        Text(
            text = statusText,
            fontSize = 11.sp,
            color = if (state is BotConnectionState.Error) WinGoRed else TextSecondary
        )
    }
}

@Composable
fun StatusCountdownBanner(
    isRunning: Boolean,
    countdownSeconds: Int,
    currentPeriod: Long
) {
    val infiniteTransition = rememberInfiniteTransition(label = "pulse")
    val pulseAlpha by infiniteTransition.animateFloat(
        initialValue = 0.5f,
        targetValue = 1.0f,
        animationSpec = infiniteRepeatable(
            animation = tween(800, easing = LinearEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "pulse_alpha"
    )

    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(14.dp),
        colors = CardDefaults.cardColors(
            containerColor = if (isRunning) SurfaceHighlight else SurfaceDark
        ),
        border = BorderStroke(
            1.5.dp,
            if (isRunning) WinGoGreen.copy(alpha = 0.8f) else CardBorder
        )
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        modifier = Modifier
                            .size(12.dp)
                            .clip(CircleShape)
                            .alpha(if (isRunning) pulseAlpha else 1f)
                            .background(if (isRunning) WinGoGreen else TextMuted)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = if (isRunning) "AUTO SIGNAL RUNNING" else "AUTO SIGNAL STOPPED",
                        fontWeight = FontWeight.Bold,
                        fontSize = 13.sp,
                        color = if (isRunning) WinGoGreen else TextSecondary
                    )
                }

                if (isRunning) {
                    Surface(
                        shape = RoundedCornerShape(6.dp),
                        color = WinGoGreenContainer
                    ) {
                        Text(
                            text = "1-MIN LOOP",
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            color = WinGoGreen,
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp)
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            if (isRunning) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Column {
                        Text(
                            text = "Next broadcast for period",
                            fontSize = 12.sp,
                            color = TextSecondary
                        )
                        Text(
                            text = currentPeriod.toString(),
                            fontFamily = FontFamily.Monospace,
                            fontWeight = FontWeight.Bold,
                            fontSize = 15.sp,
                            color = WinGoGold
                        )
                    }

                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(
                            text = "${countdownSeconds}s",
                            fontFamily = FontFamily.Monospace,
                            fontSize = 24.sp,
                            fontWeight = FontWeight.ExtraBold,
                            color = if (countdownSeconds <= 10) WinGoRed else WinGoGreen
                        )
                    }
                }

                Spacer(modifier = Modifier.height(8.dp))
                val progress = (countdownSeconds.toFloat() / 60f).coerceIn(0f, 1f)
                LinearProgressIndicator(
                    progress = { progress },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(6.dp)
                        .clip(RoundedCornerShape(3.dp)),
                    color = if (countdownSeconds <= 10) WinGoRed else WinGoGreen,
                    trackColor = BackgroundDark
                )
            } else {
                Text(
                    text = "Tap '🚀 Start Signal' below to begin automated 1-minute live prediction broadcasting to your Telegram channel.",
                    fontSize = 13.sp,
                    color = TextSecondary,
                    lineHeight = 18.sp
                )
            }
        }
    }
}

@Composable
fun PeriodControlCard(
    currentPeriod: Long,
    lastPostedPeriod: Long,
    onEditPeriodClick: () -> Unit,
    onIncrementPeriod: () -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(14.dp),
        colors = CardDefaults.cardColors(containerColor = SurfaceDark),
        border = BorderStroke(1.dp, CardBorder)
    ) {
        Column(modifier = Modifier.padding(14.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(
                        text = "CURRENT PERIOD",
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        color = TextMuted
                    )
                    Spacer(modifier = Modifier.height(2.dp))
                    Text(
                        text = currentPeriod.toString(),
                        fontFamily = FontFamily.Monospace,
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold,
                        color = TextPrimary
                    )
                }

                Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                    OutlinedButton(
                        onClick = onIncrementPeriod,
                        shape = RoundedCornerShape(8.dp),
                        contentPadding = PaddingValues(horizontal = 10.dp, vertical = 4.dp),
                        border = BorderStroke(1.dp, CardBorder)
                    ) {
                        Text("+1", fontSize = 12.sp, color = WinGoCyan, fontWeight = FontWeight.Bold)
                    }

                    Button(
                        onClick = onEditPeriodClick,
                        shape = RoundedCornerShape(8.dp),
                        contentPadding = PaddingValues(horizontal = 10.dp, vertical = 4.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = SurfaceHighlight),
                        modifier = Modifier.testTag("btn_set_period")
                    ) {
                        Icon(
                            imageVector = Icons.Default.Edit,
                            contentDescription = "Edit Period",
                            modifier = Modifier.size(14.dp),
                            tint = WinGoGold
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("Set", fontSize = 12.sp, color = WinGoGold)
                    }
                }
            }

            Spacer(modifier = Modifier.height(8.dp))
            HorizontalDivider(color = CardBorder.copy(alpha = 0.5f))
            Spacer(modifier = Modifier.height(8.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    text = "Last Broadcasted Period:",
                    fontSize = 12.sp,
                    color = TextSecondary
                )
                Text(
                    text = lastPostedPeriod.toString(),
                    fontFamily = FontFamily.Monospace,
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Medium,
                    color = TextPrimary
                )
            }
        }
    }
}

@Composable
fun LiveSignalPreviewCard(
    prediction: Prediction?,
    officialTag: String,
    fallbackPeriod: Long
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = SurfaceDark),
        border = BorderStroke(1.dp, CardBorder)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "🎮 TELEGRAM SIGNAL PREVIEW",
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Bold,
                    color = WinGoGold
                )
                if (prediction != null) {
                    val timeStr = SimpleDateFormat("hh:mm:ss a", Locale.getDefault()).format(Date(prediction.timestamp))
                    Text(
                        text = timeStr,
                        fontSize = 11.sp,
                        color = TextMuted
                    )
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            // The exact formatted message simulation
            Surface(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(10.dp),
                color = BackgroundDark,
                border = BorderStroke(1.dp, CardBorder)
            ) {
                Column(modifier = Modifier.padding(14.dp)) {
                    Text(
                        text = "🎮 WIN GO 1 MIN LIVE PREDICTION 🎮",
                        fontWeight = FontWeight.Bold,
                        fontSize = 14.sp,
                        color = WinGoGold,
                        textAlign = TextAlign.Center,
                        modifier = Modifier.fillMaxWidth()
                    )
                    Text(
                        text = "━━━━━━━━━━━━━━━━━━━━━",
                        color = TextMuted,
                        textAlign = TextAlign.Center,
                        modifier = Modifier.fillMaxWidth()
                    )

                    Spacer(modifier = Modifier.height(6.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text(text = "🆔 PERIOD:", fontWeight = FontWeight.Bold, fontSize = 13.sp, color = TextPrimary)
                        Text(
                            text = (prediction?.period ?: fallbackPeriod).toString(),
                            fontFamily = FontFamily.Monospace,
                            fontWeight = FontWeight.Bold,
                            fontSize = 13.sp,
                            color = TextPrimary
                        )
                    }

                    Spacer(modifier = Modifier.height(4.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(text = "🎯 BET ON:", fontWeight = FontWeight.Bold, fontSize = 13.sp, color = TextPrimary)
                        Text(
                            text = prediction?.betOn ?: "🟢 GREEN",
                            fontWeight = FontWeight.Bold,
                            fontSize = 14.sp,
                            color = when {
                                prediction?.betOn?.contains("RED") == true && prediction.betOn.contains("VIOLET") -> WinGoViolet
                                prediction?.betOn?.contains("GREEN") == true && prediction.betOn.contains("VIOLET") -> WinGoViolet
                                prediction?.betOn?.contains("GREEN") == true -> WinGoGreen
                                prediction?.betOn?.contains("RED") == true -> WinGoRed
                                else -> WinGoGreen
                            }
                        )
                    }

                    Spacer(modifier = Modifier.height(4.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(text = "🔮 LUCKY NUMBER:", fontWeight = FontWeight.Bold, fontSize = 13.sp, color = TextPrimary)
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Surface(
                                shape = RoundedCornerShape(4.dp),
                                color = SurfaceHighlight,
                                border = BorderStroke(1.dp, CardBorder)
                            ) {
                                Text(
                                    text = "  ${prediction?.luckyNumber ?: 7}  ",
                                    fontFamily = FontFamily.Monospace,
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 13.sp,
                                    color = WinGoGold,
                                    modifier = Modifier.padding(horizontal = 4.dp, vertical = 2.dp)
                                )
                            }
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(
                                text = "| ${prediction?.size ?: "BIG"}",
                                fontWeight = FontWeight.Bold,
                                fontSize = 13.sp,
                                color = TextPrimary
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(4.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text(text = "📊 PLAN:", fontWeight = FontWeight.Bold, fontSize = 13.sp, color = TextPrimary)
                        Surface(
                            shape = RoundedCornerShape(4.dp),
                            color = WinGoVioletContainer
                        ) {
                            Text(
                                text = prediction?.plan ?: "x1",
                                fontWeight = FontWeight.Bold,
                                fontSize = 12.sp,
                                color = Color(0xFFE0AAFF),
                                modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(4.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text(text = "⏰ TIME:", fontWeight = FontWeight.Bold, fontSize = 13.sp, color = TextPrimary)
                        Text(text = "1 MINUTE LIVE", fontSize = 13.sp, color = TextSecondary)
                    }

                    Text(
                        text = "━━━━━━━━━━━━━━━━━━━━━",
                        color = TextMuted,
                        textAlign = TextAlign.Center,
                        modifier = Modifier.fillMaxWidth()
                    )

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text(text = "📢 OFFICIAL:", fontWeight = FontWeight.Bold, fontSize = 13.sp, color = TextPrimary)
                        Text(text = officialTag, fontWeight = FontWeight.Bold, fontSize = 13.sp, color = WinGoCyan)
                    }
                }
            }
        }
    }
}

@Composable
fun AdminButtonsGrid(
    isRunning: Boolean,
    isBusy: Boolean,
    onStartClick: () -> Unit,
    onStopClick: () -> Unit,
    onSendNowClick: () -> Unit,
    onWinClick: () -> Unit,
    onLossClick: () -> Unit,
    onSetPeriodClick: () -> Unit
) {
    Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
        Text(
            text = "ADMIN CONTROLS",
            fontSize = 11.sp,
            fontWeight = FontWeight.Bold,
            color = TextMuted
        )

        // Row 1: Start / Stop Signal
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            Button(
                onClick = onStartClick,
                enabled = !isRunning && !isBusy,
                modifier = Modifier
                    .weight(1f)
                    .height(48.dp)
                    .testTag("btn_start_signal"),
                shape = RoundedCornerShape(10.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = WinGoGreen,
                    contentColor = Color(0xFF00381B),
                    disabledContainerColor = SurfaceVariantDark,
                    disabledContentColor = TextMuted
                )
            ) {
                Icon(Icons.Default.PlayArrow, contentDescription = null, modifier = Modifier.size(18.dp))
                Spacer(modifier = Modifier.width(6.dp))
                Text("🚀 Start Signal", fontWeight = FontWeight.Bold)
            }

            Button(
                onClick = onStopClick,
                enabled = isRunning && !isBusy,
                modifier = Modifier
                    .weight(1f)
                    .height(48.dp)
                    .testTag("btn_stop_signal"),
                shape = RoundedCornerShape(10.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = WinGoRed,
                    contentColor = Color.White,
                    disabledContainerColor = SurfaceVariantDark,
                    disabledContentColor = TextMuted
                )
            ) {
                Icon(Icons.Default.Stop, contentDescription = null, modifier = Modifier.size(18.dp))
                Spacer(modifier = Modifier.width(6.dp))
                Text("🛑 Stop Signal", fontWeight = FontWeight.Bold)
            }
        }

        // Row 2: Win / Loss
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            Button(
                onClick = onWinClick,
                enabled = !isBusy,
                modifier = Modifier
                    .weight(1f)
                    .height(48.dp)
                    .testTag("btn_win"),
                shape = RoundedCornerShape(10.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = Color(0xFF00C853),
                    contentColor = Color.White
                )
            ) {
                Icon(Icons.Default.CheckCircle, contentDescription = null, modifier = Modifier.size(18.dp))
                Spacer(modifier = Modifier.width(6.dp))
                Text("✅ Win", fontWeight = FontWeight.Bold)
            }

            Button(
                onClick = onLossClick,
                enabled = !isBusy,
                modifier = Modifier
                    .weight(1f)
                    .height(48.dp)
                    .testTag("btn_loss"),
                shape = RoundedCornerShape(10.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = Color(0xFFE53935),
                    contentColor = Color.White
                )
            ) {
                Icon(Icons.Default.Clear, contentDescription = null, modifier = Modifier.size(18.dp))
                Spacer(modifier = Modifier.width(6.dp))
                Text("❌ Loss", fontWeight = FontWeight.Bold)
            }
        }

        // Row 3: Single Immediate Signal & Period setting
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            OutlinedButton(
                onClick = onSendNowClick,
                enabled = !isBusy,
                modifier = Modifier
                    .weight(1f)
                    .height(44.dp)
                    .testTag("btn_send_now"),
                shape = RoundedCornerShape(10.dp),
                border = BorderStroke(1.dp, WinGoCyan.copy(alpha = 0.7f)),
                colors = ButtonDefaults.outlinedButtonColors(contentColor = WinGoCyan)
            ) {
                if (isBusy) {
                    CircularProgressIndicator(
                        modifier = Modifier.size(16.dp),
                        strokeWidth = 2.dp,
                        color = WinGoCyan
                    )
                } else {
                    Icon(Icons.Default.Send, contentDescription = null, modifier = Modifier.size(16.dp))
                    Spacer(modifier = Modifier.width(6.dp))
                    Text("⚡ Send Signal Now", fontWeight = FontWeight.SemiBold, fontSize = 12.sp)
                }
            }

            OutlinedButton(
                onClick = onSetPeriodClick,
                modifier = Modifier
                    .weight(1f)
                    .height(44.dp),
                shape = RoundedCornerShape(10.dp),
                border = BorderStroke(1.dp, CardBorder),
                colors = ButtonDefaults.outlinedButtonColors(contentColor = TextSecondary)
            ) {
                Icon(Icons.Default.Edit, contentDescription = null, modifier = Modifier.size(16.dp))
                Spacer(modifier = Modifier.width(6.dp))
                Text("✏️ Set Period", fontWeight = FontWeight.SemiBold, fontSize = 12.sp)
            }
        }
    }
}

@Composable
fun StatsOverviewCard(stats: com.example.ui.viewmodel.UiStats) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(14.dp),
        colors = CardDefaults.cardColors(containerColor = SurfaceDark),
        border = BorderStroke(1.dp, CardBorder)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 12.dp, horizontal = 8.dp),
            horizontalArrangement = Arrangement.SpaceAround
        ) {
            StatItem(label = "TOTAL", value = stats.totalSignals.toString(), color = TextPrimary)
            StatItem(label = "WINS", value = stats.wins.toString(), color = WinGoGreen)
            StatItem(label = "LOSSES", value = stats.losses.toString(), color = WinGoRed)
            StatItem(
                label = "WIN RATE",
                value = if (stats.wins + stats.losses > 0) "${stats.winRate.toInt()}%" else "--",
                color = WinGoGold
            )
        }
    }
}

@Composable
fun StatItem(label: String, value: String, color: Color) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Text(text = label, fontSize = 10.sp, fontWeight = FontWeight.Bold, color = TextMuted)
        Spacer(modifier = Modifier.height(2.dp))
        Text(text = value, fontSize = 16.sp, fontWeight = FontWeight.ExtraBold, color = color)
    }
}

@Composable
fun HistoryItemCard(
    prediction: Prediction,
    onMarkStatus: (PredictionStatus) -> Unit
) {
    val timeFormat = remember { SimpleDateFormat("hh:mm:ss a", Locale.getDefault()) }
    val formattedTime = remember(prediction.timestamp) { timeFormat.format(Date(prediction.timestamp)) }

    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = SurfaceDark),
        border = BorderStroke(1.dp, CardBorder)
    ) {
        Column(modifier = Modifier.padding(12.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "ID: ${prediction.period}",
                    fontFamily = FontFamily.Monospace,
                    fontWeight = FontWeight.Bold,
                    fontSize = 13.sp,
                    color = TextPrimary
                )

                Row(verticalAlignment = Alignment.CenterVertically) {
                    when (prediction.status) {
                        PredictionStatus.WIN -> {
                            Surface(
                                shape = RoundedCornerShape(4.dp),
                                color = WinGoGreenContainer
                            ) {
                                Text(
                                    text = "WIN ✅",
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = WinGoGreen,
                                    modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                                )
                            }
                        }
                        PredictionStatus.LOSS -> {
                            Surface(
                                shape = RoundedCornerShape(4.dp),
                                color = WinGoRedContainer
                            ) {
                                Text(
                                    text = "LOSS ❌",
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = WinGoRed,
                                    modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                                )
                            }
                        }
                        PredictionStatus.PENDING -> {
                            Surface(
                                shape = RoundedCornerShape(4.dp),
                                color = SurfaceHighlight
                            ) {
                                Text(
                                    text = "PENDING",
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Medium,
                                    color = TextSecondary,
                                    modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                                )
                            }
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    // Lucky number badge
                    Surface(
                        shape = CircleShape,
                        color = SurfaceHighlight,
                        border = BorderStroke(1.dp, CardBorder),
                        modifier = Modifier.size(28.dp)
                    ) {
                        Box(contentAlignment = Alignment.Center) {
                            Text(
                                text = prediction.luckyNumber.toString(),
                                fontWeight = FontWeight.Bold,
                                fontSize = 13.sp,
                                color = WinGoGold
                            )
                        }
                    }

                    Spacer(modifier = Modifier.width(8.dp))

                    Text(
                        text = prediction.betOn,
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold
                    )

                    Spacer(modifier = Modifier.width(8.dp))

                    Surface(
                        shape = RoundedCornerShape(4.dp),
                        color = SurfaceHighlight
                    ) {
                        Text(
                            text = prediction.size,
                            fontSize = 10.sp,
                            fontWeight = FontWeight.Bold,
                            color = TextSecondary,
                            modifier = Modifier.padding(horizontal = 4.dp, vertical = 2.dp)
                        )
                    }

                    Spacer(modifier = Modifier.width(6.dp))

                    Surface(
                        shape = RoundedCornerShape(4.dp),
                        color = WinGoVioletContainer
                    ) {
                        Text(
                            text = prediction.plan,
                            fontSize = 10.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color(0xFFE0AAFF),
                            modifier = Modifier.padding(horizontal = 4.dp, vertical = 2.dp)
                        )
                    }
                }

                Text(
                    text = formattedTime,
                    fontSize = 11.sp,
                    color = TextMuted
                )
            }

            if (prediction.status == PredictionStatus.PENDING) {
                Spacer(modifier = Modifier.height(8.dp))
                HorizontalDivider(color = CardBorder.copy(alpha = 0.5f))
                Spacer(modifier = Modifier.height(6.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.End,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "Mark result:",
                        fontSize = 11.sp,
                        color = TextMuted
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    TextButton(
                        onClick = { onMarkStatus(PredictionStatus.WIN) },
                        contentPadding = PaddingValues(horizontal = 8.dp, vertical = 2.dp)
                    ) {
                        Text("Win ✅", fontSize = 12.sp, color = WinGoGreen, fontWeight = FontWeight.Bold)
                    }
                    TextButton(
                        onClick = { onMarkStatus(PredictionStatus.LOSS) },
                        contentPadding = PaddingValues(horizontal = 8.dp, vertical = 2.dp)
                    ) {
                        Text("Loss ❌", fontSize = 12.sp, color = WinGoRed, fontWeight = FontWeight.Bold)
                    }
                }
            }
        }
    }
}

@Composable
fun EmptyHistoryCard() {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(14.dp),
        colors = CardDefaults.cardColors(containerColor = SurfaceDark),
        border = BorderStroke(1.dp, CardBorder)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(32.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Icon(
                imageVector = Icons.Default.Send,
                contentDescription = null,
                modifier = Modifier.size(36.dp),
                tint = TextMuted
            )
            Spacer(modifier = Modifier.height(10.dp))
            Text(
                text = "No signals broadcasted yet",
                fontSize = 14.sp,
                fontWeight = FontWeight.Medium,
                color = TextSecondary
            )
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = "Click '🚀 Start Signal' or '⚡ Send Signal Now' to broadcast",
                fontSize = 12.sp,
                color = TextMuted,
                textAlign = TextAlign.Center
            )
        }
    }
}

@Composable
fun SetPeriodDialog(
    currentPeriod: Long,
    onDismiss: () -> Unit,
    onConfirm: (Long) -> Unit
) {
    var textValue by remember { mutableStateOf(currentPeriod.toString()) }
    var isError by remember { mutableStateOf(false) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Text(
                text = "Set Current Period",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = TextPrimary
            )
        },
        text = {
            Column {
                Text(
                    text = "Enter the current 17-digit period number (e.g. 20260913100010128):",
                    fontSize = 13.sp,
                    color = TextSecondary
                )
                Spacer(modifier = Modifier.height(12.dp))
                OutlinedTextField(
                    value = textValue,
                    onValueChange = {
                        textValue = it.filter { ch -> ch.isDigit() }
                        isError = false
                    },
                    isError = isError,
                    singleLine = true,
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = WinGoCyan,
                        unfocusedBorderColor = CardBorder,
                        focusedTextColor = TextPrimary,
                        unfocusedTextColor = TextPrimary
                    ),
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("input_period")
                )
                if (isError) {
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = "Please enter a valid numeric period number",
                        fontSize = 11.sp,
                        color = WinGoRed
                    )
                }

                Spacer(modifier = Modifier.height(10.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    val quickAdd: (Long) -> Unit = { delta ->
                        val cur = textValue.toLongOrNull() ?: currentPeriod
                        textValue = (cur + delta).toString()
                    }
                    OutlinedButton(
                        onClick = { quickAdd(-1) },
                        contentPadding = PaddingValues(horizontal = 8.dp, vertical = 2.dp)
                    ) {
                        Text("-1", fontSize = 11.sp)
                    }
                    OutlinedButton(
                        onClick = { quickAdd(1) },
                        contentPadding = PaddingValues(horizontal = 8.dp, vertical = 2.dp)
                    ) {
                        Text("+1", fontSize = 11.sp)
                    }
                    OutlinedButton(
                        onClick = { quickAdd(10) },
                        contentPadding = PaddingValues(horizontal = 8.dp, vertical = 2.dp)
                    ) {
                        Text("+10", fontSize = 11.sp)
                    }
                }
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    val parsed = textValue.toLongOrNull()
                    if (parsed != null && parsed > 0) {
                        onConfirm(parsed)
                    } else {
                        isError = true
                    }
                },
                colors = ButtonDefaults.buttonColors(containerColor = WinGoCyan)
            ) {
                Text("Save", color = Color(0xFF003822), fontWeight = FontWeight.Bold)
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancel", color = TextSecondary)
            }
        },
        containerColor = SurfaceDark
    )
}

@Composable
fun BotSettingsDialog(
    currentBotToken: String,
    currentChatId: String,
    currentOfficialTag: String,
    botConnectionState: BotConnectionState,
    onTestConnection: () -> Unit,
    onDismiss: () -> Unit,
    onSave: (token: String, chatId: String, officialTag: String) -> Unit
) {
    var token by remember { mutableStateOf(currentBotToken) }
    var chat by remember { mutableStateOf(currentChatId) }
    var tag by remember { mutableStateOf(currentOfficialTag) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Text(
                text = "Telegram Bot Settings",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = TextPrimary
            )
        },
        text = {
            Column(modifier = Modifier.fillMaxWidth()) {
                Text(
                    text = "Configure your Telegram bot token and target channel/chat ID:",
                    fontSize = 12.sp,
                    color = TextSecondary
                )
                Spacer(modifier = Modifier.height(12.dp))

                OutlinedTextField(
                    value = token,
                    onValueChange = { token = it },
                    label = { Text("Bot Token") },
                    singleLine = true,
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = WinGoCyan,
                        unfocusedBorderColor = CardBorder,
                        focusedTextColor = TextPrimary,
                        unfocusedTextColor = TextPrimary
                    ),
                    modifier = Modifier.fillMaxWidth()
                )

                Spacer(modifier = Modifier.height(8.dp))

                OutlinedTextField(
                    value = chat,
                    onValueChange = { chat = it },
                    label = { Text("Channel / Chat ID") },
                    singleLine = true,
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = WinGoCyan,
                        unfocusedBorderColor = CardBorder,
                        focusedTextColor = TextPrimary,
                        unfocusedTextColor = TextPrimary
                    ),
                    modifier = Modifier.fillMaxWidth()
                )

                Spacer(modifier = Modifier.height(8.dp))

                OutlinedTextField(
                    value = tag,
                    onValueChange = { tag = it },
                    label = { Text("Official Channel Tag") },
                    singleLine = true,
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = WinGoCyan,
                        unfocusedBorderColor = CardBorder,
                        focusedTextColor = TextPrimary,
                        unfocusedTextColor = TextPrimary
                    ),
                    modifier = Modifier.fillMaxWidth()
                )

                Spacer(modifier = Modifier.height(12.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    OutlinedButton(
                        onClick = onTestConnection,
                        shape = RoundedCornerShape(8.dp),
                        contentPadding = PaddingValues(horizontal = 10.dp, vertical = 4.dp)
                    ) {
                        Icon(Icons.Default.Refresh, contentDescription = null, modifier = Modifier.size(14.dp))
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("Test Connection", fontSize = 11.sp)
                    }

                    when (botConnectionState) {
                        is BotConnectionState.Checking -> {
                            CircularProgressIndicator(modifier = Modifier.size(16.dp), strokeWidth = 2.dp)
                        }
                        is BotConnectionState.Connected -> {
                            Text("Connected ✅", fontSize = 11.sp, color = WinGoGreen, fontWeight = FontWeight.Bold)
                        }
                        is BotConnectionState.Error -> {
                            Text("Failed ❌", fontSize = 11.sp, color = WinGoRed, fontWeight = FontWeight.Bold)
                        }
                        is BotConnectionState.Idle -> {}
                    }
                }
            }
        },
        confirmButton = {
            Button(
                onClick = { onSave(token, chat, tag) },
                colors = ButtonDefaults.buttonColors(containerColor = WinGoGreen),
                modifier = Modifier.testTag("dialog_save_settings")
            ) {
                Text("Save Settings", color = Color(0xFF00381B), fontWeight = FontWeight.Bold)
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancel", color = TextSecondary)
            }
        },
        containerColor = SurfaceDark
    )
}
