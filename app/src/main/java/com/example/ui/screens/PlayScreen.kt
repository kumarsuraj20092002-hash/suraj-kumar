package com.example.ui.screens

import android.app.Activity
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.scaleIn
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.focusable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.VolumeOff
import androidx.compose.material.icons.automirrored.filled.VolumeUp
import androidx.compose.material.icons.filled.Bolt
import androidx.compose.material.icons.filled.Flag
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.MonetizationOn
import androidx.compose.material.icons.filled.Pause
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.filled.Tune
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.runtime.withFrameNanos
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.input.key.Key
import androidx.compose.ui.input.key.KeyEventType
import androidx.compose.ui.input.key.key
import androidx.compose.ui.input.key.onKeyEvent
import androidx.compose.ui.input.key.type
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.R
import com.example.game.HapticCue
import com.example.ui.AppDestination
import com.example.ui.DesignSubTab
import com.example.ui.GameUiState
import com.example.ui.GameViewModel
import com.example.ui.components.ArcadeControlDeck
import com.example.ui.components.PixelGameCanvas
import com.example.ui.theme.ArcadeBg
import com.example.ui.theme.ArcadeBorder
import com.example.ui.theme.ArcadeSurface
import com.example.ui.theme.ArcadeSurfaceElevated
import com.example.ui.theme.HyperCoral
import com.example.ui.theme.MintEmerald
import com.example.ui.theme.NeonCyan
import com.example.ui.theme.NeonViolet
import com.example.ui.theme.TextPrimary
import com.example.ui.theme.TextSecondary
import com.example.ui.theme.VoltAmber
import java.util.Locale

