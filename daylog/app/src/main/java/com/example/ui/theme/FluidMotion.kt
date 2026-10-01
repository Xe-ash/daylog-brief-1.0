package com.example.ui.theme

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.AnimationVector1D
import androidx.compose.animation.core.CubicBezierEasing
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.SpringSpec
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxScope
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.composed
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawWithContent
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.BlendMode
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

/**
 * Fluid Motion Physics & Micro-Animation Engine for Daylog.
 *
 * Implements real water droplet surface tension dynamics:
 * 1. Physical volume conservation: when pressed, squashes vertically (scaleY ~ 0.91)
 *    and expands horizontally (scaleX ~ 1.06).
 * 2. Surface tension rebound: on release, elastically overshoots with fluid wobble.
 * 3. Droplet popups: bloom onto screen with liquid splash expansion (scale/shape morph)
 *    and pull together like condensing beads of water on exit.
 */
object FluidMotion {

    /** Water droplet elasticity spring for rebound and wobble */
    fun <T> dropletWobbleSpring(): SpringSpec<T> = spring(
        dampingRatio = 0.50f, // Low damping creates authentic liquid surface wobble
        stiffness = 420f      // Responsive liquid surface tension
    )

    /** Compression spring when finger presses down into fluid */
    fun <T> dropletSquashSpring(): SpringSpec<T> = spring(
        dampingRatio = 0.68f,
        stiffness = 650f
    )

    /** Window droplet bloom entrance spring */
    fun <T> windowBloomSpring(): SpringSpec<T> = spring(
        dampingRatio = 0.56f,
        stiffness = 360f
    )

    /** Easing for fluid exit condensation */
    val LiquidExitEasing = CubicBezierEasing(0.35f, 0.0f, 0.85f, 0.15f)
}

/**
 * Modifier that transforms any button or interactive element like a real water droplet:
 * - On press: squashes along Y and expands along X (simulating liquid volume conservation).
 * - On release: oscillates back to rest with physical liquid wobble.
 * - Draws an iridescent liquid surface specular highlight that reacts to touch.
 */
fun Modifier.fluidDropletPress(
    interactionSource: MutableInteractionSource,
    enabled: Boolean = true,
    squashFactorY: Float = 0.91f,
    stretchFactorX: Float = 1.06f,
    onClick: (() -> Unit)? = null
): Modifier = composed {
    val isPressed by interactionSource.collectIsPressedAsState()

    // Squash & Stretch physics
    val scaleX by animateFloatAsState(
        targetValue = if (isPressed && enabled) stretchFactorX else 1.0f,
        animationSpec = if (isPressed) FluidMotion.dropletSquashSpring() else FluidMotion.dropletWobbleSpring(),
        label = "fluid_scale_x"
    )

    val scaleY by animateFloatAsState(
        targetValue = if (isPressed && enabled) squashFactorY else 1.0f,
        animationSpec = if (isPressed) FluidMotion.dropletSquashSpring() else FluidMotion.dropletWobbleSpring(),
        label = "fluid_scale_y"
    )

    val translationY by animateFloatAsState(
        targetValue = if (isPressed && enabled) 2.2f else 0.0f,
        animationSpec = if (isPressed) FluidMotion.dropletSquashSpring() else FluidMotion.dropletWobbleSpring(),
        label = "fluid_trans_y"
    )

    // Fluid liquid sheen shimmer on press
    val liquidSheenAlpha by animateFloatAsState(
        targetValue = if (isPressed && enabled) 0.35f else 0.0f,
        animationSpec = tween(durationMillis = if (isPressed) 80 else 240),
        label = "fluid_sheen"
    )

    this
        .graphicsLayer {
            this.scaleX = scaleX
            this.scaleY = scaleY
            this.translationY = translationY * density
        }
        .drawWithContent {
            drawContent()
            if (liquidSheenAlpha > 0.01f) {
                // Subtle droplet surface refraction highlight
                drawRect(
                    brush = Brush.radialGradient(
                        colors = listOf(
                            Color.White.copy(alpha = liquidSheenAlpha),
                            Color.Transparent
                        ),
                        center = Offset(size.width * 0.5f, size.height * 0.25f),
                        radius = size.width * 0.65f
                    ),
                    blendMode = BlendMode.Screen
                )
            }
        }
        .then(
            if (onClick != null) {
                Modifier.clickable(
                    interactionSource = interactionSource,
                    indication = null,
                    enabled = enabled,
                    onClick = onClick
                )
            } else Modifier
        )
}

