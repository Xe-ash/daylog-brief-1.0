package com.example.ui.screens.habits

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.scaleIn
import androidx.compose.animation.scaleOut
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.combinedClickable
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
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Alarm
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material.icons.filled.NotificationsActive
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.model.Habit
import com.example.model.HabitStatus
import com.example.model.HabitType
import com.example.ui.DaylogViewModel
import com.example.ui.HabitItemUi
import com.example.ui.components.EmptyState
import com.example.ui.components.GlassPanel
import com.example.ui.components.GlassPanelVariant
import com.example.ui.components.PrimaryButton
import com.example.ui.components.ProgressRing
import com.example.ui.components.ScreenScaffold
import com.example.ui.dialogs.AddHabitDialog
import com.example.ui.theme.DaylogTheme
import com.example.ui.theme.DaylogTokens
import com.example.ui.theme.DaylogTypography

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun HabitsScreen(
    viewModel: DaylogViewModel,
    modifier: Modifier = Modifier
) {
    val habitsWithStatus by viewModel.selectedDateHabits.collectAsState()
    var showAddHabitDialog by remember { mutableStateOf(false) }
    var habitToDelete by remember { mutableStateOf<Habit?>(null) }
    var habitForAlarmTimePicker by remember { mutableStateOf<Habit?>(null) }

    val completedCount = habitsWithStatus.count { it.status == HabitStatus.COMPLETED }
    val totalCount = habitsWithStatus.size
    val completionFraction = if (totalCount > 0) completedCount.toFloat() / totalCount else 0f
    val bestOverallStreak = habitsWithStatus.maxOfOrNull { it.habit.longestStreak } ?: 0

    ScreenScaffold(
        title = "Habits",
        modifier = modifier
    ) {
        if (habitsWithStatus.isEmpty()) {
            EmptyState(
                title = "No habits created yet",
                body = "Add your daily non-negotiables and track your execution streak.",
                actionLabel = "Add habit",
                icon = Icons.Default.Add,
                onAction = { showAddHabitDialog = true },
                modifier = Modifier.padding(top = 40.dp)
            )
        } else {
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
                // --- 1. COMPLETION PANEL WITH REAL PROGRESS RING ---
                item {
                    GlassPanel(variant = GlassPanelVariant.Standard) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            ProgressRing(
                                progressPercent = completionFraction * 100f,
                                modifier = Modifier.size(76.dp),
                                strokeWidth = 8.dp
                            ) {
                                Text(
                                    text = "${(completionFraction * 100).toInt()}%",
                                    style = DaylogTypography.Label.copy(
                                        fontWeight = FontWeight.Bold,
                                        color = DaylogTokens.TextPrimary
                                    )
                                )
                            }

                            Spacer(modifier = Modifier.width(20.dp))

                            Column {
                                Text(
                                    text = "$completedCount of $totalCount done",
                                    style = DaylogTypography.Title,
                                    color = DaylogTokens.TextPrimary
                                )
                                if (bestOverallStreak > 0) {
                                    Spacer(modifier = Modifier.height(4.dp))
                                    Text(
                                        text = "Best streak: ${bestOverallStreak}d",
                                        style = DaylogTypography.Label,
                                        color = DaylogTokens.TextSecondary
                                    )
                                }
                            }
                        }
                    }
                }

                // --- 2. HEADER FOR HABIT LIST WITH "Add habit" BUTTON ---
                item {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "Your habits",
                            style = DaylogTypography.Title,
                            color = DaylogTokens.TextPrimary
                        )

                        PrimaryButton(
                            text = "Add habit",
                            onClick = { showAddHabitDialog = true },
                            leadingIcon = Icons.Default.Add
                        )
                    }
                }

                // --- 3. ALL HABITS INSIDE ONE STANDARD PANEL AS SUBTLE ROWS ---
                item {
                    GlassPanel(
                        variant = GlassPanelVariant.Standard,
                        contentPadding = PaddingValues(0.dp)
                    ) {
                        Column(modifier = Modifier.fillMaxWidth()) {
                            habitsWithStatus.forEachIndexed { index, habitItem ->
                                val habit = habitItem.habit
                                val isCompleted = habitItem.status == HabitStatus.COMPLETED
                                var menuExpanded by remember { mutableStateOf(false) }

                                val checkBg by animateColorAsState(
                                    targetValue = if (isCompleted) DaylogTokens.Productive else Color.Transparent,
                                    animationSpec = tween(durationMillis = 200),
                                    label = "check_bg"
                                )

                                val checkBorder by animateColorAsState(
                                    targetValue = if (isCompleted) DaylogTokens.Productive else DaylogTokens.TextTertiary,
                                    animationSpec = tween(durationMillis = 200),
                                    label = "check_border"
                                )

                                val checkScale by animateFloatAsState(
                                    targetValue = if (isCompleted) 1.0f else 0.85f,
                                    animationSpec = tween(durationMillis = 200),
                                    label = "check_scale"
                                )

                                Box(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .combinedClickable(
                                            onClick = { viewModel.toggleHabit(habit.id) },
                                            onLongClick = { menuExpanded = true }
                                        )
                                        .padding(horizontal = 20.dp, vertical = 14.dp)
                                ) {
                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        // Animated check circle
                                        Box(
                                            modifier = Modifier
                                                .size(28.dp)
                                                .scale(checkScale)
                                                .clip(CircleShape)
                                                .background(checkBg)
                                                .border(BorderStroke(2.dp, checkBorder), CircleShape),
                                            contentAlignment = Alignment.Center
                                        ) {
                                            if (isCompleted) {
                                                Icon(
                                                    imageVector = Icons.Default.Check,
                                                    contentDescription = "Completed",
                                                    tint = DaylogTokens.Night,
                                                    modifier = Modifier.size(16.dp)
                                                )
                                            }
                                        }

                                        Spacer(modifier = Modifier.width(14.dp))

                                        // Habit details
                                        Column(modifier = Modifier.weight(1f)) {
                                            Text(
                                                text = habit.name,
                                                style = DaylogTypography.Body.copy(
                                                    fontWeight = FontWeight.Medium,
                                                    color = if (isCompleted) DaylogTokens.TextSecondary else DaylogTokens.TextPrimary
                                                )
                                            )

                                            // Target in plain words (e.g. "30 min") or best streak
                                            val subtitleParts = mutableListOf<String>()
                                            if (habit.type == HabitType.DURATION && habit.targetValue > 0) {
                                                subtitleParts.add("${habit.targetValue.toInt()} min")
                                            }
                                            if (habit.longestStreak > 0) {
                                                subtitleParts.add("Best: ${habit.longestStreak}d")
                                            }

                                            if (subtitleParts.isNotEmpty()) {
                                                Spacer(modifier = Modifier.height(2.dp))
                                                Text(
                                                    text = subtitleParts.joinToString(" • "),
                                                    style = DaylogTypography.Label.copy(
                                                        fontSize = 11.sp,
                                                        color = DaylogTokens.TextTertiary
                                                    )
                                                )
                                            }
                                        }

                                        // Show streak chip only when streak is 2 days or more
                                        if (habit.currentStreak >= 2) {
                                            Box(
                                                modifier = Modifier
                                                    .clip(CircleShape)
                                                    .background(DaylogTokens.AuroraViolet.copy(alpha = 0.20f))
                                                    .border(BorderStroke(1.dp, DaylogTokens.AuroraViolet.copy(alpha = 0.40f)), CircleShape)
                                                    .padding(horizontal = 8.dp, vertical = 3.dp)
                                            ) {
                                                Text(
                                                    text = "${habit.currentStreak}d",
                                                    style = DaylogTypography.Label.copy(
                                                        fontSize = 11.sp,
                                                        fontWeight = FontWeight.SemiBold,
                                                        color = DaylogTokens.TextPrimary
                                                    )
                                                )
                                            }
                                            Spacer(modifier = Modifier.width(6.dp))
                                        }

                                        // Alarm section: Time Badge + Toggle Switch
                                        val isAlarmSet = !habit.reminderTime.isNullOrBlank()
                                        Row(
                                            verticalAlignment = Alignment.CenterVertically
                                        ) {
                                            if (isAlarmSet) {
                                                Box(
                                                    modifier = Modifier
                                                        .clip(CircleShape)
                                                        .background(DaylogTokens.AuroraCyan.copy(alpha = 0.15f))
                                                        .border(
                                                            BorderStroke(1.dp, DaylogTokens.AuroraCyan.copy(alpha = 0.45f)),
                                                            CircleShape
                                                        )
                                                        .clickable { habitForAlarmTimePicker = habit }
                                                        .padding(horizontal = 8.dp, vertical = 3.dp)
                                                ) {
                                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                                        Icon(
                                                            imageVector = Icons.Default.Alarm,
                                                            contentDescription = "Alarm time",
                                                            tint = DaylogTokens.AuroraCyan,
                                                            modifier = Modifier.size(13.dp)
                                                        )
                                                        Spacer(modifier = Modifier.width(4.dp))
                                                        Text(
                                                            text = com.example.alarm.HabitAlarmScheduler.formatDisplayTime(habit.reminderTime!!),
                                                            style = DaylogTypography.Label.copy(
                                                                fontSize = 11.sp,
                                                                fontWeight = FontWeight.Bold,
                                                                color = DaylogTokens.AuroraCyan
                                                            )
                                                        )
                                                    }
                                                }
                                                Spacer(modifier = Modifier.width(4.dp))
                                            }

                                            Switch(
                                                checked = isAlarmSet,
                                                onCheckedChange = { checked ->
                                                    if (checked) {
                                                        habitForAlarmTimePicker = habit
                                                    } else {
                                                        viewModel.cancelHabitAlarm(habit.id)
                                                    }
                                                },
                                                colors = SwitchDefaults.colors(
                                                    checkedThumbColor = DaylogTokens.Night,
                                                    checkedTrackColor = DaylogTokens.Productive,
                                                    uncheckedThumbColor = DaylogTokens.TextTertiary,
                                                    uncheckedTrackColor = DaylogTokens.NightDeep
                                                ),
                                                modifier = Modifier.scale(0.8f)
                                            )
                                        }

                                        // Overflow menu anchor
                                        Box {
                                            IconButton(
                                                onClick = { menuExpanded = true },
                                                modifier = Modifier.size(36.dp)
                                            ) {
                                                Icon(
                                                    imageVector = Icons.Default.MoreVert,
                                                    contentDescription = "Habit options",
                                                    tint = DaylogTokens.TextTertiary,
                                                    modifier = Modifier.size(18.dp)
                                                )
                                            }

                                            DropdownMenu(
                                                expanded = menuExpanded,
                                                onDismissRequest = { menuExpanded = false }
                                            ) {
                                                DropdownMenuItem(
                                                    text = {
                                                        Text(if (isAlarmSet) "Edit Alarm Time" else "Set Alarm Time")
                                                    },
                                                    leadingIcon = {
                                                        Icon(
                                                            imageVector = Icons.Default.Alarm,
                                                            contentDescription = null,
                                                            tint = DaylogTokens.AuroraCyan,
                                                            modifier = Modifier.size(18.dp)
                                                        )
                                                    },
                                                    onClick = {
                                                        menuExpanded = false
                                                        habitForAlarmTimePicker = habit
                                                    }
                                                )

                                                if (isAlarmSet) {
                                                    DropdownMenuItem(
                                                        text = { Text("Turn Off Alarm") },
                                                        onClick = {
                                                            menuExpanded = false
                                                            viewModel.cancelHabitAlarm(habit.id)
                                                        }
                                                    )
                                                }

                                                DropdownMenuItem(
                                                    text = { Text("Test Alarm Sound & Notice") },
                                                    leadingIcon = {
                                                        Icon(
                                                            imageVector = Icons.Default.NotificationsActive,
                                                            contentDescription = null,
                                                            tint = DaylogTokens.Productive,
                                                            modifier = Modifier.size(18.dp)
                                                        )
                                                    },
                                                    onClick = {
                                                        menuExpanded = false
                                                        viewModel.testFireHabitAlarm(habit.id)
                                                    }
                                                )

                                                DropdownMenuItem(
                                                    text = { Text("Delete", color = DaylogTokens.Distraction) },
                                                    onClick = {
                                                        menuExpanded = false
                                                        habitToDelete = habit
                                                    }
                                                )
                                            }
                                        }
                                    }
                                }

                                // Hairline divider between rows
                                if (index < habitsWithStatus.size - 1) {
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

    // Material 3 Habit TimePicker Dialog
    if (habitForAlarmTimePicker != null) {
        HabitTimePickerDialog(
            habitName = habitForAlarmTimePicker!!.name,
            initialTimeString = habitForAlarmTimePicker!!.reminderTime,
            onDismiss = { habitForAlarmTimePicker = null },
            onConfirm = { timeStr ->
                viewModel.setHabitAlarm(habitForAlarmTimePicker!!.id, timeStr)
                habitForAlarmTimePicker = null
            },
            onDisableAlarm = {
                viewModel.cancelHabitAlarm(habitForAlarmTimePicker!!.id)
                habitForAlarmTimePicker = null
            }
        )
    }

    // Add Habit Dialog
    if (showAddHabitDialog) {
        AddHabitDialog(
            onDismiss = { showAddHabitDialog = false },
            onSave = { name, type, target, unit, reminderTime ->
                viewModel.addHabit(name, type, target, unit, reminderTime)
                showAddHabitDialog = false
            }
        )
    }

    // Delete Confirmation Dialog
    if (habitToDelete != null) {
        AlertDialog(
            onDismissRequest = { habitToDelete = null },
            title = {
                Text(
                    text = "Delete habit",
                    style = DaylogTypography.Title,
                    color = DaylogTokens.TextPrimary
                )
            },
            text = {
                Text(
                    text = "Delete '${habitToDelete!!.name}'? Its streak history will be lost.",
                    style = DaylogTypography.Body,
                    color = DaylogTokens.TextSecondary
                )
            },
            confirmButton = {
                TextButton(
                    onClick = {
                        viewModel.deleteHabit(habitToDelete!!.id)
                        habitToDelete = null
                    }
                ) {
                    Text("Delete", color = DaylogTokens.Distraction, fontWeight = FontWeight.Bold)
                }
            },
            dismissButton = {
                TextButton(onClick = { habitToDelete = null }) {
                    Text("Cancel", color = DaylogTokens.TextSecondary)
                }
            },
            containerColor = DaylogTokens.NightDeep
        )
    }
}

@Preview(name = "HabitsScreen Preview")
@Composable
fun HabitsScreenPreview() {
    DaylogTheme {
        // HabitsScreen preview
    }
}