@Composable
fun PlayScreen(
    uiState: GameUiState,
    viewModel: GameViewModel,
    modifier: Modifier = Modifier
) {
    val haptics = LocalHapticFeedback.current
    var keyLeft by remember { mutableStateOf(false) }
    var keyRight by remember { mutableStateOf(false) }
    var keyJump by remember { mutableStateOf(false) }
    var keyDash by remember { mutableStateOf(false) }
    val focusRequester = remember { FocusRequester() }

    LaunchedEffect(Unit) {
        runCatching { focusRequester.requestFocus() }
    }

    DisposableEffect(Unit) {
        onDispose {
            viewModel.clearLiveInput()
        }
    }

    LaunchedEffect(uiState.destination, uiState.isPaused, uiState.activeLevelNumber) {
        var lastNanos = 0L
        while (true) {
            withFrameNanos { nanos ->
                if (lastNanos == 0L) lastNanos = nanos
                val dt = ((nanos - lastNanos) / 1_000_000_000f).coerceIn(0.001f, 0.032f)
                lastNanos = nanos

                val moveDir = when {
                    keyLeft && !keyRight -> -1f
                    keyRight && !keyLeft -> 1f
                    else -> 0f
                }

                val cue = viewModel.stepSimulation(dt, moveDir, keyJump, keyDash)
                when (cue) {
                    HapticCue.JUMP, HapticCue.SHARD -> haptics.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                    HapticCue.DASH, HapticCue.STOMP, HapticCue.SPRING, HapticCue.HIT, HapticCue.CLEAR ->
                        haptics.performHapticFeedback(HapticFeedbackType.LongPress)
                    null -> Unit
                }
            }
        }
    }

    val context = LocalContext.current
    val activity = context as? Activity
    val stage = uiState.activeStage
    val snap = uiState.snapshot

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(ArcadeBg)
            .focusRequester(focusRequester)
            .focusable()
            .onKeyEvent { event ->
                val down = event.type == KeyEventType.KeyDown
                val up = event.type == KeyEventType.KeyUp
                if (!down && !up) return@onKeyEvent false
                when (event.key) {
                    Key.DirectionLeft, Key.A -> { keyLeft = down; true }
                    Key.DirectionRight, Key.D -> { keyRight = down; true }
                    Key.Spacebar, Key.W, Key.DirectionUp -> { keyJump = down; true }
                    Key.ShiftLeft, Key.ShiftRight, Key.J -> { keyDash = down; true }
                    Key.R -> { if (down) viewModel.restartActiveStage(); true }
                    else -> false
                }
            }
    ) {
        // Minimal Clean Top HUD Bar (No extra menu clutter during active play)
        CompactHudBar(
            levelNumber = uiState.activeLevelNumber,
            totalScore = uiState.totalLiveScore,
            bestScore = uiState.highScore,
            coins = uiState.coins,
            canDash = snap.canDash,
            accentColor = uiState.equippedCharacter.primaryColor,
            onPause = { viewModel.togglePause() }
        )

        // Fullscreen Adaptive Viewport (Portrait & Landscape ready)
        Box(
            modifier = Modifier
                .weight(1f)
                .fillMaxWidth()
        ) {
            PixelGameCanvas(
                engine = viewModel.engine,
                frameTick = viewModel.frameTick,
                showHitboxes = uiState.physicsConfig.showHitboxes,
                showPixelGrid = uiState.physicsConfig.showPixelGrid,
                modifier = Modifier.fillMaxSize()
            )

            // Pause Overlay (options shown only when paused)
            androidx.compose.animation.AnimatedVisibility(
                visible = uiState.isPaused && !snap.stageCleared && !snap.isPlayerOut,
                enter = fadeIn() + scaleIn(initialScale = 0.94f),
                exit = fadeOut(),
                modifier = Modifier.align(Alignment.Center)
            ) {
                PauseOverlayCard(
                    stageName = "LV ${stage.code} • ${stage.name}",
                    soundEnabled = uiState.physicsConfig.soundEnabled,
                    onToggleSound = { viewModel.toggleSound() },
                    onResume = { viewModel.setPaused(false) },
                    onRestart = { viewModel.restartActiveStage() },
                    onGoHomeShop = { viewModel.navigateTo(AppDestination.HOME) },
                    onOpenLevels = { viewModel.navigateTo(AppDestination.STAGES) },
                    onTunePhysics = {
                        viewModel.selectDesignSubTab(DesignSubTab.PHYSICS)
                        viewModel.navigateTo(AppDestination.DESIGN)
                    }
                )
            }

            // Player Out Overlay: Pure "GAME END" focus panel + options cleanly below
            androidx.compose.animation.AnimatedVisibility(
                visible = snap.isPlayerOut && !snap.stageCleared,
                enter = fadeIn() + scaleIn(initialScale = 0.94f),
                exit = fadeOut(),
                modifier = Modifier.fillMaxSize()
            ) {
                PlayerOutOverlayCard(
                    levelNumber = uiState.activeLevelNumber,
                    runScore = uiState.totalLiveScore,
                    bestScore = uiState.highScore,
                    stageCoins = snap.stageCoins,
                    totalCoins = uiState.coins,
                    roadName = uiState.equippedRoad.name,
                    soundEnabled = uiState.physicsConfig.soundEnabled,
                    onToggleSound = { viewModel.toggleSound() },
                    onContinueCheckpoint = { viewModel.continueFromOutCheckpoint() },
                    onRetry = { viewModel.restartActiveStage() },
                    onGoHomeShop = { viewModel.navigateTo(AppDestination.HOME) },
                    onOpenLevels = { viewModel.navigateTo(AppDestination.STAGES) }
                )
            }

            // Stage Clear Overlay (ALL post-clear options revealed cleanly!)
            androidx.compose.animation.AnimatedVisibility(
                visible = snap.stageCleared,
                enter = fadeIn() + scaleIn(initialScale = 0.92f),
                exit = fadeOut(),
                modifier = Modifier.align(Alignment.Center)
            ) {
                StageClearOverlayCard(
                    levelNumber = uiState.activeLevelNumber,
                    stageName = "LV ${stage.code} • ${stage.name}",
                    elapsedMs = snap.elapsedMs,
                    parTimeMs = stage.parTimeMs,
                    stageScore = snap.stageScore,
                    totalRunScore = uiState.totalLiveScore,
                    bestScore = uiState.highScore,
                    stageCoins = snap.stageCoins,
                    shardsCollected = snap.shardsCollected,
                    totalShards = snap.totalShards,
                    maxCombo = snap.maxCombo,
                    deaths = snap.deaths,
                    onNextLevel = { viewModel.nextLevel(activity) },
                    onReplay = { viewModel.restartActiveStage() },
                    onGoHomeShop = { viewModel.navigateTo(AppDestination.HOME) },
                    onOpenLevels = { viewModel.navigateTo(AppDestination.STAGES) }
                )
            }
        }

        // Pure Game Control Deck Only (zero bottom navigation options)
        ArcadeControlDeck(
            canDash = snap.canDash,
            liveInput = viewModel.liveInput
        )
    }
}

