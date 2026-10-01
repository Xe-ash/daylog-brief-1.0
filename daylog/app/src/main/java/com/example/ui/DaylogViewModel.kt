package com.example.ui

import android.app.Application
import android.content.Context
import android.content.Intent
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.alarm.HabitAlarmReceiver
import com.example.alarm.HabitAlarmScheduler
import com.example.data.AuthRepository
import com.example.data.AuthState
import com.example.data.DaylogRepository
import com.example.data.FirestoreDebugManager
import com.example.data.FirestoreWriteLog
import com.example.data.SyncState
import com.example.model.Activity
import com.example.model.ActivityClassification
import com.example.model.ActivityStatus
import com.example.model.DailyGoal
import com.example.model.DayNote
import com.example.model.DayReflection
import com.example.model.DaySummary
import com.example.model.Habit
import com.example.model.HabitFrequency
import com.example.model.HabitLog
import com.example.model.HabitStatus
import com.example.model.HabitType
import com.example.model.Task
import com.example.model.TaskPriority
import com.example.model.TimeGap
import com.example.model.UserProfile
import com.example.util.BrutalistMetrics
import com.example.util.DateTimeUtils
import com.example.util.NightlyReviewManager
import com.google.firebase.auth.FirebaseUser
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch

data class HabitItemUi(
    val habit: Habit,
    val log: HabitLog?,
    val status: HabitStatus
)

enum class AnalyticsRange(val label: String, val days: Int) {
    SEVEN_DAYS("7 DAYS", 7),
    THIRTY_DAYS("30 DAYS", 30),
    NINETY_DAYS("90 DAYS", 90),
    ALL_TIME("ALL TIME", 365)
}

data class AnalyticsState(
    val totalTrackedHours: Double = 0.0,
    val productiveHours: Double = 0.0,
    val neutralHours: Double = 0.0,
    val unproductiveHours: Double = 0.0,
    val untrackedHours: Double = 0.0,
    val averageProductiveHoursPerDay: Double = 0.0,
    val averageUnproductiveHoursPerDay: Double = 0.0,
    val averageGoalCompletionPercent: Double = 0.0,
    val averageHabitConsistencyPercent: Double = 0.0,
    val trackingConsistencyDays: String = "0 / 7",
    val trackingConsistencyPercent: Double = 0.0,
    val currentStreakDays: Int = 0,
    val habitStreakDays: Int = 0,
    val goalStreakDays: Int = 0,
    val bestDayDate: String = "",
    val bestDayProductiveHours: Double = 0.0,
    val bestDayGoalPercent: Double = 0.0,
    val mostDistractedDayDate: String = "",
    val mostDistractedUnproductiveHours: Double = 0.0,
    val categoryBreakdown: List<Pair<String, Long>> = emptyMap<String, Long>().toList(),
    val timeLeaks: List<Pair<String, Long>> = emptyList(),
    val opportunityCostItems: List<String> = emptyList(),
    val dailyProductivityPoints: List<Pair<String, Long>> = emptyList(),
    val dayDistributionBars: List<DaySummary> = emptyList(),
    val insights: List<String> = emptyList()
)

