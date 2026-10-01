package com.example.ui.dialogs

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Alarm
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.ExpandLess
import androidx.compose.material.icons.filled.ExpandMore
import androidx.compose.material.icons.filled.Schedule
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableDoubleStateOf
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.draw.scale
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.example.model.ActivityClassification
import com.example.model.HabitType
import com.example.model.TaskPriority
import com.example.ui.components.GlassButton
import com.example.ui.components.PrimaryButton
import com.example.ui.theme.DaylogTokens
import com.example.ui.theme.DaylogTypography
import com.example.ui.theme.FluidDropletWindow
import com.example.util.DateTimeUtils
import java.util.Calendar

// ==========================================
// Aurora Glass Dialog Primitives
// ==========================================

@Composable
fun GlassDialogContainer(
    title: String,
    onDismiss: () -> Unit,
    subtitle: String? = null,
    content: @Composable ColumnScope.() -> Unit
) {
    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(usePlatformDefaultWidth = false)
    ) {
        FluidDropletWindow(
            onDismissRequest = onDismiss,
            initialCornerRadius = 44.dp,
            settledCornerRadius = DaylogTokens.StandardRadius,
            modifier = Modifier
                .fillMaxWidth(0.92f)
                .wrapContentHeight()
        ) { dismissWithFluid ->
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .wrapContentHeight()
                    .shadow(
                        elevation = 24.dp,
                        shape = RoundedCornerShape(DaylogTokens.StandardRadius),
                        ambientColor = DaylogTokens.AuroraViolet.copy(alpha = 0.25f),
                        spotColor = DaylogTokens.Night
                    )
                    .clip(RoundedCornerShape(DaylogTokens.StandardRadius))
                    .background(DaylogTokens.NightElevated)
                    .background(Color.White.copy(alpha = 0.05f))
                    .border(
                        BorderStroke(1.dp, DaylogTokens.GlassStroke),
                        RoundedCornerShape(DaylogTokens.StandardRadius)
                    )
                    .drawBehind {
                        val rPx = DaylogTokens.StandardRadius.toPx()
                        drawLine(
                            color = DaylogTokens.GlassHighlightTop,
                            start = Offset(rPx, 1f),
                            end = Offset(size.width - rPx, 1f),
                            strokeWidth = 1.dp.toPx()
                        )
                    }
                    .padding(20.dp)
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .verticalScroll(rememberScrollState())
                ) {
                    // Header
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = title,
                                style = DaylogTypography.Title,
                                color = DaylogTokens.TextPrimary
                            )
                            if (subtitle != null) {
                                Spacer(modifier = Modifier.height(2.dp))
                                Text(
                                    text = subtitle,
                                    style = DaylogTypography.Body.copy(fontSize = 13.sp),
                                    color = DaylogTokens.TextSecondary
                                )
                            }
                        }
                        IconButton(
                            onClick = dismissWithFluid,
                            modifier = Modifier.size(40.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.Close,
                                contentDescription = "Close",
                                tint = DaylogTokens.TextSecondary
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    content()
                }
            }
        }
    }
}

@Composable
fun GlassDialogTextField(
    value: String,
    onValueChange: (String) -> Unit,
    label: String,
    placeholder: String = "",
    modifier: Modifier = Modifier,
    keyboardOptions: KeyboardOptions = KeyboardOptions.Default,
    singleLine: Boolean = true,
    maxLines: Int = 1
) {
    Column(modifier = modifier) {
        Text(
            text = label,
            style = DaylogTypography.Label.copy(
                fontWeight = FontWeight.SemiBold,
                fontSize = 12.sp
            ),
            color = DaylogTokens.TextSecondary
        )
        Spacer(modifier = Modifier.height(6.dp))
        OutlinedTextField(
            value = value,
            onValueChange = onValueChange,
            placeholder = {
                Text(
                    text = placeholder,
                    style = DaylogTypography.Body.copy(fontSize = 14.sp),
                    color = DaylogTokens.TextTertiary
                )
            },
            textStyle = DaylogTypography.Body.copy(
                fontSize = 14.sp,
                color = DaylogTokens.TextPrimary
            ),
            singleLine = singleLine,
            maxLines = maxLines,
            keyboardOptions = keyboardOptions,
            shape = RoundedCornerShape(12.dp),
            colors = OutlinedTextFieldDefaults.colors(
                focusedContainerColor = DaylogTokens.Night,
                unfocusedContainerColor = DaylogTokens.Night.copy(alpha = 0.6f),
                focusedBorderColor = DaylogTokens.AuroraViolet,
                unfocusedBorderColor = Color.White.copy(alpha = 0.12f),
                cursorColor = DaylogTokens.AuroraCyan
            ),
            modifier = Modifier.fillMaxWidth()
        )
    }
}