@Composable
private fun CompactHudBar(
    levelNumber: Int,
    totalScore: Int,
    bestScore: Int,
    coins: Int,
    canDash: Boolean,
    accentColor: Color,
    onPause: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .background(ArcadeSurface)
            .statusBarsPadding()
            .padding(horizontal = 12.dp, vertical = 6.dp),
        horizontalArrangement = Arrangement.spacedBy(6.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        HudChip("LV $levelNumber", accentColor, Modifier.weight(0.75f))
        HudChip("SCORE $totalScore", NeonCyan, Modifier.weight(1.1f))
        HudChip("BEST $bestScore", TextPrimary, Modifier.weight(1f))
        HudChip("${coins}C", VoltAmber, Modifier.weight(0.75f))

        Box(
            modifier = Modifier
                .size(8.dp)
                .clip(RoundedCornerShape(4.dp))
                .background(if (canDash) MintEmerald else HyperCoral)
        )

        IconButton(
            onClick = onPause,
            modifier = Modifier
                .size(48.dp)
                .clip(RoundedCornerShape(10.dp))
                .background(ArcadeSurfaceElevated)
                .testTag("hud_pause_button")
        ) {
            Icon(
                imageVector = Icons.Filled.Pause,
                contentDescription = "Pause Game",
                tint = TextPrimary,
                modifier = Modifier.size(20.dp)
            )
        }
    }
}

@Composable
private fun HudChip(
    label: String,
    accent: Color,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
            .clip(RoundedCornerShape(8.dp))
            .background(ArcadeSurfaceElevated)
            .border(1.dp, accent.copy(alpha = 0.35f), RoundedCornerShape(8.dp))
            .padding(horizontal = 6.dp, vertical = 8.dp),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = label,
            style = MaterialTheme.typography.labelSmall,
            color = accent,
            maxLines = 1
        )
    }
}

