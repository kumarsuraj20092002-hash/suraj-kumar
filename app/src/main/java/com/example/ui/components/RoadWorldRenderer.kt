package com.example.ui.components

import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.Stroke
import com.example.game.RoadThemeProfile
import com.example.game.RoadWorldStyle
import com.example.ui.theme.ArcadeBg
import kotlin.math.ceil
import kotlin.math.floor
import kotlin.math.sin

/**
 * Renders multi-layer, world-specific animated pixel/vector backgrounds for each unlockable Road
 * so buying a new road upgrades both the foreground sculpted tiles AND the entire parallax world.
 */
fun DrawScope.drawRoadSceneryBackground(
    road: RoadThemeProfile,
    scrollDistancePx: Float,
    worldTime: Float,
    scale: Float,
    groundBaselinePx: Float
) {
    // 1. Rich sky gradient unique to this road's horizon & zenith colors
    drawRect(
        brush = Brush.verticalGradient(
            colors = listOf(
                ArcadeBg,
                road.skyTintColor,
                road.skyHorizonColor.copy(alpha = 0.92f),
                road.roadDeepColor
            )
        ),
        topLeft = Offset.Zero,
        size = size
    )

    when (road.worldStyle) {
        RoadWorldStyle.NEON_SKYLINE -> drawSkylineWorld(road, scrollDistancePx, worldTime, scale, groundBaselinePx)
        RoadWorldStyle.MOLTEN_VOLCANO -> drawVolcanoWorld(road, scrollDistancePx, worldTime, scale, groundBaselinePx)
        RoadWorldStyle.BIO_JUNGLE -> drawJungleWorld(road, scrollDistancePx, worldTime, scale, groundBaselinePx)
        RoadWorldStyle.ROYAL_TEMPLE -> drawTempleWorld(road, scrollDistancePx, worldTime, scale, groundBaselinePx)
        RoadWorldStyle.FROST_GLACIER -> drawGlacierWorld(road, scrollDistancePx, worldTime, scale, groundBaselinePx)
        RoadWorldStyle.GALAXY_COSMOS -> drawGalaxyWorld(road, scrollDistancePx, worldTime, scale, groundBaselinePx)
    }
}

private fun DrawScope.drawSkylineWorld(
    road: RoadThemeProfile,
    scrollDistancePx: Float,
    worldTime: Float,
    scale: Float,
    groundBaselinePx: Float
) {
    // Far stars & neon moon glow
    drawStarfield(scrollDistancePx * 0.08f, worldTime, scale, road.windowColor, 18)

    // Mid-layer megacity towers with rooftop antennas & illuminated windows
    val spanPx = 58f * scale
    val scrollPx = scrollDistancePx * 0.50f
    val firstIdx = floor(scrollPx / spanPx).toInt() - 1
    val lastIdx = ceil((scrollPx + size.width) / spanPx).toInt() + 1

    for (i in firstIdx..lastIdx) {
        val sx = i * spanPx - scrollPx
        val hash = ((i * 73856093) xor 0x5A17) and 0xFF
        val towerH = size.height * (0.24f + (hash % 36) / 100f)
        val towerW = spanPx * (0.56f + ((hash ushr 3) % 3) * 0.11f)
        val topY = groundBaselinePx - towerH

        drawRect(
            color = road.towerColor.copy(alpha = 0.85f),
            topLeft = Offset(sx, topY),
            size = Size(towerW, towerH)
        )

        // Rooftop antenna & blinking beacon
        if (hash % 2 == 0) {
            val antX = sx + towerW * 0.5f
            val antH = 10f * scale
            drawLine(
                color = road.windowColor.copy(alpha = 0.5f),
                start = Offset(antX, topY),
                end = Offset(antX, topY - antH),
                strokeWidth = 1.6f * scale
            )
            val blink = (sin(worldTime * 4f + i) + 1f) * 0.5f
            drawCircle(
                color = Color(0xFFFB7185).copy(alpha = 0.35f + 0.55f * blink),
                radius = 2.4f * scale,
                center = Offset(antX, topY - antH)
            )
        }

        val winSize = (3f * scale).coerceAtLeast(3f)
        val rows = ((towerH / (winSize * 3f)).toInt()).coerceAtMost(8)
        for (wr in 0 until rows) {
            if ((hash + wr) % 2 == 0) {
                drawRect(
                    color = road.windowColor.copy(alpha = 0.44f),
                    topLeft = Offset(sx + towerW * 0.24f, topY + winSize * 2f + wr * winSize * 3f),
                    size = Size(winSize, winSize)
                )
            }
            if ((hash + wr * 3) % 3 == 0) {
                drawRect(
                    color = road.railColor.copy(alpha = 0.32f),
                    topLeft = Offset(sx + towerW * 0.62f, topY + winSize * 2f + wr * winSize * 3f),
                    size = Size(winSize, winSize)
                )
            }
        }
    }
}

