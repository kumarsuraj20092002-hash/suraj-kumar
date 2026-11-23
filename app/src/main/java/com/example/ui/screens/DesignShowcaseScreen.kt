package com.example.ui.screens

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
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
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowLeft
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowRight
import androidx.compose.material.icons.filled.Bolt
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Pause
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Slider
import androidx.compose.material3.SliderDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.FilterQuality
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.PathEffect
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.IntSize
import androidx.compose.ui.unit.dp
import com.example.R
import com.example.audio.LocalSoundManager
import com.example.data.PhysicsConfig
import com.example.game.LevelDefinitions
import com.example.game.PaletteSwatch
import com.example.game.PixelArtAtlas
import com.example.game.PixelSpriteSpec
import com.example.game.StageDefinition
import com.example.ui.AppDestination
import com.example.ui.DesignSubTab
import com.example.ui.GameUiState
import com.example.ui.GameViewModel
import com.example.ui.theme.ArcadeBg
import com.example.ui.theme.ArcadeBorder
import com.example.ui.theme.ArcadeSurface
import com.example.ui.theme.ArcadeSurfaceElevated
import com.example.ui.theme.HyperCoral
import com.example.ui.theme.MintEmerald
import com.example.ui.theme.NeonCyan
import com.example.ui.theme.NeonViolet
import com.example.ui.theme.TextMuted
import com.example.ui.theme.TextPrimary
import com.example.ui.theme.TextSecondary
import com.example.ui.theme.VoltAmber
import kotlinx.coroutines.delay
import java.util.Locale
import kotlin.math.roundToInt

