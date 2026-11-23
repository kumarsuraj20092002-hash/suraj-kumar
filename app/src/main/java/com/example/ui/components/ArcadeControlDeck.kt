package com.example.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowLeft
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowRight
import androidx.compose.material.icons.filled.Bolt
import androidx.compose.material.icons.filled.KeyboardArrowUp
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Rect
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.LayoutCoordinates
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.toSize
import com.example.R
import com.example.game.LiveInputState
import com.example.ui.theme.ArcadeBg
import com.example.ui.theme.ArcadeBorder
import com.example.ui.theme.ArcadeSurface
import com.example.ui.theme.ArcadeSurfaceElevated
import com.example.ui.theme.MintEmerald
import com.example.ui.theme.NeonCyan
import com.example.ui.theme.TextMuted
import com.example.ui.theme.TextPrimary
import com.example.ui.theme.VoltAmber

private data class DeckVisualState(
    val left: Boolean = false,
    val right: Boolean = false,
    val jump: Boolean = false,
    val dash: Boolean = false
)

private fun relativeBounds(parent: LayoutCoordinates?, child: LayoutCoordinates): Rect {
    if (parent == null || !parent.isAttached || !child.isAttached) return Rect.Zero
    val topLeft = parent.localPositionOf(child, Offset.Zero)
    return Rect(topLeft, child.size.toSize())
}

