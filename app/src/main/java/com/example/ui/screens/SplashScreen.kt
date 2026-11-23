package com.example.ui.screens

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
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
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Bolt
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.runtime.withFrameNanos
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.FilterQuality
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.IntSize
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.R
import com.example.game.PixelArtAtlas
import com.example.game.RoadCatalog
import com.example.ui.components.drawRoadSceneryBackground
import com.example.ui.theme.ArcadeBg
import com.example.ui.theme.NeonCyan
import com.example.ui.theme.NeonViolet
import com.example.ui.theme.TextPrimary
import com.example.ui.theme.TextSecondary
import com.example.ui.theme.VoltAmber
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlin.math.roundToInt
import kotlin.math.sin

/**
 * Redesigned First Page: Clean, dramatic animated arcade title stage with
 * primary "START GAME" action and a compact "HOME" shortcut right beside it.
 */
@Composable
fun SplashScreen(
    activeLevelNumber: Int = 1,
    equippedCharacterId: String,
    equippedRoadId: String,
    onStartGame: () -> Unit,
    onOpenHome: () -> Unit,
    modifier: Modifier = Modifier
) {
    val titleScale = remember { Animatable(0.76f) }
    val titleAlpha = remember { Animatable(0f) }
    val ctaAlpha = remember { Animatable(0f) }
    val ctaTranslateY = remember { Animatable(24f) }

    LaunchedEffect(Unit) {
        launch {
            titleAlpha.animateTo(1f, animationSpec = tween(480, easing = FastOutSlowInEasing))
        }
        launch {
            titleScale.animateTo(
                1f,
                animationSpec = spring(
                    dampingRatio = Spring.DampingRatioMediumBouncy,
                    stiffness = Spring.StiffnessMediumLow
                )
            )
        }
        delay(180L)
        launch {
            ctaAlpha.animateTo(1f, animationSpec = tween(420, easing = FastOutSlowInEasing))
        }
        ctaTranslateY.animateTo(
            0f,
            animationSpec = spring(dampingRatio = Spring.DampingRatioLowBouncy, stiffness = Spring.StiffnessMediumLow)
        )
    }

    // 60fps continuous world & runner animation
    var worldClock by remember { mutableFloatStateOf(0f) }
    LaunchedEffect(Unit) {
        var lastNanos = 0L
        while (true) {
            withFrameNanos { nanos ->
                if (lastNanos == 0L) lastNanos = nanos
                val dt = ((nanos - lastNanos) / 1_000_000_000f).coerceIn(0.001f, 0.05f)
                lastNanos = nanos
                worldClock += dt
            }
        }
    }

    val infiniteTransition = rememberInfiniteTransition(label = "title_pulse")
    val glowPulse by infiniteTransition.animateFloat(
        initialValue = 0.72f,
        targetValue = 1.0f,
        animationSpec = infiniteRepeatable(
            animation = tween(1100, easing = LinearEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "glow_pulse"
    )

    val charSpec = remember(equippedCharacterId) { PixelArtAtlas.characterById(equippedCharacterId) }
    val road = remember(equippedRoadId) { RoadCatalog.byId(equippedRoadId) }
    val roadTiles = remember(road.id) { PixelArtAtlas.roadTilesById(road.id) }

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(ArcadeBg)
            .testTag("splash_screen")
    ) {
        // Full-bleed living parallax world + road + sprinting runner stage
        Canvas(modifier = Modifier.fillMaxSize()) {
            val w = size.width
            val h = size.height
            val scale = (minOf(w, h) / 190f).coerceIn(1.5f, 3.6f)
            val tilePx = (36.dp.toPx()).roundToInt().coerceAtLeast(24)
            val roadFloorY = (h * 0.67f).roundToInt()

            drawRoadSceneryBackground(
                road = road,
                scrollDistancePx = worldClock * 95f * scale,
                worldTime = worldClock,
                scale = scale,
                groundBaselinePx = roadFloorY.toFloat()
            )

            // Soft vignette for strong logo contrast
            drawRect(
                brush = Brush.verticalGradient(
                    colors = listOf(
                        ArcadeBg.copy(alpha = 0.55f),
                        Color.Transparent,
                        ArcadeBg.copy(alpha = 0.88f)
                    )
                ),
                topLeft = Offset.Zero,
                size = size
            )

            // Raised neon rail in midground right ahead of runner
            val railScroll = ((worldClock * 165f) % (w + tilePx * 4f))
            val railX = (w - railScroll).roundToInt()
            val railY = roadFloorY - tilePx * 2
            for (r in 0..2) {
                drawImage(
                    image = roadTiles.railTile.right,
                    dstOffset = IntOffset(railX + r * tilePx, railY),
                    dstSize = IntSize(tilePx, tilePx),
                    filterQuality = FilterQuality.None
                )
            }
            val shardFrame = ((worldClock * 8f).toInt()) % PixelArtAtlas.shardFrames.size
            val shardSize = (tilePx * 0.72f).roundToInt()
            drawImage(
                image = PixelArtAtlas.shardFrames[shardFrame].right,
                dstOffset = IntOffset(railX + tilePx, railY - shardSize + (sin(worldClock * 5f) * 4f).roundToInt()),
                dstSize = IntSize(shardSize, shardSize),
                filterQuality = FilterQuality.None
            )

            // Scrolling sculpted road top & sub-surface tiers
            val roadScrollPx = ((worldClock * 210f) % tilePx).roundToInt()
            var tx = -roadScrollPx
            while (tx < w) {
                drawImage(
                    image = roadTiles.topTile.right,
                    dstOffset = IntOffset(tx, roadFloorY),
                    dstSize = IntSize(tilePx, tilePx),
                    filterQuality = FilterQuality.None
                )
                var subY = roadFloorY + tilePx
                while (subY < h) {
                    drawImage(
                        image = roadTiles.subTile.right,
                        dstOffset = IntOffset(tx, subY),
                        dstSize = IntSize(tilePx, tilePx),
                        filterQuality = FilterQuality.None
                    )
                    subY += tilePx
                }
                tx += tilePx
            }

            // Road neon rim highlight
            drawLine(
                color = road.roadTopColor.copy(alpha = 0.75f * glowPulse),
                start = Offset(0f, roadFloorY.toFloat()),
                end = Offset(w, roadFloorY.toFloat()),
                strokeWidth = 3f
            )

            // Crisp Pixel Runner sprinting center-left along the road
            val heroPx = 96.dp.toPx().roundToInt()
            val heroX = ((w * 0.5f) - heroPx * 0.55f).roundToInt()
            val bobY = (sin(worldClock * 11f) * 2.5f).roundToInt()
            val heroY = roadFloorY - heroPx + bobY + 4

            // Aura glow behind runner
            drawCircle(
                brush = Brush.radialGradient(
                    colors = listOf(
                        charSpec.primaryColor.copy(alpha = 0.38f * glowPulse),
                        Color.Transparent
                    ),
                    center = Offset(heroX + heroPx * 0.5f, heroY + heroPx * 0.5f),
                    radius = heroPx * 0.85f
                ),
                center = Offset(heroX + heroPx * 0.5f, heroY + heroPx * 0.5f),
                radius = heroPx * 0.85f
            )

            // Dash energy speed lines behind runner
            for (t in 1..6) {
                val trailW = (18 + (6 - t) * 5).dp.toPx()
                val trailX = heroX - t * 14.dp.toPx()
                val trailY = heroY + heroPx * 0.45f + ((t % 3) - 1) * 10.dp.toPx()
                drawRect(
                    color = (if (t % 2 == 0) charSpec.primaryColor else charSpec.scarfColor).copy(
                        alpha = (1f - t / 7f) * 0.65f
                    ),
                    topLeft = Offset(trailX, trailY),
                    size = Size(trailW, 3.dp.toPx())
                )
            }

            val runIdx = ((worldClock * 11f).toInt()) % charSpec.sprites.runFrames.size
            drawImage(
                image = charSpec.sprites.runFrames[runIdx].right,
                dstOffset = IntOffset(heroX, heroY),
                dstSize = IntSize(heroPx, heroPx),
                filterQuality = FilterQuality.None
            )
        }

        // Foreground UI: Clean Title Emblem at top + ONLY START GAME at bottom
        BoxWithConstraints(
            modifier = Modifier
                .fillMaxSize()
                .statusBarsPadding()
                .navigationBarsPadding()
        ) {
            val compactVertical = maxHeight < 520.dp
            val emblemSize = if (compactVertical) 52.dp else 76.dp
            val titleSizeSp = if (compactVertical) 34.sp else 42.sp
            val outerVerticalPad = if (compactVertical) 12.dp else 24.dp

            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(horizontal = 24.dp, vertical = outerVerticalPad),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.SpaceBetween
            ) {
                // Top Game Logo Emblem
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    modifier = Modifier
                        .padding(top = if (compactVertical) 4.dp else 12.dp)
                        .graphicsLayer {
                            scaleX = titleScale.value
                            scaleY = titleScale.value
                            alpha = titleAlpha.value
                        }
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(6.dp),
                        modifier = Modifier
                            .clip(RoundedCornerShape(20.dp))
                            .background(ArcadeBg.copy(alpha = 0.78f))
                            .border(1.dp, road.roadTopColor.copy(alpha = 0.65f), RoundedCornerShape(20.dp))
                            .padding(horizontal = 14.dp, vertical = 6.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Filled.Bolt,
                            contentDescription = null,
                            tint = VoltAmber,
                            modifier = Modifier.size(16.dp)
                        )
                        Text(
                            text = "100% OFFLINE • NO INTERNET NEEDED",
                            style = MaterialTheme.typography.labelSmall,
                            color = road.roadTopColor
                        )
                    }

                    Spacer(Modifier.height(if (compactVertical) 8.dp else 14.dp))

                    // Custom Volt Dash App Icon Medallion
                    Box(
                        modifier = Modifier
                            .size(emblemSize)
                            .shadow(
                                elevation = 18.dp,
                                shape = RoundedCornerShape(20.dp),
                                ambientColor = NeonCyan,
                                spotColor = VoltAmber
                            )
                            .clip(RoundedCornerShape(20.dp))
                            .background(Color(0xFF0B1026))
                            .border(
                                width = 2.dp,
                                brush = Brush.linearGradient(
                                    listOf(
                                        NeonCyan.copy(alpha = 0.9f * glowPulse),
                                        VoltAmber.copy(alpha = 0.9f * glowPulse),
                                        NeonViolet.copy(alpha = 0.85f)
                                    )
                                ),
                                shape = RoundedCornerShape(20.dp)
                            )
                            .testTag("splash_app_emblem")
                    ) {
                        Image(
                            painter = painterResource(id = R.drawable.ic_launcher_art),
                            contentDescription = "Volt Dash Game Emblem",
                            contentScale = ContentScale.Crop,
                            modifier = Modifier.fillMaxSize()
                        )
                    }

                    Spacer(Modifier.height(if (compactVertical) 6.dp else 12.dp))

                    Text(
                        text = stringResource(R.string.app_name).uppercase(),
                        style = MaterialTheme.typography.displaySmall.copy(
                            fontSize = titleSizeSp,
                            lineHeight = (titleSizeSp.value + 4f).sp,
                            letterSpacing = 3.sp,
                            fontWeight = FontWeight.ExtraBold
                        ),
                        color = TextPrimary,
                        textAlign = TextAlign.Center
                    )

                    Spacer(Modifier.height(6.dp))

                    Box(
                        modifier = Modifier
                            .width(120.dp)
                            .height(4.dp)
                            .clip(RoundedCornerShape(2.dp))
                            .background(
                                Brush.horizontalGradient(
                                    listOf(Color.Transparent, charSpec.primaryColor, NeonViolet, Color.Transparent)
                                )
                            )
                    )

                    Spacer(Modifier.height(8.dp))

                    Text(
                        text = if (activeLevelNumber > 1) {
                            "RESUME LEVEL $activeLevelNumber • UNLIMITED STAGES"
                        } else {
                            "UNLIMITED PIXEL RUNNER"
                        },
                        style = MaterialTheme.typography.labelLarge.copy(letterSpacing = 1.6.sp),
                        color = if (activeLevelNumber > 1) VoltAmber else TextSecondary,
                        textAlign = TextAlign.Center
                    )
                }

            // Bottom: Prominent START GAME + compact HOME button beside it
            Row(
                modifier = Modifier
                    .widthIn(max = 390.dp)
                    .fillMaxWidth()
                    .padding(bottom = 16.dp)
                    .graphicsLayer {
                        alpha = ctaAlpha.value
                        translationY = ctaTranslateY.value
                    },
                horizontalArrangement = Arrangement.spacedBy(10.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Button(
                    onClick = onStartGame,
                    colors = ButtonDefaults.buttonColors(
                        containerColor = charSpec.primaryColor,
                        contentColor = ArcadeBg
                    ),
                    shape = RoundedCornerShape(18.dp),
                    modifier = Modifier
                        .weight(1f)
                        .height(62.dp)
                        .shadow(
                            elevation = 16.dp,
                            shape = RoundedCornerShape(18.dp),
                            ambientColor = charSpec.primaryColor,
                            spotColor = charSpec.primaryColor
                        )
                        .border(
                            width = 2.dp,
                            color = Color.White.copy(alpha = 0.5f * glowPulse),
                            shape = RoundedCornerShape(18.dp)
                        )
                        .testTag("start_game_button")
                ) {
                    Icon(
                        imageVector = Icons.Filled.PlayArrow,
                        contentDescription = null,
                        modifier = Modifier.size(28.dp)
                    )
                    Spacer(Modifier.width(6.dp))
                    Text(
                        text = if (activeLevelNumber > 1) "CONTINUE LV $activeLevelNumber" else "START GAME",
                        style = MaterialTheme.typography.titleMedium.copy(
                            fontWeight = FontWeight.ExtraBold,
                            letterSpacing = 1.1.sp
                        ),
                        maxLines = 1
                    )
                }

                OutlinedButton(
                    onClick = onOpenHome,
                    shape = RoundedCornerShape(18.dp),
                    colors = ButtonDefaults.outlinedButtonColors(
                        containerColor = ArcadeBg.copy(alpha = 0.86f),
                        contentColor = TextPrimary
                    ),
                    border = androidx.compose.foundation.BorderStroke(
                        width = 1.5.dp,
                        color = road.roadTopColor.copy(alpha = 0.75f)
                    ),
                    contentPadding = androidx.compose.foundation.layout.PaddingValues(horizontal = 12.dp, vertical = 6.dp),
                    modifier = Modifier
                        .height(62.dp)
                        .widthIn(min = 82.dp)
                        .testTag("start_home_button")
                ) {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.Center
                    ) {
                        Icon(
                            imageVector = Icons.Filled.Home,
                            contentDescription = "Open Home Hub",
                            tint = road.roadTopColor,
                            modifier = Modifier.size(22.dp)
                        )
                        Spacer(Modifier.height(2.dp))
                        Text(
                            text = "HOME",
                            style = MaterialTheme.typography.labelSmall.copy(
                                fontWeight = FontWeight.Bold,
                                letterSpacing = 0.8.sp
                            ),
                            color = TextPrimary,
                            maxLines = 1
                        )
                    }
                }
            }
        }
    }
}
}
