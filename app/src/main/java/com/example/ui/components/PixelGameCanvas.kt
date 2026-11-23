package com.example.ui.components

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.FilterQuality
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.imageResource
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.IntSize
import com.example.R
import com.example.game.BiomeType
import com.example.game.HeroAnimState
import com.example.game.PixelArtAtlas
import com.example.game.PlatformerEngine
import com.example.game.RoadWorldStyle
import com.example.game.SpriteFramePair
import com.example.ui.theme.ArcadeBg
import com.example.ui.theme.HyperCoral
import com.example.ui.theme.MintEmerald
import com.example.ui.theme.NeonCyan
import com.example.ui.theme.NeonViolet
import com.example.ui.theme.VoltAmber
import kotlin.math.ceil
import kotlin.math.floor
import kotlin.math.min
import kotlin.math.roundToInt
import kotlin.math.sin

@Composable
fun PixelGameCanvas(
    engine: PlatformerEngine,
    frameTick: Long,
    showHitboxes: Boolean,
    showPixelGrid: Boolean,
    modifier: Modifier = Modifier
) {
    val stage = engine.stage
    val bgRes = when (stage.biome) {
        BiomeType.SKYLINE -> R.drawable.img_hero_skyline
        BiomeType.FOUNDRY -> R.drawable.img_biome_citadel
    }
    val resources = LocalContext.current.resources
    val backdropBitmap = remember(bgRes) {
        ImageBitmap.imageResource(res = resources, id = bgRes)
    }

    Box(
        modifier = modifier
            .background(ArcadeBg)
    ) {
        Canvas(modifier = Modifier.fillMaxSize()) {
            // Read frameTick so Compose invalidates this Canvas cleanly every frame (60fps)
            @Suppress("UNUSED_VARIABLE")
            val tick = frameTick

            val isPortrait = size.height > size.width
            val logicalHeight = stage.heightTiles * PlatformerEngine.TILE_SIZE
            val minHorizontalLogical = (if (isPortrait) 13.5f else 17f) * PlatformerEngine.TILE_SIZE
            val scale = min(size.height / logicalHeight, size.width / minHorizontalLogical)
                .coerceAtLeast(1f)
            val worldScreenH = logicalHeight * scale
            val worldTopPx = ((size.height - worldScreenH) * if (isPortrait) 0.56f else 0.5f)
                .coerceAtLeast(0f)

            val visibleLogicalW = size.width / scale
            engine.visibleLogicalWidth = visibleLogicalW

            val shakeX = if (engine.shakeIntensity > 0.1f) {
                sin(engine.worldTime * 90f) * engine.shakeIntensity
            } else 0f
            val shakeY = if (engine.shakeIntensity > 0.1f) {
                sin(engine.worldTime * 115f + 1.2f) * engine.shakeIntensity * 0.6f
            } else 0f

            val camX = engine.cameraX + shakeX
            val camY = engine.cameraY + shakeY
            val road = engine.activeRoad
            val roadTiles = PixelArtAtlas.roadTilesById(road.id)

            val scrollDistPx = (camX + engine.player.x * 0.25f) * scale

            // 1. Road-Specific World Scenery (Sky, Volcanoes / Bio-Canopy / Pyramids / Aurora / Cosmos)
            drawRoadSceneryBackground(
                road = road,
                scrollDistancePx = scrollDistPx,
                worldTime = engine.worldTime,
                scale = scale,
                groundBaselinePx = worldTopPx + worldScreenH
            )

            // 2. Subtle pixel skyline overlay for Starter Neon Highway
            if (road.worldStyle == RoadWorldStyle.NEON_SKYLINE) {
                drawScrollingBackdrop(
                    backdrop = backdropBitmap,
                    camX = camX,
                    playerX = engine.player.x,
                    scale = scale
                )
            }

            // Atmospheric gradient scrim so foreground tiles & sprites pop cleanly
            drawRect(
                brush = Brush.verticalGradient(
                    colors = listOf(
                        ArcadeBg.copy(alpha = 0.16f),
                        Color.Transparent,
                        ArcadeBg.copy(alpha = 0.34f)
                    )
                ),
                topLeft = Offset.Zero,
                size = size
            )

            val startCol = maxOf(0, floor(camX / PlatformerEngine.TILE_SIZE).toInt() - 1)
            val endCol = minOf(
                stage.widthTiles - 1,
                ceil((camX + visibleLogicalW) / PlatformerEngine.TILE_SIZE).toInt() + 1
            )

            // Optional 16x16 tile grid overlay for design debugging
            if (showPixelGrid) {
                val gridColor = road.roadTopColor.copy(alpha = 0.16f)
                for (c in startCol..endCol + 1) {
                    val sx = (c * PlatformerEngine.TILE_SIZE - camX) * scale
                    drawLine(gridColor, Offset(sx, worldTopPx), Offset(sx, worldTopPx + worldScreenH), strokeWidth = 1f)
                }
                for (r in 0..stage.heightTiles) {
                    val sy = worldTopPx + (r * PlatformerEngine.TILE_SIZE - camY) * scale
                    drawLine(gridColor, Offset(0f, sy), Offset(size.width, sy), strokeWidth = 1f)
                }
            }

            // 4. Foreground Road & Platform Tiles (1.0x speed)
            val tileSizePx = ceil(PlatformerEngine.TILE_SIZE * scale).toInt() + 1
            for (r in 0 until stage.heightTiles) {
                for (c in startCol..endCol) {
                    val ch = engine.charAt(c, r)
                    if (ch == '.') continue

                    val sx = ((c * PlatformerEngine.TILE_SIZE - camX) * scale).roundToInt()
                    val sy = (worldTopPx + (r * PlatformerEngine.TILE_SIZE - camY) * scale).roundToInt()

                    when (ch) {
                        '#' -> {
                            val aboveIsSolid = engine.charAt(c, r - 1) == '#'
                            val tileBmp = if (aboveIsSolid) {
                                roadTiles.subTile.right
                            } else {
                                roadTiles.topTile.right
                            }
                            drawImage(
                                image = tileBmp,
                                dstOffset = IntOffset(sx, sy),
                                dstSize = IntSize(tileSizePx, tileSizePx),
                                filterQuality = FilterQuality.None
                            )
                        }
                        '=' -> {
                            drawImage(
                                image = roadTiles.railTile.right,
                                dstOffset = IntOffset(sx, sy),
                                dstSize = IntSize(tileSizePx, tileSizePx),
                                filterQuality = FilterQuality.None
                            )
                        }
                        '^' -> {
                            drawImage(
                                image = PixelArtAtlas.spikeTile.right,
                                dstOffset = IntOffset(sx, sy),
                                dstSize = IntSize(tileSizePx, tileSizePx),
                                filterQuality = FilterQuality.None
                            )
                        }
                        'S' -> {
                            val bouncePhase = if (engine.player.vy < -180f &&
                                kotlin.math.abs(engine.player.x - c * 16f) < 20f
                            ) 1 else 0
                            drawImage(
                                image = PixelArtAtlas.springFrames[bouncePhase].right,
                                dstOffset = IntOffset(sx, sy),
                                dstSize = IntSize(tileSizePx, tileSizePx),
                                filterQuality = FilterQuality.None
                            )
                        }
                    }
                }
            }

            // Extend deep sub-surface bedrock below track in tall Portrait viewports
            val bottomBandY = worldTopPx + worldScreenH
            if (bottomBandY < size.height) {
                drawRect(
                    brush = Brush.verticalGradient(
                        listOf(road.roadDeepColor, ArcadeBg)
                    ),
                    topLeft = Offset(0f, bottomBandY),
                    size = Size(size.width, size.height - bottomBandY)
                )
                drawLine(
                    color = road.roadTopColor.copy(alpha = 0.35f),
                    start = Offset(0f, bottomBandY),
                    end = Offset(size.width, bottomBandY),
                    strokeWidth = 2f
                )
            }

            // Draw Checkpoint Beacon
            engine.getCheckpointTile()?.let { (cc, cr) ->
                val active = engine.isCheckpointActive()
                val cx = (cc * PlatformerEngine.TILE_SIZE - camX) * scale
                val cy = worldTopPx + (cr * PlatformerEngine.TILE_SIZE - camY) * scale
                val poleColor = if (active) MintEmerald else road.railColor.copy(alpha = 0.7f)
                drawRect(
                    color = poleColor,
                    topLeft = Offset(cx + 6f * scale, cy + 2f * scale),
                    size = Size(2.5f * scale, 14f * scale)
                )
                drawRect(
                    color = if (active) MintEmerald else HyperCoral,
                    topLeft = Offset(cx + 8.5f * scale, cy + 2f * scale),
                    size = Size(6f * scale, 5f * scale)
                )
            }

            // Draw Warp Portal
            val portalFrame = ((engine.worldTime * 6f).toInt()) % PixelArtAtlas.portalFrames.size
            val gx = ((engine.goalX - camX) * scale).roundToInt()
            val gy = (worldTopPx + (engine.goalY - camY) * scale).roundToInt()
            drawCircle(
                color = NeonViolet.copy(alpha = 0.25f + 0.1f * sin(engine.worldTime * 6f)),
                radius = 13f * scale,
                center = Offset(gx + 8f * scale, gy + 8f * scale)
            )
            drawImage(
                image = PixelArtAtlas.portalFrames[portalFrame].right,
                dstOffset = IntOffset(gx, gy),
                dstSize = IntSize(tileSizePx, tileSizePx),
                filterQuality = FilterQuality.None
            )

            // Draw Shards
            val shardFrame = ((engine.worldTime * 8f).toInt()) % PixelArtAtlas.shardFrames.size
            for (shard in engine.shards) {
                if (shard.collected) continue
                val bob = sin(engine.worldTime * 5f + shard.x * 0.2f) * 1.5f
                val sx = ((shard.x - camX) * scale).roundToInt()
                val sy = (worldTopPx + (shard.y + bob - camY) * scale).roundToInt()
                drawImage(
                    image = PixelArtAtlas.shardFrames[shardFrame].right,
                    dstOffset = IntOffset(sx, sy),
                    dstSize = IntSize(tileSizePx, tileSizePx),
                    filterQuality = FilterQuality.None
                )
            }

            // Draw Dash Crystals
            val crystalFrame = ((engine.worldTime * 4f).toInt()) % PixelArtAtlas.crystalFrames.size
            for (crystal in engine.crystals) {
                val bob = sin(engine.worldTime * 4f + crystal.x * 0.15f) * 1.8f
                val cx = ((crystal.x - camX) * scale).roundToInt()
                val cy = (worldTopPx + (crystal.y + bob - camY) * scale).roundToInt()
                if (crystal.isAvailable) {
                    drawCircle(
                        color = MintEmerald.copy(alpha = 0.22f),
                        radius = 10f * scale,
                        center = Offset(cx + 8f * scale, cy + 8f * scale)
                    )
                    drawImage(
                        image = PixelArtAtlas.crystalFrames[crystalFrame].right,
                        dstOffset = IntOffset(cx, cy),
                        dstSize = IntSize(tileSizePx, tileSizePx),
                        filterQuality = FilterQuality.None
                    )
                } else {
                    drawCircle(
                        color = MintEmerald.copy(alpha = 0.35f),
                        radius = 5.5f * scale,
                        center = Offset(cx + 8f * scale, cy + 8f * scale),
                        style = Stroke(width = 1.5f * scale)
                    )
                }
            }

            // Draw Patrol Drones
            val droneFrame = ((engine.worldTime * 6f).toInt()) % PixelArtAtlas.droneFrames.size
            for (drone in engine.drones) {
                if (!drone.alive) continue
                val dx = ((drone.x - camX) * scale).roundToInt()
                val dy = (worldTopPx + (drone.y - camY) * scale).roundToInt()
                val pair = PixelArtAtlas.droneFrames[droneFrame]
                val bmp = if (drone.vx >= 0f) pair.right else pair.left
                drawImage(
                    image = bmp,
                    dstOffset = IntOffset(dx, dy),
                    dstSize = IntSize(tileSizePx, tileSizePx),
                    filterQuality = FilterQuality.None
                )
                if (showHitboxes) {
                    drawRect(
                        color = HyperCoral,
                        topLeft = Offset((drone.x + 2f - camX) * scale, worldTopPx + (drone.y + 3f - camY) * scale),
                        size = Size(12f * scale, 10f * scale),
                        style = Stroke(width = 1.5f)
                    )
                }
            }

            // Draw Dash Afterimages
            val charSprites = PixelArtAtlas.characterById(engine.activeCharacter.id).sprites
            for (ghost in engine.afterimages) {
                val alpha = (ghost.life / ghost.maxLife).coerceIn(0f, 1f) * 0.52f
                val gxPos = ((ghost.x - 2.5f - camX) * scale).roundToInt()
                val gyPos = (worldTopPx + (ghost.y - 2f - camY) * scale).roundToInt()
                val bmp = if (ghost.facingRight) {
                    charSprites.dashFrame.right
                } else {
                    charSprites.dashFrame.left
                }
                drawImage(
                    image = bmp,
                    dstOffset = IntOffset(gxPos, gyPos),
                    dstSize = IntSize(tileSizePx, tileSizePx),
                    alpha = alpha,
                    filterQuality = FilterQuality.None
                )
            }

            // Draw Player Sprite
            val p = engine.player
            val spritePair: SpriteFramePair = when (p.animState) {
                HeroAnimState.IDLE -> {
                    val idx = ((p.animClock * 3f).toInt()) % charSprites.idleFrames.size
                    charSprites.idleFrames[idx]
                }
                HeroAnimState.RUN -> {
                    val idx = ((p.animClock * 12f).toInt()) % charSprites.runFrames.size
                    charSprites.runFrames[idx]
                }
                HeroAnimState.JUMP -> charSprites.jumpFrame
                HeroAnimState.FALL -> charSprites.fallFrame
                HeroAnimState.DASH -> charSprites.dashFrame
                HeroAnimState.WALL_SLIDE -> charSprites.wallFrame
            }
            val playerBmp = if (p.facingRight) spritePair.right else spritePair.left
            val pxDraw = ((p.x - 2.5f - camX) * scale).roundToInt()
            val pyDraw = (worldTopPx + (p.y - 2f - camY) * scale).roundToInt()
            if (p.dashTimer > 0f) {
                drawCircle(
                    color = engine.activeCharacter.primaryColor.copy(alpha = 0.28f),
                    radius = 12f * scale,
                    center = Offset((p.x + p.width / 2f - camX) * scale, worldTopPx + (p.y + p.height / 2f - camY) * scale)
                )
            }
            drawImage(
                image = playerBmp,
                dstOffset = IntOffset(pxDraw, pyDraw),
                dstSize = IntSize(tileSizePx, tileSizePx),
                filterQuality = FilterQuality.None
            )

            // Draw Particles
            for (pt in engine.particles) {
                val alpha = (pt.life / pt.maxLife).coerceIn(0f, 1f)
                val ps = pt.size * scale
                drawRect(
                    color = pt.color.copy(alpha = alpha),
                    topLeft = Offset((pt.x - camX) * scale - ps / 2f, worldTopPx + (pt.y - camY) * scale - ps / 2f),
                    size = Size(ps, ps)
                )
            }

            // Optional AABB Hitbox & Velocity Overlay
            if (showHitboxes) {
                drawHitboxDebug(engine, camX, camY, scale, worldTopPx)
            }

            // Respawn / Out flash tint
            if (engine.respawnFlashTimer > 0f) {
                drawRect(
                    color = HyperCoral.copy(alpha = (engine.respawnFlashTimer / 0.25f) * 0.35f),
                    topLeft = Offset.Zero,
                    size = size
                )
            }
        }
    }
}