@Composable
fun GlassSelectableChip(
    text: String,
    isSelected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    accentColor: Color? = null,
    icon: ImageVector? = null
) {
    val activeColor = accentColor ?: DaylogTokens.AuroraViolet
    val bgColor = if (isSelected) {
        activeColor.copy(alpha = 0.22f)
    } else {
        Color.White.copy(alpha = 0.05f)
    }
    val borderColor = if (isSelected) {
        activeColor
    } else {
        Color.White.copy(alpha = 0.10f)
    }
    val textColor = if (isSelected) {
        DaylogTokens.TextPrimary
    } else {
        DaylogTokens.TextSecondary
    }

    Box(
        modifier = modifier
            .height(36.dp)
            .clip(RoundedCornerShape(10.dp))
            .background(bgColor)
            .border(BorderStroke(1.dp, borderColor), RoundedCornerShape(10.dp))
            .clickable(onClick = onClick)
            .padding(horizontal = 12.dp),
        contentAlignment = Alignment.Center
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.Center
        ) {
            if (accentColor != null) {
                Box(
                    modifier = Modifier
                        .size(6.dp)
                        .clip(CircleShape)
                        .background(accentColor)
                )
                Spacer(modifier = Modifier.width(6.dp))
            }
            Text(
                text = text,
                style = DaylogTypography.Label.copy(
                    fontSize = 12.sp,
                    fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium
                ),
                color = textColor,
                maxLines = 1
            )
        }
    }
}

// ==========================================
// Dialog: Manual Activity (Log Past)
// ==========================================

