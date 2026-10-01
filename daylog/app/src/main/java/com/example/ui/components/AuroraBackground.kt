package com.example.ui.components

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.compositionLocalOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.drawWithCache
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.tooling.preview.Preview
import com.example.ui.theme.DaylogTheme
import com.example.ui.theme.DaylogTokens
import dev.chrisbanes.haze.HazeState

val LocalHazeState = compositionLocalOf { HazeState() }

/**
 * High-Performance Aurora Background.
 *
 * Uses `drawWithCache` to pre-compile the layered atmospheric gradients once when layout
 * dimensions are measured.
 *
 * Critical Senior Performance Optimization:
 * By eliminating continuous per-frame canvas invalidation from infinite transitions,
 * HazeState captures and blurs the background once. This prevents Haze from re-running
 * full-screen 24-28dp Gaussian blurs on every 16ms/8ms frame across all GlassPanel instances,
 * eliminating GPU fill-rate exhaustion and UI thread frame drops.
 */
@Composable
fun AuroraBackground(
    modifier: Modifier = Modifier,
    hazeState: HazeState = remember { HazeState() },
    content: @Composable () -> Unit
) {
    CompositionLocalProvider(LocalHazeState provides hazeState) {
        Box(modifier = modifier.fillMaxSize()) {
            // Background layer with Haze source (must be a sibling of content, never an ancestor)
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .then(HazeBridge.applyHazeSource(Modifier, hazeState))
                    .drawWithCache {
                        val width = size.width
                        val height = size.height

                        val baseVerticalGradient = Brush.verticalGradient(
                            colors = listOf(DaylogTokens.Night, DaylogTokens.NightDeep),
                            startY = 0f,
                            endY = height
                        )

                        // 4 atmospheric aurora glow anchors
                        val b1Brush = Brush.radialGradient(
                            colors = listOf(DaylogTokens.AuroraViolet.copy(alpha = 0.45f), Color.Transparent),
                            center = Offset(width * 0.35f, height * 0.25f),
                            radius = width * 0.85f
                        )

                        val b2Brush = Brush.radialGradient(
                            colors = listOf(DaylogTokens.AuroraCyan.copy(alpha = 0.38f), Color.Transparent),
                            center = Offset(width * 0.72f, height * 0.45f),
                            radius = width * 0.80f
                        )

                        val b3Brush = Brush.radialGradient(
                            colors = listOf(DaylogTokens.AuroraMagenta.copy(alpha = 0.32f), Color.Transparent),
                            center = Offset(width * 0.38f, height * 0.72f),
                            radius = width * 0.85f
                        )

                        val b4Brush = Brush.radialGradient(
                            colors = listOf(DaylogTokens.AuroraViolet.copy(alpha = 0.48f), Color.Transparent),
                            center = Offset(width * 0.75f, height * 0.85f),
                            radius = width * 0.75f
                        )

                        onDrawBehind {
                            drawRect(brush = baseVerticalGradient)
                            drawRect(brush = b1Brush)
                            drawRect(brush = b2Brush)
                            drawRect(brush = b3Brush)
                            drawRect(brush = b4Brush)
                        }
                    }
            )

            // Content composable rendered as sibling
            content()
        }
    }
}

@Preview(name = "AuroraBackground Preview", widthDp = 360, heightDp = 800)
@Composable
fun AuroraBackgroundPreview() {
    DaylogTheme {
        AuroraBackground {}
    }
}