@Composable
fun ArcadeControlDeck(
    canDash: Boolean,
    liveInput: LiveInputState,
    modifier: Modifier = Modifier
) {
    var deckCoords by remember { mutableStateOf<LayoutCoordinates?>(null) }
    var leftBounds by remember { mutableStateOf(Rect.Zero) }
    var rightBounds by remember { mutableStateOf(Rect.Zero) }
    var jumpBounds by remember { mutableStateOf(Rect.Zero) }
    var dashBounds by remember { mutableStateOf(Rect.Zero) }
    var visualState by remember { mutableStateOf(DeckVisualState()) }

    BoxWithConstraints(
        modifier = modifier
            .fillMaxWidth()
            .background(
                Brush.verticalGradient(
                    colors = listOf(ArcadeSurface, ArcadeBg)
                )
            )
            .navigationBarsPadding()
    ) {
        val isCompactPortrait = maxWidth < 400.dp
        val dirW = if (isCompactPortrait) 66.dp else 78.dp
        val dirH = if (isCompactPortrait) 68.dp else 74.dp
        val dashSize = if (isCompactPortrait) 64.dp else 72.dp
        val jumpSize = if (isCompactPortrait) 76.dp else 86.dp

        Row(
            modifier = Modifier
                .fillMaxWidth()
                .onGloballyPositioned { deckCoords = it }
                .pointerInput(liveInput) {
                    awaitPointerEventScope {
                        var prevJump = false
                        var prevDash = false
                        while (true) {
                            val event = awaitPointerEvent()
                            var left = false
                            var right = false
                            var jump = false
                            var dash = false

                            for (change in event.changes) {
                                if (change.pressed) {
                                    val pos = change.position
                                    if (leftBounds.inflate(24f).contains(pos)) left = true
                                    if (rightBounds.inflate(24f).contains(pos)) right = true
                                    if (jumpBounds.inflate(24f).contains(pos)) jump = true
                                    if (dashBounds.inflate(24f).contains(pos)) dash = true
                                }
                            }

                            // Synchronous lock-free write: zero recomposition delay!
                            liveInput.moveDir = when {
                                left && !right -> -1f
                                right && !left -> 1f
                                else -> 0f
                            }
                            if (jump && !prevJump) {
                                liveInput.jumpPressLatch = true
                            }
                            if (dash && !prevDash) {
                                liveInput.dashPressLatch = true
                            }
                            liveInput.jumpHeld = jump
                            prevJump = jump
                            prevDash = dash

                            val nextVisual = DeckVisualState(left, right, jump, dash)
                            if (nextVisual != visualState) {
                                visualState = nextVisual
                            }
                        }
                    }
                }
                .padding(horizontal = if (isCompactPortrait) 12.dp else 20.dp, vertical = 12.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Left Thumb Cluster: Directional Rocker
            Row(
                horizontalArrangement = Arrangement.spacedBy(if (isCompactPortrait) 10.dp else 14.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                ArcadePadButton(
                    label = stringResource(R.string.ctrl_left),
                    icon = Icons.AutoMirrored.Filled.KeyboardArrowLeft,
                    pressed = visualState.left,
                    accentColor = NeonCyan,
                    modifier = Modifier
                        .size(width = dirW, height = dirH)
                        .onGloballyPositioned { leftBounds = relativeBounds(deckCoords, it) }
                        .testTag("control_left_button")
                )
                ArcadePadButton(
                    label = stringResource(R.string.ctrl_right),
                    icon = Icons.AutoMirrored.Filled.KeyboardArrowRight,
                    pressed = visualState.right,
                    accentColor = NeonCyan,
                    modifier = Modifier
                        .size(width = dirW, height = dirH)
                        .onGloballyPositioned { rightBounds = relativeBounds(deckCoords, it) }
                        .testTag("control_right_button")
                )
            }

            // Right Thumb Cluster: Dash + Jump Action Buttons
            Row(
                horizontalArrangement = Arrangement.spacedBy(if (isCompactPortrait) 10.dp else 16.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                ArcadeActionOrb(
                    label = stringResource(R.string.ctrl_dash),
                    icon = Icons.Filled.Bolt,
                    pressed = visualState.dash,
                    enabledVisual = canDash,
                    accentColor = if (canDash) VoltAmber else TextMuted,
                    modifier = Modifier
                        .size(dashSize)
                        .onGloballyPositioned { dashBounds = relativeBounds(deckCoords, it) }
                        .testTag("control_dash_button")
                )
                ArcadeActionOrb(
                    label = stringResource(R.string.ctrl_jump),
                    icon = Icons.Filled.KeyboardArrowUp,
                    pressed = visualState.jump,
                    enabledVisual = true,
                    accentColor = MintEmerald,
                    modifier = Modifier
                        .size(jumpSize)
                        .onGloballyPositioned { jumpBounds = relativeBounds(deckCoords, it) }
                        .testTag("control_jump_button")
                )
            }
        }
    }
}

@Composable
private fun ArcadePadButton(
    label: String,
    icon: ImageVector,
    pressed: Boolean,
    accentColor: Color,
    modifier: Modifier = Modifier
) {
    val bg = if (pressed) accentColor.copy(alpha = 0.28f) else ArcadeSurfaceElevated
    val border = if (pressed) accentColor else ArcadeBorder
    val contentTint = if (pressed) accentColor else TextPrimary

    Box(
        modifier = modifier
            .graphicsLayer {
                val s = if (pressed) 0.94f else 1f
                scaleX = s
                scaleY = s
            }
            .clip(RoundedCornerShape(16.dp))
            .background(bg)
            .border(width = if (pressed) 2.5.dp else 1.5.dp, color = border, shape = RoundedCornerShape(16.dp)),
        contentAlignment = Alignment.Center
    ) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Icon(
                imageVector = icon,
                contentDescription = label,
                tint = contentTint,
                modifier = Modifier.size(34.dp)
            )
            Text(
                text = label,
                style = MaterialTheme.typography.labelSmall,
                color = contentTint.copy(alpha = 0.85f)
            )
        }
    }
}

@Composable
private fun ArcadeActionOrb(
    label: String,
    icon: ImageVector,
    pressed: Boolean,
    enabledVisual: Boolean,
    accentColor: Color,
    modifier: Modifier = Modifier
) {
    val fill = when {
        pressed -> accentColor.copy(alpha = 0.36f)
        enabledVisual -> accentColor.copy(alpha = 0.14f)
        else -> ArcadeSurfaceElevated.copy(alpha = 0.6f)
    }
    val border = if (pressed || enabledVisual) accentColor else ArcadeBorder

    Box(
        modifier = modifier
            .graphicsLayer {
                val s = if (pressed) 0.92f else 1f
                scaleX = s
                scaleY = s
            }
            .clip(CircleShape)
            .background(fill)
            .border(width = if (pressed) 3.dp else 2.dp, color = border, shape = CircleShape),
        contentAlignment = Alignment.Center
    ) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Icon(
                imageVector = icon,
                contentDescription = label,
                tint = if (enabledVisual || pressed) accentColor else TextMuted,
                modifier = Modifier.size(28.dp)
            )
            Spacer(modifier = Modifier.height(1.dp))
            Text(
                text = label,
                style = MaterialTheme.typography.labelSmall,
                color = if (enabledVisual || pressed) TextPrimary else TextMuted
            )
        }
    }
}