@Composable
fun DesignShowcaseScreen(
    uiState: GameUiState,
    viewModel: GameViewModel,
    modifier: Modifier = Modifier
) {
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
            contentPadding = PaddingValues(bottom = 24.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Hero Design Bible Banner
            item {
                DesignHeroHeader(
                    selectedSubTab = uiState.designSubTab,
                    onSelectSubTab = { viewModel.selectDesignSubTab(it) },
                    onBackHome = { viewModel.navigateTo(AppDestination.HOME) }
                )
            }

            when (uiState.designSubTab) {
                DesignSubTab.ASSETS -> {
                    item {
                        SpriteInspectorSection(
                            selectedSpriteId = uiState.selectedSpriteId,
                            onSelectSprite = { viewModel.selectSprite(it) }
                        )
                    }
                    item {
                        PaletteShowcaseSection()
                    }
                }
                DesignSubTab.PHYSICS -> {
                    item {
                        PhysicsTunerSection(
                            config = uiState.physicsConfig,
                            onConfigChanged = { viewModel.updatePhysicsConfig(it) },
                            onTestInGame = { viewModel.navigateTo(AppDestination.PLAY) }
                        )
                    }
                }
                DesignSubTab.LEVELS -> {
                    item {
                        LevelBlueprintSection(
                            selectedIndex = uiState.selectedBlueprintStageIndex,
                            onSelectStage = { viewModel.selectBlueprintStage(it) },
                            onLaunchStage = { viewModel.launchLevel(it + 1, resetRunScore = true) }
                        )
                    }
                }
                DesignSubTab.CONTROLS -> {
                    item {
                        ControlsErgonomicsSection(
                            coyoteMs = uiState.physicsConfig.coyoteTimeMs,
                            jumpBufferMs = uiState.physicsConfig.jumpBufferMs
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun DesignHeroHeader(
    selectedSubTab: DesignSubTab,
    onSelectSubTab: (DesignSubTab) -> Unit,
    onBackHome: () -> Unit
) {
    Column(modifier = Modifier.fillMaxWidth()) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(184.dp)
        ) {
            Image(
                painter = painterResource(id = R.drawable.img_hero_skyline),
                contentDescription = "Volt Dash Skyline Art",
                contentScale = ContentScale.Crop,
                modifier = Modifier.fillMaxSize()
            )
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(
                        Brush.verticalGradient(
                            colors = listOf(
                                ArcadeBg.copy(alpha = 0.25f),
                                ArcadeBg.copy(alpha = 0.94f)
                            )
                        )
                    )
            )
            IconButton(
                onClick = onBackHome,
                modifier = Modifier
                    .align(Alignment.TopStart)
                    .statusBarsPadding()
                    .padding(8.dp)
                    .size(48.dp)
                    .clip(CircleShape)
                    .background(ArcadeBg.copy(alpha = 0.72f))
                    .testTag("design_back_button")
            ) {
                Icon(
                    imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                    contentDescription = "Back to Home",
                    tint = TextPrimary
                )
            }
            Column(
                modifier = Modifier
                    .align(Alignment.BottomStart)
                    .padding(horizontal = 16.dp, vertical = 12.dp)
            ) {
                Text(
                    text = "16-BIT ENGINE // MOBILE PLATFORMER SPEC",
                    style = MaterialTheme.typography.labelSmall,
                    color = NeonCyan
                )
                Spacer(Modifier.height(4.dp))
                Text(
                    text = stringResource(R.string.design_header_title),
                    style = MaterialTheme.typography.headlineMedium,
                    color = TextPrimary
                )
                Text(
                    text = stringResource(R.string.design_header_subtitle),
                    style = MaterialTheme.typography.bodyMedium,
                    color = TextSecondary
                )
            }
        }

        // Sub-tab Navigation Strip
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .horizontalScroll(rememberScrollState())
                .padding(horizontal = 16.dp, vertical = 10.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            DesignSubTab.entries.forEach { tab ->
                val selected = tab == selectedSubTab
                FilterChip(
                    selected = selected,
                    onClick = { onSelectSubTab(tab) },
                    label = {
                        Text(
                            text = tab.label.uppercase(),
                            style = MaterialTheme.typography.labelLarge
                        )
                    },
                    colors = FilterChipDefaults.filterChipColors(
                        selectedContainerColor = NeonCyan.copy(alpha = 0.2f),
                        selectedLabelColor = NeonCyan,
                        containerColor = ArcadeSurface,
                        labelColor = TextSecondary
                    ),
                    border = FilterChipDefaults.filterChipBorder(
                        enabled = true,
                        selected = selected,
                        borderColor = ArcadeBorder,
                        selectedBorderColor = NeonCyan
                    ),
                    modifier = Modifier
                        .height(40.dp)
                        .testTag("design_tab_${tab.name.lowercase()}")
                )
            }
        }
    }
}

@Composable
private fun SpriteInspectorSection(
    selectedSpriteId: String,
    onSelectSprite: (String) -> Unit
) {
    val sound = LocalSoundManager.current
    val spriteSpec: PixelSpriteSpec = remember(selectedSpriteId) {
        PixelArtAtlas.spriteCatalog.firstOrNull { it.id == selectedSpriteId }
            ?: PixelArtAtlas.spriteCatalog.first()
    }
    var frameIndex by remember(selectedSpriteId) { mutableIntStateOf(0) }
    var isPlaying by remember { mutableStateOf(true) }
    var showGrid by remember { mutableStateOf(true) }
    var showHitbox by remember { mutableStateOf(true) }

    LaunchedEffect(selectedSpriteId, isPlaying, spriteSpec.fps) {
        if (!isPlaying || spriteSpec.frames.size <= 1) return@LaunchedEffect
        val intervalMs = (1000L / spriteSpec.fps.coerceAtLeast(1)).coerceAtLeast(80L)
        while (true) {
            delay(intervalMs)
            frameIndex = (frameIndex + 1) % spriteSpec.frames.size
        }
    }

    val safeFrame = frameIndex.coerceIn(0, spriteSpec.frames.lastIndex)

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        Text(
            text = "PIXEL ART SPRITE & TILESET ATLAS",
            style = MaterialTheme.typography.titleSmall,
            color = VoltAmber
        )

        // Interactive Inspector Card
        Surface(
            color = ArcadeSurface,
            shape = RoundedCornerShape(16.dp),
            modifier = Modifier
                .fillMaxWidth()
                .border(1.dp, ArcadeBorder, RoundedCornerShape(16.dp))
        ) {
            Column(
                modifier = Modifier.padding(14.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = spriteSpec.category.uppercase(),
                            style = MaterialTheme.typography.labelSmall,
                            color = NeonCyan
                        )
                        Text(
                            text = spriteSpec.name,
                            style = MaterialTheme.typography.titleLarge,
                            color = TextPrimary
                        )
                    }
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(8.dp))
                            .background(ArcadeSurfaceElevated)
                            .border(1.dp, ArcadeBorder, RoundedCornerShape(8.dp))
                            .padding(horizontal = 10.dp, vertical = 5.dp)
                    ) {
                        Text(
                            text = "F ${safeFrame + 1}/${spriteSpec.frames.size}",
                            style = MaterialTheme.typography.labelMedium,
                            color = VoltAmber
                        )
                    }
                }

                // Zoomed Nearest-Neighbor Sprite Preview Stage
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(190.dp)
                        .clip(RoundedCornerShape(12.dp))
                        .background(ArcadeBg)
                        .border(1.dp, NeonCyan.copy(alpha = 0.35f), RoundedCornerShape(12.dp)),
                    contentAlignment = Alignment.Center
                ) {
                    Canvas(modifier = Modifier.size(160.dp)) {
                        // Subtle dark checkerboard background
                        val cell = size.width / 16f
                        for (r in 0 until 16) {
                            for (c in 0 until 16) {
                                val checker = if ((r + c) % 2 == 0) {
                                    Color(0xFF0E1525)
                                } else {
                                    Color(0xFF131C30)
                                }
                                drawRect(
                                    color = checker,
                                    topLeft = Offset(c * cell, r * cell),
                                    size = Size(cell, cell)
                                )
                            }
                        }

                        // Render crisp pixel art bitmap
                        val bmp = spriteSpec.frames[safeFrame].right
                        drawImage(
                            image = bmp,
                            dstOffset = IntOffset.Zero,
                            dstSize = IntSize(size.width.roundToInt(), size.height.roundToInt()),
                            filterQuality = FilterQuality.None
                        )

                        // Optional 16x16 grid overlay
                        if (showGrid) {
                            val gridColor = NeonCyan.copy(alpha = 0.22f)
                            for (i in 0..16) {
                                val p = i * cell
                                drawLine(gridColor, Offset(p, 0f), Offset(p, size.height), strokeWidth = 1f)
                                drawLine(gridColor, Offset(0f, p), Offset(size.width, p), strokeWidth = 1f)
                            }
                        }

                        // Optional AABB Hitbox visualization
                        if (showHitbox) {
                            drawRect(
                                color = MintEmerald,
                                topLeft = Offset(cell * 2.5f, cell * 2f),
                                size = Size(cell * 11f, cell * 14f),
                                style = Stroke(width = 3f)
                            )
                        }
                    }
                }

                // Playback + Overlay Controls
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        IconButton(
                            onClick = {
                                sound.playButtonClick()
                                isPlaying = false
                                frameIndex = if (safeFrame - 1 < 0) spriteSpec.frames.lastIndex else safeFrame - 1
                            },
                            modifier = Modifier.size(48.dp)
                        ) {
                            Icon(
                                imageVector = Icons.AutoMirrored.Filled.KeyboardArrowLeft,
                                contentDescription = "Previous Frame",
                                tint = TextPrimary
                            )
                        }
                        IconButton(
                            onClick = {
                                sound.playButtonClick()
                                isPlaying = !isPlaying
                            },
                            modifier = Modifier
                                .size(48.dp)
                                .clip(CircleShape)
                                .background(NeonCyan.copy(alpha = 0.16f))
                        ) {
                            Icon(
                                imageVector = if (isPlaying) Icons.Filled.Pause else Icons.Filled.PlayArrow,
                                contentDescription = if (isPlaying) "Pause Animation" else "Play Animation",
                                tint = NeonCyan
                            )
                        }
                        IconButton(
                            onClick = {
                                sound.playButtonClick()
                                isPlaying = false
                                frameIndex = (safeFrame + 1) % spriteSpec.frames.size
                            },
                            modifier = Modifier.size(48.dp)
                        ) {
                            Icon(
                                imageVector = Icons.AutoMirrored.Filled.KeyboardArrowRight,
                                contentDescription = "Next Frame",
                                tint = TextPrimary
                            )
                        }
                    }

                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        FilterChip(
                            selected = showGrid,
                            onClick = {
                                sound.playButtonClick()
                                showGrid = !showGrid
                            },
                            label = { Text("16PX GRID", style = MaterialTheme.typography.labelSmall) },
                            modifier = Modifier.height(36.dp)
                        )
                        FilterChip(
                            selected = showHitbox,
                            onClick = {
                                sound.playButtonClick()
                                showHitbox = !showHitbox
                            },
                            label = { Text("AABB", style = MaterialTheme.typography.labelSmall) },
                            modifier = Modifier.height(36.dp)
                        )
                    }
                }

                // Technical Specs & Design Note
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    SpecPill("SIZE", spriteSpec.dimensions, NeonCyan, Modifier.weight(1f))
                    SpecPill("RATE", "${spriteSpec.fps} FPS", VoltAmber, Modifier.weight(1f))
                }
                SpecPill("HITBOX", spriteSpec.hitboxDesc, MintEmerald, Modifier.fillMaxWidth())

                Text(
                    text = spriteSpec.designNote,
                    style = MaterialTheme.typography.bodyMedium,
                    color = TextSecondary
                )
            }
        }

        // Sprite Selector Carousel
        LazyRow(
            horizontalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            items(PixelArtAtlas.spriteCatalog) { spec ->
                val isSelected = spec.id == selectedSpriteId
                Surface(
                    color = if (isSelected) ArcadeSurfaceElevated else ArcadeSurface,
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier
                        .width(116.dp)
                        .clip(RoundedCornerShape(12.dp))
                        .border(
                            width = if (isSelected) 1.5.dp else 1.dp,
                            color = if (isSelected) NeonCyan else ArcadeBorder,
                            shape = RoundedCornerShape(12.dp)
                        )
                        .clickable { onSelectSprite(spec.id) }
                        .testTag("sprite_chip_${spec.id}")
                ) {
                    Column(
                        modifier = Modifier.padding(10.dp),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        Canvas(modifier = Modifier.size(48.dp)) {
                            drawImage(
                                image = spec.frames.first().right,
                                dstOffset = IntOffset.Zero,
                                dstSize = IntSize(size.width.roundToInt(), size.height.roundToInt()),
                                filterQuality = FilterQuality.None
                            )
                        }
                        Text(
                            text = spec.name,
                            style = MaterialTheme.typography.labelSmall,
                            color = if (isSelected) NeonCyan else TextPrimary,
                            maxLines = 2
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun SpecPill(
    label: String,
    value: String,
    accent: Color,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier
            .clip(RoundedCornerShape(8.dp))
            .background(ArcadeSurfaceElevated)
            .padding(horizontal = 10.dp, vertical = 7.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(text = label, style = MaterialTheme.typography.labelSmall, color = TextMuted)
        Text(text = value, style = MaterialTheme.typography.labelMedium, color = accent)
    }
}

@Composable
private fun PaletteShowcaseSection() {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp),
        verticalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        Text(
            text = "8-COLOR ARCADE PALETTE SYSTEM",
            style = MaterialTheme.typography.titleSmall,
            color = NeonCyan
        )
        Surface(
            color = ArcadeSurface,
            shape = RoundedCornerShape(16.dp),
            modifier = Modifier
                .fillMaxWidth()
                .border(1.dp, ArcadeBorder, RoundedCornerShape(16.dp))
        ) {
            Column(
                modifier = Modifier.padding(14.dp),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                PixelArtAtlas.paletteSwatches.forEach { swatch ->
                    PaletteRow(swatch)
                }
            }
        }
    }
}

@Composable
private fun PaletteRow(swatch: PaletteSwatch) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        Box(
            modifier = Modifier
                .size(34.dp)
                .clip(RoundedCornerShape(8.dp))
                .background(swatch.color)
                .border(1.dp, TextPrimary.copy(alpha = 0.25f), RoundedCornerShape(8.dp))
        )
        Column(modifier = Modifier.weight(1f)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(text = swatch.name, style = MaterialTheme.typography.titleMedium, color = TextPrimary)
                Text(text = swatch.hex, style = MaterialTheme.typography.labelMedium, color = NeonCyan)
            }
            Text(text = swatch.role, style = MaterialTheme.typography.bodyMedium, color = TextSecondary)
        }
    }
}

