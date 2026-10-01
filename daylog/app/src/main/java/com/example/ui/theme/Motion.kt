package com.example.ui.theme

import androidx.compose.animation.core.CubicBezierEasing
import androidx.compose.animation.core.Easing
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.SpringSpec
import androidx.compose.animation.core.TweenSpec
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp

/**
 * DAYLOG Motion System Tokens & Easing Curves
 *
 * Implements the DAYLOG Premium Motion PRD:
 * - Immediate response (Respond First, Animate Second)
 * - Custom cubic bezier curves for crisp entrances and controlled exits
 * - Targeted, short durations (no slow, floaty animations)
 * - Restrained physical springs with low bounce
 */
object DaylogMotion {

    // --- DURATIONS (ms) ---
    const val DurationInstant = 80
    const val DurationMicro = 120
    const val DurationFast = 160
    const val DurationStandard = 220
    const val DurationComplex = 300
    const val DurationPage = 240
    const val DurationAmbientFast = 3500
    const val DurationAmbientMid = 6000
    const val DurationAmbientSlow = 10000

    // --- EASING CURVES (PRD §6) ---
    /** Fast beginning, controlled settle (cubic-bezier(0.22, 1, 0.36, 1)) */
    val CrispEntrance: Easing = CubicBezierEasing(0.22f, 1.0f, 0.36f, 1.0f)

    /** Immediate response with compact settling (cubic-bezier(0.16, 1, 0.3, 1)) */
    val SnappyInteraction: Easing = CubicBezierEasing(0.16f, 1.0f, 0.30f, 1.0f)

    /** Fast departure (cubic-bezier(0.4, 0, 0.8, 0.1)) */
    val ControlledExit: Easing = CubicBezierEasing(0.40f, 0.0f, 0.80f, 0.10f)

    /** Subtle physical response with slight overshoot (cubic-bezier(0.34, 1.3, 0.64, 1)) */
    val SoftPhysicalResponse: Easing = CubicBezierEasing(0.34f, 1.30f, 0.64f, 1.0f)

    /** Smooth continuous interpolation for atmospheric motion */
    val OrganicAtmosphere: Easing = FastOutSlowInEasing

    // --- SPRING SPECS (PRD §72, 73) ---
    /** Precise, snappy travel for indicators and pills */
    fun <T> snappySpring(): SpringSpec<T> = spring(
        dampingRatio = Spring.DampingRatioLowBouncy,
        stiffness = Spring.StiffnessMediumLow
    )

    /** Crisp physical spring without bounce */
    fun <T> preciseSpring(): SpringSpec<T> = spring(
        dampingRatio = Spring.DampingRatioNoBouncy,
        stiffness = Spring.StiffnessMedium
    )

    // --- TWEEN SPECS ---
    fun <T> microTween(easing: Easing = SnappyInteraction): TweenSpec<T> =
        tween(durationMillis = DurationMicro, easing = easing)

    fun <T> fastTween(easing: Easing = CrispEntrance): TweenSpec<T> =
        tween(durationMillis = DurationFast, easing = easing)

    fun <T> standardTween(easing: Easing = CrispEntrance): TweenSpec<T> =
        tween(durationMillis = DurationStandard, easing = easing)

    fun <T> exitTween(easing: Easing = ControlledExit): TweenSpec<T> =
        tween(durationMillis = DurationFast, easing = easing)
}