@Composable
fun ManualActivityDialog(
    initialStartMillis: Long? = null,
    initialEndMillis: Long? = null,
    onDismiss: () -> Unit,
    onSave: (name: String, category: String, classification: ActivityClassification, startMillis: Long, endMillis: Long, rating: Int?, note: String?) -> Unit
) {
    val cal = Calendar.getInstance()
    val defaultEnd = initialEndMillis ?: cal.timeInMillis
    val defaultStart = initialStartMillis ?: (defaultEnd - 45 * 60 * 1000)

    var name by remember { mutableStateOf("") }
    var category by remember { mutableStateOf("Work") }
    var classification by remember { mutableStateOf(ActivityClassification.PRODUCTIVE) }

    // Frictionless duration choice: 15m, 30m, 45m, 60m or Custom
    var selectedDurationMinutes by remember { mutableIntStateOf(45) }
    var isCustomTime by remember { mutableStateOf(false) }

    var startTimeStr by remember { mutableStateOf(DateTimeUtils.formatTime(defaultStart)) }
    var endTimeStr by remember { mutableStateOf(DateTimeUtils.formatTime(defaultEnd)) }

    var note by remember { mutableStateOf("") }
    var showNoteField by remember { mutableStateOf(false) }

    data class QuickPreset(
        val label: String,
        val category: String,
        val classification: ActivityClassification,
        val defaultMinutes: Int
    )
    val quickPresets = remember {
        listOf(
            QuickPreset("⚡ Deep work", "Work", ActivityClassification.PRODUCTIVE, 45),
            QuickPreset("📚 Study", "Study", ActivityClassification.PRODUCTIVE, 45),
            QuickPreset("🏃 Workout", "Health", ActivityClassification.PRODUCTIVE, 30),
            QuickPreset("☕ Break", "Rest", ActivityClassification.NEUTRAL, 15)
        )
    }

    GlassDialogContainer(
        title = "Log past activity",
        subtitle = "What did you focus on?",
        onDismiss = onDismiss
    ) {
        // 1. One-tap quick presets (Instant fill)
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            quickPresets.forEach { preset ->
                val isSelected = name == preset.label.substringAfter(" ")
                GlassSelectableChip(
                    text = preset.label,
                    isSelected = isSelected,
                    accentColor = when (preset.classification) {
                        ActivityClassification.PRODUCTIVE -> DaylogTokens.Productive
                        ActivityClassification.NEUTRAL -> DaylogTokens.Neutral
                        else -> DaylogTokens.Productive
                    },
                    onClick = {
                        name = preset.label.substringAfter(" ")
                        category = preset.category
                        classification = preset.classification
                        selectedDurationMinutes = preset.defaultMinutes
                        isCustomTime = false
                        val now = System.currentTimeMillis()
                        val start = now - preset.defaultMinutes * 60 * 1000L
                        startTimeStr = DateTimeUtils.formatTime(start)
                        endTimeStr = DateTimeUtils.formatTime(now)
                    },
                    modifier = Modifier.weight(1f)
                )
            }
        }

        Spacer(modifier = Modifier.height(14.dp))

        // 2. Activity Name input
        GlassDialogTextField(
            value = name,
            onValueChange = { name = it },
            label = "Activity name",
            placeholder = "e.g. Deep work, Reading, Gym"
        )

        Spacer(modifier = Modifier.height(14.dp))

        // 3. Duration selector (Clear & human-friendly)
        Text(
            text = "Duration",
            style = DaylogTypography.Label.copy(fontWeight = FontWeight.SemiBold, fontSize = 12.sp),
            color = DaylogTokens.TextSecondary
        )
        Spacer(modifier = Modifier.height(6.dp))
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            val durations = listOf(15, 30, 45, 60)
            durations.forEach { mins ->
                val isSelected = !isCustomTime && selectedDurationMinutes == mins
                GlassSelectableChip(
                    text = if (mins < 60) "${mins}m" else "1h",
                    isSelected = isSelected,
                    accentColor = DaylogTokens.AuroraCyan,
                    onClick = {
                        selectedDurationMinutes = mins
                        isCustomTime = false
                        val now = System.currentTimeMillis()
                        val start = now - mins * 60 * 1000L
                        startTimeStr = DateTimeUtils.formatTime(start)
                        endTimeStr = DateTimeUtils.formatTime(now)
                    },
                    modifier = Modifier.weight(1f)
                )
            }
            GlassSelectableChip(
                text = "Custom",
                isSelected = isCustomTime,
                accentColor = DaylogTokens.AuroraViolet,
                onClick = { isCustomTime = true },
                modifier = Modifier.weight(1f)
            )
        }

        // Custom time fields if Custom is selected
        if (isCustomTime) {
            Spacer(modifier = Modifier.height(10.dp))
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                GlassDialogTextField(
                    value = startTimeStr,
                    onValueChange = { startTimeStr = it },
                    label = "Start (HH:mm)",
                    placeholder = "09:00",
                    modifier = Modifier.weight(1f)
                )
                GlassDialogTextField(
                    value = endTimeStr,
                    onValueChange = { endTimeStr = it },
                    label = "End (HH:mm)",
                    placeholder = "10:30",
                    modifier = Modifier.weight(1f)
                )
            }
        }

        Spacer(modifier = Modifier.height(14.dp))

        // 4. Classification selector (Simple 3 options)
        Text(
            text = "Classification",
            style = DaylogTypography.Label.copy(fontWeight = FontWeight.SemiBold, fontSize = 12.sp),
            color = DaylogTokens.TextSecondary
        )
        Spacer(modifier = Modifier.height(6.dp))
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            GlassSelectableChip(
                text = "Productive",
                isSelected = classification == ActivityClassification.PRODUCTIVE,
                accentColor = DaylogTokens.Productive,
                onClick = { classification = ActivityClassification.PRODUCTIVE },
                modifier = Modifier.weight(1f)
            )
            GlassSelectableChip(
                text = "Neutral",
                isSelected = classification == ActivityClassification.NEUTRAL,
                accentColor = DaylogTokens.Neutral,
                onClick = { classification = ActivityClassification.NEUTRAL },
                modifier = Modifier.weight(1f)
            )
            GlassSelectableChip(
                text = "Distraction",
                isSelected = classification == ActivityClassification.UNPRODUCTIVE,
                accentColor = DaylogTokens.Distraction,
                onClick = { classification = ActivityClassification.UNPRODUCTIVE },
                modifier = Modifier.weight(1f)
            )
        }

        Spacer(modifier = Modifier.height(10.dp))

        // 5. Optional Note (Unobtrusive)
        if (!showNoteField) {
            Text(
                text = "+ Add note (optional)",
                style = DaylogTypography.Label.copy(
                    fontSize = 12.sp,
                    color = DaylogTokens.AuroraCyan,
                    fontWeight = FontWeight.Medium
                ),
                modifier = Modifier
                    .clip(RoundedCornerShape(6.dp))
                    .clickable { showNoteField = true }
                    .padding(vertical = 4.dp, horizontal = 2.dp)
            )
        } else {
            GlassDialogTextField(
                value = note,
                onValueChange = { note = it },
                label = "Note (optional)",
                placeholder = "Any takeaways or details..."
            )
        }

        Spacer(modifier = Modifier.height(18.dp))

        // Buttons
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            GlassButton(
                text = "Cancel",
                onClick = onDismiss,
                modifier = Modifier.weight(1f)
            )
            PrimaryButton(
                text = "Log Activity",
                enabled = name.isNotBlank() || isCustomTime,
                onClick = {
                    val (startMs, endMs) = if (isCustomTime) {
                        val s = parseTimeToEpoch(startTimeStr, defaultStart)
                        val e = parseTimeToEpoch(endTimeStr, defaultEnd)
                        Pair(s, if (e > s) e else s + selectedDurationMinutes * 60 * 1000L)
                    } else {
                        val now = System.currentTimeMillis()
                        val s = now - selectedDurationMinutes * 60 * 1000L
                        Pair(s, now)
                    }
                    val resolvedName = name.ifBlank { category }
                    onSave(
                        resolvedName,
                        category,
                        classification,
                        startMs,
                        endMs,
                        null,
                        note.ifBlank { null }
                    )
                    onDismiss()
                },
                modifier = Modifier.weight(1.3f)
            )
        }
    }
}

