package com.example.ui.screens.history

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
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
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.model.ActivityClassification
import com.example.ui.DaylogViewModel
import com.example.ui.components.EmptyState
import com.example.ui.components.GlassButton
import com.example.ui.components.GlassPanel
import com.example.ui.components.GlassPanelVariant
import com.example.ui.components.ScreenScaffold
import com.example.ui.components.SegmentedPill
import com.example.ui.components.StatBlock
import com.example.ui.dialogs.ManualActivityDialog
import com.example.ui.theme.DaylogTheme
import com.example.ui.theme.DaylogTokens
import com.example.ui.theme.DaylogTypography
import com.example.util.DateTimeUtils
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale

@Composable
fun HistoryScreen(
    viewModel: DaylogViewModel,
    onDateSelectedForView: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    val activities by viewModel.activities.collectAsState()
    val dailyGoals by viewModel.dailyGoals.collectAsState()
    val selectedDate by viewModel.selectedDate.collectAsState()
    val userProfile by viewModel.userProfile.collectAsState()

    var selectedMode by remember { mutableStateOf("Calendar") }
    var showManualEntryDialog by remember { mutableStateOf(false) }

    val todayStr = remember { DateTimeUtils.getTodayDateString() }
    val sdf = SimpleDateFormat("yyyy-MM-dd", Locale.US)

    // Calendar state: month offset from current month
    var monthOffset by remember { mutableIntStateOf(0) }
    val cal = remember(monthOffset) {
        Calendar.getInstance().apply {
            add(Calendar.MONTH, monthOffset)
            set(Calendar.DAY_OF_MONTH, 1)
        }
    }

    val currentMonthName = remember(cal) {
        SimpleDateFormat("MMMM yyyy", Locale.US).format(cal.time)
    }

    val daysInMonth = cal.getActualMaximum(Calendar.DAY_OF_MONTH)
    val firstDayOfWeek = cal.get(Calendar.DAY_OF_WEEK) - 1 // Sunday=0

    // Selected day's activities & metrics
    val selectedDayActivities = activities.filter { it.date == selectedDate }.sortedBy { it.startedAt }
    val prodSec = selectedDayActivities.filter { it.classification == ActivityClassification.PRODUCTIVE }.sumOf { it.durationSeconds }
    val unprodSec = selectedDayActivities.filter { it.classification == ActivityClassification.UNPRODUCTIVE }.sumOf { it.durationSeconds }
    val goalMinutes = dailyGoals[selectedDate]?.targetMinutes ?: userProfile.defaultDailyGoalMinutes
    val goalPercent = if (goalMinutes > 0) ((prodSec / 60.0) / goalMinutes * 100.0).toInt() else 0

    val selectedDateHeader = remember(selectedDate) {
        try {
            val d = sdf.parse(selectedDate) ?: Date()
            SimpleDateFormat("EEEE, d MMMM", Locale.US).format(d)
        } catch (e: Exception) {
            selectedDate
        }
    }

    ScreenScaffold(
        title = "History",
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
            // Mode selector: SegmentedPill [ Calendar | Ledger ]
            item {
                SegmentedPill(
                    items = listOf("Calendar", "Ledger"),
                    selectedItem = selectedMode,
                    onItemSelected = { selectedMode = it },
                    itemLabel = { it }
                )
            }

            if (selectedMode == "Calendar") {
                // --- CALENDAR PANEL ---
                item {
                    GlassPanel(variant = GlassPanelVariant.Standard) {
                        Column(modifier = Modifier.fillMaxWidth()) {
                            // Month Header
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    text = currentMonthName,
                                    style = DaylogTypography.Title,
                                    color = DaylogTokens.TextPrimary
                                )

                                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                    Text(
                                        text = "◀",
                                        style = DaylogTypography.Label.copy(fontSize = 14.sp),
                                        color = DaylogTokens.TextSecondary,
                                        modifier = Modifier
                                            .clip(CircleShape)
                                            .clickable { monthOffset-- }
                                            .padding(8.dp)
                                    )
                                    Text(
                                        text = "▶",
                                        style = DaylogTypography.Label.copy(fontSize = 14.sp),
                                        color = DaylogTokens.TextSecondary,
                                        modifier = Modifier
                                            .clip(CircleShape)
                                            .clickable { monthOffset++ }
                                            .padding(8.dp)
                                    )
                                }
                            }

                            Spacer(modifier = Modifier.height(12.dp))

                            // Day of week header initials
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceAround
                            ) {
                                listOf("S", "M", "T", "W", "T", "F", "S").forEach { dayInitial ->
                                    Text(
                                        text = dayInitial,
                                        style = DaylogTypography.Label.copy(
                                            fontSize = 11.sp,
                                            fontWeight = FontWeight.Bold,
                                            color = DaylogTokens.TextTertiary
                                        ),
                                        modifier = Modifier.width(36.dp),
                                        textAlign = TextAlign.Center
                                    )
                                }
                            }

                            Spacer(modifier = Modifier.height(8.dp))

                            // Calendar days grid
                            val totalSlots = ((daysInMonth + firstDayOfWeek + 6) / 7) * 7
                            val weeks = totalSlots / 7

                            for (w in 0 until weeks) {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceAround
                                ) {
                                    for (d in 0 until 7) {
                                        val dayNum = w * 7 + d - firstDayOfWeek + 1
                                        if (dayNum in 1..daysInMonth) {
                                            val dayCal = Calendar.getInstance().apply {
                                                time = cal.time
                                                set(Calendar.DAY_OF_MONTH, dayNum)
                                            }
                                            val dateKey = sdf.format(dayCal.time)
                                            val isSelected = dateKey == selectedDate
                                            val isToday = dateKey == todayStr

                                            // Score calculation for cell dot
                                            val dayActs = activities.filter { it.date == dateKey }
                                            val dayProd = dayActs.filter { it.classification == ActivityClassification.PRODUCTIVE }.sumOf { it.durationSeconds }
                                            val targetM = dailyGoals[dateKey]?.targetMinutes ?: userProfile.defaultDailyGoalMinutes
                                            val dayScorePercent = if (targetM > 0) ((dayProd / 60.0) / targetM * 100.0).toInt() else 0

                                            val scoreDotColor = when {
                                                dayActs.isEmpty() -> null
                                                dayScorePercent >= 100 -> DaylogTokens.Productive
                                                dayScorePercent >= 50 -> DaylogTokens.AuroraViolet
                                                else -> DaylogTokens.Distraction
                                            }

                                            Box(
                                                modifier = Modifier
                                                    .size(44.dp) // Accessibility touch target
                                                    .clip(CircleShape)
                                                    .then(
                                                        if (isSelected) {
                                                            Modifier
                                                                .shadow(6.dp, CircleShape, ambientColor = DaylogTokens.AuroraViolet.copy(alpha = 0.4f), spotColor = DaylogTokens.AuroraViolet)
                                                                .background(DaylogTokens.AuroraViolet)
                                                        } else if (isToday) {
                                                            Modifier.border(BorderStroke(1.5.dp, Color.White.copy(alpha = 0.70f)), CircleShape)
                                                        } else {
                                                            Modifier
                                                        }
                                                    )
                                                    .clickable { viewModel.setSelectedDate(dateKey) },
                                                contentAlignment = Alignment.Center
                                            ) {
                                                Column(
                                                    horizontalAlignment = Alignment.CenterHorizontally,
                                                    verticalArrangement = Arrangement.Center
                                                ) {
                                                    Text(
                                                        text = "$dayNum",
                                                        style = DaylogTypography.Label.copy(
                                                            fontSize = 12.sp,
                                                            fontWeight = if (isSelected || isToday) FontWeight.Bold else FontWeight.Normal,
                                                            color = if (isSelected) Color.White else DaylogTokens.TextPrimary
                                                        )
                                                    )

                                                    if (scoreDotColor != null) {
                                                        Spacer(modifier = Modifier.height(2.dp))
                                                        Box(
                                                            modifier = Modifier
                                                                .size(4.dp)
                                                                .clip(CircleShape)
                                                                .background(if (isSelected) Color.White else scoreDotColor)
                                                        )
                                                    }
                                                }
                                            }
                                        } else {
                                            Spacer(modifier = Modifier.size(44.dp))
                                        }
                                    }
                                }
                            }
                        }
                    }
                }

                // --- SELECTED DAY PANEL ---
                item {
                    GlassPanel(variant = GlassPanelVariant.Standard) {
                        Column(modifier = Modifier.fillMaxWidth()) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    text = selectedDateHeader,
                                    style = DaylogTypography.Title,
                                    color = DaylogTokens.TextPrimary
                                )

                                GlassButton(
                                    text = "Open day",
                                    onClick = { onDateSelectedForView(selectedDate) }
                                )
                            }

                            Spacer(modifier = Modifier.height(16.dp))

                            // Productive / Distraction / Goal stats as StatBlocks
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                StatBlock(
                                    value = "${prodSec / 3600}h ${(prodSec % 3600) / 60}m",
                                    label = "Productive",
                                    statusColor = DaylogTokens.Productive,
                                    isNumeralCompact = true,
                                    modifier = Modifier.weight(1f)
                                )
                                StatBlock(
                                    value = "${unprodSec / 3600}h ${(unprodSec % 3600) / 60}m",
                                    label = "Distraction",
                                    statusColor = DaylogTokens.Distraction,
                                    isNumeralCompact = true,
                                    modifier = Modifier.weight(1f)
                                )
                                StatBlock(
                                    value = "$goalPercent%",
                                    label = "Goal target",
                                    statusColor = DaylogTokens.AuroraCyan,
                                    isNumeralCompact = true,
                                    modifier = Modifier.weight(1f)
                                )
                            }

                            Spacer(modifier = Modifier.height(16.dp))

                            // Day's blocks as Subtle rows or EmptyState
                            if (selectedDayActivities.isEmpty()) {
                                EmptyState(
                                    title = "Nothing logged on this day",
                                    body = "No activities recorded for this date.",
                                    actionLabel = "Log past activity",
                                    onAction = { showManualEntryDialog = true }
                                )
                            } else {
                                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                                    selectedDayActivities.forEach { act ->
                                        val catColor = when (act.classification) {
                                            ActivityClassification.PRODUCTIVE -> DaylogTokens.Productive
                                            ActivityClassification.NEUTRAL -> DaylogTokens.Neutral
                                            ActivityClassification.UNPRODUCTIVE -> DaylogTokens.Distraction
                                            else -> DaylogTokens.AuroraCyan
                                        }

                                        Row(
                                            modifier = Modifier
                                                .fillMaxWidth()
                                                .clip(RoundedCornerShape(DaylogTokens.InnerRadius))
                                                .background(DaylogTokens.GlassFill)
                                                .border(BorderStroke(1.dp, DaylogTokens.GlassStroke), RoundedCornerShape(DaylogTokens.InnerRadius))
                                                .padding(horizontal = 14.dp, vertical = 10.dp),
                                            horizontalArrangement = Arrangement.SpaceBetween,
                                            verticalAlignment = Alignment.CenterVertically
                                        ) {
                                            Row(verticalAlignment = Alignment.CenterVertically) {
                                                Box(
                                                    modifier = Modifier
                                                        .size(6.dp)
                                                        .clip(CircleShape)
                                                        .background(catColor)
                                                )
                                                Spacer(modifier = Modifier.width(10.dp))
                                                Column {
                                                    Text(
                                                        text = act.name,
                                                        style = DaylogTypography.Body.copy(fontWeight = FontWeight.Medium, color = DaylogTokens.TextPrimary)
                                                    )
                                                    Text(
                                                        text = "${DateTimeUtils.formatTime(act.startedAt)} — ${act.endedAt?.let { DateTimeUtils.formatTime(it) } ?: "Running"}",
                                                        style = DaylogTypography.Label.copy(fontSize = 11.sp, color = DaylogTokens.TextTertiary)
                                                    )
                                                }
                                            }

                                            Text(
                                                text = "${act.durationSeconds / 60}m",
                                                style = DaylogTypography.Label.copy(fontWeight = FontWeight.Bold, color = DaylogTokens.TextPrimary)
                                            )
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            } else {
                // --- LEDGER MODE ---
                item {
                    val recentDays = remember(activities) {
                        (0 until 14).map { offset ->
                            DateTimeUtils.getDateStringWithOffset(-offset)
                        }
                    }

                    GlassPanel(
                        variant = GlassPanelVariant.Standard,
                        contentPadding = PaddingValues(0.dp)
                    ) {
                        Column(modifier = Modifier.fillMaxWidth()) {
                            recentDays.forEachIndexed { idx, dateKey ->
                                val dayActs = activities.filter { it.date == dateKey }
                                val dayProd = dayActs.filter { it.classification == ActivityClassification.PRODUCTIVE }.sumOf { it.durationSeconds }
                                val targetM = dailyGoals[dateKey]?.targetMinutes ?: userProfile.defaultDailyGoalMinutes
                                val scorePercent = if (targetM > 0) ((dayProd / 60.0) / targetM * 100.0).toInt() else 0

                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .clickable {
                                            viewModel.setSelectedDate(dateKey)
                                            selectedMode = "Calendar"
                                        }
                                        .padding(horizontal = 20.dp, vertical = 14.dp),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Column {
                                        Text(
                                            text = try {
                                                val d = sdf.parse(dateKey) ?: Date()
                                                SimpleDateFormat("EEE, d MMM", Locale.US).format(d)
                                            } catch (e: Exception) {
                                                dateKey
                                            },
                                            style = DaylogTypography.Body.copy(
                                                fontWeight = FontWeight.Medium,
                                                color = DaylogTokens.TextPrimary
                                            )
                                        )
                                        Text(
                                            text = "${dayActs.size} blocks logged",
                                            style = DaylogTypography.Label.copy(fontSize = 11.sp, color = DaylogTokens.TextTertiary)
                                        )
                                    }

                                    Column(horizontalAlignment = Alignment.End) {
                                        Text(
                                            text = "${dayProd / 3600}h ${(dayProd % 3600) / 60}m",
                                            style = DaylogTypography.Label.copy(
                                                fontWeight = FontWeight.Bold,
                                                color = DaylogTokens.Productive
                                            )
                                        )
                                        Text(
                                            text = "$scorePercent% target",
                                            style = DaylogTypography.Label.copy(fontSize = 11.sp, color = DaylogTokens.TextSecondary)
                                        )
                                    }
                                }

                                if (idx < recentDays.size - 1) {
                                    Box(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .padding(horizontal = 20.dp)
                                            .height(1.dp)
                                            .background(DaylogTokens.GlassHighlightTop)
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }
    }

    if (showManualEntryDialog) {
        ManualActivityDialog(
            onDismiss = { showManualEntryDialog = false },
            onSave = { name, cat, classification, start, end, rating, note ->
                viewModel.addManualActivity(
                    name = name,
                    category = cat,
                    classification = classification,
                    startMillis = start,
                    endMillis = end,
                    qualityRating = rating,
                    note = note
                )
                showManualEntryDialog = false
            }
        )
    }
}

@Preview(name = "HistoryScreen Preview")
@Composable
fun HistoryScreenPreview() {
    DaylogTheme {
        // HistoryScreen preview
    }
}
