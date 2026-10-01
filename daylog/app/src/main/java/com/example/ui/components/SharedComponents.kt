package com.example.ui.components

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Canvas
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
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.model.Activity
import com.example.model.ActivityClassification
import com.example.model.ActivityStatus
import com.example.ui.theme.DaylogTheme
import com.example.ui.theme.DaylogTokens
import com.example.ui.theme.DaylogTypography
import com.example.ui.theme.fluidDropletPress
import java.util.Calendar

// --- PRIMARY BUTTON ---
@Composable
fun PrimaryButton(
    text: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
    leadingIcon: ImageVector? = null
) {
    val interactionSource = remember { MutableInteractionSource() }

    Box(
        modifier = modifier
            .heightIn(min = DaylogTokens.MinTouchTarget)
            .fluidDropletPress(
                interactionSource = interactionSource,
                enabled = enabled,
                squashFactorY = 0.90f,
                stretchFactorX = 1.07f,
                onClick = onClick
            )
            .shadow(
                elevation = if (enabled) 14.dp else 0.dp,
                shape = CircleShape,
                ambientColor = DaylogTokens.AuroraViolet.copy(alpha = 0.40f),
                spotColor = DaylogTokens.AuroraCyan.copy(alpha = 0.45f)
            )
            .clip(CircleShape)
            .background(
                if (enabled) DaylogTokens.PrimaryButtonGradient
                else Brush.linearGradient(listOf(DaylogTokens.TextTertiary, DaylogTokens.TextTertiary))
            )
            .padding(horizontal = 24.dp, vertical = 12.dp),
        contentAlignment = Alignment.Center
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.Center
        ) {
            if (leadingIcon != null) {
                Icon(
                    imageVector = leadingIcon,
                    contentDescription = null,
                    tint = DaylogTokens.TextPrimary,
                    modifier = Modifier.size(18.dp)
                )
                Spacer(modifier = Modifier.width(8.dp))
            }
            Text(
                text = text,
                style = DaylogTypography.Label.copy(
                    fontSize = 14.sp,
                    lineHeight = 18.sp,
                    fontWeight = FontWeight.Bold
                ),
                color = DaylogTokens.TextPrimary
            )
        }
    }
}

// --- GLASS BUTTON ---
@Composable
fun GlassButton(
    text: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
    isDestructive: Boolean = false,
    leadingIcon: ImageVector? = null
) {
    val interactionSource = remember { MutableInteractionSource() }

    val textColor = when {
        !enabled -> DaylogTokens.TextTertiary
        isDestructive -> DaylogTokens.Distraction
        else -> DaylogTokens.TextPrimary
    }

    Box(
        modifier = modifier
            .heightIn(min = DaylogTokens.MinTouchTarget)
            .fluidDropletPress(
                interactionSource = interactionSource,
                enabled = enabled,
                squashFactorY = 0.91f,
                stretchFactorX = 1.06f,
                onClick = onClick
            )
            .clip(CircleShape)
            .background(DaylogTokens.GlassFill)
            .border(
                BorderStroke(
                    1.dp,
                    if (isDestructive) SolidColor(DaylogTokens.Distraction.copy(alpha = 0.5f)) else DaylogTokens.GlassStroke
                ),
                CircleShape
            )
            .padding(horizontal = 20.dp, vertical = 12.dp),
        contentAlignment = Alignment.Center
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.Center
        ) {
            if (leadingIcon != null) {
                Icon(
                    imageVector = leadingIcon,
                    contentDescription = null,
                    tint = textColor,
                    modifier = Modifier.size(18.dp)
                )
                Spacer(modifier = Modifier.width(8.dp))
            }
            Text(
                text = text,
                style = DaylogTypography.Label.copy(
                    fontSize = 14.sp,
                    lineHeight = 18.sp,
                    fontWeight = FontWeight.SemiBold
                ),
                color = textColor
            )
        }
    }
}