@Composable
private fun PhysicsTunerSection(
    config: PhysicsConfig,
    onConfigChanged: (PhysicsConfig) -> Unit,
    onTestInGame: () -> Unit
) {
    val sound = LocalSoundManager.current
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        Text(
            text = "LIVE GAME FEEL & JUMP ARC TUNER",
            style = MaterialTheme.typography.titleSmall,
            color = MintEmerald
        )

        // Presets
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            listOf(
                PhysicsConfig.ArcadeTight,
                PhysicsConfig.TurboSpeedrun,
                PhysicsConfig.LowGravMoon
            ).forEach { preset ->
                val selected = config.presetName == preset.presetName
                FilterChip(
                    selected = selected,
                    onClick = {
                        sound.playButtonClick()
                        onConfigChanged(
                            preset.copy(
                                showHitboxes = config.showHitboxes,
                                showPixelGrid = config.showPixelGrid,
                                hapticsEnabled = config.hapticsEnabled,
                                soundEnabled = config.soundEnabled
                            )
                        )
                    },
                    leadingIcon = if (selected) {
                        { Icon(Icons.Filled.Check, contentDescription = null, modifier = Modifier.size(16.dp)) }
                    } else null,
                    label = {
                        Text(preset.presetName, style = MaterialTheme.typography.labelSmall, maxLines = 1)
                    },
                    modifier = Modifier.weight(1f)
                )
            }
        }

        // Live Parabolic Jump & Dash Trajectory Visualizer
        Surface(
            color = ArcadeSurface,
            shape = RoundedCornerShape(16.dp),
            modifier = Modifier
                .fillMaxWidth()
                .border(1.dp, ArcadeBorder, RoundedCornerShape(16.dp))
        ) {
            Column(
                modifier = Modifier.padding(14.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text(
                        text = "JUMP PARABOLA + DASH REACH",
                        style = MaterialTheme.typography.labelMedium,
                        color = NeonCyan
                    )
                    val apexTiles = ((config.jumpVelocity * config.jumpVelocity) / (2f * config.gravity)) / 16f
                    Text(
                        text = "APEX: ${String.format(Locale.US, "%.1f", apexTiles)} TILES",
                        style = MaterialTheme.typography.labelMedium,
                        color = VoltAmber
                    )
                }

                Canvas(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(130.dp)
                        .clip(RoundedCornerShape(10.dp))
                        .background(ArcadeBg)
                ) {
                    val tilePx = size.height / 7f
                    // 16px reference grid
                    val gridColor = ArcadeBorder.copy(alpha = 0.45f)
                    val cols = (size.width / tilePx).toInt() + 1
                    for (c in 0..cols) {
                        drawLine(gridColor, Offset(c * tilePx, 0f), Offset(c * tilePx, size.height))
                    }
                    for (r in 0..7) {
                        drawLine(gridColor, Offset(0f, r * tilePx), Offset(size.width, r * tilePx))
                    }

                    // Ground line
                    val groundY = size.height - tilePx * 1.2f
                    drawRect(
                        color = NeonCyan.copy(alpha = 0.25f),
                        topLeft = Offset(0f, groundY),
                        size = Size(size.width, tilePx * 1.2f)
                    )

                    // Simulate jump parabola
                    val jumpPath = Path()
                    val dashPath = Path()
                    var sx = 0f
                    var sy = 0f
                    var vy = -config.jumpVelocity
                    val dt = 0.016f
                    val pxScale = tilePx / 16f
                    val startX = tilePx * 1.2f
                    jumpPath.moveTo(startX, groundY)
                    var apexX = startX
                    var apexY = groundY

                    for (step in 1..120) {
                        sx += config.runSpeed * dt
                        vy += config.gravity * 1.1f * dt
                        sy += vy * dt
                        val drawX = startX + sx * pxScale
                        val drawY = groundY + sy * pxScale
                        if (drawY < apexY) {
                            apexY = drawY
                            apexX = drawX
                        }
                        if (drawY >= groundY) {
                            jumpPath.lineTo(drawX, groundY)
                            break
                        }
                        jumpPath.lineTo(drawX, drawY)
                    }

                    // Dashed Neon Dash extension from Apex
                    dashPath.moveTo(apexX, apexY)
                    val dashLen = config.dashSpeed * 0.145f * pxScale
                    dashPath.lineTo(apexX + dashLen, apexY)

                    drawPath(
                        path = jumpPath,
                        color = MintEmerald,
                        style = Stroke(width = 4f)
                    )
                    drawPath(
                        path = dashPath,
                        color = VoltAmber,
                        style = Stroke(
                            width = 4f,
                            pathEffect = PathEffect.dashPathEffect(floatArrayOf(10f, 6f))
                        )
                    )
                    drawCircle(color = NeonCyan, radius = 6f, center = Offset(apexX, apexY))
                }
            }
        }

        // Sliders Card
        Surface(
            color = ArcadeSurface,
            shape = RoundedCornerShape(16.dp),
            modifier = Modifier
                .fillMaxWidth()
                .border(1.dp, ArcadeBorder, RoundedCornerShape(16.dp))
        ) {
            Column(
                modifier = Modifier.padding(14.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                TuningSlider(
                    label = "Run Speed",
                    valueText = "${config.runSpeed.roundToInt()} px/s",
                    value = config.runSpeed,
                    range = 100f..210f,
                    onValueChange = { onConfigChanged(config.copy(presetName = "Custom", runSpeed = it)) }
                )
                TuningSlider(
                    label = "Jump Impulse",
                    valueText = "${config.jumpVelocity.roundToInt()} px/s",
                    value = config.jumpVelocity,
                    range = 200f..340f,
                    onValueChange = { onConfigChanged(config.copy(presetName = "Custom", jumpVelocity = it)) }
                )
                TuningSlider(
                    label = "Gravity",
                    valueText = "${config.gravity.roundToInt()} px/s²",
                    value = config.gravity,
                    range = 420f..1050f,
                    onValueChange = { onConfigChanged(config.copy(presetName = "Custom", gravity = it)) }
                )
                TuningSlider(
                    label = "Air Dash Speed",
                    valueText = "${config.dashSpeed.roundToInt()} px/s",
                    value = config.dashSpeed,
                    range = 240f..450f,
                    onValueChange = { onConfigChanged(config.copy(presetName = "Custom", dashSpeed = it)) }
                )
                TuningSlider(
                    label = "Coyote Time",
                    valueText = "${config.coyoteTimeMs} ms",
                    value = config.coyoteTimeMs.toFloat(),
                    range = 40f..200f,
                    onValueChange = { onConfigChanged(config.copy(presetName = "Custom", coyoteTimeMs = it.roundToInt())) }
                )

                Spacer(Modifier.height(4.dp))
                DebugToggleRow(
                    title = "Show Hitboxes & Velocity Vector",
                    checked = config.showHitboxes,
                    onCheckedChange = { onConfigChanged(config.copy(showHitboxes = it)) }
                )
                DebugToggleRow(
                    title = "Show 16x16 Pixel Tile Grid",
                    checked = config.showPixelGrid,
                    onCheckedChange = { onConfigChanged(config.copy(showPixelGrid = it)) }
                )
                DebugToggleRow(
                    title = "Tactile Control Haptics",
                    checked = config.hapticsEnabled,
                    onCheckedChange = { onConfigChanged(config.copy(hapticsEnabled = it)) }
                )
                DebugToggleRow(
                    title = "Arcade Sound Effects",
                    checked = config.soundEnabled,
                    onCheckedChange = { onConfigChanged(config.copy(soundEnabled = it)) }
                )
            }
        }

        Button(
            onClick = onTestInGame,
            colors = ButtonDefaults.buttonColors(containerColor = NeonCyan, contentColor = ArcadeBg),
            modifier = Modifier
                .fillMaxWidth()
                .height(50.dp)
                .testTag("test_tuned_physics_button")
        ) {
            Icon(Icons.Filled.Bolt, contentDescription = null, modifier = Modifier.size(20.dp))
            Spacer(Modifier.width(8.dp))
            Text("TEST IN LIVE GAME", style = MaterialTheme.typography.labelLarge)
        }
    }
}