private fun DrawScope.drawVolcanoWorld(
    road: RoadThemeProfile,
    scrollDistancePx: Float,
    worldTime: Float,
    scale: Float,
    groundBaselinePx: Float
) {
    // Horizon magma glow band
    drawRect(
        brush = Brush.verticalGradient(
            colors = listOf(Color.Transparent, Color(0xFFEA580C).copy(alpha = 0.38f)),
            startY = groundBaselinePx - size.height * 0.38f,
            endY = groundBaselinePx
        ),
        topLeft = Offset(0f, groundBaselinePx - size.height * 0.38f),
        size = Size(size.width, size.height * 0.38f)
    )

    // Distant volcanic mountain cones with glowing lava vents
    val spanPx = 96f * scale
    val scrollPx = scrollDistancePx * 0.42f
    val firstIdx = floor(scrollPx / spanPx).toInt() - 1
    val lastIdx = ceil((scrollPx + size.width) / spanPx).toInt() + 1

    for (i in firstIdx..lastIdx) {
        val sx = i * spanPx - scrollPx
        val hash = ((i * 19349663) xor 0x3D91) and 0xFF
        val peakH = size.height * (0.34f + (hash % 24) / 100f)
        val baseW = spanPx * 1.15f
        val peakY = groundBaselinePx - peakH
        val craterW = baseW * 0.16f
        val centerX = sx + baseW * 0.5f

        val volcanoPath = Path().apply {
            moveTo(sx, groundBaselinePx)
            lineTo(centerX - craterW * 0.5f, peakY)
            lineTo(centerX + craterW * 0.5f, peakY)
            lineTo(sx + baseW, groundBaselinePx)
            close()
        }
        drawPath(path = volcanoPath, color = road.towerColor.copy(alpha = 0.9f))

        // Glowing crater rim & magma lava stream down the mountain
        drawRect(
            color = Color(0xFFFACC15).copy(alpha = 0.8f),
            topLeft = Offset(centerX - craterW * 0.45f, peakY),
            size = Size(craterW * 0.9f, 3.5f * scale)
        )
        val lavaWave = sin(worldTime * 3.5f + i) * 2.5f * scale
        drawRect(
            brush = Brush.verticalGradient(listOf(Color(0xFFFDE047), Color(0xFFF97316))),
            topLeft = Offset(centerX - 2.5f * scale + lavaWave * 0.2f, peakY + 3f * scale),
            size = Size(5f * scale, peakH * 0.72f)
        )
    }

    // Rising glowing ember sparks
    val spanW = size.width.coerceAtLeast(1f)
    val spanH = (size.height * 0.85f).coerceAtLeast(1f)
    for (p in 0 until 22) {
        val px = ((p * 67f + worldTime * 18f - scrollDistancePx * 0.6f) % spanW + spanW) % spanW
        val py = groundBaselinePx - (((worldTime * 48f + p * 37f) % spanH))
        val pulse = 0.45f + 0.55f * ((sin(worldTime * 5f + p) + 1f) * 0.5f)
        drawCircle(
            color = road.particleColor.copy(alpha = pulse),
            radius = (1.6f + (p % 2)) * scale.coerceAtMost(2.5f),
            center = Offset(px, py)
        )
    }
}

