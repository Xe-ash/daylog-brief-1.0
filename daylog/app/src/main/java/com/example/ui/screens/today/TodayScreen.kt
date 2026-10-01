package com.example.ui.screens.today

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
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.NightlightRound
import androidx.compose.material.icons.filled.Person
import androidx.compose.material3.Icon
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
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.SyncState
import com.example.model.ActivityClassification
import com.example.ui.DaylogViewModel
import com.example.ui.components.GlassButton
import com.example.ui.components.GlassPanel
import com.example.ui.components.GlassPanelVariant
import com.example.ui.components.IsolatedTimerDisplay
import com.example.ui.components.NeonOrb
import com.example.ui.components.OrbState
import com.example.ui.components.PrimaryButton
import com.example.ui.components.ScreenScaffold
import com.example.ui.components.SegmentedDayBar
import com.example.ui.components.StatBlock
import com.example.ui.dialogs.ManualActivityDialog
import com.example.ui.dialogs.SetGoalDialog
import com.example.ui.theme.DaylogTheme
import com.example.ui.theme.DaylogTokens
import com.example.ui.theme.DaylogTypography
import com.example.ui.theme.fluidDropletPress
import com.example.util.NightlyReviewManager
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale

private data class QuickStartItem(
    val name: String,
    val category: String,
    val classification: ActivityClassification
)

private val QuickStartChips = listOf(
    QuickStartItem("Deep work", "Work", ActivityClassification.PRODUCTIVE),
    QuickStartItem("Study", "Study", ActivityClassification.PRODUCTIVE),
    QuickStartItem("Exercise", "Health", ActivityClassification.PRODUCTIVE),
    QuickStartItem("Reading", "Learning", ActivityClassification.PRODUCTIVE),
    QuickStartItem("Planning", "General", ActivityClassification.NEUTRAL),
    QuickStartItem("Break", "Rest", ActivityClassification.NEUTRAL),
    QuickStartItem("Social media", "Entertainment", ActivityClassification.UNPRODUCTIVE)
)