@Composable
private fun TuningSlider(
    label: String,
    valueText: String,
    value: Float,
    range: ClosedFloatingPointRange<Float>,
    onValueChange: (Float) -> Unit
) {
    Column {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Text(text = label, style = MaterialTheme.typography.bodyMedium, color = TextPrimary)
            Text(text = valueText, style = MaterialTheme.typography.labelMedium, color = NeonCyan)
        }
        Slider(
            value = value,
            onValueChange = onValueChange,
            valueRange = range,
            colors = SliderDefaults.colors(
                thumbColor = NeonCyan,
                activeTrackColor = NeonCyan,
                inactiveTrackColor = ArcadeSurfaceElevated
            )
        )
    }
}

@Composable
private fun DebugToggleRow(
    title: String,
    checked: Boolean,
    onCheckedChange: (Boolean) -> Unit
) {
    val sound = LocalSoundManager.current
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(text = title, style = MaterialTheme.typography.bodyMedium, color = TextPrimary)
        Switch(
            checked = checked,
            onCheckedChange = { next ->
                if (next) sound.playButtonClick()
                onCheckedChange(next)
            },
            colors = SwitchDefaults.colors(
                checkedThumbColor = ArcadeBg,
                checkedTrackColor = MintEmerald
            )
        )
    }
}

