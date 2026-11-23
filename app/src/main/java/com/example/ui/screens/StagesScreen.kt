package com.example.ui.screens

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.AllInclusive
import androidx.compose.material.icons.filled.Architecture
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Remove
import androidx.compose.material.icons.filled.Timer
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import com.example.R
import com.example.audio.LocalSoundManager
import com.example.game.BiomeType
import com.example.game.LevelDefinitions
import com.example.game.StageDefinition
import com.example.ui.AppDestination
import com.example.ui.DesignSubTab
import com.example.ui.GameUiState
import com.example.ui.GameViewModel
import com.example.ui.theme.ArcadeBg
import com.example.ui.theme.ArcadeBorder
import com.example.ui.theme.ArcadeSurface
import com.example.ui.theme.ArcadeSurfaceElevated
import com.example.ui.theme.MintEmerald
import com.example.ui.theme.NeonCyan
import com.example.ui.theme.NeonViolet
import com.example.ui.theme.TextPrimary
import com.example.ui.theme.TextSecondary
import com.example.ui.theme.VoltAmber

@Composable
fun StagesScreen(
    uiState: GameUiState,
    viewModel: GameViewModel,
    modifier: Modifier = Modifier
) {
    val sound = LocalSoundManager.current
    var endlessStartLevel by remember(uiState.activeLevelNumber) {
        mutableIntStateOf(uiState.activeLevelNumber.coerceAtLeast(1))
    }
    var loadedLevelCount by remember(uiState.highestLevel) {
        mutableIntStateOf(maxOf(20, uiState.highestLevel + 8))
    }

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(ArcadeBg),
        contentAlignment = Alignment.TopCenter
    ) {
        LazyColumn(
            modifier = Modifier
                .widthIn(max = 600.dp)
                .fillMaxSize()
                .navigationBarsPadding(),
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            item {
                Column(
                    modifier = Modifier.statusBarsPadding(),
                    verticalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        IconButton(
                            onClick = { viewModel.navigateTo(AppDestination.HOME) },
                            modifier = Modifier
                                .size(48.dp)
                                .testTag("stages_back_button")
                        ) {
                            Icon(
                                imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                                contentDescription = "Back to Home",
                                tint = TextPrimary
                            )
                        }
                        Text(
                            text = "UNLIMITED STAGES",
                            style = MaterialTheme.typography.headlineMedium,
                            color = NeonCyan
                        )
                    }
                    Text(
                        text = "100% offline • Saved at Level ${uiState.activeLevelNumber} • Infinite procedural sectors (1 – ∞).",
                        style = MaterialTheme.typography.bodyMedium,
                        color = TextSecondary
                    )
                }
            }

            // Offline Records & Unlimited Level Launcher Card
            item {
                Surface(
                    color = ArcadeSurface,
                    shape = RoundedCornerShape(16.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .border(1.5.dp, NeonViolet.copy(alpha = 0.65f), RoundedCornerShape(16.dp))
                ) {
                    Column(
                        modifier = Modifier.padding(16.dp),
                        verticalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                Icon(
                                    imageVector = Icons.Filled.AllInclusive,
                                    contentDescription = null,
                                    tint = NeonViolet,
                                    modifier = Modifier.size(22.dp)
                                )
                                Text(
                                    text = "ENDLESS MODE (LV 1 – ∞)",
                                    style = MaterialTheme.typography.titleMedium,
                                    color = TextPrimary
                                )
                            }
                            BadgeChip(label = "RESUME LV ${uiState.activeLevelNumber}", color = MintEmerald)
                        }

                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(10.dp))
                                .background(ArcadeSurfaceElevated)
                                .padding(horizontal = 12.dp, vertical = 10.dp),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Column {
                                Text("HIGH SCORE", style = MaterialTheme.typography.labelSmall, color = TextSecondary)
                                Text("${uiState.highScore}", style = MaterialTheme.typography.titleMedium, color = VoltAmber)
                            }
                            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                Text("CURRENT LEVEL", style = MaterialTheme.typography.labelSmall, color = TextSecondary)
                                Text("LEVEL ${uiState.activeLevelNumber}", style = MaterialTheme.typography.titleMedium, color = MintEmerald)
                            }
                            Column(horizontalAlignment = Alignment.End) {
                                Text("HIGHEST LEVEL", style = MaterialTheme.typography.labelSmall, color = TextSecondary)
                                Text("LEVEL ${uiState.highestLevel}", style = MaterialTheme.typography.titleMedium, color = NeonCyan)
                            }
                        }

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(10.dp)
                        ) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                modifier = Modifier
                                    .clip(RoundedCornerShape(12.dp))
                                    .background(ArcadeSurfaceElevated)
                                    .border(1.dp, ArcadeBorder, RoundedCornerShape(12.dp))
                            ) {
                                IconButton(
                                    onClick = {
                                        if (endlessStartLevel > 1) {
                                            sound.playButtonClick()
                                            endlessStartLevel--
                                        } else {
                                            sound.playActionDenied()
                                        }
                                    },
                                    modifier = Modifier.size(48.dp)
                                ) {
                                    Icon(Icons.Filled.Remove, contentDescription = "Decrease Level", tint = TextPrimary)
                                }
                                Text(
                                    text = "LV $endlessStartLevel",
                                    style = MaterialTheme.typography.labelLarge,
                                    color = NeonCyan,
                                    modifier = Modifier.padding(horizontal = 8.dp)
                                )
                                IconButton(
                                    onClick = {
                                        sound.playButtonClick()
                                        endlessStartLevel++
                                    },
                                    modifier = Modifier.size(48.dp)
                                ) {
                                    Icon(Icons.Filled.Add, contentDescription = "Increase Level", tint = TextPrimary)
                                }
                            }

                            Button(
                                onClick = {
                                    val keepRunScore = endlessStartLevel == uiState.activeLevelNumber
                                    viewModel.launchLevel(endlessStartLevel, resetRunScore = !keepRunScore)
                                },
                                colors = ButtonDefaults.buttonColors(containerColor = NeonViolet, contentColor = TextPrimary),
                                modifier = Modifier
                                    .weight(1f)
                                    .height(48.dp)
                                    .testTag("launch_unlimited_level_button")
                            ) {
                                Icon(Icons.Filled.PlayArrow, contentDescription = null)
                                Spacer(Modifier.width(6.dp))
                                Text("PLAY LV $endlessStartLevel", style = MaterialTheme.typography.labelLarge)
                            }
                        }
                    }
                }
            }

            items(count = loadedLevelCount, key = { idx -> "stage_item_${uiState.equippedRoadId}_${idx + 1}" }) { index ->
                val levelNum = index + 1
                val stage = remember(levelNum, uiState.equippedRoadId) {
                    LevelDefinitions.getStageForLevel(levelNum, uiState.equippedRoadId)
                }
                StageCard(
                    stage = stage,
                    isActive = levelNum == uiState.activeLevelNumber,
                    bestTimeMs = uiState.bestTimesByLevel[levelNum],
                    bestShards = uiState.bestShardsByLevel[levelNum],
                    onPlay = {
                        viewModel.launchLevel(
                            levelNum,
                            resetRunScore = levelNum != uiState.activeLevelNumber
                        )
                    },
                    onInspect = {
                        viewModel.selectBlueprintStage(index.coerceIn(0, LevelDefinitions.stages.lastIndex))
                        viewModel.selectDesignSubTab(DesignSubTab.LEVELS)
                        viewModel.navigateTo(AppDestination.DESIGN)
                    }
                )
            }

            item {
                OutlinedButton(
                    onClick = {
                        sound.playButtonClick()
                        loadedLevelCount += 20
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(52.dp)
                        .testTag("load_more_levels_button")
                ) {
                    Icon(Icons.Filled.AllInclusive, contentDescription = null, tint = NeonCyan, modifier = Modifier.size(18.dp))
                    Spacer(Modifier.width(8.dp))
                    Text(
                        text = "LOAD +20 MORE LEVELS (UP TO LV ${loadedLevelCount + 20})",
                        style = MaterialTheme.typography.labelLarge,
                        color = NeonCyan
                    )
                }
            }
        }
    }
}

