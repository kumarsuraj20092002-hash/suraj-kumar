package com.example.ui.theme

import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Shapes
import androidx.compose.material3.darkColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.unit.dp

private val VoltDashColorScheme = darkColorScheme(
    primary = NeonCyan,
    onPrimary = ArcadeBg,
    primaryContainer = NeonCyanDim,
    onPrimaryContainer = TextPrimary,
    secondary = VoltAmber,
    onSecondary = ArcadeBg,
    secondaryContainer = ArcadeSurfaceElevated,
    onSecondaryContainer = VoltAmber,
    tertiary = HyperCoral,
    onTertiary = ArcadeBg,
    tertiaryContainer = HyperCoralDeep,
    onTertiaryContainer = TextPrimary,
    background = ArcadeBg,
    onBackground = TextPrimary,
    surface = ArcadeSurface,
    onSurface = TextPrimary,
    surfaceVariant = ArcadeSurfaceElevated,
    onSurfaceVariant = TextSecondary,
    outline = ArcadeBorder,
    error = HyperCoral,
    onError = ArcadeBg
)

private val VoltDashShapes = Shapes(
    extraSmall = RoundedCornerShape(4.dp),
    small = RoundedCornerShape(8.dp),
    medium = RoundedCornerShape(12.dp),
    large = RoundedCornerShape(16.dp),
    extraLarge = RoundedCornerShape(22.dp)
)

@Composable
fun VoltDashTheme(content: @Composable () -> Unit) {
    MaterialTheme(
        colorScheme = VoltDashColorScheme,
        typography = VoltDashTypography,
        shapes = VoltDashShapes,
        content = content
    )
}