@Composable
private fun LevelBlueprintSection(
    selectedIndex: Int,
    onSelectStage: (Int) -> Unit,
    onLaunchStage: (Int) -> Unit
) {
    val safeIndex = selectedIndex.coerceIn(0, LevelDefinitions.stages.lastIndex)
    val stage = LevelDefinitions.stages[safeIndex]

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        Text(
            text = "LEVEL ARCHITECTURE & PACING BEATS",
            style = MaterialTheme.typography.titleSmall,
            color = NeonCyan
        )

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            LevelDefinitions.stages.forEachIndexed { idx, st ->
                val selected = idx == safeIndex
                FilterChip(
                    selected = selected,
                    onClick = { onSelectStage(idx) },
                    label = { Text("${st.code} ${st.name.split(" ").last()}", style = MaterialTheme.typography.labelSmall) },
                    modifier = Modifier.weight(1f)
                )
            }
        }

        // Full-stage Minimap Canvas
        Surface(
            color = ArcadeSurface,
            shape = RoundedCornerShape(16.dp),
            modifier = Modifier
                .fillMaxWidth()
                .border(1.dp, ArcadeBorder, RoundedCornerShape(16.dp))
        ) {
            Column(
                modifier = Modifier.padding(14.dp),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text(
                        text = "${stage.code}. ${stage.name}",
                        style = MaterialTheme.typography.titleLarge,
                        color = TextPrimary
                    )
                    Text(
                        text = "${stage.widthTiles}x${stage.heightTiles} TILES",
                        style = MaterialTheme.typography.labelMedium,
                        color = VoltAmber
                    )
                }

                StageBlueprintMinimap(stage = stage)

                // Legend row
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    LegendDot("TILE", Color(0xFF475569))
                    LegendDot("RAIL", NeonCyan)
                    LegendDot("SHARD", VoltAmber)
                    LegendDot("CRYSTAL", MintEmerald)
                    LegendDot("HAZARD", HyperCoral)
                    LegendDot("GOAL", NeonViolet)
                }

                Text(
                    text = stage.designSummary,
                    style = MaterialTheme.typography.bodyMedium,
                    color = TextSecondary
                )
            }
        }

        // Pacing Beats
        stage.pacingBeats.forEach { beat ->
            Surface(
                color = ArcadeSurface,
                shape = RoundedCornerShape(12.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .border(1.dp, ArcadeBorder, RoundedCornerShape(12.dp))
            ) {
                Row(
                    modifier = Modifier.padding(12.dp),
                    horizontalArrangement = Arrangement.spacedBy(12.dp),
                    verticalAlignment = Alignment.Top
                ) {
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(6.dp))
                            .background(NeonCyan.copy(alpha = 0.16f))
                            .padding(horizontal = 8.dp, vertical = 4.dp)
                    ) {
                        Text(
                            text = beat.zonePercent,
                            style = MaterialTheme.typography.labelSmall,
                            color = NeonCyan
                        )
                    }
                    Column(verticalArrangement = Arrangement.spacedBy(2.dp)) {
                        Text(text = beat.title, style = MaterialTheme.typography.titleMedium, color = TextPrimary)
                        Text(text = beat.description, style = MaterialTheme.typography.bodyMedium, color = TextSecondary)
                    }
                }
            }
        }

        Button(
            onClick = { onLaunchStage(selectedIndex) },
            colors = ButtonDefaults.buttonColors(containerColor = MintEmerald, contentColor = ArcadeBg),
            modifier = Modifier
                .fillMaxWidth()
                .height(50.dp)
                .testTag("blueprint_play_stage_button")
        ) {
            Icon(Icons.Filled.PlayArrow, contentDescription = null)
            Spacer(Modifier.width(8.dp))
            Text("PLAY STAGE ${stage.code}", style = MaterialTheme.typography.labelLarge)
        }
    }
}