// ==========================================
// Dialog: Add Habit (Habits Screen)
// ==========================================

@OptIn(androidx.compose.material3.ExperimentalMaterial3Api::class)
@Composable
fun AddHabitDialog(
    onDismiss: () -> Unit,
    onSave: (name: String, type: HabitType, targetValue: Double, unit: String, reminderTime: String?) -> Unit
) {
    var name by remember { mutableStateOf("") }
    var selectedType by remember { mutableStateOf(HabitType.BOOLEAN) }
    var targetValueStr by remember { mutableStateOf("30") }
    var unit by remember { mutableStateOf("mins") }
    var alarmEnabled by remember { mutableStateOf(false) }
    var alarmTimeString by remember { mutableStateOf("08:00") }
    var showTimePicker by remember { mutableStateOf(false) }

    data class HabitPreset(val label: String, val type: HabitType, val targetVal: String, val unitStr: String)
    val presets = remember {
        listOf(
            HabitPreset("📖 Read", HabitType.DURATION, "20", "mins"),
            HabitPreset("💪 Workout", HabitType.DURATION, "30", "mins"),
            HabitPreset("🧘 Meditate", HabitType.DURATION, "15", "mins"),
            HabitPreset("💧 Hydrate", HabitType.BOOLEAN, "1", "")
        )
    }

    GlassDialogContainer(
        title = "New habit",
        subtitle = "Small steps built daily",
        onDismiss = onDismiss
    ) {
        // 1. One-tap popular habit suggestions
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            presets.forEach { p ->
                val cleanLabel = p.label.substringAfter(" ")
                val isSelected = name == cleanLabel
                GlassSelectableChip(
                    text = p.label,
                    isSelected = isSelected,
                    accentColor = DaylogTokens.Productive,
                    onClick = {
                        name = cleanLabel
                        selectedType = p.type
                        targetValueStr = p.targetVal
                        unit = p.unitStr
                    },
                    modifier = Modifier.weight(1f)
                )
            }
        }

        Spacer(modifier = Modifier.height(14.dp))

        // 2. Habit Name Input
        GlassDialogTextField(
            value = name,
            onValueChange = { name = it },
            label = "Habit name",
            placeholder = "e.g. Read 10 pages, Floss, Walk"
        )

        Spacer(modifier = Modifier.height(14.dp))

        // 3. Goal Type: Simple Check vs Timed Target
        Text(
            text = "Goal target",
            style = DaylogTypography.Label.copy(fontWeight = FontWeight.SemiBold, fontSize = 12.sp),
            color = DaylogTokens.TextSecondary
        )
        Spacer(modifier = Modifier.height(6.dp))
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            GlassSelectableChip(
                text = "✓ Daily checkmark",
                isSelected = selectedType == HabitType.BOOLEAN,
                accentColor = DaylogTokens.Productive,
                onClick = {
                    selectedType = HabitType.BOOLEAN
                    targetValueStr = "1"
                    unit = ""
                },
                modifier = Modifier.weight(1f)
            )
            GlassSelectableChip(
                text = "⏱ Timed focus",
                isSelected = selectedType == HabitType.DURATION,
                accentColor = DaylogTokens.AuroraCyan,
                onClick = {
                    selectedType = HabitType.DURATION
                    targetValueStr = "30"
                    unit = "mins"
                },
                modifier = Modifier.weight(1f)
            )
        }

        // Quick minute pills only if timed
        if (selectedType == HabitType.DURATION) {
            Spacer(modifier = Modifier.height(10.dp))
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                listOf(15, 20, 30, 45).forEach { mins ->
                    val isSelected = targetValueStr == mins.toString()
                    GlassSelectableChip(
                        text = "${mins}m",
                        isSelected = isSelected,
                        accentColor = DaylogTokens.AuroraCyan,
                        onClick = {
                            targetValueStr = mins.toString()
                            unit = "mins"
                        },
                        modifier = Modifier.weight(1f)
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(14.dp))

        // 4. Daily Alarm Reminder (Sleek row)
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(DaylogTokens.InnerRadius))
                .background(DaylogTokens.NightDeep)
                .border(
                    BorderStroke(1.dp, DaylogTokens.GlassHighlightTop),
                    RoundedCornerShape(DaylogTokens.InnerRadius)
                )
                .padding(horizontal = 14.dp, vertical = 8.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.weight(1f)
                ) {
                    Icon(
                        imageVector = Icons.Default.Alarm,
                        contentDescription = null,
                        tint = if (alarmEnabled) DaylogTokens.AuroraCyan else DaylogTokens.TextTertiary,
                        modifier = Modifier.size(18.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "Daily reminder",
                        style = DaylogTypography.Body.copy(
                            fontWeight = FontWeight.Medium,
                            fontSize = 13.sp,
                            color = DaylogTokens.TextPrimary
                        )
                    )
                }

                Row(verticalAlignment = Alignment.CenterVertically) {
                    if (alarmEnabled) {
                        Box(
                            modifier = Modifier
                                .clip(CircleShape)
                                .background(DaylogTokens.AuroraCyan.copy(alpha = 0.15f))
                                .border(BorderStroke(1.dp, DaylogTokens.AuroraCyan.copy(alpha = 0.45f)), CircleShape)
                                .clickable { showTimePicker = true }
                                .padding(horizontal = 8.dp, vertical = 4.dp)
                        ) {
                            Text(
                                text = com.example.alarm.HabitAlarmScheduler.formatDisplayTime(alarmTimeString),
                                style = DaylogTypography.Label.copy(
                                    fontWeight = FontWeight.Bold,
                                    color = DaylogTokens.AuroraCyan,
                                    fontSize = 11.sp
                                )
                            )
                        }
                        Spacer(modifier = Modifier.width(8.dp))
                    }

                    androidx.compose.material3.Switch(
                        checked = alarmEnabled,
                        onCheckedChange = { alarmEnabled = it },
                        colors = androidx.compose.material3.SwitchDefaults.colors(
                            checkedThumbColor = DaylogTokens.Night,
                            checkedTrackColor = DaylogTokens.Productive,
                            uncheckedThumbColor = DaylogTokens.TextTertiary,
                            uncheckedTrackColor = DaylogTokens.NightDeep
                        ),
                        modifier = Modifier.scale(0.85f)
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(18.dp))

        // Buttons
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            GlassButton(
                text = "Cancel",
                onClick = onDismiss,
                modifier = Modifier.weight(1f)
            )
            PrimaryButton(
                text = "Save Habit",
                enabled = name.isNotBlank(),
                onClick = {
                    val targetNum = targetValueStr.toDoubleOrNull() ?: 1.0
                    val finalReminder = if (alarmEnabled) alarmTimeString else null
                    onSave(name, selectedType, targetNum, unit, finalReminder)
                    onDismiss()
                },
                modifier = Modifier.weight(1.3f)
            )
        }
    }

    if (showTimePicker) {
        com.example.ui.screens.habits.HabitTimePickerDialog(
            habitName = if (name.isNotBlank()) name else "New Habit",
            initialTimeString = alarmTimeString,
            onDismiss = { showTimePicker = false },
            onConfirm = { chosenTime ->
                alarmTimeString = chosenTime
                showTimePicker = false
            }
        )
    }
}

