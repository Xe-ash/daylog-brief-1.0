package com.example.ui.components

import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawWithCache
import androidx.compose.ui.draw.scale
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.model.ActivityClassification
import com.example.ui.theme.DaylogTheme
import com.example.ui.theme.DaylogTokens
import com.example.ui.theme.DaylogTypography
import kotlin.math.cos
import kotlin.math.sin

enum class OrbState {
    Idle,
    Active,
    Paused
}

@Composable
fun NeonOrb(
    orbState: OrbState,
    modifier: Modifier = Modifier,
    activeClassification: ActivityClassification? = null,
    timerDisplay: @Composable (() -> Unit)? = null,
    activityTitle: String? = null,
    onClick: () -> Unit
) {
    val interactionSource = remember { MutableInteractionSource() }
    val isPressed by interactionSource.collectIsPressedAsState()

    // Press scale 0.97, 100ms as per Motion Budget §5.6
    val pressScale by animateFloatAsState(
        targetValue = if (isPressed) 0.97f else 1.0f,
        animationSpec = tween(durationMillis = 100),
        label = "orb_press_scale"
    )

    val infiniteTransition = rememberInfiniteTransition(label = "orb_transition")

    // Idle: 6s period breathing (0.97 to 1.03), slow orbit
    // Active: 2x speed (3s period), brighter
    // Paused: static, dimmed
    val isAnimated = orbState != OrbState.Paused
    val animDuration = if (orbState == OrbState.Active) 3000 else 6000

    val breathingScaleState = if (isAnimated) {
        infiniteTransition.animateFloat(
            initialValue = 0.97f,
            targetValue = 1.03f,
            animationSpec = infiniteRepeatable(
                animation = tween(animDuration, easing = LinearEasing),
                repeatMode = RepeatMode.Reverse
            ),
            label = "orb_breath"
        )
    } else remember { androidx.compose.runtime.mutableFloatStateOf(0.98f) }

    val orbitAngleState = if (isAnimated) {
        infiniteTransition.animateFloat(
            initialValue = 0f,
            targetValue = (2 * Math.PI).toFloat(),
            animationSpec = infiniteRepeatable(
                animation = tween(animDuration * 2, easing = LinearEasing),
                repeatMode = RepeatMode.Restart
            ),
            label = "orb_orbit"
        )
    } else remember { androidx.compose.runtime.mutableFloatStateOf(0.8f) }

    // Color bias based on active category status color
    val statusColor = when (activeClassification) {
        ActivityClassification.PRODUCTIVE -> DaylogTokens.Productive
        ActivityClassification.NEUTRAL -> DaylogTokens.Neutral
        ActivityClassification.UNPRODUCTIVE -> DaylogTokens.Distraction
        else -> DaylogTokens.AuroraViolet
    }

    val c1 = if (orbState == OrbState.Active) statusColor else DaylogTokens.AuroraViolet
    val c2 = if (orbState == OrbState.Active) statusColor.copy(alpha = 0.8f) else DaylogTokens.AuroraCyan
    val c3 = DaylogTokens.AuroraMagenta

    val overallAlpha = when (orbState) {
        OrbState.Active -> 1.0f
        OrbState.Idle -> 0.85f
        OrbState.Paused -> 0.45f
    }

    val accessibilityDesc = when (orbState) {
        OrbState.Active -> "Focus orb active: ${activityTitle ?: "tracking"}. Tap to pause or stop."
        OrbState.Paused -> "Focus orb paused. Tap to resume."
        OrbState.Idle -> "Focus orb idle. Tap to start activity."
    }

    Box(
        modifier = modifier
            .size(200.dp)
            .graphicsLayer {
                val bs = breathingScaleState.value
                scaleX = pressScale * bs
                scaleY = pressScale * bs
            }
            .clip(CircleShape)
            .clickable(
                interactionSource = interactionSource,
                indication = null,
                onClick = onClick
            )
            .semantics { contentDescription = accessibilityDesc },
        contentAlignment = Alignment.Center
    ) {
        // Hardware-accelerated GPU layer for orbital rotation and breathing
        Box(
            modifier = Modifier
                .size(200.dp)
                .graphicsLayer {
                    if (isAnimated) {
                        rotationZ = (orbitAngleState.value * 180f / Math.PI.toFloat())
                    }
                }
                .drawWithCache {
                    val radius = size.minDimension / 2f
                    val center = Offset(size.width / 2f, size.height / 2f)

                    // 1. Soft outer glow
                    val glowBrush = Brush.radialGradient(
                        colors = listOf(
                            c1.copy(alpha = 0.35f * overallAlpha),
                            Color.Transparent
                        ),
                        center = center,
                        radius = radius * 1.05f
                    )

                    // 2. Three orbiting radial accents (cached relative to center, rotated by GPU)
                    val orbitR = radius * 0.30f
                    val o1Center = Offset(center.x + orbitR, center.y)
                    val o2Center = Offset(
                        center.x + orbitR * -0.5f,
                        center.y + orbitR * 0.866f
                    )
                    val o3Center = Offset(
                        center.x + orbitR * -0.5f,
                        center.y - orbitR * 0.866f
                    )

                    val brush1 = Brush.radialGradient(
                        colors = listOf(
                            c1.copy(alpha = 0.70f * overallAlpha),
                            Color.Transparent
                        ),
                        center = o1Center,
                        radius = radius * 0.75f
                    )

                    val brush2 = Brush.radialGradient(
                        colors = listOf(
                            c2.copy(alpha = 0.65f * overallAlpha),
                            Color.Transparent
                        ),
                        center = o2Center,
                        radius = radius * 0.70f
                    )

                    val brush3 = Brush.radialGradient(
                        colors = listOf(
                            c3.copy(alpha = 0.50f * overallAlpha),
                            Color.Transparent
                        ),
                        center = o3Center,
                        radius = radius * 0.65f
                    )

                    // 3. Brighter core
                    val coreBrush = Brush.radialGradient(
                        colors = listOf(
                            Color.White.copy(alpha = 0.40f * overallAlpha),
                            c1.copy(alpha = 0.20f * overallAlpha),
                            Color.Transparent
                        ),
                        center = center,
                        radius = radius * 0.45f
                    )

                    onDrawBehind {
                        drawCircle(brush = glowBrush, radius = radius, center = center)
                        drawCircle(brush = brush1, radius = radius * 0.85f, center = center)
                        drawCircle(brush = brush2, radius = radius * 0.85f, center = center)
                        drawCircle(brush = brush3, radius = radius * 0.85f, center = center)
                        drawCircle(brush = coreBrush, radius = radius * 0.50f, center = center)
                    }
                }
        )

        // Center content: timer text inside if active, or calm indicator
        Column(
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            if (timerDisplay != null) {
                timerDisplay()
            } else if (orbState == OrbState.Idle) {
                Text(
                    text = "READY",
                    style = DaylogTypography.Label.copy(
                        color = DaylogTokens.TextPrimary.copy(alpha = 0.9f),
                        letterSpacing = 1.sp,
                        fontWeight = FontWeight.Bold
                    )
                )
            } else if (orbState == OrbState.Paused) {
                Text(
                    text = "PAUSED",
                    style = DaylogTypography.Label.copy(
                        color = DaylogTokens.TextSecondary,
                        letterSpacing = 1.sp,
                        fontWeight = FontWeight.Bold
                    )
                )
            }
        }
    }
}

@Preview(name = "NeonOrb Idle Preview")
@Composable
fun NeonOrbPreview() {
    DaylogTheme {
        NeonOrb(orbState = OrbState.Idle, onClick = {})
    }
}