@Composable
private fun StageBlueprintMinimap(stage: StageDefinition) {
    Canvas(
        modifier = Modifier
            .fillMaxWidth()
            .height(92.dp)
            .clip(RoundedCornerShape(10.dp))
            .background(ArcadeBg)
            .border(1.dp, ArcadeBorder, RoundedCornerShape(10.dp))
    ) {
        val cw = size.width / stage.widthTiles.toFloat()
        val ch = size.height / stage.heightTiles.toFloat()
        for (r in 0 until stage.heightTiles) {
            val rowStr = stage.rows[r]
            for (c in 0 until stage.widthTiles) {
                val tile = if (c < rowStr.length) rowStr[c] else '.'
                val color = when (tile) {
                    '#' -> Color(0xFF475569)
                    '=' -> NeonCyan
                    '^', 'E' -> HyperCoral
                    'C', 'S' -> VoltAmber
                    'D', 'K', 'P' -> MintEmerald
                    'G' -> NeonViolet
                    else -> null
                }
                if (color != null) {
                    drawRect(
                        color = color,
                        topLeft = Offset(c * cw, r * ch),
                        size = Size(cw + 0.6f, ch + 0.6f)
                    )
                }
            }
        }
    }
}

@Composable
private fun LegendDot(label: String, color: Color) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(4.dp)
    ) {
        Box(
            modifier = Modifier
                .size(8.dp)
                .clip(CircleShape)
                .background(color)
        )
        Text(text = label, style = MaterialTheme.typography.labelSmall, color = TextSecondary)
    }
}