class DaylogViewModel(
    private val authRepository: AuthRepository,
    private val daylogRepository: DaylogRepository,
    private val appContext: Context? = null
) : ViewModel() {

    val authState = MutableStateFlow<AuthState>(AuthState.Loading)
    val userProfile: StateFlow<UserProfile> = daylogRepository.userProfile

    private val _selectedDate = MutableStateFlow(DateTimeUtils.getTodayDateString())
    val selectedDate: StateFlow<String> = _selectedDate.asStateFlow()

    private val _analyticsRange = MutableStateFlow(AnalyticsRange.SEVEN_DAYS)
    val analyticsRange: StateFlow<AnalyticsRange> = _analyticsRange.asStateFlow()

    val activities: StateFlow<List<Activity>> = daylogRepository.activities
    val habits: StateFlow<List<Habit>> = daylogRepository.habits
    val habitLogs: StateFlow<List<HabitLog>> = daylogRepository.habitLogs
    val tasks: StateFlow<List<Task>> = daylogRepository.tasks
    val dailyGoals: StateFlow<Map<String, DailyGoal>> = daylogRepository.dailyGoals
    val dayNotes: StateFlow<List<DayNote>> = daylogRepository.dayNotes
    val dayReflections: StateFlow<Map<String, DayReflection>> = daylogRepository.dayReflections

    // In-app Nightly Review Note State
    private val _showNightlyReviewNote = MutableStateFlow(false)
    val showNightlyReviewNote: StateFlow<Boolean> = _showNightlyReviewNote.asStateFlow()

    // Cloud Sync & Firestore Write Diagnostics
    val syncState: StateFlow<SyncState> = FirestoreDebugManager.syncState
    val writeLogs: StateFlow<List<FirestoreWriteLog>> = FirestoreDebugManager.writeLogs
    val lastSyncTimestamp: StateFlow<Long?> = FirestoreDebugManager.lastSyncTimestamp
    val totalWritesSucceeded: StateFlow<Int> = FirestoreDebugManager.totalSucceeded
    val totalWritesFailed: StateFlow<Int> = FirestoreDebugManager.totalFailed

    // Running activity (event-driven, isolated from ticker, cached for instant UI)
    val runningActivity: StateFlow<Activity?> = combine(activities, _selectedDate) { acts, _ ->
        acts.find { it.status == ActivityStatus.RUNNING }
    }.distinctUntilChanged().stateIn(viewModelScope, SharingStarted.Eagerly, null)

    // Selected date's activities (instant memory cache)
    val selectedDateActivities: StateFlow<List<Activity>> = combine(activities, _selectedDate) { acts, date ->
        acts.filter { it.date == date }.sortedBy { it.startedAt }
    }.distinctUntilChanged().stateIn(viewModelScope, SharingStarted.Eagerly, emptyList())

    // Selected date's daily goal [VM FIX: default to user profile daily target]
    val selectedDateGoal: StateFlow<DailyGoal?> = combine(dailyGoals, userProfile, _selectedDate) { goals, profile, date ->
        goals[date] ?: DailyGoal(
            id = "default_$date",
            date = date,
            title = "Complete ${profile.defaultDailyGoalMinutes / 60} focused hours of work.",
            targetMinutes = profile.defaultDailyGoalMinutes
        )
    }.distinctUntilChanged().stateIn(viewModelScope, SharingStarted.Eagerly, null)

    // Selected date's habits
    val selectedDateHabits: StateFlow<List<HabitItemUi>> = combine(habits, habitLogs, _selectedDate) { hList, lList, date ->
        hList.map { habit ->
            val log = lList.find { it.habitId == habit.id && it.date == date }
            val status = log?.status ?: HabitStatus.NOT_COMPLETED
            HabitItemUi(habit, log, status)
        }
    }.distinctUntilChanged().stateIn(viewModelScope, SharingStarted.Eagerly, emptyList())

    // Selected date's tasks
    val selectedDateTasks: StateFlow<List<Task>> = combine(tasks, _selectedDate) { tList, date ->
        tList.filter { it.date == date }
    }.distinctUntilChanged().stateIn(viewModelScope, SharingStarted.Eagerly, emptyList())

    // Selected date's notes
    val selectedDateNotes: StateFlow<List<DayNote>> = combine(dayNotes, _selectedDate) { nList, date ->
        nList.filter { it.date == date }
    }.distinctUntilChanged().stateIn(viewModelScope, SharingStarted.Eagerly, emptyList())

    // Selected date's reflection
    val selectedDateReflection: StateFlow<DayReflection?> = combine(dayReflections, _selectedDate) { rMap, date ->
        rMap[date]
    }.distinctUntilChanged().stateIn(viewModelScope, SharingStarted.Eagerly, null)

    // Untracked gaps
    val untrackedGaps: StateFlow<List<TimeGap>> = combine(selectedDateActivities, _selectedDate) { acts, date ->
        DateTimeUtils.findUntrackedGaps(acts, date)
    }.distinctUntilChanged().stateIn(viewModelScope, SharingStarted.Eagerly, emptyList())

    // Selected date DaySummary (computed on activity/habit/goal events)
    val selectedDaySummary: StateFlow<DaySummary> = combine(
        selectedDateActivities,
        selectedDateGoal,
        selectedDateHabits,
        _selectedDate
    ) { acts, goal, habitsWithStatus, date ->
        val goalTitle = goal?.title ?: "Complete daily focus target"
        val targetMins = goal?.targetMinutes ?: 360
        BrutalistMetrics.calculateDaySummary(
            date = date,
            activities = acts,
            targetProductiveMinutes = targetMins,
            primaryGoalText = goalTitle,
            habits = habitsWithStatus.map { it.habit },
            habitLogs = habitsWithStatus.mapNotNull { it.log },
            nowMillis = System.currentTimeMillis()
        )
    }.distinctUntilChanged().stateIn(
        viewModelScope,
        SharingStarted.Eagerly,
        DaySummary(date = DateTimeUtils.getTodayDateString())
    )

    // Analytics computation (pre-cached so tab switches load instantly)
    val analyticsState: StateFlow<AnalyticsState> = combine(
        activities,
        dailyGoals,
        habits,
        habitLogs,
        _analyticsRange
    ) { acts, goals, hList, lList, range ->
        computeAnalytics(acts, goals, hList, lList, range)
    }.distinctUntilChanged().stateIn(viewModelScope, SharingStarted.Eagerly, AnalyticsState())

    init {
        // Monitor Auth state
        viewModelScope.launch {
            authRepository.getAuthStateFlow().collect { user ->
                if (user != null) {
                    daylogRepository.setUserId(user.uid)
                    val profile = UserProfile(
                        uid = user.uid,
                        email = user.email ?: "",
                        displayName = user.displayName ?: user.email?.substringBefore("@") ?: "User",
                        photoURL = user.photoUrl?.toString(),
                        onboardingCompleted = true
                    )
                    authState.value = AuthState.Authenticated(user, profile)
                } else {
                    daylogRepository.setUserId("local_guest")
                    authState.value = AuthState.Unauthenticated
                }
            }
        }

        // Auto-popup nightly review note if currently night time (>= 20:00 or < 04:00) and not dismissed today
        viewModelScope.launch {
            delay(1000)
            if (NightlyReviewManager.isNightTime()) {
                val today = DateTimeUtils.getTodayDateString()
                val isDismissed = appContext?.let { NightlyReviewManager.isDismissedForDate(it, today) } ?: false
                if (!isDismissed) {
                    _showNightlyReviewNote.value = true
                }
            }
        }
    }

    fun setSelectedDate(dateStr: String) {
        _selectedDate.value = dateStr
    }

    fun setAnalyticsRange(range: AnalyticsRange) {
        _analyticsRange.value = range
    }

    // --- ACTIVITY ACTIONS ---

    fun startActivity(name: String, category: String, classification: ActivityClassification) {
        viewModelScope.launch {
            daylogRepository.startActivity(
                name = name,
                categoryName = category,
                classification = classification,
                date = _selectedDate.value
            )
        }
    }

    fun stopActivity(activityId: String, qualityRating: Int? = null, note: String? = null) {
        viewModelScope.launch {
            daylogRepository.stopActivity(
                activityId = activityId,
                endedAt = System.currentTimeMillis(),
                qualityRating = qualityRating,
                note = note
            )
        }
    }

    fun switchActivity(name: String, category: String, classification: ActivityClassification) {
        viewModelScope.launch {
            daylogRepository.switchActivity(
                newActivityName = name,
                newCategoryName = category,
                newClassification = classification,
                date = _selectedDate.value
            )
        }
    }

    fun pauseActivity(activityId: String) {
        viewModelScope.launch {
            daylogRepository.pauseActivity(activityId)
        }
    }

    fun resumeActivity(activityId: String) {
        viewModelScope.launch {
            daylogRepository.resumeActivity(activityId)
        }
    }

    fun addManualActivity(
        name: String,
        category: String,
        classification: ActivityClassification,
        startMillis: Long,
        endMillis: Long,
        qualityRating: Int? = null,
        note: String? = null
    ) {
        viewModelScope.launch {
            daylogRepository.addManualActivity(
                name = name,
                categoryName = category,
                classification = classification,
                date = _selectedDate.value,
                startTimeMillis = startMillis,
                endTimeMillis = endMillis,
                qualityRating = qualityRating,
                note = note
            )
        }
    }

    fun updateActivity(activity: Activity) {
        viewModelScope.launch {
            daylogRepository.updateActivity(activity)
        }
    }

    fun deleteActivity(activityId: String) {
        viewModelScope.launch {
            daylogRepository.deleteActivity(activityId)
        }
    }

    // --- GOAL ACTIONS ---

    fun setDailyGoal(title: String, targetHours: Int) {
        viewModelScope.launch {
            daylogRepository.setDailyGoal(
                date = _selectedDate.value,
                title = title,
                targetMinutes = targetHours * 60
            )
        }
    }

    // --- HABITS ACTIONS ---

    fun toggleHabit(habitId: String, targetStatus: HabitStatus? = null) {
        viewModelScope.launch {
            daylogRepository.toggleHabitStatus(
                habitId = habitId,
                date = _selectedDate.value,
                targetStatus = targetStatus
            )
        }
    }

    fun addHabit(
        name: String,
        type: HabitType,
        targetValue: Double,
        unit: String,
        reminderTime: String? = null
    ) {
        viewModelScope.launch {
            val created = daylogRepository.addHabit(
                name = name,
                type = type,
                targetValue = targetValue,
                unit = unit,
                reminderTime = reminderTime
            )
            if (reminderTime != null && appContext != null) {
                HabitAlarmScheduler.scheduleHabitAlarm(
                    context = appContext,
                    habitId = created.id,
                    habitName = created.name,
                    timeString = reminderTime
                )
            }
        }
    }

    fun setHabitAlarm(habitId: String, reminderTime: String) {
        viewModelScope.launch {
            daylogRepository.updateHabitReminder(habitId, reminderTime)
            val habit = habits.value.find { it.id == habitId }
            val habitName = habit?.name ?: "Daily Habit"
            if (appContext != null) {
                HabitAlarmScheduler.scheduleHabitAlarm(
                    context = appContext,
                    habitId = habitId,
                    habitName = habitName,
                    timeString = reminderTime
                )
            }
        }
    }

    fun cancelHabitAlarm(habitId: String) {
        viewModelScope.launch {
            daylogRepository.updateHabitReminder(habitId, null)
            if (appContext != null) {
                HabitAlarmScheduler.cancelHabitAlarm(appContext, habitId)
            }
        }
    }

    fun testFireHabitAlarm(habitId: String) {
        if (appContext != null) {
            val habit = habits.value.find { it.id == habitId }
            val habitName = habit?.name ?: "Habit"
            val intent = Intent(appContext, HabitAlarmReceiver::class.java).apply {
                action = HabitAlarmReceiver.ACTION_HABIT_ALARM
                putExtra(HabitAlarmReceiver.EXTRA_HABIT_ID, habitId)
                putExtra(HabitAlarmReceiver.EXTRA_HABIT_NAME, habitName)
                putExtra(HabitAlarmReceiver.EXTRA_REMINDER_TIME, habit?.reminderTime)
            }
            appContext.sendBroadcast(intent)
        }
    }

    fun deleteHabit(habitId: String) {
        viewModelScope.launch {
            if (appContext != null) {
                HabitAlarmScheduler.cancelHabitAlarm(appContext, habitId)
            }
            daylogRepository.deleteHabit(habitId)
        }
    }

    // --- NIGHTLY REVIEW ACTIONS ---

    fun openNightlyReviewNote() {
        _showNightlyReviewNote.value = true
    }

    fun dismissNightlyReviewNote(recordDismissal: Boolean = true) {
        if (recordDismissal && appContext != null) {
            NightlyReviewManager.dismissForDate(appContext, _selectedDate.value)
        }
        _showNightlyReviewNote.value = false
    }

    // --- TASKS ACTIONS ---

    fun addTask(title: String, priority: TaskPriority, estimatedMinutes: Int) {
        viewModelScope.launch {
            daylogRepository.addTask(
                title = title,
                date = _selectedDate.value,
                priority = priority,
                estimatedMinutes = estimatedMinutes
            )
        }
    }

    fun toggleTask(taskId: String, completed: Boolean) {
        viewModelScope.launch {
            daylogRepository.toggleTask(taskId, completed)
        }
    }

    fun deleteTask(taskId: String) {
        viewModelScope.launch {
            daylogRepository.deleteTask(taskId)
        }
    }

    // --- NOTES & REFLECTION ---

    fun saveNote(content: String, title: String? = null) {
        viewModelScope.launch {
            daylogRepository.saveNote(
                date = _selectedDate.value,
                content = content,
                title = title
            )
        }
    }

    fun deleteNote(noteId: String) {
        viewModelScope.launch {
            daylogRepository.deleteNote(noteId)
        }
    }

    fun saveReflection(
        wentWell: String,
        wentWrong: String,
        distractedBy: String,
        changeTomorrow: String,
        dayRating: Int
    ) {
        viewModelScope.launch {
            daylogRepository.saveDayReflection(
                date = _selectedDate.value,
                wentWell = wentWell,
                wentWrong = wentWrong,
                distractedBy = distractedBy,
                changeTomorrow = changeTomorrow,
                dayRating = dayRating
            )
        }
    }

    // --- AUTHENTICATION ---

    suspend fun signInWithEmail(email: String, pass: String): Result<FirebaseUser> {
        return authRepository.signInWithEmail(email, pass)
    }

    suspend fun signUpWithEmail(email: String, pass: String, displayName: String = ""): Result<FirebaseUser> {
        return authRepository.signUpWithEmail(email, pass)
    }

    suspend fun sendPasswordReset(email: String): Result<Unit> {
        return authRepository.sendPasswordReset(email)
    }

    suspend fun signInWithGoogle(): Result<FirebaseUser> {
        return authRepository.signInWithGoogle()
    }

    fun signOut() {
        viewModelScope.launch {
            authRepository.signOut()
        }
    }

    fun deleteAccount(onError: (String) -> Unit) {
        viewModelScope.launch {
            val res = authRepository.deleteAccount()
            if (res.isFailure) {
                onError(res.exceptionOrNull()?.message ?: "Failed to delete account.")
            }
        }
    }

    fun updateProfile(userProfile: UserProfile) {
        viewModelScope.launch {
            daylogRepository.updateUserProfile(userProfile)
        }
    }



    // --- ANALYTICS ENGINE ---

    private fun computeAnalytics(
        acts: List<Activity>,
        goals: Map<String, DailyGoal>,
        hList: List<Habit>,
        lList: List<HabitLog>,
        range: AnalyticsRange
    ): AnalyticsState {
        val daysCount = range.days
        val rangeDates = (0 until daysCount).map { offset ->
            DateTimeUtils.getDateStringWithOffset(-offset)
        }.reversed()

        val filteredActs = acts.filter { it.date in rangeDates }
        val prodSec = filteredActs.filter { it.classification == ActivityClassification.PRODUCTIVE }.sumOf { it.durationSeconds }
        val neutSec = filteredActs.filter { it.classification == ActivityClassification.NEUTRAL }.sumOf { it.durationSeconds }
        val unprodSec = filteredActs.filter { it.classification == ActivityClassification.UNPRODUCTIVE }.sumOf { it.durationSeconds }
        val totalTrackedSec = prodSec + neutSec + unprodSec
        val prodHours = prodSec / 3600.0
        val neutHours = neutSec / 3600.0
        val unprodHours = unprodSec / 3600.0
        val totalTrackedHours = totalTrackedSec / 3600.0

        val avgProdHours = if (daysCount > 0) prodHours / daysCount else 0.0
        val avgUnprodHours = if (daysCount > 0) unprodHours / daysCount else 0.0

        val daySummaries = rangeDates.map { date ->
            val dayActs = filteredActs.filter { it.date == date }
            val goal = goals[date]
            val targetMins = goal?.targetMinutes ?: 360
            val goalTitle = goal?.title ?: "Complete daily focus target"
            BrutalistMetrics.calculateDaySummary(
                date = date,
                activities = dayActs,
                targetProductiveMinutes = targetMins,
                primaryGoalText = goalTitle,
                habits = hList,
                habitLogs = lList.filter { it.date == date }
            )
        }

        val daysWithTracking = daySummaries.count { it.totalTrackedSeconds > 0 }
        val trackingPercent = if (daysCount > 0) (daysWithTracking.toDouble() / daysCount) * 100.0 else 0.0

        val bestDay = daySummaries.maxByOrNull { it.productiveSeconds }
        val mostDistractedDay = daySummaries.maxByOrNull { it.unproductiveSeconds }

        val categoryMap = mutableMapOf<String, Long>()
        filteredActs.forEach { act ->
            val cat = act.categoryName.ifBlank { "OTHER" }.uppercase()
            categoryMap[cat] = (categoryMap[cat] ?: 0L) + act.durationSeconds
        }
        val sortedCategories = categoryMap.toList().sortedByDescending { it.second }

        val leaksMap = mutableMapOf<String, Long>()
        filteredActs.filter { it.classification == ActivityClassification.UNPRODUCTIVE }.forEach { act ->
            val cat = act.name.ifBlank { act.categoryName }.uppercase()
            leaksMap[cat] = (leaksMap[cat] ?: 0L) + act.durationSeconds
        }
        val sortedLeaks = leaksMap.toList().sortedByDescending { it.second }

        val opportunityCosts = BrutalistMetrics.calculateOpportunityCosts(unprodSec)

        val insights = BrutalistMetrics.generateDeterministicInsights(daySummaries, sortedCategories, sortedLeaks)

        return AnalyticsState(
            totalTrackedHours = totalTrackedHours,
            productiveHours = prodHours,
            neutralHours = neutHours,
            unproductiveHours = unprodHours,
            untrackedHours = daySummaries.sumOf { it.untrackedSeconds } / 3600.0,
            averageProductiveHoursPerDay = avgProdHours,
            averageUnproductiveHoursPerDay = avgUnprodHours,
            trackingConsistencyDays = "$daysWithTracking / $daysCount",
            trackingConsistencyPercent = trackingPercent,
            bestDayDate = bestDay?.date ?: "",
            bestDayProductiveHours = (bestDay?.productiveSeconds ?: 0L) / 3600.0,
            bestDayGoalPercent = bestDay?.goalCompletionPercent ?: 0.0,
            mostDistractedDayDate = mostDistractedDay?.date ?: "",
            mostDistractedUnproductiveHours = (mostDistractedDay?.unproductiveSeconds ?: 0L) / 3600.0,
            categoryBreakdown = sortedCategories,
            timeLeaks = sortedLeaks,
            opportunityCostItems = opportunityCosts,
            dayDistributionBars = daySummaries,
            insights = insights
        )
    }

    // --- CLOUD SYNC & FIRESTORE DIAGNOSTICS ---

    fun runDiagnostics(onResult: (Result<String>) -> Unit) {
        viewModelScope.launch {
            val res = daylogRepository.runConnectionDiagnostic()
            onResult(res)
        }
    }

    fun forceSyncLocalData(onResult: (Int, String?) -> Unit) {
        viewModelScope.launch {
            try {
                val user = authRepository.currentUser
                if (user == null) {
                    onResult(0, "Please sign in with Google or Email in Settings first to sync data to the cloud.")
                    return@launch
                }
                val count = daylogRepository.syncAllLocalDataToCloud(user.uid)
                onResult(count, null)
            } catch (e: Exception) {
                onResult(0, e.localizedMessage ?: "Sync error")
            }
        }
    }

    fun clearWriteLogs() {
        FirestoreDebugManager.clearLogs()
    }
}