// ==========================================
// Dialog: Switch Activity
// ==========================================

@Composable
fun SwitchActivityDialog(
    currentActivityName: String,
    onDismiss: () -> Unit,
    onSwitch: (name: String, category: String, classification: ActivityClassification) -> Unit
) {
    var customName by remember { mutableStateOf("") }
    var selectedClassification by remember { mutableStateOf(ActivityClassification.PRODUCTIVE) }

    val quickOptions = listOf(
        Triple("Deep Work", "WORK", ActivityClassification.PRODUCTIVE),
        Triple("Study & Prep", "STUDY", ActivityClassification.PRODUCTIVE),
        Triple("Exercise", "EXERCISE", ActivityClassification.PRODUCTIVE),
        Triple("Reading", "READING", ActivityClassification.PRODUCTIVE),
        Triple("Lunch / Meal", "FOOD", ActivityClassification.NEUTRAL),
        Triple("Rest & Break", "REST", ActivityClassification.NEUTRAL),
        Triple("Commute", "COMMUTE", ActivityClassification.NEUTRAL),
        Triple("Social Media", "SOCIAL MEDIA", ActivityClassification.UNPRODUCTIVE),
        Triple("Video Streaming", "ENTERTAINMENT", ActivityClassification.UNPRODUCTIVE),
        Triple("Random Browsing", "PHONE", ActivityClassification.UNPRODUCTIVE)
    )

    GlassDialogContainer(
        title = "Switch activity",
        subtitle = "Current: $currentActivityName",
        onDismiss = onDismiss
    ) {
        Text(
            text = "Quick switch",
            style = DaylogTypography.Label.copy(fontWeight = FontWeight.SemiBold, fontSize = 12.sp),
            color = DaylogTokens.TextSecondary
        )
        Spacer(modifier = Modifier.height(8.dp))

        quickOptions.chunked(2).forEach { rowItems ->
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                rowItems.forEach { (title, cat, cls) ->
                    val color = when (cls) {
                        ActivityClassification.PRODUCTIVE -> DaylogTokens.Productive
                        ActivityClassification.NEUTRAL -> DaylogTokens.Neutral
                        ActivityClassification.UNPRODUCTIVE -> DaylogTokens.Distraction
                        ActivityClassification.UNKNOWN -> DaylogTokens.TextSecondary
                    }
                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .height(42.dp)
                            .clip(RoundedCornerShape(12.dp))
                            .background(Color.White.copy(alpha = 0.05f))
                            .border(BorderStroke(1.dp, color.copy(alpha = 0.35f)), RoundedCornerShape(12.dp))
                            .clickable {
                                onSwitch(title, cat, cls)
                                onDismiss()
                            }
                            .padding(horizontal = 12.dp),
                        contentAlignment = Alignment.CenterStart
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Box(
                                modifier = Modifier
                                    .size(7.dp)
                                    .clip(CircleShape)
                                    .background(color)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = title,
                                style = DaylogTypography.Body.copy(
                                    fontSize = 13.sp,
                                    fontWeight = FontWeight.Medium
                                ),
                                color = DaylogTokens.TextPrimary,
                                maxLines = 1
                            )
                        }
                    }
                }
            }
            Spacer(modifier = Modifier.height(8.dp))
        }

        Spacer(modifier = Modifier.height(10.dp))
        Text(
            text = "Or custom activity",
            style = DaylogTypography.Label.copy(fontWeight = FontWeight.SemiBold, fontSize = 12.sp),
            color = DaylogTokens.TextSecondary
        )
        Spacer(modifier = Modifier.height(8.dp))

        GlassDialogTextField(
            value = customName,
            onValueChange = { customName = it },
            label = "Custom name",
            placeholder = "e.g. Architecture Review, Refactoring"
        )

        Spacer(modifier = Modifier.height(10.dp))

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            GlassSelectableChip(
                text = "Productive",
                isSelected = selectedClassification == ActivityClassification.PRODUCTIVE,
                accentColor = DaylogTokens.Productive,
                onClick = { selectedClassification = ActivityClassification.PRODUCTIVE },
                modifier = Modifier.weight(1f)
            )
            GlassSelectableChip(
                text = "Neutral",
                isSelected = selectedClassification == ActivityClassification.NEUTRAL,
                accentColor = DaylogTokens.Neutral,
                onClick = { selectedClassification = ActivityClassification.NEUTRAL },
                modifier = Modifier.weight(1f)
            )
            GlassSelectableChip(
                text = "Distraction",
                isSelected = selectedClassification == ActivityClassification.UNPRODUCTIVE,
                accentColor = DaylogTokens.Distraction,
                onClick = { selectedClassification = ActivityClassification.UNPRODUCTIVE },
                modifier = Modifier.weight(1f)
            )
        }

        Spacer(modifier = Modifier.height(18.dp))

        PrimaryButton(
            text = "Start Activity",
            enabled = customName.isNotBlank(),
            onClick = {
                onSwitch(customName, "CUSTOM", selectedClassification)
                onDismiss()
            },
            modifier = Modifier.fillMaxWidth()
        )
    }
}