@Composable
private fun ControlsErgonomicsSection(
    coyoteMs: Int,
    jumpBufferMs: Int
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        Text(
            text = "MOBILE TOUCH ERGONOMICS & INPUT PIPELINE",
            style = MaterialTheme.typography.titleSmall,
            color = VoltAmber
        )

        Surface(
            color = ArcadeSurface,
            shape = RoundedCornerShape(16.dp),
            modifier = Modifier
                .fillMaxWidth()
                .border(1.dp, ArcadeBorder, RoundedCornerShape(16.dp))
        ) {
            Column(
                modifier = Modifier.padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Text(
                    text = "Dual-Thumb Split Arcade Deck",
                    style = MaterialTheme.typography.titleLarge,
                    color = TextPrimary
                )
                Text(
                    text = "A single unified PointerInputEventScope tracks every finger simultaneously across the lower deck. Players can hold RIGHT with their left thumb while feathering JUMP and triggering DASH with zero gesture blocking.",
                    style = MaterialTheme.typography.bodyMedium,
                    color = TextSecondary
                )

                // Visual thumb zone diagram
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(12.dp))
                        .background(ArcadeBg)
                        .border(1.dp, ArcadeBorder, RoundedCornerShape(12.dp))
                        .padding(14.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            DiagramBox("L", NeonCyan, 44)
                            DiagramBox("R", NeonCyan, 44)
                        }
                        Spacer(Modifier.height(6.dp))
                        Text("LEFT THUMB (74dp)", style = MaterialTheme.typography.labelSmall, color = NeonCyan)
                    }

                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Row(
                            horizontalArrangement = Arrangement.spacedBy(8.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            DiagramCircle("DASH", VoltAmber, 42)
                            DiagramCircle("JUMP", MintEmerald, 52)
                        }
                        Spacer(Modifier.height(6.dp))
                        Text("RIGHT THUMB (68/82dp)", style = MaterialTheme.typography.labelSmall, color = MintEmerald)
                    }
                }
            }
        }

        Surface(
            color = ArcadeSurface,
            shape = RoundedCornerShape(16.dp),
            modifier = Modifier
                .fillMaxWidth()
                .border(1.dp, ArcadeBorder, RoundedCornerShape(16.dp))
        ) {
            Column(
                modifier = Modifier.padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                Text(
                    text = "Input Forgiveness Windows",
                    style = MaterialTheme.typography.titleLarge,
                    color = TextPrimary
                )
                TimelineCard(
                    title = "Coyote Time ($coyoteMs ms)",
                    accent = NeonCyan,
                    desc = "Keeps jump active briefly after running off a ledge, turning missed edge taps on glass screens into crisp launches."
                )
                TimelineCard(
                    title = "Jump Buffering ($jumpBufferMs ms)",
                    accent = MintEmerald,
                    desc = "Tapping Jump right before touching down queues the jump and fires automatically on the exact landing frame."
                )
                TimelineCard(
                    title = "Variable Jump Cut + Wall Slide",
                    accent = VoltAmber,
                    desc = "Releasing Jump early applies 1.4x gravity for micro-hops; holding toward vertical walls clamps descent speed to 52 px/s."
                )
            }
        }
    }
}

