package com.example.ui.components

import androidx.activity.compose.BackHandler
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
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
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.NightlightRound
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material.icons.outlined.CheckCircle
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
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
import com.example.model.Habit
import com.example.model.HabitStatus
import com.example.model.Task
import com.example.model.TaskPriority
import com.example.ui.HabitItemUi
import com.example.ui.theme.DaylogTokens
import com.example.ui.theme.DaylogTypography
import com.example.ui.theme.FluidDropletWindow
import com.example.util.DateTimeUtils

@Composable
fun NightlyReviewNoteDialog(
    dateStr: String,
    missedHabits: List<HabitItemUi>,
    missedTasks: List<Task>,
    onToggleHabit: (habitId: String) -> Unit,
    onToggleTask: (taskId: String) -> Unit,
    onDismiss: () -> Unit
) {
    val totalMissed = missedHabits.size + missedTasks.size
    val formattedDate = DateTimeUtils.formatShortDate(dateStr)

    // Hardware back press dismisses note
    BackHandler(onBack = onDismiss)

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.Black.copy(alpha = 0.70f))
            .clickable(
                interactionSource = remember { MutableInteractionSource() },
                indication = null,
                onClick = onDismiss
            ),
        contentAlignment = Alignment.Center
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
                    .clickable(
                        interactionSource = remember { MutableInteractionSource() },
                        indication = null,
                        onClick = { /* consume click */ }
                    )
                .shadow(
                    elevation = 32.dp,
                    shape = RoundedCornerShape(DaylogTokens.StandardRadius),
                    ambientColor = DaylogTokens.AuroraViolet.copy(alpha = 0.45f),
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
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.weight(1f)
                    ) {
                        Box(
                            modifier = Modifier
                                .size(40.dp)
                                .clip(CircleShape)
                                .background(DaylogTokens.AuroraViolet.copy(alpha = 0.22f))
                                .border(
                                    BorderStroke(1.dp, DaylogTokens.AuroraViolet.copy(alpha = 0.50f)),
                                    CircleShape
                                ),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Default.NightlightRound,
                                contentDescription = null,
                                tint = DaylogTokens.AuroraCyan,
                                modifier = Modifier.size(22.dp)
                            )
                        }

                        Spacer(modifier = Modifier.width(12.dp))

                        Column {
                            Text(
                                text = "Nightly Wrap-up",
                                style = DaylogTypography.Title,
                                color = DaylogTokens.TextPrimary
                            )
                            Spacer(modifier = Modifier.height(2.dp))
                            Text(
                                text = "Daily reflection • $formattedDate",
                                style = DaylogTypography.Body.copy(fontSize = 12.sp),
                                color = DaylogTokens.TextSecondary
                            )
                        }
                    }

                    IconButton(
                        onClick = dismissWithFluid,
                        modifier = Modifier.size(36.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Close,
                            contentDescription = "Dismiss",
                            tint = DaylogTokens.TextSecondary
                        )
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                // Summary status note banner
                val bannerBg = if (totalMissed > 0) {
                    DaylogTokens.Distraction.copy(alpha = 0.12f)
                } else {
                    DaylogTokens.Productive.copy(alpha = 0.12f)
                }
                val bannerBorder = if (totalMissed > 0) {
                    DaylogTokens.Distraction.copy(alpha = 0.35f)
                } else {
                    DaylogTokens.Productive.copy(alpha = 0.35f)
                }

                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(DaylogTokens.InnerRadius))
                        .background(bannerBg)
                        .border(BorderStroke(1.dp, bannerBorder), RoundedCornerShape(DaylogTokens.InnerRadius))
                        .padding(horizontal = 14.dp, vertical = 10.dp)
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = if (totalMissed > 0) Icons.Default.Warning else Icons.Default.CheckCircle,
                            contentDescription = null,
                            tint = if (totalMissed > 0) DaylogTokens.Distraction else DaylogTokens.Productive,
                            modifier = Modifier.size(18.dp)
                        )
                        Spacer(modifier = Modifier.width(10.dp))
                        Text(
                            text = if (totalMissed > 0) {
                                "$totalMissed missed item${if (totalMissed > 1) "s" else ""} remaining for today"
                            } else {
                                "All habits and tasks completed today! Excellent execution."
                            },
                            style = DaylogTypography.Body.copy(
                                fontSize = 13.sp,
                                fontWeight = FontWeight.SemiBold,
                                color = DaylogTokens.TextPrimary
                            )
                        )
                    }
                }

                // Section: Missed Habits
                Spacer(modifier = Modifier.height(18.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "Missed Habits",
                        style = DaylogTypography.Label.copy(fontWeight = FontWeight.Bold, fontSize = 13.sp),
                        color = DaylogTokens.TextPrimary
                    )
                    Box(
                        modifier = Modifier
                            .clip(CircleShape)
                            .background(DaylogTokens.GlassFill)
                            .border(BorderStroke(1.dp, DaylogTokens.GlassStroke), CircleShape)
                            .padding(horizontal = 8.dp, vertical = 2.dp)
                    ) {
                        Text(
                            text = "${missedHabits.size} missed",
                            style = DaylogTypography.Label.copy(
                                fontSize = 11.sp,
                                color = if (missedHabits.isNotEmpty()) DaylogTokens.Distraction else DaylogTokens.Productive
                            )
                        )
                    }
                }

                Spacer(modifier = Modifier.height(8.dp))

                if (missedHabits.isEmpty()) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(DaylogTokens.InnerRadius))
                            .background(DaylogTokens.NightDeep)
                            .padding(horizontal = 14.dp, vertical = 10.dp)
                    ) {
                        Text(
                            text = "✓ All habits completed for today. Streaks preserved!",
                            style = DaylogTypography.Label.copy(fontSize = 12.sp),
                            color = DaylogTokens.Productive
                        )
                    }
                } else {
                    Column(
                        modifier = Modifier.fillMaxWidth(),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        missedHabits.forEach { habitItem ->
                            val habit = habitItem.habit
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
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Column(modifier = Modifier.weight(1f)) {
                                        Text(
                                            text = habit.name,
                                            style = DaylogTypography.Body.copy(
                                                fontSize = 14.sp,
                                                fontWeight = FontWeight.Medium,
                                                color = DaylogTokens.TextPrimary
                                            )
                                        )
                                        if (habit.currentStreak > 0) {
                                            Spacer(modifier = Modifier.height(2.dp))
                                            Text(
                                                text = "🔥 ${habit.currentStreak}d streak at risk!",
                                                style = DaylogTypography.Label.copy(
                                                    fontSize = 11.sp,
                                                    color = DaylogTokens.Neutral
                                                )
                                            )
                                        }
                                    }

                                    // Quick complete action
                                    Box(
                                        modifier = Modifier
                                            .clip(CircleShape)
                                            .background(DaylogTokens.Productive.copy(alpha = 0.15f))
                                            .border(
                                                BorderStroke(1.dp, DaylogTokens.Productive.copy(alpha = 0.5f)),
                                                CircleShape
                                            )
                                            .clickable { onToggleHabit(habit.id) }
                                            .padding(horizontal = 12.dp, vertical = 6.dp)
                                    ) {
                                        Row(verticalAlignment = Alignment.CenterVertically) {
                                            Icon(
                                                imageVector = Icons.Default.Check,
                                                contentDescription = "Mark done",
                                                tint = DaylogTokens.Productive,
                                                modifier = Modifier.size(14.dp)
                                            )
                                            Spacer(modifier = Modifier.width(4.dp))
                                            Text(
                                                text = "Mark Done",
                                                style = DaylogTypography.Label.copy(
                                                    fontSize = 11.sp,
                                                    fontWeight = FontWeight.Bold,
                                                    color = DaylogTokens.Productive
                                                )
                                            )
                                        }
                                    }
                                }
                            }
                        }
                    }
                }

                // Section: Missed Tasks
                Spacer(modifier = Modifier.height(18.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "Incomplete Tasks",
                        style = DaylogTypography.Label.copy(fontWeight = FontWeight.Bold, fontSize = 13.sp),
                        color = DaylogTokens.TextPrimary
                    )
                    Box(
                        modifier = Modifier
                            .clip(CircleShape)
                            .background(DaylogTokens.GlassFill)
                            .border(BorderStroke(1.dp, DaylogTokens.GlassStroke), CircleShape)
                            .padding(horizontal = 8.dp, vertical = 2.dp)
                    ) {
                        Text(
                            text = "${missedTasks.size} incomplete",
                            style = DaylogTypography.Label.copy(
                                fontSize = 11.sp,
                                color = if (missedTasks.isNotEmpty()) DaylogTokens.Distraction else DaylogTokens.Productive
                            )
                        )
                    }
                }

                Spacer(modifier = Modifier.height(8.dp))

                if (missedTasks.isEmpty()) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(DaylogTokens.InnerRadius))
                            .background(DaylogTokens.NightDeep)
                            .padding(horizontal = 14.dp, vertical = 10.dp)
                    ) {
                        Text(
                            text = "✓ All tasks completed for today.",
                            style = DaylogTypography.Label.copy(fontSize = 12.sp),
                            color = DaylogTokens.Productive
                        )
                    }
                } else {
                    Column(
                        modifier = Modifier.fillMaxWidth(),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        missedTasks.forEach { task ->
                            val priorityColor = when (task.priority) {
                                TaskPriority.HIGH -> DaylogTokens.Distraction
                                TaskPriority.MEDIUM -> DaylogTokens.Neutral
                                TaskPriority.LOW -> DaylogTokens.Productive
                            }

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
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Column(modifier = Modifier.weight(1f)) {
                                        Text(
                                            text = task.title,
                                            style = DaylogTypography.Body.copy(
                                                fontSize = 14.sp,
                                                fontWeight = FontWeight.Medium,
                                                color = DaylogTokens.TextPrimary
                                            )
                                        )
                                        Spacer(modifier = Modifier.height(3.dp))
                                        Row(verticalAlignment = Alignment.CenterVertically) {
                                            Box(
                                                modifier = Modifier
                                                    .size(6.dp)
                                                    .clip(CircleShape)
                                                    .background(priorityColor)
                                            )
                                            Spacer(modifier = Modifier.width(6.dp))
                                            Text(
                                                text = "${task.priority.name} • ${task.estimatedMinutes}m est.",
                                                style = DaylogTypography.Label.copy(
                                                    fontSize = 11.sp,
                                                    color = DaylogTokens.TextTertiary
                                                )
                                            )
                                        }
                                    }

                                    // Quick complete checkbox
                                    Box(
                                        modifier = Modifier
                                            .clip(CircleShape)
                                            .background(DaylogTokens.GlassFill)
                                            .border(BorderStroke(1.dp, DaylogTokens.GlassStroke), CircleShape)
                                            .clickable { onToggleTask(task.id) }
                                            .padding(horizontal = 12.dp, vertical = 6.dp)
                                    ) {
                                        Row(verticalAlignment = Alignment.CenterVertically) {
                                            Icon(
                                                imageVector = Icons.Default.Check,
                                                contentDescription = "Complete task",
                                                tint = DaylogTokens.Productive,
                                                modifier = Modifier.size(14.dp)
                                            )
                                            Spacer(modifier = Modifier.width(4.dp))
                                            Text(
                                                text = "Done",
                                                style = DaylogTypography.Label.copy(
                                                    fontSize = 11.sp,
                                                    fontWeight = FontWeight.Bold,
                                                    color = DaylogTokens.Productive
                                                )
                                            )
                                        }
                                    }
                                }
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(22.dp))

                // Dismiss / Close button
                PrimaryButton(
                    text = "Acknowledge & Close Note",
                    onClick = dismissWithFluid,
                    modifier = Modifier.fillMaxWidth()
                )
            }
        }
        }
    }
}
