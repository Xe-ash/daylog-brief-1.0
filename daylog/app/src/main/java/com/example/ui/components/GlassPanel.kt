package com.example.ui.components

import android.os.Build
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxScope
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.example.ui.theme.DaylogTheme
import com.example.ui.theme.DaylogTokens
import dev.chrisbanes.haze.*

enum class GlassPanelVariant {
    Hero,
    Standard,
    Subtle
}

@Composable
fun GlassPanel(
    variant: GlassPanelVariant = GlassPanelVariant.Standard,
    modifier: Modifier = Modifier,
    contentPadding: PaddingValues = when (variant) {
        GlassPanelVariant.Hero -> PaddingValues(DaylogTokens.HeroPanelInnerPadding)
        GlassPanelVariant.Standard -> PaddingValues(DaylogTokens.PanelInnerPadding)
        GlassPanelVariant.Subtle -> PaddingValues(16.dp)
    },
    content: @Composable BoxScope.() -> Unit
) {
    val hazeState = LocalHazeState.current
    val radius = when (variant) {
        GlassPanelVariant.Hero -> DaylogTokens.HeroRadius
        GlassPanelVariant.Standard -> DaylogTokens.StandardRadius
        GlassPanelVariant.Subtle -> DaylogTokens.InnerRadius
    }
    val shape = RoundedCornerShape(radius)

    val fillAlpha = when (variant) {
        GlassPanelVariant.Hero -> 0.10f
        GlassPanelVariant.Standard -> 0.08f
        GlassPanelVariant.Subtle -> 0.05f
    }
    val blurRadius = when (variant) {
        GlassPanelVariant.Hero -> 28.dp
        GlassPanelVariant.Standard -> 24.dp
        GlassPanelVariant.Subtle -> 0.dp
    }

    val supportsBlur = Build.VERSION.SDK_INT >= 31 && variant != GlassPanelVariant.Subtle

    var panelModifier = modifier.fillMaxWidth()

    if (variant == GlassPanelVariant.Hero) {
        panelModifier = panelModifier.shadow(
            elevation = 16.dp,
            shape = shape,
            ambientColor = DaylogTokens.AuroraViolet.copy(alpha = 0.12f),
            spotColor = DaylogTokens.AuroraViolet.copy(alpha = 0.20f)
        )
    }

    panelModifier = panelModifier.clip(shape)

    panelModifier = if (supportsBlur) {
        val hazeStyle = androidx.compose.runtime.remember(blurRadius) {
            HazeStyle(
                backgroundColor = DaylogTokens.Night,
                tint = HazeTint(DaylogTokens.Night.copy(alpha = 0.35f)),
                blurRadius = blurRadius
            )
        }
        HazeBridge.applyHazeEffect(panelModifier, hazeState, hazeStyle)
    } else {
        val baseTint = if (variant == GlassPanelVariant.Subtle) {
            DaylogTokens.Night.copy(alpha = 0.40f)
        } else {
            DaylogTokens.Night.copy(alpha = 0.70f)
        }
        panelModifier.background(baseTint)
    }

    panelModifier = panelModifier
        .background(Color.White.copy(alpha = fillAlpha))
        .border(BorderStroke(1.dp, DaylogTokens.GlassStroke), shape)
        .drawBehind {
            val rPx = radius.toPx()
            drawLine(
                color = DaylogTokens.GlassHighlightTop,
                start = Offset(rPx, 1f),
                end = Offset(size.width - rPx, 1f),
                strokeWidth = 1.dp.toPx()
            )
        }
        .padding(contentPadding)

    Box(
        modifier = panelModifier,
        content = content
    )
}

@Preview(name = "GlassPanel Preview", widthDp = 360)
@Composable
fun GlassPanelPreview() {
    DaylogTheme {
        GlassPanel(variant = GlassPanelVariant.Standard) {
            Text("Aurora Glass Standard Panel", color = DaylogTokens.TextPrimary)
        }
    }
}
