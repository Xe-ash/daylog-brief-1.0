package com.example.ui.screens.habits

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Alarm
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Schedule
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.material3.TimePicker
import androidx.compose.material3.TimePickerDefaults
import androidx.compose.material3.rememberTimePickerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.example.alarm.HabitAlarmScheduler
import com.example.ui.components.GlassButton
import com.example.ui.components.PrimaryButton
import com.example.ui.theme.DaylogTokens
import com.example.ui.theme.DaylogTypography
import com.example.ui.theme.FluidDropletWindow
import java.util.Calendar
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HabitTimePickerDialog(
    habitName: String,
    initialTimeString: String? = null,
    onDismiss: () -> Unit,
    onConfirm: (timeString: String) -> Unit,
    onDisableAlarm: (() -> Unit)? = null
) {
    // Parse initial hour and minute
    val (initialHour, initialMinute) = remember(initialTimeString) {
        if (!initialTimeString.isNullOrBlank() && initialTimeString.contains(":")) {
            val parts = initialTimeString.split(":")
            val h = parts.getOrNull(0)?.toIntOrNull() ?: 8
            val m = parts.getOrNull(1)?.toIntOrNull() ?: 0
            Pair(h, m)
        } else {
            val cal = Calendar.getInstance()
            // Default to next nearest round hour or 8:00 AM
            Pair(cal.get(Calendar.HOUR_OF_DAY), cal.get(Calendar.MINUTE))
        }
    }

    val timePickerState = rememberTimePickerState(
        initialHour = initialHour,
        initialMinute = initialMinute,
        is24Hour = false
    )

    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(usePlatformDefaultWidth = false)
    ) {
        FluidDropletWindow(
            onDismissRequest = onDismiss,
            initialCornerRadius = 42.dp,
            settledCornerRadius = DaylogTokens.StandardRadius,
            modifier = Modifier
                .fillMaxWidth(0.94f)
                .wrapContentHeight()
        ) { dismissWithFluid ->
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .wrapContentHeight()
                .shadow(
                    elevation = 28.dp,
                    shape = RoundedCornerShape(DaylogTokens.StandardRadius),
                    ambientColor = DaylogTokens.AuroraViolet.copy(alpha = 0.35f),
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
                    .verticalScroll(rememberScrollState()),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                // Header
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.weight(1f)
                    ) {
                        Box(
                            modifier = Modifier
                                .size(36.dp)
                                .clip(CircleShape)
                                .background(DaylogTokens.AuroraViolet.copy(alpha = 0.20f))
                                .border(
                                    BorderStroke(1.dp, DaylogTokens.AuroraViolet.copy(alpha = 0.40f)),
                                    CircleShape
                                ),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Default.Alarm,
                                contentDescription = null,
                                tint = DaylogTokens.AuroraViolet,
                                modifier = Modifier.size(20.dp)
                            )
                        }

                        Spacer(modifier = Modifier.width(12.dp))

                        Column {
                            Text(
                                text = "Set habit alarm",
                                style = DaylogTypography.Title,
                                color = DaylogTokens.TextPrimary
                            )
                            Spacer(modifier = Modifier.height(2.dp))
                            Text(
                                text = habitName,
                                style = DaylogTypography.Body.copy(fontSize = 13.sp),
                                color = DaylogTokens.TextSecondary,
                                maxLines = 1
                            )
                        }
                    }

                    IconButton(
                        onClick = dismissWithFluid,
                        modifier = Modifier.size(36.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Close,
                            contentDescription = "Close",
                            tint = DaylogTokens.TextSecondary
                        )
                    }
                }

                Spacer(modifier = Modifier.height(18.dp))

                // Material 3 TimePicker with Aurora color styling
                TimePicker(
                    state = timePickerState,
                    colors = TimePickerDefaults.colors(
                        clockDialColor = DaylogTokens.NightDeep,
                        clockDialSelectedContentColor = DaylogTokens.Night,
                        clockDialUnselectedContentColor = DaylogTokens.TextPrimary,
                        selectorColor = DaylogTokens.AuroraViolet,
                        containerColor = DaylogTokens.NightElevated,
                        periodSelectorBorderColor = DaylogTokens.AuroraViolet.copy(alpha = 0.4f),
                        periodSelectorSelectedContainerColor = DaylogTokens.AuroraViolet,
                        periodSelectorUnselectedContainerColor = DaylogTokens.NightDeep,
                        periodSelectorSelectedContentColor = DaylogTokens.TextPrimary,
                        periodSelectorUnselectedContentColor = DaylogTokens.TextSecondary,
                        timeSelectorSelectedContainerColor = DaylogTokens.AuroraViolet.copy(alpha = 0.30f),
                        timeSelectorUnselectedContainerColor = DaylogTokens.NightDeep,
                        timeSelectorSelectedContentColor = DaylogTokens.TextPrimary,
                        timeSelectorUnselectedContentColor = DaylogTokens.TextSecondary
                    )
                )

                Spacer(modifier = Modifier.height(14.dp))

                // Helpful note about precision and edge case
                val formattedPreview = String.format(
                    Locale.US,
                    "%02d:%02d",
                    timePickerState.hour,
                    timePickerState.minute
                )
                val displayFriendly = HabitAlarmScheduler.formatDisplayTime(formattedPreview)

                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(DaylogTokens.InnerRadius))
                        .background(DaylogTokens.NightDeep)
                        .border(
                            BorderStroke(1.dp, DaylogTokens.GlassHighlightTop),
                            RoundedCornerShape(DaylogTokens.InnerRadius)
                        )
                        .padding(horizontal = 14.dp, vertical = 10.dp)
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = Icons.Default.Schedule,
                            contentDescription = null,
                            tint = DaylogTokens.AuroraCyan,
                            modifier = Modifier.size(16.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "Fires daily at $displayFriendly with audio alert",
                            style = DaylogTypography.Label.copy(fontSize = 12.sp),
                            color = DaylogTokens.TextSecondary
                        )
                    }
                }

                Spacer(modifier = Modifier.height(20.dp))

                // Buttons
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    if (onDisableAlarm != null && !initialTimeString.isNullOrBlank()) {
                        GlassButton(
                            text = "Turn Off",
                            onClick = {
                                onDisableAlarm()
                                dismissWithFluid()
                            },
                            modifier = Modifier.weight(1f)
                        )
                    } else {
                        GlassButton(
                            text = "Cancel",
                            onClick = dismissWithFluid,
                            modifier = Modifier.weight(1f)
                        )
                    }

                    PrimaryButton(
                        text = "Set Alarm",
                        onClick = {
                            val timeStr = String.format(
                                Locale.US,
                                "%02d:%02d",
                                timePickerState.hour,
                                timePickerState.minute
                            )
                            onConfirm(timeStr)
                            dismissWithFluid()
                        },
                        modifier = Modifier.weight(1f)
                    )
                }
            }
        }
        }
    }
}