private fun DrawScope.drawJungleWorld(
    road: RoadThemeProfile,
    scrollDistancePx: Float,
    worldTime: Float,
    scale: Float,
    groundBaselinePx: Float
) {
    // Misty glowing bio-canopy spires & giant cyber trees with hanging luminous vines
    val spanPx = 74f * scale
    val scrollPx = scrollDistancePx * 0.48f
    val firstIdx = floor(scrollPx / spanPx).toInt() - 1
    val lastIdx = ceil((scrollPx + size.width) / spanPx).toInt() + 1

    for (i in firstIdx..lastIdx) {
        val sx = i * spanPx - scrollPx
        val hash = ((i * 83492791) xor 0x1B27) and 0xFF
        val treeH = size.height * (0.36f + (hash % 28) / 100f)
        val trunkW = 12f * scale
        val topY = groundBaselinePx - treeH
        val cx = sx + spanPx * 0.5f

        // Tree trunk silhouette
        drawRect(
            color = road.towerColor.copy(alpha = 0.88f),
            topLeft = Offset(cx - trunkW * 0.5f, topY + 12f * scale),
            size = Size(trunkW, treeH - 12f * scale)
        )

        // Stepped pixel canopy domes
        val canopyW = spanPx * 0.86f
        val canopyH = 18f * scale
        drawRect(
            color = Color(0xFF064E3B).copy(alpha = 0.85f),
            topLeft = Offset(cx - canopyW * 0.5f, topY),
            size = Size(canopyW, canopyH)
        )
        drawRect(
            color = road.roadTopColor.copy(alpha = 0.42f),
            topLeft = Offset(cx - canopyW * 0.36f, topY - 8f * scale),
            size = Size(canopyW * 0.72f, 9f * scale)
        )

        // Hanging glowing cyber-vines gently swaying
        val sway = sin(worldTime * 2.2f + i) * 2.5f * scale
        val vineX1 = cx - canopyW * 0.32f + sway * 0.3f
        val vineX2 = cx + canopyW * 0.28f - sway * 0.3f
        drawRect(
            color = road.railColor.copy(alpha = 0.55f),
            topLeft = Offset(vineX1, topY + canopyH),
            size = Size(2.2f * scale, (16f + (hash % 18)) * scale)
        )
        drawRect(
            color = road.windowColor.copy(alpha = 0.48f),
            topLeft = Offset(vineX2, topY + canopyH),
            size = Size(2.2f * scale, (12f + (hash % 14)) * scale)
        )
    }

    // Floating bio-luminescent fireflies
    val jungleW = size.width.coerceAtLeast(1f)
    for (p in 0 until 20) {
        val fx = ((p * 83f + sin(worldTime + p) * 14f - scrollDistancePx * 0.65f) % jungleW + jungleW) % jungleW
        val fy = groundBaselinePx * (0.15f + ((p * 29) % 70) / 100f) + sin(worldTime * 2.5f + p) * 6f
        val glow = 0.3f + 0.65f * ((sin(worldTime * 4f + p * 1.7f) + 1f) * 0.5f)
        drawCircle(
            color = road.particleColor.copy(alpha = glow),
            radius = 2.4f * scale.coerceAtMost(2.5f),
            center = Offset(fx, fy)
        )
    }
}

private fun DrawScope.drawTempleWorld(
    road: RoadThemeProfile,
    scrollDistancePx: Float,
    worldTime: Float,
    scale: Float,
    groundBaselinePx: Float
) {
    // Blazing royal sun-disc / eclipse halo in the sky
    val sunCenter = Offset(size.width * 0.72f, size.height * 0.24f)
    val sunRadius = (minOf(size.width, size.height) * 0.14f).coerceAtLeast(24f)
    drawCircle(
        brush = Brush.radialGradient(
            listOf(
                Color(0xFFFDE68A).copy(alpha = 0.85f),
                Color(0xFFF59E0B).copy(alpha = 0.45f),
                Color.Transparent
            ),
            center = sunCenter,
            radius = sunRadius * 2.1f
        ),
        center = sunCenter,
        radius = sunRadius * 2.1f
    )
    drawCircle(
        color = Color(0xFFFBBF24).copy(alpha = 0.9f),
        center = sunCenter,
        radius = sunRadius
    )
    drawCircle(
        color = road.skyTintColor,
        center = Offset(sunCenter.x - sunRadius * 0.14f, sunCenter.y - sunRadius * 0.08f),
        radius = sunRadius * 0.72f
    )

    // Great stepped pyramids + golden obelisks
    val spanPx = 104f * scale
    val scrollPx = scrollDistancePx * 0.44f
    val firstIdx = floor(scrollPx / spanPx).toInt() - 1
    val lastIdx = ceil((scrollPx + size.width) / spanPx).toInt() + 1

    for (i in firstIdx..lastIdx) {
        val sx = i * spanPx - scrollPx
        val hash = ((i * 514229) xor 0x6F11) and 0xFF
        val pyrH = size.height * (0.28f + (hash % 24) / 100f)
        val pyrW = spanPx * 1.08f
        val cx = sx + pyrW * 0.5f
        val apexY = groundBaselinePx - pyrH

        val pyramidPath = Path().apply {
            moveTo(sx, groundBaselinePx)
            lineTo(cx, apexY)
            lineTo(sx + pyrW, groundBaselinePx)
            close()
        }
        drawPath(path = pyramidPath, color = road.towerColor.copy(alpha = 0.88f))

        // Illuminated golden capstone + step bands
        val capPath = Path().apply {
            moveTo(cx - pyrW * 0.08f, apexY + pyrH * 0.16f)
            lineTo(cx, apexY)
            lineTo(cx + pyrW * 0.08f, apexY + pyrH * 0.16f)
            close()
        }
        drawPath(path = capPath, color = Color(0xFFFBBF24).copy(alpha = 0.85f))

        drawLine(
            color = Color(0xFFFBBF24).copy(alpha = 0.35f),
            start = Offset(cx - pyrW * 0.25f, apexY + pyrH * 0.5f),
            end = Offset(cx + pyrW * 0.25f, apexY + pyrH * 0.5f),
            strokeWidth = 2f * scale
        )
    }

    // Golden sand/dust shimmer
    drawStarfield(scrollDistancePx * 0.2f, worldTime, scale, road.particleColor, 16)
}