@Composable
private fun StageCard(
    stage: StageDefinition,
    isActive: Boolean,
    bestTimeMs: Long?,
    bestShards: Int?,
    onPlay: () -> Unit,
    onInspect: () -> Unit
) {
    val bgRes = if (stage.biome == BiomeType.SKYLINE) {
        R.drawable.img_hero_skyline
    } else {
        R.drawable.img_biome_citadel
    }
    val totalShards = stage.rows.sumOf { row -> row.count { it == 'C' } }
    val borderColor = if (isActive) NeonCyan else ArcadeBorder

    Surface(
        color = ArcadeSurface,
        shape = RoundedCornerShape(16.dp),
        modifier = Modifier
            .fillMaxWidth()
            .border(if (isActive) 1.5.dp else 1.dp, borderColor, RoundedCornerShape(16.dp))
    ) {
        Column {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(126.dp)
            ) {
                Image(
                    painter = painterResource(id = bgRes),
                    contentDescription = stage.name,
                    contentScale = ContentScale.Crop,
                    modifier = Modifier.fillMaxSize()
                )
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(
                            Brush.verticalGradient(
                                listOf(
                                    ArcadeBg.copy(alpha = 0.25f),
                                    ArcadeBg.copy(alpha = 0.92f)
                                )
                            )
                        )
                )
                Row(
                    modifier = Modifier
                        .align(Alignment.TopStart)
                        .padding(12.dp),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    BadgeChip(label = "LV ${stage.levelNumber} • ${stage.code}", color = NeonCyan)
                    BadgeChip(label = stage.difficulty, color = VoltAmber)
                    if (isActive) {
                        BadgeChip(label = "RESUME", color = MintEmerald)
                    }
                }

                Column(
                    modifier = Modifier
                        .align(Alignment.BottomStart)
                        .padding(12.dp)
                ) {
                    Text(
                        text = stage.name,
                        style = MaterialTheme.typography.headlineSmall,
                        color = TextPrimary
                    )
                    Text(
                        text = stage.subtitle,
                        style = MaterialTheme.typography.bodyMedium,
                        color = TextSecondary
                    )
                }
            }

            Column(
                modifier = Modifier.padding(14.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(10.dp))
                        .background(ArcadeSurfaceElevated)
                        .padding(horizontal = 12.dp, vertical = 8.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = Icons.Filled.Timer,
                            contentDescription = null,
                            tint = NeonCyan,
                            modifier = Modifier.size(16.dp)
                        )
                        Spacer(Modifier.width(6.dp))
                        Text(
                            text = "PAR ${formatTime(stage.parTimeMs)}",
                            style = MaterialTheme.typography.labelMedium,
                            color = TextSecondary
                        )
                    }
                    Text(
                        text = bestTimeMs?.let { "BEST ${formatTime(it)}" } ?: "UNCLAIMED",
                        style = MaterialTheme.typography.labelMedium,
                        color = if (bestTimeMs != null) MintEmerald else TextSecondary
                    )
                    Text(
                        text = "SHARDS ${bestShards ?: 0}/$totalShards",
                        style = MaterialTheme.typography.labelMedium,
                        color = VoltAmber
                    )
                }

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    Button(
                        onClick = onPlay,
                        colors = ButtonDefaults.buttonColors(containerColor = NeonCyan, contentColor = ArcadeBg),
                        modifier = Modifier
                            .weight(1.2f)
                            .height(48.dp)
                            .testTag("stage_play_${stage.code}")
                    ) {
                        Icon(Icons.Filled.PlayArrow, contentDescription = null, modifier = Modifier.size(18.dp))
                        Spacer(Modifier.width(6.dp))
                        Text("PLAY", style = MaterialTheme.typography.labelLarge)
                    }
                    OutlinedButton(
                        onClick = onInspect,
                        modifier = Modifier
                            .weight(1f)
                            .height(48.dp)
                            .testTag("stage_blueprint_${stage.code}")
                    ) {
                        Icon(Icons.Filled.Architecture, contentDescription = null, modifier = Modifier.size(18.dp))
                        Spacer(Modifier.width(6.dp))
                        Text("DESIGN", style = MaterialTheme.typography.labelLarge)
                    }
                }
            }
        }
    }
}

@Composable
private fun BadgeChip(label: String, color: androidx.compose.ui.graphics.Color) {
    Box(
        modifier = Modifier
            .clip(RoundedCornerShape(6.dp))
            .background(ArcadeBg.copy(alpha = 0.85f))
            .border(1.dp, color.copy(alpha = 0.65f), RoundedCornerShape(6.dp))
            .padding(horizontal = 8.dp, vertical = 4.dp)
    ) {
        Text(
            text = label,
            style = MaterialTheme.typography.labelSmall,
            color = color
        )
    }
}
