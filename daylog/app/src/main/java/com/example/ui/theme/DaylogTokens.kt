package com.example.ui.theme

import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp

/**
 * Aurora Glass UI Design Tokens (Section 4)
 */
object DaylogTokens {
    // --- COLORS ---
    val Night = Color(0xFF07060F)
    val NightDeep = Color(0xFF0D0B1F)
    val NightElevated = Color(0xFF141226)
    val AuroraViolet = Color(0xFF7C5CFF)
    val AuroraCyan = Color(0xFF22D3EE)
    val AuroraMagenta = Color(0xFFFF4FD8)
    val Productive = Color(0xFF3DF5B0)
    val Neutral = Color(0xFFFFC857)
    val Distraction = Color(0xFFFF5C7A)
    val TextPrimary = Color(0xFFF4F2FF)
    val TextSecondary = Color(0xFFB9B5D9)
    val TextTertiary = Color(0xFF8682AB)

    // Glass tokens
    val GlassFill = Color(0x14FFFFFF) // #FFFFFF at 8%
    val GlassHighlightTop = Color(0x1FFFFFFF) // White at 12% inner top highlight

    // 1dp gradient stroke: white 30% top-left to 4% bottom-right
    val GlassStroke = Brush.linearGradient(
        colors = listOf(
            Color(0x4DFFFFFF), // 30% white
            Color(0x0AFFFFFF)  // 4% white
        ),
        start = Offset(0f, 0f),
        end = Offset(Float.POSITIVE_INFINITY, Float.POSITIVE_INFINITY)
    )

    // Button Gradients
    val PrimaryButtonGradient = Brush.horizontalGradient(
        colors = listOf(AuroraViolet, AuroraCyan)
    )

    // --- SHAPES ---
    val HeroRadius = 32.dp
    val StandardRadius = 24.dp
    val InnerRadius = 16.dp

    val HeroShape = RoundedCornerShape(HeroRadius)
    val StandardShape = RoundedCornerShape(StandardRadius)
    val InnerShape = RoundedCornerShape(InnerRadius)
    val PillShape = CircleShape

    // --- SPACING ---
    val ScreenHorizontalPadding = 20.dp
    val PanelGap = 16.dp
    val PanelInnerPadding = 20.dp
    val HeroPanelInnerPadding = 24.dp
    val TitleContentGap = 12.dp
    val NumberLabelGap = 8.dp
    val MinTouchTarget = 48.dp
    val BottomBarHeight = 64.dp
    val BottomBarMargin = 16.dp
    val ContentBottomPadding = BottomBarHeight + 32.dp // 96.dp
    val FadeHeight = 24.dp
}