// ==========================================
// Dialog: Activity Rating (Session Complete)
// ==========================================

@Composable
fun ActivityRatingDialog(
    activityName: String,
    durationText: String,
    onDismiss: () -> Unit,
    onSubmit: (rating: Int?, note: String?) -> Unit
) {
    var rating by remember { mutableIntStateOf(0) }
    var note by remember { mutableStateOf("") }

    GlassDialogContainer(
        title = "Session complete",
        subtitle = "$activityName ($durationText)",
        onDismiss = onDismiss
    ) {
        Text(
            text = "Rate this session",
            style = DaylogTypography.Label.copy(fontWeight = FontWeight.SemiBold, fontSize = 12.sp),
            color = DaylogTokens.TextSecondary
        )
        Spacer(modifier = Modifier.height(8.dp))

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            (1..5).forEach { star ->
                val isFilled = rating >= star
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .height(44.dp)
                        .clip(RoundedCornerShape(12.dp))
                        .background(
                            if (isFilled) DaylogTokens.AuroraViolet.copy(alpha = 0.28f)
                            else Color.White.copy(alpha = 0.05f)
                        )
                        .border(
                            BorderStroke(
                                1.dp,
                                if (isFilled) DaylogTokens.AuroraViolet
                                else Color.White.copy(alpha = 0.10f)
                            ),
                            RoundedCornerShape(12.dp)
                        )
                        .clickable { rating = star },
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "$star ★",
                        style = DaylogTypography.Title.copy(fontSize = 15.sp),
                        color = if (isFilled) DaylogTokens.TextPrimary else DaylogTokens.TextTertiary
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        GlassDialogTextField(
            value = note,
            onValueChange = { note = it },
            label = "Session note (optional)",
            placeholder = "What was accomplished?"
        )

        Spacer(modifier = Modifier.height(20.dp))

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            GlassButton(
                text = "Skip",
                onClick = {
                    onSubmit(null, null)
                    onDismiss()
                },
                modifier = Modifier.weight(1f)
            )
            PrimaryButton(
                text = "Save",
                onClick = {
                    onSubmit(if (rating > 0) rating else null, note.ifBlank { null })
                    onDismiss()
                },
                modifier = Modifier.weight(1f)
            )
        }
    }
}

