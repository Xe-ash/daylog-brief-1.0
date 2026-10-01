package com.example.ui

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
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
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import com.example.model.HabitStatus
import com.example.ui.components.AuroraBackground
import com.example.ui.components.GlassBottomBar
import com.example.ui.components.NightlyReviewNoteDialog
import com.example.ui.screens.analytics.AnalyticsScreen
import com.example.ui.screens.auth.AuthScreen
import com.example.ui.screens.habits.HabitsScreen
import com.example.ui.screens.history.HistoryScreen
import com.example.ui.screens.settings.SettingsScreen
import com.example.ui.screens.today.TodayScreen
import com.example.ui.theme.DaylogTheme
import dev.chrisbanes.haze.HazeState

enum class DaylogTab(
    val label: String,
    val selectedIcon: ImageVector,
    val unselectedIcon: ImageVector
) {
    TODAY("Today", Icons.Filled.Schedule, Icons.Outlined.Schedule),
    HISTORY("History", Icons.Filled.CalendarMonth, Icons.Outlined.CalendarMonth),
    ANALYTICS("Analytics", Icons.Filled.Assessment, Icons.Outlined.Assessment),
    HABITS("Habits", Icons.Filled.CheckCircle, Icons.Outlined.CheckCircle),
    SETTINGS("Settings", Icons.Filled.Settings, Icons.Outlined.Settings)
}

@Composable
fun DaylogApp(
    viewModel: DaylogViewModel
) {
    val hazeState = remember { HazeState() }
    var currentTab by remember { mutableStateOf(DaylogTab.TODAY) }
    var showAuthScreen by remember { mutableStateOf(false) }

    val showNightlyNote by viewModel.showNightlyReviewNote.collectAsState()
    val habitsWithStatus by viewModel.selectedDateHabits.collectAsState()
    val todayTasks by viewModel.selectedDateTasks.collectAsState()
    val selectedDate by viewModel.selectedDate.collectAsState()

    val missedHabits = remember(habitsWithStatus) {
        habitsWithStatus.filter { it.status != HabitStatus.COMPLETED }
    }
    val missedTasks = remember(todayTasks) {
        todayTasks.filter { !it.completed }
    }

    DaylogTheme(darkTheme = true) {
        AuroraBackground(hazeState = hazeState) {
            if (showAuthScreen) {
                AuthScreen(
                    viewModel = viewModel,
                    onAuthSuccess = { showAuthScreen = false },
                    onContinueOffline = { showAuthScreen = false }
                )
            } else {
                Box(modifier = Modifier.fillMaxSize()) {
                    // Tab content with 200ms crossfade
                    AnimatedContent(
                        targetState = currentTab,
                        transitionSpec = {
                            fadeIn(animationSpec = tween(200)) togetherWith
                                    fadeOut(animationSpec = tween(200))
                        },
                        label = "tab_crossfade",
                        modifier = Modifier.fillMaxSize()
                    ) { tab ->
                        when (tab) {
                            DaylogTab.TODAY -> TodayScreen(viewModel = viewModel)
                            DaylogTab.HISTORY -> HistoryScreen(
                                viewModel = viewModel,
                                onDateSelectedForView = {
                                    currentTab = DaylogTab.TODAY
                                }
                            )
                            DaylogTab.ANALYTICS -> AnalyticsScreen(viewModel = viewModel)
                            DaylogTab.HABITS -> HabitsScreen(viewModel = viewModel)
                            DaylogTab.SETTINGS -> SettingsScreen(
                                viewModel = viewModel,
                                onNavigateToAuth = { showAuthScreen = true }
                            )
                        }
                    }

                    // Floating Glass Bottom Bar
                    GlassBottomBar(
                        currentTab = currentTab,
                        onTabSelected = { currentTab = it },
                        modifier = Modifier.align(Alignment.BottomCenter)
                    )

                    // In-app Nightly Review Note Popup
                    if (showNightlyNote) {
                        NightlyReviewNoteDialog(
                            dateStr = selectedDate,
                            missedHabits = missedHabits,
                            missedTasks = missedTasks,
                            onToggleHabit = { habitId -> viewModel.toggleHabit(habitId) },
                            onToggleTask = { taskId ->
                                val task = todayTasks.find { it.id == taskId }
                                val newStatus = !(task?.completed ?: false)
                                viewModel.toggleTask(taskId, newStatus)
                            },
                            onDismiss = { viewModel.dismissNightlyReviewNote(recordDismissal = true) }
                        )
                    }
                }
            }
        }
    }
}