// --- SEGMENTED PILL (Period selectors / Filters) ---
@Composable
fun <T> SegmentedPill(
    items: List<T>,
    selectedItem: T,
    onItemSelected: (T) -> Unit,
    modifier: Modifier = Modifier,
    itemLabel: (T) -> String = { it.toString() }
) {
    BoxWithConstraints(
        modifier = modifier
            .fillMaxWidth()
            .height(44.dp)
            .clip(CircleShape)
            .background(DaylogTokens.GlassFill)
            .border(BorderStroke(1.dp, DaylogTokens.GlassStroke), CircleShape)
            .padding(4.dp)
    ) {
        val totalWidth = maxWidth
        val count = items.size.coerceAtLeast(1)
        val tabWidth = totalWidth / count
        val selectedIndex = items.indexOf(selectedItem).coerceAtLeast(0)

        val pillOffset by animateDpAsState(
            targetValue = tabWidth * selectedIndex,
            animationSpec = tween(durationMillis = 200),
            label = "segmented_pill_anim"
        )

        // Sliding selection pill
        Box(
            modifier = Modifier
                .offset(x = pillOffset)
                .width(tabWidth)
                .height(36.dp)
                .shadow(
                    elevation = 6.dp,
                    shape = CircleShape,
                    ambientColor = DaylogTokens.AuroraViolet.copy(alpha = 0.35f),
                    spotColor = DaylogTokens.AuroraViolet.copy(alpha = 0.45f)
                )
                .clip(CircleShape)
                .background(DaylogTokens.AuroraViolet.copy(alpha = 0.35f))
                .border(BorderStroke(1.dp, DaylogTokens.AuroraViolet.copy(alpha = 0.60f)), CircleShape)
        )

        Row(
            modifier = Modifier.fillMaxSize(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            items.forEach { item ->
                val isSelected = item == selectedItem
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .height(36.dp)
                        .clip(CircleShape)
                        .clickable { onItemSelected(item) },
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = itemLabel(item),
                        style = DaylogTypography.Label.copy(
                            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium
                        ),
                        color = if (isSelected) DaylogTokens.TextPrimary else DaylogTokens.TextSecondary,
                        maxLines = 1
                    )
                }
            }
        }
    }
}

// --- STAT BLOCK (Numeral over Label, 400ms crossfade) ---
@Composable
fun StatBlock(
    value: String,
    label: String,
    modifier: Modifier = Modifier,
    statusColor: Color? = null,
    isNumeralCompact: Boolean = false
) {
    Column(
        modifier = modifier,
        horizontalAlignment = Alignment.Start
    ) {
        AnimatedContent(
            targetState = value,
            transitionSpec = {
                fadeIn(animationSpec = tween(400)) togetherWith fadeOut(animationSpec = tween(400))
            },
            label = "stat_value_crossfade"
        ) { targetVal ->
            Text(
                text = targetVal,
                style = if (isNumeralCompact) DaylogTypography.Numeral.copy(fontSize = 28.sp, lineHeight = 32.sp)
                else DaylogTypography.Numeral,
                color = DaylogTokens.TextPrimary
            )
        }

        Spacer(modifier = Modifier.height(DaylogTokens.NumberLabelGap))

        Row(
            verticalAlignment = Alignment.CenterVertically
        ) {
            if (statusColor != null) {
                Box(
                    modifier = Modifier
                        .size(6.dp)
                        .clip(CircleShape)
                        .background(statusColor)
                )
                Spacer(modifier = Modifier.width(6.dp))
            }
            Text(
                text = label,
                style = DaylogTypography.Label,
                color = DaylogTokens.TextSecondary
            )
        }
    }
}

// --- PROGRESS RING (Mint to Cyan gradient stroke) ---
@Composable
fun ProgressRing(
    progressPercent: Float,
    modifier: Modifier = Modifier,
    strokeWidth: Dp = 10.dp,
    centerContent: @Composable () -> Unit = {}
) {
    val clampedProgress = progressPercent.coerceIn(0f, 100f) / 100f
    val sweepAngle = clampedProgress * 360f

    val gradientBrush = Brush.sweepGradient(
        colors = listOf(
            DaylogTokens.Productive,
            DaylogTokens.AuroraCyan,
            DaylogTokens.Productive
        )
    )

    Box(
        modifier = modifier
            .semantics { contentDescription = "Progress: ${(progressPercent).toInt()}% completed" },
        contentAlignment = Alignment.Center
    ) {
        Canvas(modifier = Modifier.fillMaxSize()) {
            val strokePx = strokeWidth.toPx()
            val arcSize = Size(size.width - strokePx, size.height - strokePx)
            val topLeft = Offset(strokePx / 2f, strokePx / 2f)

            // Background track
            drawArc(
                color = Color.White.copy(alpha = 0.08f),
                startAngle = 0f,
                sweepAngle = 360f,
                useCenter = false,
                topLeft = topLeft,
                size = arcSize,
                style = Stroke(width = strokePx, cap = StrokeCap.Round)
            )

            // Progress arc
            if (sweepAngle > 0f) {
                drawArc(
                    brush = gradientBrush,
                    startAngle = -90f,
                    sweepAngle = sweepAngle,
                    useCenter = false,
                    topLeft = topLeft,
                    size = arcSize,
                    style = Stroke(width = strokePx, cap = StrokeCap.Round)
                )
            }
        }

        centerContent()
    }
}