private fun DrawScope.drawScrollingBackdrop(
    backdrop: ImageBitmap,
    camX: Float,
    playerX: Float,
    scale: Float
) {
    val imgAspect = backdrop.width.toFloat() / backdrop.height.coerceAtLeast(1).toFloat()
    val drawH = size.height.roundToInt().coerceAtLeast(1)
    val drawW = (drawH * imgAspect).roundToInt().coerceAtLeast(1)

    val scrollPx = ((camX * 0.32f + playerX * 0.14f) * scale)
    val offsetMod = ((scrollPx % drawW) + drawW) % drawW
    var x = -offsetMod.roundToInt()
    while (x < size.width) {
        drawImage(
            image = backdrop,
            dstOffset = IntOffset(x, 0),
            dstSize = IntSize(drawW, drawH),
            alpha = 0.36f,
            filterQuality = FilterQuality.Low
        )
        x += drawW
    }
}

private fun DrawScope.drawHitboxDebug(
    engine: PlatformerEngine,
    camX: Float,
    camY: Float,
    scale: Float,
    worldTopPx: Float
) {
    val p = engine.player
    val px = (p.x - camX) * scale
    val py = worldTopPx + (p.y - camY) * scale
    val pw = p.width * scale
    val ph = p.height * scale
    drawRect(
        color = MintEmerald,
        topLeft = Offset(px, py),
        size = Size(pw, ph),
        style = Stroke(width = 2f)
    )
    val center = Offset(px + pw / 2f, py + ph / 2f)
    val vecEnd = Offset(
        center.x + (p.vx * 0.12f) * scale,
        center.y + (p.vy * 0.12f) * scale
    )
    drawLine(VoltAmber, center, vecEnd, strokeWidth = 2.5f)
}
