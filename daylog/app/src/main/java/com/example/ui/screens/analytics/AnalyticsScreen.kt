package com.example.ui.screens.analytics

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Assessment
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.AnalyticsRange
import com.example.ui.DaylogViewModel
import com.example.ui.components.EmptyState
import com.example.ui.components.GlassPanel
import com.example.ui.components.GlassPanelVariant
import com.example.ui.components.ScreenScaffold
import com.example.ui.components.SegmentedPill
import com.example.ui.components.StatBlock
import com.example.ui.theme.DaylogTheme
import com.example.ui.theme.DaylogTokens
import com.example.ui.theme.DaylogTypography
import com.example.util.DateTimeUtils
import java.text.SimpleDateFormat
import java.util.Locale
import kotlin.math.roundToInt

@Composable
fun AnalyticsScreen(
    viewModel: DaylogViewModel,
    modifier: Modifier = Modifier
) {
    val analyticsRange by viewModel.analyticsRange.collectAsState()
    val state by viewModel.analyticsState.collectAsState()

    val rangeList = listOf(
        AnalyticsRange.SEVEN_DAYS,
        AnalyticsRange.THIRTY_DAYS,
        AnalyticsRange.NINETY_DAYS,
        AnalyticsRange.ALL_TIME
    )

    val hasData = state.totalTrackedHours > 0.05

    ScreenScaffold(
        title = "Analytics",
        modifier = modifier
    ) {
        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(
                start = DaylogTokens.ScreenHorizontalPadding,
                end = DaylogTokens.ScreenHorizontalPadding,
                top = 8.dp,
                bottom = DaylogTokens.ContentBottomPadding
            ),
            verticalArrangement = Arrangement.spacedBy(DaylogTokens.PanelGap)
        ) {
            // Range selector: [ 7 days | 30 days | 90 days | All ]
            item {
                SegmentedPill(
                    items = rangeList,
                    selectedItem = analyticsRange,
                    onItemSelected = { viewModel.setAnalyticsRange(it) },
                    itemLabel = {
                        when (it) {
                            AnalyticsRange.SEVEN_DAYS -> "7 days"
                            AnalyticsRange.THIRTY_DAYS -> "30 days"
                            AnalyticsRange.NINETY_DAYS -> "90 days"
                            AnalyticsRange.ALL_TIME -> "All"
                        }
                    }
                )
            }

            if (!hasData) {
                // Empty state (no data in the period):
                // Replace stats, curve and bars with one EmptyState
                item {
                    val rangeText = if (analyticsRange == AnalyticsRange.ALL_TIME) "this period" else "the last ${analyticsRange.days} days"
                    EmptyState(
                        title = "No focus logged in $rangeText",
                        body = "Start a block on Today and your week will show up here.",
                        actionLabel = "Go to Today",
                        icon = Icons.Default.Assessment,
                        onAction = {
                            viewModel.setSelectedDate(DateTimeUtils.getTodayDateString())
                        },
                        modifier = Modifier.padding(top = 40.dp)
                    )
                }
            } else {
                // --- 1. HERO STATS PANEL (Untracked dropped; Productive / Neutral / Distraction trio) ---
                item {
                    GlassPanel(variant = GlassPanelVariant.Standard) {
                        Column(modifier = Modifier.fillMaxWidth()) {
                            val prodH = state.productiveHours.toInt()
                            val prodM = ((state.productiveHours - prodH) * 60).roundToInt()

                            val neutH = state.neutralHours.toInt()
                            val neutM = ((state.neutralHours - neutH) * 60).roundToInt()

                            val unprodH = state.unproductiveHours.toInt()
                            val unprodM = ((state.unproductiveHours - unprodH) * 60).roundToInt()

                            val avgH = state.averageProductiveHoursPerDay.toInt()
                            val avgM = ((state.averageProductiveHoursPerDay - avgH) * 60).roundToInt()

                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                StatBlock(
                                    value = "${prodH}h ${prodM}m",
                                    label = "Productive",
                                    statusColor = DaylogTokens.Productive,
                                    isNumeralCompact = true,
                                    modifier = Modifier.weight(1f)
                                )
                                StatBlock(
                                    value = "${neutH}h ${neutM}m",
                                    label = "Neutral",
                                    statusColor = DaylogTokens.Neutral,
                                    isNumeralCompact = true,
                                    modifier = Modifier.weight(1f)
                                )
                                StatBlock(
                                    value = "${unprodH}h ${unprodM}m",
                                    label = "Distraction",
                                    statusColor = DaylogTokens.Distraction,
                                    isNumeralCompact = true,
                                    modifier = Modifier.weight(1f)
                                )
                            }

                            Spacer(modifier = Modifier.height(14.dp))

                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(1.dp)
                                    .background(DaylogTokens.GlassHighlightTop)
                            )

                            Spacer(modifier = Modifier.height(12.dp))

                            Text(
                                text = "Daily average: ${avgH}h ${avgM}m productive",
                                style = DaylogTypography.Label,
                                color = DaylogTokens.TextSecondary
                            )
                        }
                    }
                }

                // --- 2. PRODUCTIVE HOURS LINE CHART ---
                item {
                    GlassPanel(variant = GlassPanelVariant.Standard) {
                        Column(modifier = Modifier.fillMaxWidth()) {
                            Text(
                                text = "Productive hours",
                                style = DaylogTypography.Title,
                                color = DaylogTokens.TextPrimary
                            )
                            Spacer(modifier = Modifier.height(DaylogTokens.TitleContentGap))

                            val daysData = state.dayDistributionBars.takeLast(7)
                            val values = daysData.map { (it.productiveSeconds / 3600f) }
                            val maxVal = (values.maxOrNull() ?: 1f).coerceAtLeast(4f)

                            var selectedIndex by remember { mutableStateOf<Int?>(null) }

                            if (selectedIndex != null && selectedIndex!! in daysData.indices) {
                                val item = daysData[selectedIndex!!]
                                val hrs = item.productiveSeconds / 3600f
                                Text(
                                    text = "${item.date}: ${String.format(Locale.US, "%.1f", hrs)}h productive",
                                    style = DaylogTypography.Label.copy(
                                        color = DaylogTokens.AuroraCyan,
                                        fontWeight = FontWeight.Bold
                                    )
                                )
                                Spacer(modifier = Modifier.height(6.dp))
                            }

                            // Smooth Line Chart Canvas
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(160.dp)
                            ) {
                                Canvas(
                                    modifier = Modifier
                                        .fillMaxSize()
                                        .pointerInput(daysData) {
                                            detectTapGestures { offset ->
                                                val colWidth = size.width / daysData.size.coerceAtLeast(1)
                                                val tappedIdx = (offset.x / colWidth).toInt().coerceIn(0, daysData.size - 1)
                                                selectedIndex = tappedIdx
                                            }
                                        }
                                ) {
                                    val w = size.width
                                    val h = size.height - 30f // Leave room for baseline and labels
                                    val n = daysData.size
                                    if (n < 2) return@Canvas

                                    val points = values.mapIndexed { idx, v ->
                                        val x = (idx.toFloat() / (n - 1)) * (w - 20f) + 10f
                                        val y = h - (v / maxVal) * (h - 20f) - 10f
                                        Offset(x, y)
                                    }

                                    // Cubic smoothing path
                                    val linePath = Path()
                                    val fillPath = Path()

                                    linePath.moveTo(points.first().x, points.first().y)
                                    fillPath.moveTo(points.first().x, h)
                                    fillPath.lineTo(points.first().x, points.first().y)

                                    for (i in 0 until points.size - 1) {
                                        val p0 = points[i]
                                        val p1 = points[i + 1]
                                        val controlX = (p0.x + p1.x) / 2f
                                        linePath.cubicTo(controlX, p0.y, controlX, p1.y, p1.x, p1.y)
                                        fillPath.cubicTo(controlX, p0.y, controlX, p1.y, p1.x, p1.y)
                                    }

                                    fillPath.lineTo(points.last().x, h)
                                    fillPath.close()

                                    // Gradient fill below
                                    drawPath(
                                        path = fillPath,
                                        brush = Brush.verticalGradient(
                                            colors = listOf(
                                                DaylogTokens.AuroraViolet.copy(alpha = 0.30f),
                                                Color.Transparent
                                            ),
                                            startY = 0f,
                                            endY = h
                                        )
                                    )

                                    // Line stroke (Violet-to-Cyan gradient)
                                    drawPath(
                                        path = linePath,
                                        brush = Brush.horizontalGradient(
                                            listOf(DaylogTokens.AuroraViolet, DaylogTokens.AuroraCyan)
                                        ),
                                        style = Stroke(width = 3.dp.toPx(), cap = StrokeCap.Round)
                                    )

                                    // Dot on each point ONLY for the last point
                                    val lastPt = points.last()
                                    drawCircle(
                                        color = DaylogTokens.AuroraCyan,
                                        radius = 5.dp.toPx(),
                                        center = lastPt
                                    )
                                    drawCircle(
                                        color = Color.White,
                                        radius = 2.dp.toPx(),
                                        center = lastPt
                                    )

                                    // If tapped, draw indicator bubble on that point
                                    if (selectedIndex != null && selectedIndex!! in points.indices) {
                                        val selPt = points[selectedIndex!!]
                                        drawCircle(
                                            color = Color.White,
                                            radius = 6.dp.toPx(),
                                            center = selPt
                                        )
                                    }
                                }
                            }

                            // X-axis weekday initials
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                daysData.forEach { day ->
                                    val initial = try {
                                        val d = SimpleDateFormat("yyyy-MM-dd", Locale.US).parse(day.date) ?: java.util.Date()
                                        SimpleDateFormat("E", Locale.US).format(d).take(1)
                                    } catch (e: Exception) {
                                        "•"
                                    }
                                    Text(
                                        text = initial,
                                        style = DaylogTypography.Label.copy(
                                            fontSize = 11.sp,
                                            color = DaylogTokens.TextTertiary,
                                            fontWeight = FontWeight.Bold
                                        ),
                                        modifier = Modifier.width(28.dp),
                                        textAlign = TextAlign.Center
                                    )
                                }
                            }
                        }
                    }
                }

                // --- 3. BY DAY (STACKED BARS) ---
                item {
                    GlassPanel(variant = GlassPanelVariant.Standard) {
                        Column(modifier = Modifier.fillMaxWidth()) {
                            Text(
                                text = "By day",
                                style = DaylogTypography.Title,
                                color = DaylogTokens.TextPrimary
                            )
                            Spacer(modifier = Modifier.height(DaylogTokens.TitleContentGap))

                            val daysData = state.dayDistributionBars.takeLast(7)
                            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                                daysData.forEach { day ->
                                    val dayName = try {
                                        val d = SimpleDateFormat("yyyy-MM-dd", Locale.US).parse(day.date) ?: java.util.Date()
                                        SimpleDateFormat("EEE", Locale.US).format(d)
                                    } catch (e: Exception) {
                                        day.date
                                    }

                                    val totalTrackedSec = day.totalTrackedSeconds.coerceAtLeast(1)
                                    val prodRatio = day.productiveSeconds.toFloat() / totalTrackedSec
                                    val neutRatio = day.neutralSeconds.toFloat() / totalTrackedSec
                                    val unprodRatio = day.unproductiveSeconds.toFloat() / totalTrackedSec

                                    val hoursDisplay = String.format(Locale.US, "%.1fh", day.productiveSeconds / 3600f)

                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Text(
                                            text = dayName,
                                            style = DaylogTypography.Label,
                                            color = DaylogTokens.TextSecondary,
                                            modifier = Modifier.width(36.dp)
                                        )

                                        Spacer(modifier = Modifier.width(8.dp))

                                        // Stacked horizontal bar
                                        Box(
                                            modifier = Modifier
                                                .weight(1f)
                                                .height(10.dp)
                                                .clip(CircleShape)
                                                .background(Color.White.copy(alpha = 0.06f))
                                        ) {
                                            Row(modifier = Modifier.fillMaxSize()) {
                                                if (day.productiveSeconds > 0) {
                                                    Box(
                                                        modifier = Modifier
                                                            .weight(prodRatio.coerceAtLeast(0.01f))
                                                            .fillMaxSize()
                                                            .background(DaylogTokens.Productive)
                                                    )
                                                }
                                                if (day.neutralSeconds > 0) {
                                                    Box(
                                                        modifier = Modifier
                                                            .weight(neutRatio.coerceAtLeast(0.01f))
                                                            .fillMaxSize()
                                                            .background(DaylogTokens.Neutral)
                                                    )
                                                }
                                                if (day.unproductiveSeconds > 0) {
                                                    Box(
                                                        modifier = Modifier
                                                            .weight(unprodRatio.coerceAtLeast(0.01f))
                                                            .fillMaxSize()
                                                            .background(DaylogTokens.Distraction)
                                                    )
                                                }
                                            }
                                        }

                                        Spacer(modifier = Modifier.width(12.dp))

                                        Text(
                                            text = hoursDisplay,
                                            style = DaylogTypography.Label.copy(
                                                fontWeight = FontWeight.Bold,
                                                color = DaylogTokens.TextPrimary
                                            ),
                                            modifier = Modifier.width(36.dp),
                                            textAlign = TextAlign.End
                                        )
                                    }
                                }
                            }
                        }
                    }
                }

                // --- 4. BY CATEGORY (DONUT + LEGEND) ---
                if (state.categoryBreakdown.isNotEmpty()) {
                    item {
                        GlassPanel(variant = GlassPanelVariant.Standard) {
                            Column(modifier = Modifier.fillMaxWidth()) {
                                Text(
                                    text = "By category",
                                    style = DaylogTypography.Title,
                                    color = DaylogTokens.TextPrimary
                                )
                                Spacer(modifier = Modifier.height(DaylogTokens.TitleContentGap))

                                val totalSec = state.categoryBreakdown.sumOf { it.second }.coerceAtLeast(1L)
                                val colors = listOf(
                                    DaylogTokens.Productive,
                                    DaylogTokens.AuroraCyan,
                                    DaylogTokens.AuroraViolet,
                                    DaylogTokens.Neutral,
                                    DaylogTokens.AuroraMagenta,
                                    DaylogTokens.Distraction
                                )

                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    // Donut Canvas
                                    Box(
                                        modifier = Modifier.size(100.dp),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        Canvas(modifier = Modifier.fillMaxSize()) {
                                            val strokeWidth = 14.dp.toPx()
                                            val arcSize = Size(size.width - strokeWidth, size.height - strokeWidth)
                                            val topLeft = Offset(strokeWidth / 2f, strokeWidth / 2f)

                                            var startAngle = -90f
                                            state.categoryBreakdown.take(6).forEachIndexed { idx, (_, dur) ->
                                                val sweep = (dur.toFloat() / totalSec) * 360f
                                                if (sweep > 0f) {
                                                    drawArc(
                                                        color = colors[idx % colors.size],
                                                        startAngle = startAngle,
                                                        sweepAngle = sweep - 2f, // Subtle slice gap
                                                        useCenter = false,
                                                        topLeft = topLeft,
                                                        size = arcSize,
                                                        style = Stroke(width = strokeWidth, cap = StrokeCap.Round)
                                                    )
                                                    startAngle += sweep
                                                }
                                            }
                                        }
                                    }

                                    Spacer(modifier = Modifier.width(20.dp))

                                    // Legend list
                                    Column(
                                        modifier = Modifier.weight(1f),
                                        verticalArrangement = Arrangement.spacedBy(6.dp)
                                    ) {
                                        state.categoryBreakdown.take(5).forEachIndexed { idx, (cat, dur) ->
                                            val col = colors[idx % colors.size]
                                            val hours = String.format(Locale.US, "%.1fh", dur / 3600f)

                                            Row(
                                                modifier = Modifier.fillMaxWidth(),
                                                horizontalArrangement = Arrangement.SpaceBetween,
                                                verticalAlignment = Alignment.CenterVertically
                                            ) {
                                                Row(verticalAlignment = Alignment.CenterVertically) {
                                                    Box(
                                                        modifier = Modifier
                                                            .size(8.dp)
                                                            .clip(CircleShape)
                                                            .background(col)
                                                    )
                                                    Spacer(modifier = Modifier.width(8.dp))
                                                    Text(
                                                        text = cat.lowercase().replaceFirstChar { it.uppercase() },
                                                        style = DaylogTypography.Label,
                                                        color = DaylogTokens.TextPrimary
                                                    )
                                                }

                                                Text(
                                                    text = hours,
                                                    style = DaylogTypography.Label.copy(
                                                        fontWeight = FontWeight.Bold,
                                                        color = DaylogTokens.TextSecondary
                                                    )
                                                )
                                            }
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}

@Preview(name = "AnalyticsScreen Preview")
@Composable
fun AnalyticsScreenPreview() {
    DaylogTheme {
        // AnalyticsScreen preview
    }
}