// --- SEGMENTED DAY BAR (Spans waking window 06:00 to 23:00) ---
@Composable
fun SegmentedDayBar(
    activities: List<Activity>,
    dateStr: String,
    modifier: Modifier = Modifier,
    barHeight: Dp = 14.dp
) {
    val wakeHour = 6
    val sleepHour = 23
    val totalWindowMinutes = (sleepHour - wakeHour) * 60 // 17h = 1020 mins

    val cal = Calendar.getInstance()
    val isToday = (activities.firstOrNull()?.date ?: "") == dateStr || true
    val currentHour = cal.get(Calendar.HOUR_OF_DAY)
    val currentMinute = cal.get(Calendar.MINUTE)
    val currentMinutesSinceMidnight = currentHour * 60 + currentMinute
    val currentOffsetMinutes = (currentMinutesSinceMidnight - (wakeHour * 60)).coerceIn(0, totalWindowMinutes)

    Box(
        modifier = modifier
            .fillMaxWidth()
            .height(barHeight)
            .clip(CircleShape)
            .background(Color.White.copy(alpha = 0.06f))
            .border(BorderStroke(1.dp, DaylogTokens.GlassStroke), CircleShape)
    ) {
        Canvas(modifier = Modifier.fillMaxSize()) {
            val w = size.width
            val h = size.height

            // Render activity segments
            activities.forEach { act ->
                val startCal = Calendar.getInstance().apply { timeInMillis = act.startedAt }
                val startMins = startCal.get(Calendar.HOUR_OF_DAY) * 60 + startCal.get(Calendar.MINUTE)
                val durationMins = (act.durationSeconds / 60).coerceAtLeast(1).toInt()
                val endMins = startMins + durationMins

                val segStartClamped = (startMins - wakeHour * 60).coerceIn(0, totalWindowMinutes)
                val segEndClamped = (endMins - wakeHour * 60).coerceIn(0, totalWindowMinutes)

                if (segEndClamped > segStartClamped) {
                    val startX = (segStartClamped.toFloat() / totalWindowMinutes.toFloat()) * w
                    val segW = ((segEndClamped - segStartClamped).toFloat() / totalWindowMinutes.toFloat()) * w

                    val segColor = when (act.classification) {
                        ActivityClassification.PRODUCTIVE -> DaylogTokens.Productive
                        ActivityClassification.NEUTRAL -> DaylogTokens.Neutral
                        ActivityClassification.UNPRODUCTIVE -> DaylogTokens.Distraction
                        else -> DaylogTokens.AuroraViolet
                    }

                    drawRoundRect(
                        color = segColor,
                        topLeft = Offset(startX, 0f),
                        size = Size(segW.coerceAtLeast(2f), h),
                        cornerRadius = CornerRadius(h / 2f, h / 2f)
                    )
                }
            }

            // Current time indicator marker
            val markerX = (currentOffsetMinutes.toFloat() / totalWindowMinutes.toFloat()) * w
            drawLine(
                color = Color.White.copy(alpha = 0.90f),
                start = Offset(markerX, 0f),
                end = Offset(markerX, h),
                strokeWidth = 2.dp.toPx()
            )
        }
    }
}

// --- EMPTY STATE ---
@Composable
fun EmptyState(
    title: String,
    body: String,
    modifier: Modifier = Modifier,
    icon: ImageVector? = null,
    actionLabel: String? = null,
    onAction: (() -> Unit)? = null
) {
    Column(
        modifier = modifier
            .fillMaxWidth()
            .padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        if (icon != null) {
            Box(
                modifier = Modifier
                    .size(56.dp)
                    .clip(CircleShape)
                    .background(DaylogTokens.GlassFill)
                    .border(BorderStroke(1.dp, DaylogTokens.GlassStroke), CircleShape),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = icon,
                    contentDescription = null,
                    tint = DaylogTokens.TextSecondary,
                    modifier = Modifier.size(28.dp)
                )
            }
            Spacer(modifier = Modifier.height(16.dp))
        }

        Text(
            text = title,
            style = DaylogTypography.Title,
            color = DaylogTokens.TextPrimary,
            textAlign = TextAlign.Center
        )

        Spacer(modifier = Modifier.height(8.dp))

        Text(
            text = body,
            style = DaylogTypography.Body,
            color = DaylogTokens.TextSecondary,
            textAlign = TextAlign.Center
        )

        if (actionLabel != null && onAction != null) {
            Spacer(modifier = Modifier.height(20.dp))
            PrimaryButton(
                text = actionLabel,
                onClick = onAction
            )
        }
    }
}

@Preview(name = "SharedComponents Preview")
@Composable
fun SharedComponentsPreview() {
    DaylogTheme {
        Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(16.dp)) {
            PrimaryButton(text = "Start activity", onClick = {})
            GlassButton(text = "Open day", onClick = {})
            StatBlock(value = "12h 40m", label = "Productive", statusColor = DaylogTokens.Productive)
        }
    }
}