// ==========================================
// Dialog: Day Review / Reflection
// ==========================================

@Composable
fun DayReviewDialog(
    date: String,
    onDismiss: () -> Unit,
    onSave: (wentWell: String, wentWrong: String, distractedBy: String, changeTomorrow: String, rating: Int) -> Unit
) {
    var wentWell by remember { mutableStateOf("") }
    var wentWrong by remember { mutableStateOf("") }
    var distractedBy by remember { mutableStateOf("") }
    var changeTomorrow by remember { mutableStateOf("") }
    var rating by remember { mutableIntStateOf(4) }

    GlassDialogContainer(
        title = "Evening reflection",
        subtitle = date,
        onDismiss = onDismiss
    ) {
        GlassDialogTextField(
            value = wentWell,
            onValueChange = { wentWell = it },
            label = "1. What went well today?",
            placeholder = "Completed key milestones, maintained focus..."
        )

        Spacer(modifier = Modifier.height(12.dp))

        GlassDialogTextField(
            value = wentWrong,
            onValueChange = { wentWrong = it },
            label = "2. What went off track?",
            placeholder = "Started late, lost energy after lunch..."
        )

        Spacer(modifier = Modifier.height(12.dp))

        GlassDialogTextField(
            value = distractedBy,
            onValueChange = { distractedBy = it },
            label = "3. What was the main distraction?",
            placeholder = "Notifications, meetings, social media..."
        )

        Spacer(modifier = Modifier.height(12.dp))

        GlassDialogTextField(
            value = changeTomorrow,
            onValueChange = { changeTomorrow = it },
            label = "4. What is one adjustment for tomorrow?",
            placeholder = "Plan the top 2 tasks the night before..."
        )

        Spacer(modifier = Modifier.height(14.dp))

        Text(
            text = "Overall day score",
            style = DaylogTypography.Label.copy(fontWeight = FontWeight.SemiBold, fontSize = 12.sp),
            color = DaylogTokens.TextSecondary
        )
        Spacer(modifier = Modifier.height(8.dp))

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            (1..5).forEach { star ->
                val isFilled = rating >= star
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .height(40.dp)
                        .clip(RoundedCornerShape(10.dp))
                        .background(
                            if (isFilled) DaylogTokens.AuroraViolet.copy(alpha = 0.28f)
                            else Color.White.copy(alpha = 0.05f)
                        )
                        .border(
                            BorderStroke(
                                1.dp,
                                if (isFilled) DaylogTokens.AuroraViolet
                                else Color.White.copy(alpha = 0.10f)
                            ),
                            RoundedCornerShape(10.dp)
                        )
                        .clickable { rating = star },
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "$star ★",
                        style = DaylogTypography.Label.copy(
                            fontSize = 13.sp,
                            fontWeight = FontWeight.Bold
                        ),
                        color = if (isFilled) DaylogTokens.TextPrimary else DaylogTokens.TextTertiary
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(20.dp))

        PrimaryButton(
            text = "Save Reflection",
            onClick = {
                onSave(wentWell, wentWrong, distractedBy, changeTomorrow, rating)
                onDismiss()
            },
            modifier = Modifier.fillMaxWidth()
        )
    }
}

// ==========================================
// Dialog: Set Daily Goal
// ==========================================