private fun DrawScope.drawGlacierWorld(
    road: RoadThemeProfile,
    scrollDistancePx: Float,
    worldTime: Float,
    scale: Float,
    groundBaselinePx: Float
) {
    // Shimmering Aurora Borealis waves across upper night sky
    for (band in 0..2) {
        val bandY = size.height * (0.14f + band * 0.09f)
        val color = when (band) {
            0 -> Color(0xFF34D399).copy(alpha = 0.22f)
            1 -> Color(0xFF38BDF8).copy(alpha = 0.24f)
            else -> Color(0xFFA855F7).copy(alpha = 0.16f)
        }
        val path = Path()
        val segments = 16
        for (s in 0..segments) {
            val x = (s.toFloat() / segments) * size.width
            val wave = sin(worldTime * 1.4f + s * 0.55f + band * 1.3f) * 14f * scale.coerceAtMost(2.5f)
            if (s == 0) path.moveTo(x, bandY + wave) else path.lineTo(x, bandY + wave)
        }
        drawPath(
            path = path,
            color = color,
            style = Stroke(width = 12f * scale.coerceAtMost(2.8f))
        )
    }

    // Sharp crystalline ice peaks with bright snow edge highlights
    val spanPx = 88f * scale
    val scrollPx = scrollDistancePx * 0.45f
    val firstIdx = floor(scrollPx / spanPx).toInt() - 1
    val lastIdx = ceil((scrollPx + size.width) / spanPx).toInt() + 1

    for (i in firstIdx..lastIdx) {
        val sx = i * spanPx - scrollPx
        val hash = ((i * 297121507) xor 0x4A29) and 0xFF
        val peakH = size.height * (0.30f + (hash % 30) / 100f)
        val peakW = spanPx * 1.12f
        val peakX = sx + peakW * 0.46f
        val peakY = groundBaselinePx - peakH

        val mountain = Path().apply {
            moveTo(sx, groundBaselinePx)
            lineTo(peakX, peakY)
            lineTo(sx + peakW, groundBaselinePx)
            close()
        }
        drawPath(path = mountain, color = road.towerColor.copy(alpha = 0.88f))

        // Left sunlit ice facet highlight
        val facet = Path().apply {
            moveTo(peakX, peakY)
            lineTo(peakX - peakW * 0.14f, peakY + peakH * 0.35f)
            lineTo(peakX + peakW * 0.05f, peakY + peakH * 0.45f)
            close()
        }
        drawPath(path = facet, color = Color(0xFFE0F2FE).copy(alpha = 0.55f))
    }

    // Falling snow particles
    val snowW = size.width.coerceAtLeast(1f)
    for (p in 0 until 24) {
        val sx = ((p * 73f - scrollDistancePx * 0.35f + sin(worldTime * 1.8f + p) * 10f) % snowW + snowW) % snowW
        val sy = ((worldTime * 36f + p * 41f) % groundBaselinePx.coerceAtLeast(100f))
        drawRect(
            color = Color.White.copy(alpha = 0.65f),
            topLeft = Offset(sx, sy),
            size = Size(2.5f * scale.coerceAtMost(2.5f), 2.5f * scale.coerceAtMost(2.5f))
        )
    }
}

