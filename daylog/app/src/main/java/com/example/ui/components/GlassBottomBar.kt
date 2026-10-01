package com.example.ui.components

import android.os.Build
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBars
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Assessment
import androidx.compose.material.icons.filled.CalendarMonth
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Schedule
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.outlined.Assessment
import androidx.compose.material.icons.outlined.CalendarMonth
import androidx.compose.material.icons.outlined.CheckCircle
import androidx.compose.material.icons.outlined.Schedule
import androidx.compose.material.icons.outlined.Settings
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.draw.scale
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.DaylogTab
import com.example.ui.theme.DaylogTheme
import com.example.ui.theme.DaylogTokens
import com.example.ui.theme.DaylogTypography
import dev.chrisbanes.haze.*

@Composable
fun GlassBottomBar(
    currentTab: DaylogTab,
    onTabSelected: (DaylogTab) -> Unit,
    modifier: Modifier = Modifier
) {
    val hazeState = LocalHazeState.current
    val supportsBlur = Build.VERSION.SDK_INT >= 31

    Box(
        modifier = modifier
            .fillMaxWidth()
            .windowInsetsPadding(WindowInsets.navigationBars)
            .padding(
                start = DaylogTokens.ScreenHorizontalPadding,
                end = DaylogTokens.ScreenHorizontalPadding,
                bottom = DaylogTokens.BottomBarMargin
            ),
        contentAlignment = Alignment.Center
    ) {
        // Floating 64dp capsule with Haze blur like Standard panel
        BoxWithConstraints(
            modifier = Modifier
                .fillMaxWidth()
                .height(DaylogTokens.BottomBarHeight)
                .shadow(
                    elevation = 16.dp,
                    shape = CircleShape,
                    ambientColor = DaylogTokens.AuroraViolet.copy(alpha = 0.15f),
                    spotColor = DaylogTokens.Night
                )
                .clip(CircleShape)
                .then(
                    if (supportsBlur) {
                        HazeBridge.applyHazeEffect(
                            Modifier,
                            hazeState,
                            HazeStyle(
                                backgroundColor = DaylogTokens.Night,
                                tint = HazeTint(DaylogTokens.Night.copy(alpha = 0.35f)),
                                blurRadius = 24.dp
                            )
                        )
                    } else {
                        Modifier.background(DaylogTokens.Night.copy(alpha = 0.70f))
                    }
                )
                .background(Color.White.copy(alpha = 0.08f))
                .border(BorderStroke(1.dp, DaylogTokens.GlassStroke), CircleShape)
                .drawBehind {
                    // 1dp inner top highlight line at white 12%
                    val rPx = size.height / 2f
                    drawLine(
                        color = DaylogTokens.GlassHighlightTop,
                        start = Offset(rPx, 1f),
                        end = Offset(size.width - rPx, 1f),
                        strokeWidth = 1.dp.toPx()
                    )
                }
                .padding(horizontal = 6.dp)
        ) {
            val totalWidth = maxWidth
            val tabCount = DaylogTab.entries.size
            val tabWidth = totalWidth / tabCount
            val selectedIndex = DaylogTab.entries.indexOf(currentTab)

            // Sliding selection pill (200ms) with faint Violet glow
            val pillOffset by animateDpAsState(
                targetValue = tabWidth * selectedIndex,
                animationSpec = tween(durationMillis = 200),
                label = "bottom_nav_pill"
            )

            Box(
                modifier = Modifier
                    .offset(x = pillOffset)
                    .align(Alignment.CenterStart)
                    .width(tabWidth)
                    .height(48.dp)
                    .padding(horizontal = 4.dp)
                    .shadow(
                        elevation = 8.dp,
                        shape = CircleShape,
                        ambientColor = DaylogTokens.AuroraViolet.copy(alpha = 0.30f),
                        spotColor = DaylogTokens.AuroraViolet.copy(alpha = 0.40f)
                    )
                    .clip(CircleShape)
                    .background(DaylogTokens.AuroraViolet.copy(alpha = 0.22f))
                    .border(BorderStroke(1.dp, DaylogTokens.AuroraViolet.copy(alpha = 0.50f)), CircleShape)
            )

            Row(
                modifier = Modifier.fillMaxSize(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                DaylogTab.entries.forEach { tab ->
                    val isSelected = tab == currentTab
                    val interactionSource = remember { MutableInteractionSource() }
                    val isPressed by interactionSource.collectIsPressedAsState()

                    // Press scale 0.97, 100ms
                    val pressScale by animateFloatAsState(
                        targetValue = if (isPressed) 0.97f else 1.0f,
                        animationSpec = tween(durationMillis = 100),
                        label = "tab_press"
                    )

                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .height(64.dp) // Minimum 48dp touch target
                            .scale(pressScale)
                            .clickable(
                                interactionSource = interactionSource,
                                indication = null,
                                onClick = { onTabSelected(tab) }
                            ),
                        contentAlignment = Alignment.Center
                    ) {
                        Column(
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.Center
                        ) {
                            Icon(
                                imageVector = if (isSelected) tab.selectedIcon else tab.unselectedIcon,
                                contentDescription = tab.label,
                                modifier = Modifier.size(20.dp),
                                tint = if (isSelected) DaylogTokens.TextPrimary else DaylogTokens.TextSecondary
                            )
                            Spacer(modifier = Modifier.height(2.dp))
                            Text(
                                text = tab.label,
                                style = DaylogTypography.Label.copy(
                                    fontSize = 11.sp,
                                    lineHeight = 14.sp,
                                    color = if (isSelected) DaylogTokens.TextPrimary else DaylogTokens.TextSecondary
                                ),
                                maxLines = 1,
                                softWrap = false
                            )
                        }
                    }
                }
            }
        }
    }
}

@Preview(name = "GlassBottomBar Preview", widthDp = 360)
@Composable
fun GlassBottomBarPreview() {
    DaylogTheme {
        GlassBottomBar(
            currentTab = DaylogTab.TODAY,
            onTabSelected = {}
        )
    }
}