@Composable
private fun DiagramBox(text: String, color: Color, sizeDp: Int) {
    Box(
        modifier = Modifier
            .size(sizeDp.dp)
            .clip(RoundedCornerShape(10.dp))
            .background(color.copy(alpha = 0.16f))
            .border(1.5.dp, color, RoundedCornerShape(10.dp)),
        contentAlignment = Alignment.Center
    ) {
        Text(text = text, style = MaterialTheme.typography.labelLarge, color = color)
    }
}

@Composable
private fun DiagramCircle(text: String, color: Color, sizeDp: Int) {
    Box(
        modifier = Modifier
            .size(sizeDp.dp)
            .clip(CircleShape)
            .background(color.copy(alpha = 0.16f))
            .border(1.5.dp, color, CircleShape),
        contentAlignment = Alignment.Center
    ) {
        Text(text = text, style = MaterialTheme.typography.labelSmall, color = color)
    }
}

@Composable
private fun TimelineCard(title: String, accent: Color, desc: String) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(10.dp))
            .background(ArcadeSurfaceElevated)
            .padding(12.dp),
        verticalArrangement = Arrangement.spacedBy(4.dp)
    ) {
        Text(text = title, style = MaterialTheme.typography.titleMedium, color = accent)
        Text(text = desc, style = MaterialTheme.typography.bodyMedium, color = TextSecondary)
    }
}