/**
 * A fluid container for dialogs, bottom sheets, and modal popups.
 * Transforms the window itself like a water droplet:
 * - IN: Drops down, stretches vertically, wobbles horizontally, and expands its corners like liquid settling.
 * - OUT: Compresses and beads together like a droplet pulling away.
 */
@Composable
fun FluidDropletWindow(
    onDismissRequest: () -> Unit,
    modifier: Modifier = Modifier,
    initialCornerRadius: Dp = 48.dp,
    settledCornerRadius: Dp = 24.dp,
    content: @Composable BoxScope.(dismissWithFluidAnimation: () -> Unit) -> Unit
) {
    val coroutineScope = rememberCoroutineScope()
    var isDismissing by remember { mutableStateOf(false) }

    // Physical animatables for droplet transform
    val scaleX = remember { Animatable(0.74f) }
    val scaleY = remember { Animatable(0.52f) }
    val translationY = remember { Animatable(-30f) }
    val alpha = remember { Animatable(0.0f) }
    val cornerRadius = remember { Animatable(initialCornerRadius.value) }

    // Surface wave highlight sweep
    val waveProgress = remember { Animatable(0f) }

    fun triggerFluidDismiss() {
        if (isDismissing) return
        isDismissing = true
        coroutineScope.launch {
            // Liquid droplet condensation exit
            launch { alpha.animateTo(0f, tween(160, easing = LinearEasing)) }
            launch { scaleX.animateTo(0.80f, tween(160, easing = FluidMotion.LiquidExitEasing)) }
            launch { scaleY.animateTo(0.50f, tween(160, easing = FluidMotion.LiquidExitEasing)) }
            launch { translationY.animateTo(24f, tween(160, easing = FluidMotion.LiquidExitEasing)) }
            launch { cornerRadius.animateTo(initialCornerRadius.value, tween(160)) }
            delay(165)
            onDismissRequest()
        }
    }

    LaunchedEffect(Unit) {
        // Phase 1: Rapid droplet expansion & vertical stretch
        launch { alpha.animateTo(1.0f, tween(110)) }
        launch {
            scaleX.animateTo(1.0f, FluidMotion.windowBloomSpring())
        }
        launch {
            scaleY.animateTo(1.0f, FluidMotion.windowBloomSpring())
        }
        launch {
            translationY.animateTo(0f, FluidMotion.windowBloomSpring())
        }
        launch {
            cornerRadius.animateTo(
                targetValue = settledCornerRadius.value,
                animationSpec = spring(dampingRatio = 0.65f, stiffness = 320f)
            )
        }
        // Fluid surface refraction wave that glides across upon impact
        launch {
            waveProgress.animateTo(1.0f, tween(450, easing = FastOutSlowInEasing))
        }
    }

    Box(
        modifier = modifier
            .graphicsLayer {
                this.scaleX = scaleX.value
                this.scaleY = scaleY.value
                this.translationY = translationY.value * density
                this.alpha = alpha.value
                this.shape = RoundedCornerShape(cornerRadius.value.dp)
                this.clip = true
            }
            .drawWithContent {
                drawContent()
                // Liquid surface sheen wave on entrance
                if (waveProgress.value in 0.01f..0.99f) {
                    val sweepX = size.width * (waveProgress.value * 1.5f - 0.25f)
                    drawRect(
                        brush = Brush.linearGradient(
                            colors = listOf(
                                Color.Transparent,
                                DaylogTokens.AuroraCyan.copy(alpha = 0.18f * (1f - waveProgress.value)),
                                DaylogTokens.AuroraViolet.copy(alpha = 0.25f * (1f - waveProgress.value)),
                                Color.Transparent
                            ),
                            start = Offset(sweepX - 80f, 0f),
                            end = Offset(sweepX + 80f, size.height)
                        ),
                        blendMode = BlendMode.Screen
                    )
                }
            }
    ) {
        content { triggerFluidDismiss() }
    }
}