private fun DrawScope.drawGalaxyWorld(
    road: RoadThemeProfile,
    scrollDistancePx: Float,
    worldTime: Float,
    scale: Float,
    groundBaselinePx: Float
) {
    // Rich starfield
    drawStarfield(scrollDistancePx * 0.15f, worldTime, scale, Color(0xFFE0F2FE), 28)

    // Giant ringed synthwave planet in upper sky
    val planetCenter = Offset(size.width * 0.72f, size.height * 0.26f)
    val planetRadius = (minOf(size.width, size.height) * 0.13f).coerceAtLeast(22f)
    drawCircle(
        brush = Brush.linearGradient(
            listOf(Color(0xFFF472B6), Color(0xFF7E22CE), Color(0xFF1E1B4B)),
            start = Offset(planetCenter.x - planetRadius, planetCenter.y - planetRadius),
            end = Offset(planetCenter.x + planetRadius, planetCenter.y + planetRadius)
        ),
        center = planetCenter,
        radius = planetRadius
    )
    drawOval(
        color = Color(0xFF22D3EE).copy(alpha = 0.75f),
        topLeft = Offset(planetCenter.x - planetRadius * 1.75f, planetCenter.y - planetRadius * 0.28f),
        size = Size(planetRadius * 3.5f, planetRadius * 0.56f),
        style = Stroke(width = 3f * scale.coerceAtMost(2.4f))
    )

    // Synthwave cosmic monoliths & glowing warp horizon grid
    val horizonY = groundBaselinePx - size.height * 0.20f
    drawLine(
        color = Color(0xFFF472B6).copy(alpha = 0.55f),
        start = Offset(0f, horizonY),
        end = Offset(size.width, horizonY),
        strokeWidth = 2f
    )

    val gridSpan = 42f * scale
    val scrollGrid = ((scrollDistancePx * 0.55f) % gridSpan + gridSpan) % gridSpan
    var gx = -scrollGrid
    while (gx < size.width + gridSpan) {
        drawLine(
            color = Color(0xFFA855F7).copy(alpha = 0.24f),
            start = Offset(gx, horizonY),
            end = Offset(gx - 36f * scale, groundBaselinePx),
            strokeWidth = 1.5f
        )
        gx += gridSpan
    }

    // Floating holographic spires
    val spanPx = 92f * scale
    val scrollPx = scrollDistancePx * 0.48f
    val firstIdx = floor(scrollPx / spanPx).toInt() - 1
    val lastIdx = ceil((scrollPx + size.width) / spanPx).toInt() + 1
    for (i in firstIdx..lastIdx) {
        val sx = i * spanPx - scrollPx
        val hash = ((i * 15485863) xor 0x7E13) and 0xFF
        val spireH = size.height * (0.18f + (hash % 22) / 100f)
        val bob = sin(worldTime * 2.2f + i) * 4f * scale
        val topY = groundBaselinePx - spireH - 12f * scale + bob
        drawRect(
            color = road.towerColor.copy(alpha = 0.85f),
            topLeft = Offset(sx + 18f * scale, topY),
            size = Size(20f * scale, spireH)
        )
        drawRect(
            color = road.railColor.copy(alpha = 0.6f),
            topLeft = Offset(sx + 18f * scale, topY),
            size = Size(20f * scale, 3f * scale)
        )
    }
}

private fun DrawScope.drawStarfield(
    scrollPx: Float,
    worldTime: Float,
    scale: Float,
    color: Color,
    count: Int
) {
    val starW = size.width.coerceAtLeast(1f)
    val starH = (size.height * 0.65f).coerceAtLeast(1f)
    for (i in 0 until count) {
        val sx = ((i * 97f - scrollPx) % starW + starW) % starW
        val sy = ((i * 53f) % starH) + 8f
        val twinkle = 0.35f + 0.65f * ((sin(worldTime * 3.2f + i * 1.1f) + 1f) * 0.5f)
        val starSize = (1.8f + (i % 2)) * scale.coerceAtMost(2.2f)
        drawRect(
            color = color.copy(alpha = twinkle),
            topLeft = Offset(sx, sy),
            size = Size(starSize, starSize)
        )
    }
}