@Composable
private fun PlayerOutOverlayCard(
    levelNumber: Int,
    runScore: Int,
    bestScore: Int,
    stageCoins: Int,
    totalCoins: Int,
    roadName: String,
    soundEnabled: Boolean,
    onToggleSound: () -> Unit,
    onContinueCheckpoint: () -> Unit,
    onRetry: () -> Unit,
    onGoHomeShop: () -> Unit,
    onOpenLevels: () -> Unit
) {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(ArcadeBg.copy(alpha = 0.88f))
            .padding(horizontal = 20.dp),
        contentAlignment = Alignment.Center
    ) {
        Column(
            modifier = Modifier
                .widthIn(max = 380.dp)
                .fillMaxWidth(),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(18.dp)
        ) {
            // Pure GAME END Focal Card (No clutter inside)
            Surface(
                color = ArcadeSurface,
                shape = RoundedCornerShape(22.dp),
                tonalElevation = 10.dp,
                modifier = Modifier
                    .fillMaxWidth()
                    .border(2.dp, HyperCoral.copy(alpha = 0.85f), RoundedCornerShape(22.dp))
            ) {
                Column(
                    modifier = Modifier.padding(horizontal = 20.dp, vertical = 22.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    Icon(
                        imageVector = Icons.Filled.Warning,
                        contentDescription = null,
                        tint = HyperCoral,
                        modifier = Modifier.size(34.dp)
                    )
                    Text(
                        text = "GAME END",
                        style = MaterialTheme.typography.displaySmall.copy(fontWeight = FontWeight.ExtraBold),
                        color = HyperCoral
                    )
                    Text(
                        text = "LV $levelNumber • $roadName",
                        style = MaterialTheme.typography.labelMedium,
                        color = TextSecondary
                    )

                    Spacer(Modifier.height(2.dp))

                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(14.dp))
                            .background(ArcadeSurfaceElevated)
                            .padding(horizontal = 14.dp, vertical = 12.dp),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        StatColumn("SCORE", "$runScore", NeonCyan)
                        StatColumn("BEST", "$bestScore", MintEmerald)
                        StatColumn("EARNED", "+${stageCoins}C", VoltAmber)
                        StatColumn("WALLET", "${totalCoins}C", VoltAmber)
                    }
                }
            }

            // Options cleanly separated below the GAME END panel
            Column(
                modifier = Modifier.fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    Button(
                        onClick = onRetry,
                        colors = ButtonDefaults.buttonColors(containerColor = NeonCyan, contentColor = ArcadeBg),
                        shape = RoundedCornerShape(14.dp),
                        modifier = Modifier
                            .weight(1f)
                            .height(50.dp)
                            .testTag("out_retry_button")
                    ) {
                        Icon(Icons.Filled.Refresh, contentDescription = null, modifier = Modifier.size(18.dp))
                        Spacer(Modifier.width(6.dp))
                        Text("RETRY", style = MaterialTheme.typography.labelLarge)
                    }

                    Button(
                        onClick = onContinueCheckpoint,
                        colors = ButtonDefaults.buttonColors(containerColor = MintEmerald, contentColor = ArcadeBg),
                        shape = RoundedCornerShape(14.dp),
                        modifier = Modifier
                            .weight(1f)
                            .height(50.dp)
                            .testTag("out_continue_button")
                    ) {
                        Icon(Icons.Filled.PlayArrow, contentDescription = null, modifier = Modifier.size(20.dp))
                        Spacer(Modifier.width(6.dp))
                        Text("CONTINUE", style = MaterialTheme.typography.labelLarge)
                    }
                }

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    OutlinedButton(
                        onClick = onGoHomeShop,
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier
                            .weight(1.25f)
                            .height(48.dp)
                            .testTag("out_home_shop_button")
                    ) {
                        Icon(Icons.Filled.Home, contentDescription = null, modifier = Modifier.size(16.dp))
                        Spacer(Modifier.width(4.dp))
                        Text("SHOP", style = MaterialTheme.typography.labelSmall)
                    }
                    OutlinedButton(
                        onClick = onOpenLevels,
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier
                            .weight(1f)
                            .height(48.dp)
                            .testTag("out_levels_button")
                    ) {
                        Icon(Icons.Filled.Flag, contentDescription = null, modifier = Modifier.size(16.dp))
                        Spacer(Modifier.width(4.dp))
                        Text("LEVELS", style = MaterialTheme.typography.labelSmall)
                    }
                    OutlinedButton(
                        onClick = onToggleSound,
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier
                            .weight(0.95f)
                            .height(48.dp)
                            .testTag("out_sound_toggle")
                    ) {
                        Icon(
                            imageVector = if (soundEnabled) Icons.AutoMirrored.Filled.VolumeUp else Icons.AutoMirrored.Filled.VolumeOff,
                            contentDescription = if (soundEnabled) "Mute SFX" else "Unmute SFX",
                            tint = if (soundEnabled) NeonCyan else TextSecondary,
                            modifier = Modifier.size(16.dp)
                        )
                        Spacer(Modifier.width(4.dp))
                        Text(
                            text = if (soundEnabled) "SFX" else "MUTE",
                            style = MaterialTheme.typography.labelSmall,
                            color = if (soundEnabled) NeonCyan else TextSecondary
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun PauseOverlayCard(
    stageName: String,
    soundEnabled: Boolean,
    onToggleSound: () -> Unit,
    onResume: () -> Unit,
    onRestart: () -> Unit,
    onGoHomeShop: () -> Unit,
    onOpenLevels: () -> Unit,
    onTunePhysics: () -> Unit
) {
    Surface(
        color = ArcadeSurface.copy(alpha = 0.96f),
        shape = RoundedCornerShape(18.dp),
        tonalElevation = 8.dp,
        modifier = Modifier
            .padding(horizontal = 24.dp)
            .widthIn(max = 360.dp)
            .fillMaxWidth()
            .border(1.5.dp, NeonCyan.copy(alpha = 0.5f), RoundedCornerShape(18.dp))
    ) {
        Column(
            modifier = Modifier.padding(20.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            Text(
                text = stringResource(R.string.pause_title),
                style = MaterialTheme.typography.headlineSmall,
                color = NeonCyan
            )
            Text(
                text = stageName,
                style = MaterialTheme.typography.bodyMedium,
                color = TextSecondary
            )
            Spacer(modifier = Modifier.height(4.dp))
            Button(
                onClick = onResume,
                colors = ButtonDefaults.buttonColors(containerColor = NeonCyan, contentColor = ArcadeBg),
                modifier = Modifier
                    .fillMaxWidth()
                    .height(48.dp)
                    .testTag("pause_resume_button")
            ) {
                Icon(Icons.Filled.PlayArrow, contentDescription = null, modifier = Modifier.size(20.dp))
                Spacer(Modifier.width(8.dp))
                Text(stringResource(R.string.pause_resume), style = MaterialTheme.typography.labelLarge)
            }
            OutlinedButton(
                onClick = onRestart,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(48.dp)
                    .testTag("pause_restart_button")
            ) {
                Icon(Icons.Filled.Refresh, contentDescription = null, modifier = Modifier.size(18.dp))
                Spacer(Modifier.width(8.dp))
                Text(stringResource(R.string.pause_restart), style = MaterialTheme.typography.labelLarge)
            }
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                OutlinedButton(
                    onClick = onGoHomeShop,
                    modifier = Modifier
                        .weight(1.1f)
                        .height(48.dp)
                        .testTag("pause_home_button")
                ) {
                    Icon(Icons.Filled.Home, contentDescription = null, modifier = Modifier.size(16.dp))
                    Spacer(Modifier.width(4.dp))
                    Text("HOME", style = MaterialTheme.typography.labelSmall)
                }
                OutlinedButton(
                    onClick = onOpenLevels,
                    modifier = Modifier
                        .weight(1f)
                        .height(48.dp)
                        .testTag("pause_levels_button")
                ) {
                    Icon(Icons.Filled.Flag, contentDescription = null, modifier = Modifier.size(16.dp))
                    Spacer(Modifier.width(4.dp))
                    Text("LEVELS", style = MaterialTheme.typography.labelSmall)
                }
            }
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                OutlinedButton(
                    onClick = onTunePhysics,
                    modifier = Modifier
                        .weight(1f)
                        .height(48.dp)
                        .testTag("pause_tune_button")
                ) {
                    Icon(Icons.Filled.Tune, contentDescription = null, modifier = Modifier.size(16.dp))
                    Spacer(Modifier.width(4.dp))
                    Text("PHYSICS", style = MaterialTheme.typography.labelSmall)
                }
                OutlinedButton(
                    onClick = onToggleSound,
                    modifier = Modifier
                        .weight(1f)
                        .height(48.dp)
                        .testTag("pause_sound_toggle")
                ) {
                    Icon(
                        imageVector = if (soundEnabled) Icons.AutoMirrored.Filled.VolumeUp else Icons.AutoMirrored.Filled.VolumeOff,
                        contentDescription = if (soundEnabled) "Mute Sound" else "Unmute Sound",
                        tint = if (soundEnabled) NeonCyan else TextSecondary,
                        modifier = Modifier.size(16.dp)
                    )
                    Spacer(Modifier.width(4.dp))
                    Text(
                        text = if (soundEnabled) "SFX ON" else "SFX OFF",
                        style = MaterialTheme.typography.labelSmall,
                        color = if (soundEnabled) NeonCyan else TextSecondary
                    )
                }
            }
        }
    }
}

@Composable
private fun StageClearOverlayCard(
    levelNumber: Int,
    stageName: String,
    elapsedMs: Long,
    parTimeMs: Long,
    stageScore: Int,
    totalRunScore: Int,
    bestScore: Int,
    stageCoins: Int,
    shardsCollected: Int,
    totalShards: Int,
    maxCombo: Int,
    deaths: Int,
    onNextLevel: () -> Unit,
    onReplay: () -> Unit,
    onGoHomeShop: () -> Unit,
    onOpenLevels: () -> Unit
) {
    val underPar = elapsedMs <= parTimeMs
    val shardRatio = if (totalShards > 0) shardsCollected.toFloat() / totalShards else 1f
    val rank = when {
        underPar && shardRatio >= 0.75f && deaths == 0 -> "S"
        underPar || shardRatio >= 0.7f -> "A"
        else -> "B"
    }
    val rankColor = when (rank) {
        "S" -> VoltAmber
        "A" -> NeonCyan
        else -> MintEmerald
    }

    Surface(
        color = ArcadeSurface.copy(alpha = 0.97f),
        shape = RoundedCornerShape(18.dp),
        tonalElevation = 8.dp,
        modifier = Modifier
            .padding(horizontal = 20.dp)
            .widthIn(max = 380.dp)
            .fillMaxWidth()
            .border(1.5.dp, rankColor.copy(alpha = 0.7f), RoundedCornerShape(18.dp))
    ) {
        Column(
            modifier = Modifier.padding(20.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                Box(
                    modifier = Modifier
                        .size(46.dp)
                        .clip(RoundedCornerShape(10.dp))
                        .background(rankColor.copy(alpha = 0.18f))
                        .border(1.5.dp, rankColor, RoundedCornerShape(10.dp)),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = rank,
                        style = MaterialTheme.typography.displaySmall,
                        color = rankColor
                    )
                }
                Column {
                    Text(
                        text = "LEVEL $levelNumber CLEAR!",
                        style = MaterialTheme.typography.headlineSmall,
                        color = rankColor
                    )
                    Text(
                        text = stageName,
                        style = MaterialTheme.typography.bodyMedium,
                        color = TextSecondary
                    )
                }
            }

            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(12.dp))
                    .background(ArcadeSurfaceElevated)
                    .padding(12.dp),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                StatColumn("RUN", "$totalRunScore", NeonCyan)
                StatColumn("BEST", "$bestScore", MintEmerald)
                StatColumn("COINS", "+${stageCoins}C", VoltAmber)
                StatColumn("TIME", formatTime(elapsedMs), if (underPar) MintEmerald else TextPrimary)
            }

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceEvenly
            ) {
                Text("SHARDS $shardsCollected/$totalShards", style = MaterialTheme.typography.labelMedium, color = VoltAmber)
                Text("MAX FLOW x$maxCombo", style = MaterialTheme.typography.labelMedium, color = NeonViolet)
            }

            Button(
                onClick = onNextLevel,
                colors = ButtonDefaults.buttonColors(containerColor = MintEmerald, contentColor = ArcadeBg),
                modifier = Modifier
                    .fillMaxWidth()
                    .height(48.dp)
                    .testTag("clear_next_stage_button")
            ) {
                Icon(Icons.Filled.Star, contentDescription = null, modifier = Modifier.size(18.dp))
                Spacer(Modifier.width(8.dp))
                Text("NEXT LEVEL (LV ${levelNumber + 1})", style = MaterialTheme.typography.labelLarge)
            }

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                OutlinedButton(
                    onClick = onReplay,
                    modifier = Modifier
                        .weight(1f)
                        .height(48.dp)
                        .testTag("clear_replay_button")
                ) {
                    Text("REPLAY", style = MaterialTheme.typography.labelSmall)
                }
                OutlinedButton(
                    onClick = onGoHomeShop,
                    modifier = Modifier
                        .weight(1.1f)
                        .height(48.dp)
                        .testTag("clear_home_shop_button")
                ) {
                    Icon(Icons.Filled.Home, contentDescription = null, modifier = Modifier.size(15.dp))
                    Spacer(Modifier.width(4.dp))
                    Text("SHOP", style = MaterialTheme.typography.labelSmall)
                }
                OutlinedButton(
                    onClick = onOpenLevels,
                    modifier = Modifier
                        .weight(1f)
                        .height(48.dp)
                        .testTag("clear_levels_button")
                ) {
                    Text("LEVELS", style = MaterialTheme.typography.labelSmall)
                }
            }
        }
    }
}

@Composable
private fun StatColumn(label: String, value: String, valueColor: Color) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Text(text = label, style = MaterialTheme.typography.labelSmall, color = TextSecondary)
        Text(
            text = value,
            style = MaterialTheme.typography.labelLarge.copy(fontWeight = FontWeight.Bold),
            color = valueColor
        )
    }
}

fun formatTime(ms: Long): String {
    val totalHundredths = ms / 10L
    val hundredths = totalHundredths % 100L
    val totalSecs = totalHundredths / 100L
    val secs = totalSecs % 60L
    val mins = totalSecs / 60L
    return String.format(Locale.US, "%02d:%02d.%02d", mins, secs, hundredths)
}