@Composable
fun TodayScreen(
    viewModel: DaylogViewModel,
    modifier: Modifier = Modifier
) {
    val selectedDate by viewModel.selectedDate.collectAsState()
    val runningActivity by viewModel.runningActivity.collectAsState()
    val activities by viewModel.selectedDateActivities.collectAsState()
    val dailyGoal by viewModel.selectedDateGoal.collectAsState()
    val daySummary by viewModel.selectedDaySummary.collectAsState()
    val syncState by viewModel.syncState.collectAsState()
    val userProfile by viewModel.userProfile.collectAsState()

    var showManualEntryDialog by remember { mutableStateOf(false) }
    var showGoalDialog by remember { mutableStateOf(false) }

    // Date line in sentence case, e.g. "Thursday, 1 October"
    val dateLine = remember(selectedDate) {
        try {
            val parsed = SimpleDateFormat("yyyy-MM-dd", Locale.US).parse(selectedDate) ?: Date()
            SimpleDateFormat("EEEE, d MMMM", Locale.US).format(parsed)
        } catch (e: Exception) {
            "Today"
        }
    }

    // Sync dot color
    val syncDotColor = when (syncState) {
        is SyncState.Synced -> DaylogTokens.Productive
        is SyncState.Syncing -> DaylogTokens.Neutral
        is SyncState.Error -> DaylogTokens.Distraction
        else -> DaylogTokens.TextTertiary
    }

    // Compute unlogged time since 06:00
    val unloggedText = remember(daySummary.totalTrackedSeconds) {
        val cal = Calendar.getInstance()
        val nowSec = cal.get(Calendar.HOUR_OF_DAY) * 3600L + cal.get(Calendar.MINUTE) * 60L
        val elapsedWakingSec = (nowSec - 6 * 3600L).coerceIn(0L, 17 * 3600L)
        val unloggedSec = (elapsedWakingSec - daySummary.totalTrackedSeconds).coerceAtLeast(0L)
        val h = unloggedSec / 3600
        val m = (unloggedSec % 3600) / 60
        if (h > 0) "${h}h ${m}m" else "${m}m"
    }

    ScreenScaffold(
        title = "Today",
        dateLine = dateLine,
        headerAction = {
            Row(verticalAlignment = Alignment.CenterVertically) {
                // Night Note button
                Box(
                    modifier = Modifier
                        .fluidDropletPress(
                            interactionSource = remember { androidx.compose.foundation.interaction.MutableInteractionSource() },
                            squashFactorY = 0.90f,
                            stretchFactorX = 1.08f,
                            onClick = { viewModel.openNightlyReviewNote() }
                        )
                        .clip(CircleShape)
                        .background(DaylogTokens.AuroraViolet.copy(alpha = 0.20f))
                        .border(BorderStroke(1.dp, DaylogTokens.AuroraViolet.copy(alpha = 0.45f)), CircleShape)
                        .padding(horizontal = 10.dp, vertical = 6.dp)
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = Icons.Default.NightlightRound,
                            contentDescription = "Nightly Wrap-up",
                            tint = DaylogTokens.AuroraCyan,
                            modifier = Modifier.size(14.dp)
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(
                            text = "Night Note",
                            style = DaylogTypography.Label.copy(
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold,
                                color = DaylogTokens.TextPrimary
                            )
                        )
                    }
                }

                Spacer(modifier = Modifier.width(10.dp))

                // Avatar with small sync dot (green synced, amber syncing, rose failed)
                Box(
                    modifier = Modifier
                        .size(40.dp)
                        .clip(CircleShape)
                        .background(DaylogTokens.GlassFill)
                        .border(BorderStroke(1.dp, DaylogTokens.GlassStroke), CircleShape),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.Person,
                        contentDescription = "User profile",
                        tint = DaylogTokens.TextSecondary,
                        modifier = Modifier.size(22.dp)
                    )
                    // Small sync dot
                    Box(
                        modifier = Modifier
                            .size(9.dp)
                            .align(Alignment.BottomEnd)
                            .clip(CircleShape)
                            .background(syncDotColor)
                            .border(1.5.dp, DaylogTokens.Night, CircleShape)
                    )
                }
            }
        },
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
            // Evening/Night Wrap-up Banner
            if (NightlyReviewManager.isNightTime()) {
                item {
                    GlassPanel(
                        variant = GlassPanelVariant.Standard,
                        modifier = Modifier.clickable { viewModel.openNightlyReviewNote() }
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                modifier = Modifier.weight(1f)
                            ) {
                                Box(
                                    modifier = Modifier
                                        .size(38.dp)
                                        .clip(CircleShape)
                                        .background(DaylogTokens.AuroraViolet.copy(alpha = 0.25f))
                                        .border(BorderStroke(1.dp, DaylogTokens.AuroraViolet.copy(alpha = 0.5f)), CircleShape),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.NightlightRound,
                                        contentDescription = null,
                                        tint = DaylogTokens.AuroraCyan,
                                        modifier = Modifier.size(20.dp)
                                    )
                                }

                                Spacer(modifier = Modifier.width(12.dp))

                                Column {
                                    Text(
                                        text = "Nightly Wrap-up",
                                        style = DaylogTypography.Body.copy(
                                            fontWeight = FontWeight.Bold,
                                            color = DaylogTokens.TextPrimary
                                        )
                                    )
                                    Spacer(modifier = Modifier.height(2.dp))
                                    Text(
                                        text = "Review today's missed habits and tasks",
                                        style = DaylogTypography.Label.copy(
                                            fontSize = 11.sp,
                                            color = DaylogTokens.TextSecondary
                                        )
                                    )
                                }
                            }

                            Box(
                                modifier = Modifier
                                    .clip(CircleShape)
                                    .background(DaylogTokens.AuroraCyan.copy(alpha = 0.15f))
                                    .border(BorderStroke(1.dp, DaylogTokens.AuroraCyan.copy(alpha = 0.45f)), CircleShape)
                                    .padding(horizontal = 10.dp, vertical = 5.dp)
                            ) {
                                Text(
                                    text = "View Note",
                                    style = DaylogTypography.Label.copy(
                                        fontWeight = FontWeight.Bold,
                                        color = DaylogTokens.AuroraCyan,
                                        fontSize = 11.sp
                                    )
                                )
                            }
                        }
                    }
                }
            }
            // --- 1. HERO PANEL (Merged Today's Intention & Ready to Focus) ---
            item {
                GlassPanel(
                    variant = GlassPanelVariant.Hero
                ) {
                    Column(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        // NeonOrb - The One Star
                        NeonOrb(
                            orbState = if (runningActivity != null) OrbState.Active else OrbState.Idle,
                            activeClassification = runningActivity?.classification,
                            activityTitle = runningActivity?.name,
                            timerDisplay = if (runningActivity != null) {
                                {
                                    IsolatedTimerDisplay(
                                        startedAt = runningActivity!!.startedAt,
                                        baseDurationSeconds = runningActivity!!.durationSeconds,
                                        style = DaylogTypography.Numeral.copy(fontSize = 32.sp, lineHeight = 36.sp),
                                        color = DaylogTokens.TextPrimary
                                    )
                                }
                            } else null,
                            onClick = {
                                if (runningActivity != null) {
                                    viewModel.stopActivity(runningActivity!!.id)
                                } else {
                                    // Start default focus block
                                    viewModel.startActivity("Deep work", "Work", ActivityClassification.PRODUCTIVE)
                                }
                            }
                        )

                        Spacer(modifier = Modifier.height(16.dp))

                        // Display timer or headline
                        if (runningActivity == null) {
                            Text(
                                text = "00:00:00",
                                style = DaylogTypography.Numeral,
                                color = DaylogTokens.TextPrimary
                            )
                            Spacer(modifier = Modifier.height(DaylogTokens.NumberLabelGap))
                            Text(
                                text = "What are you focusing on?",
                                style = DaylogTypography.Body,
                                color = DaylogTokens.TextSecondary
                            )
                        } else {
                            Text(
                                text = runningActivity!!.name,
                                style = DaylogTypography.Title,
                                color = DaylogTokens.TextPrimary,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )
                            Spacer(modifier = Modifier.height(4.dp))
                            val classificationColor = when (runningActivity!!.classification) {
                                ActivityClassification.PRODUCTIVE -> DaylogTokens.Productive
                                ActivityClassification.NEUTRAL -> DaylogTokens.Neutral
                                ActivityClassification.UNPRODUCTIVE -> DaylogTokens.Distraction
                                else -> DaylogTokens.AuroraCyan
                            }
                            Text(
                                text = "${runningActivity!!.categoryName.lowercase().replaceFirstChar { it.uppercase() }} • Active",
                                style = DaylogTypography.Label,
                                color = classificationColor
                            )
                        }

                        Spacer(modifier = Modifier.height(20.dp))

                        // Action Buttons: [ Start activity ] [ Log past ]
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            if (runningActivity == null) {
                                PrimaryButton(
                                    text = "Start activity",
                                    onClick = {
                                        viewModel.startActivity("Deep work", "Work", ActivityClassification.PRODUCTIVE)
                                    },
                                    modifier = Modifier.weight(1f)
                                )
                            } else {
                                PrimaryButton(
                                    text = "Stop activity",
                                    onClick = {
                                        viewModel.stopActivity(runningActivity!!.id)
                                    },
                                    modifier = Modifier.weight(1f)
                                )
                            }

                            GlassButton(
                                text = "Log past",
                                onClick = { showManualEntryDialog = true },
                                modifier = Modifier.weight(1f)
                            )
                        }

                        Spacer(modifier = Modifier.height(20.dp))

                        // Subtle Divider
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(1.dp)
                                .background(DaylogTokens.GlassHighlightTop)
                        )

                        Spacer(modifier = Modifier.height(16.dp))

                        // Goal Progress Section
                        val targetMins = dailyGoal?.targetMinutes ?: (userProfile.defaultDailyGoalMinutes)
                        val targetHours = targetMins / 60
                        val prodMins = daySummary.productiveSeconds / 60
                        val percent = daySummary.goalCompletionPercent.toInt()

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Text(
                                    text = "Today's goal: ${targetHours}h focused",
                                    style = DaylogTypography.Body.copy(
                                        fontSize = 14.sp,
                                        fontWeight = FontWeight.Medium,
                                        color = DaylogTokens.TextPrimary
                                    )
                                )
                                Spacer(modifier = Modifier.width(8.dp))
                                Text(
                                    text = "Edit",
                                    style = DaylogTypography.Label.copy(color = DaylogTokens.AuroraCyan),
                                    modifier = Modifier
                                        .clip(CircleShape)
                                        .clickable { showGoalDialog = true }
                                        .padding(horizontal = 4.dp, vertical = 2.dp)
                                )
                            }

                            Text(
                                text = "${prodMins}m of ${targetHours}h  $percent%",
                                style = DaylogTypography.Label,
                                color = DaylogTokens.TextSecondary
                            )
                        }

                        Spacer(modifier = Modifier.height(10.dp))

                        // Goal Progress Bar
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(6.dp)
                                .clip(CircleShape)
                                .background(Color.White.copy(alpha = 0.08f))
                        ) {
                            val fraction = (daySummary.goalCompletionPercent / 100.0).coerceIn(0.0, 1.0).toFloat()
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth(fraction)
                                    .fillMaxHeight()
                                    .clip(CircleShape)
                                    .background(
                                        Brush.horizontalGradient(
                                            listOf(DaylogTokens.Productive, DaylogTokens.AuroraCyan)
                                        )
                                    )
                            )
                        }
                    }
                }
            }

            // --- 2. QUICK START CHIPS ---
            item {
                LazyRow(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    items(QuickStartChips, key = { it.name }) { chip ->
                        val chipColor = when (chip.classification) {
                            ActivityClassification.PRODUCTIVE -> DaylogTokens.Productive
                            ActivityClassification.NEUTRAL -> DaylogTokens.Neutral
                            ActivityClassification.UNPRODUCTIVE -> DaylogTokens.Distraction
                            else -> DaylogTokens.AuroraCyan
                        }

                        Box(
                            modifier = Modifier
                                .heightIn(min = DaylogTokens.MinTouchTarget)
                                .clip(CircleShape)
                                .background(DaylogTokens.GlassFill)
                                .border(BorderStroke(1.dp, DaylogTokens.GlassStroke), CircleShape)
                                .clickable {
                                    viewModel.startActivity(
                                        name = chip.name,
                                        category = chip.category,
                                        classification = chip.classification
                                    )
                                }
                                .padding(horizontal = 16.dp, vertical = 12.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.Center
                            ) {
                                Box(
                                    modifier = Modifier
                                        .size(6.dp)
                                        .clip(CircleShape)
                                        .background(chipColor)
                                )
                                Spacer(modifier = Modifier.width(8.dp))
                                Text(
                                    text = chip.name,
                                    style = DaylogTypography.Label.copy(
                                        fontWeight = FontWeight.Medium,
                                        color = DaylogTokens.TextPrimary
                                    )
                                )
                            }
                        }
                    }
                }
            }

            // --- 3. TODAY SO FAR PANEL ---
            item {
                GlassPanel(
                    variant = GlassPanelVariant.Standard
                ) {
                    Column(
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        // Header: Title and small Score chip
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = "Today so far",
                                style = DaylogTypography.Title,
                                color = DaylogTokens.TextPrimary
                            )

                            Box(
                                modifier = Modifier
                                    .clip(CircleShape)
                                    .background(DaylogTokens.GlassFill)
                                    .border(BorderStroke(1.dp, DaylogTokens.GlassStroke), CircleShape)
                                    .padding(horizontal = 10.dp, vertical = 4.dp)
                            ) {
                                Text(
                                    text = "Score ${daySummary.dayScore}",
                                    style = DaylogTypography.Label.copy(
                                        fontWeight = FontWeight.Bold,
                                        color = DaylogTokens.TextPrimary
                                    )
                                )
                            }
                        }

                        Spacer(modifier = Modifier.height(DaylogTokens.TitleContentGap))

                        // SegmentedDayBar spanning waking window (06:00 to 23:00)
                        SegmentedDayBar(
                            activities = activities,
                            dateStr = selectedDate,
                            barHeight = 14.dp
                        )

                        Spacer(modifier = Modifier.height(16.dp))

                        // Stat trio: Productive / Neutral / Distraction
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            val prodM = daySummary.productiveSeconds / 60
                            val neutM = daySummary.neutralSeconds / 60
                            val unprodM = daySummary.unproductiveSeconds / 60

                            StatBlock(
                                value = "${prodM}m",
                                label = "Productive",
                                statusColor = DaylogTokens.Productive,
                                isNumeralCompact = true,
                                modifier = Modifier.weight(1f)
                            )
                            StatBlock(
                                value = "${neutM}m",
                                label = "Neutral",
                                statusColor = DaylogTokens.Neutral,
                                isNumeralCompact = true,
                                modifier = Modifier.weight(1f)
                            )
                            StatBlock(
                                value = "${unprodM}m",
                                label = "Distr.",
                                statusColor = DaylogTokens.Distraction,
                                isNumeralCompact = true,
                                modifier = Modifier.weight(1f)
                            )
                        }

                        Spacer(modifier = Modifier.height(14.dp))

                        // Small line: "Unlogged since 06:00: X"
                        Text(
                            text = "Unlogged since 06:00: $unloggedText",
                            style = DaylogTypography.Label,
                            color = DaylogTokens.TextSecondary
                        )
                    }
                }
            }
        }
    }

    // Dialogs
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

    if (showGoalDialog) {
        val currentGoalHours = (dailyGoal?.targetMinutes ?: userProfile.defaultDailyGoalMinutes) / 60
        SetGoalDialog(
            currentTitle = dailyGoal?.title ?: "Complete $currentGoalHours focused hours of work.",
            currentTargetHours = currentGoalHours,
            onDismiss = { showGoalDialog = false },
            onSave = { title, hrs ->
                viewModel.setDailyGoal(title, hrs)
                showGoalDialog = false
            }
        )
    }
}

@Preview(name = "TodayScreen Preview", widthDp = 360, heightDp = 800)
@Composable
fun TodayScreenPreview() {
    DaylogTheme {
        // TodayScreen preview
    }
}