@Composable
fun SetGoalDialog(
    currentTitle: String,
    currentTargetHours: Int,
    onDismiss: () -> Unit,
    onSave: (title: String, targetHours: Int) -> Unit
) {
    var title by remember { mutableStateOf(currentTitle) }
    var hours by remember { mutableIntStateOf(currentTargetHours) }

    GlassDialogContainer(
        title = "Set daily goal",
        subtitle = "Align focus target for the day",
        onDismiss = onDismiss
    ) {
        GlassDialogTextField(
            value = title,
            onValueChange = { title = it },
            label = "Primary goal title",
            placeholder = "e.g. Finish sprint backlog, deep reading"
        )

        Spacer(modifier = Modifier.height(14.dp))

        Text(
            text = "Target focus hours",
            style = DaylogTypography.Label.copy(fontWeight = FontWeight.SemiBold, fontSize = 12.sp),
            color = DaylogTokens.TextSecondary
        )
        Spacer(modifier = Modifier.height(8.dp))

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            listOf(2, 4, 6, 8, 10).forEach { h ->
                GlassSelectableChip(
                    text = "${h}h",
                    isSelected = hours == h,
                    onClick = { hours = h },
                    modifier = Modifier.weight(1f)
                )
            }
        }

        Spacer(modifier = Modifier.height(20.dp))

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            GlassButton(
                text = "Cancel",
                onClick = onDismiss,
                modifier = Modifier.weight(1f)
            )
            PrimaryButton(
                text = "Set Goal",
                enabled = title.isNotBlank(),
                onClick = {
                    onSave(title, hours)
                    onDismiss()
                },
                modifier = Modifier.weight(1f)
            )
        }
    }
}

// ==========================================
// Dialog: Add Task
// ==========================================

@Composable
fun AddTaskDialog(
    onDismiss: () -> Unit,
    onSave: (title: String, priority: TaskPriority, estimatedMins: Int) -> Unit
) {
    var title by remember { mutableStateOf("") }
    var priority by remember { mutableStateOf(TaskPriority.MEDIUM) }
    var estimatedMins by remember { mutableIntStateOf(30) }

    GlassDialogContainer(
        title = "Add daily task",
        subtitle = "Prioritize intentional execution",
        onDismiss = onDismiss
    ) {
        GlassDialogTextField(
            value = title,
            onValueChange = { title = it },
            label = "Task title",
            placeholder = "e.g. Design review, Write docs"
        )

        Spacer(modifier = Modifier.height(14.dp))

        Text(
            text = "Priority",
            style = DaylogTypography.Label.copy(fontWeight = FontWeight.SemiBold, fontSize = 12.sp),
            color = DaylogTokens.TextSecondary
        )
        Spacer(modifier = Modifier.height(6.dp))

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            TaskPriority.entries.forEach { p ->
                val col = when (p) {
                    TaskPriority.HIGH -> DaylogTokens.Distraction
                    TaskPriority.MEDIUM -> DaylogTokens.Neutral
                    TaskPriority.LOW -> DaylogTokens.Productive
                }
                GlassSelectableChip(
                    text = p.name.lowercase().replaceFirstChar { it.uppercase() },
                    isSelected = priority == p,
                    accentColor = col,
                    onClick = { priority = p },
                    modifier = Modifier.weight(1f)
                )
            }
        }

        Spacer(modifier = Modifier.height(14.dp))

        Text(
            text = "Estimated minutes",
            style = DaylogTypography.Label.copy(fontWeight = FontWeight.SemiBold, fontSize = 12.sp),
            color = DaylogTokens.TextSecondary
        )
        Spacer(modifier = Modifier.height(6.dp))

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            listOf(15, 30, 45, 60, 90).forEach { m ->
                GlassSelectableChip(
                    text = "${m}m",
                    isSelected = estimatedMins == m,
                    onClick = { estimatedMins = m },
                    modifier = Modifier.weight(1f)
                )
            }
        }

        Spacer(modifier = Modifier.height(20.dp))

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            GlassButton(
                text = "Cancel",
                onClick = onDismiss,
                modifier = Modifier.weight(1f)
            )
            PrimaryButton(
                text = "Add Task",
                enabled = title.isNotBlank(),
                onClick = {
                    onSave(title, priority, estimatedMins)
                    onDismiss()
                },
                modifier = Modifier.weight(1f)
            )
        }
    }
}

// ==========================================
// Helper functions
// ==========================================

private fun parseTimeToEpoch(timeStr: String, fallbackEpoch: Long): Long {
    return try {
        val parts = timeStr.trim().split(":")
        val hrs = parts[0].toInt()
        val mins = parts[1].toInt()
        val cal = Calendar.getInstance()
        cal.timeInMillis = fallbackEpoch
        cal.set(Calendar.HOUR_OF_DAY, hrs)
        cal.set(Calendar.MINUTE, mins)
        cal.set(Calendar.SECOND, 0)
        cal.timeInMillis
    } catch (_: Exception) {
        fallbackEpoch
    }
}
