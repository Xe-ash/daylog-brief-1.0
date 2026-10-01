package com.example.data

import android.util.Log
import com.example.data.FirestoreSerialization.toActivityOrNull
import com.example.data.FirestoreSerialization.toDailyGoalOrNull
import com.example.data.FirestoreSerialization.toDayNoteOrNull
import com.example.data.FirestoreSerialization.toDayReflectionOrNull
import com.example.data.FirestoreSerialization.toFirestoreMap
import com.example.data.FirestoreSerialization.toHabitLogOrNull
import com.example.data.FirestoreSerialization.toHabitOrNull
import com.example.data.FirestoreSerialization.toTaskOrNull
import com.example.data.FirestoreSerialization.toUserProfileOrNull
import com.example.model.Activity
import com.example.model.ActivityClassification
import com.example.model.ActivitySource
import com.example.model.ActivityStatus
import com.example.model.DailyGoal
import com.example.model.DayNote
import com.example.model.DayReflection
import com.example.model.Habit
import com.example.model.HabitFrequency
import com.example.model.HabitLog
import com.example.model.HabitStatus
import com.example.model.HabitType
import com.example.model.Task
import com.example.model.TaskPriority
import com.example.model.UserProfile
import com.example.util.DateTimeUtils
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.FirebaseFirestoreSettings
import com.google.firebase.firestore.ListenerRegistration
import com.google.firebase.firestore.SetOptions
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import java.util.UUID

class DaylogRepository(
    private val firestore: FirebaseFirestore = FirebaseFirestore.getInstance()
) {
    private val scope = CoroutineScope(Dispatchers.IO + SupervisorJob())

    private var currentUid: String = "local_guest"
    private var listeningForUid: String? = null
    private val activeListeners = mutableListOf<ListenerRegistration>()

    // Local in-memory caches for 0ms page loads & immediate responsiveness
    private val _userProfile = MutableStateFlow(UserProfile(uid = currentUid))
    val userProfile: StateFlow<UserProfile> = _userProfile.asStateFlow()

    private val _activities = MutableStateFlow<List<Activity>>(emptyList())
    val activities: StateFlow<List<Activity>> = _activities.asStateFlow()

    private val _habits = MutableStateFlow<List<Habit>>(defaultHabits())
    val habits: StateFlow<List<Habit>> = _habits.asStateFlow()

    private val _habitLogs = MutableStateFlow<List<HabitLog>>(emptyList())
    val habitLogs: StateFlow<List<HabitLog>> = _habitLogs.asStateFlow()

    private val _tasks = MutableStateFlow<List<Task>>(emptyList())
    val tasks: StateFlow<List<Task>> = _tasks.asStateFlow()

    private val _dailyGoals = MutableStateFlow<Map<String, DailyGoal>>(emptyMap())
    val dailyGoals: StateFlow<Map<String, DailyGoal>> = _dailyGoals.asStateFlow()

    private val _dayNotes = MutableStateFlow<List<DayNote>>(emptyList())
    val dayNotes: StateFlow<List<DayNote>> = _dayNotes.asStateFlow()

    private val _dayReflections = MutableStateFlow<Map<String, DayReflection>>(emptyMap())
    val dayReflections: StateFlow<Map<String, DayReflection>> = _dayReflections.asStateFlow()

    init {
        try {
            // Enable offline disk persistence for instant caching
            val settings = FirebaseFirestoreSettings.Builder()
                .setPersistenceEnabled(true)
                .setCacheSizeBytes(FirebaseFirestoreSettings.CACHE_SIZE_UNLIMITED)
                .build()
            firestore.firestoreSettings = settings
        } catch (e: Exception) {
            Log.w("DaylogRepo", "Firestore settings init note: ${e.message}")
        }

        // Seed initial sample data for guest / initial experience
        seedInitialDayData()
    }

    /**
     * Updates authentication ID and ensures listeners are cleanly managed without duplicates or loops.
     */
    fun setUserId(uid: String) {
        if (uid.isBlank()) return
        if (listeningForUid == uid && activeListeners.isNotEmpty()) {
            return // Already listening to this user, ignore redundant call
        }

        currentUid = uid
        detachListeners()

        if (uid == "local_guest") {
            listeningForUid = null
            FirestoreDebugManager.setSyncState(SyncState.LocalOnly)
            return
        }

        listeningForUid = uid
        FirestoreDebugManager.setSyncState(SyncState.Syncing)
        attachFirestoreListeners(uid)
    }

    private fun detachListeners() {
        if (activeListeners.isNotEmpty()) {
            Log.d("DaylogRepo", "Detaching ${activeListeners.size} active Firestore listeners")
            activeListeners.forEach { it.remove() }
            activeListeners.clear()
        }
    }

    private fun attachFirestoreListeners(uid: String) {
        if (uid.isBlank() || uid == "local_guest") return

        try {
            Log.d("DaylogRepo", "Attaching Firestore event listeners for UID: $uid")

            // 1. Profile listener
            val profileRef = firestore.collection("users").document(uid)
            val regProfile = profileRef.addSnapshotListener { snap, err ->
                if (err != null) {
                    Log.w("DaylogRepo", "Profile listen failed", err)
                    return@addSnapshotListener
                }
                if (snap != null && snap.exists()) {
                    val p = snap.toUserProfileOrNull()
                    if (p != null && _userProfile.value != p) {
                        _userProfile.value = p
                    }
                }
            }
            activeListeners.add(regProfile)

            // 2. Activities listener
            val actRef = firestore.collection("users").document(uid).collection("activities")
            val regAct = actRef.addSnapshotListener { snap, err ->
                if (err != null) {
                    Log.w("DaylogRepo", "Activities listen failed", err)
                    FirestoreDebugManager.setSyncState(SyncState.Error(FirestoreDebugManager.formatFirestoreError(err)))
                    return@addSnapshotListener
                }
                if (snap != null) {
                    val remoteList = snap.documents.mapNotNull { it.toActivityOrNull() }
                    if (remoteList.isEmpty() && _activities.value.isNotEmpty()) {
                        // User just signed in and cloud has 0 activities: auto-sync local guest activities to cloud
                        scope.launch {
                            syncAllLocalDataToCloud(uid)
                        }
                    } else {
                        // Merge remote with any pending local running activity
                        val remoteIds = remoteList.map { it.id }.toSet()
                        val pendingLocal = _activities.value.filter { it.id !in remoteIds && it.status == ActivityStatus.RUNNING }
                        val sorted = (remoteList + pendingLocal).sortedBy { it.startedAt }
                        if (_activities.value != sorted) {
                            _activities.value = sorted
                        }
                    }
                    FirestoreDebugManager.setSyncState(SyncState.Synced)
                }
            }
            activeListeners.add(regAct)

            // 3. Habits listener
            val habitsRef = firestore.collection("users").document(uid).collection("habits")
            val regHabits = habitsRef.addSnapshotListener { snap, err ->
                if (err != null) {
                    Log.w("DaylogRepo", "Habits listen failed", err)
                    return@addSnapshotListener
                }
                if (snap != null && !snap.isEmpty) {
                    val list = snap.documents.mapNotNull { it.toHabitOrNull() }
                    if (_habits.value != list) {
                        _habits.value = list
                    }
                }
            }
            activeListeners.add(regHabits)

            // 4. HabitLogs listener
            val logsRef = firestore.collection("users").document(uid).collection("habitLogs")
            val regLogs = logsRef.addSnapshotListener { snap, err ->
                if (err != null) return@addSnapshotListener
                if (snap != null) {
                    val list = snap.documents.mapNotNull { it.toHabitLogOrNull() }
                    if (_habitLogs.value != list) {
                        _habitLogs.value = list
                    }
                }
            }
            activeListeners.add(regLogs)

            // 5. Tasks listener
            val tasksRef = firestore.collection("users").document(uid).collection("tasks")
            val regTasks = tasksRef.addSnapshotListener { snap, err ->
                if (err != null) return@addSnapshotListener
                if (snap != null) {
                    val list = snap.documents.mapNotNull { it.toTaskOrNull() }
                    if (_tasks.value != list) {
                        _tasks.value = list
                    }
                }
            }
            activeListeners.add(regTasks)

            // 6. Goals listener
            val goalsRef = firestore.collection("users").document(uid).collection("goals")
            val regGoals = goalsRef.addSnapshotListener { snap, err ->
                if (err != null) return@addSnapshotListener
                if (snap != null) {
                    val map = snap.documents.mapNotNull { it.toDailyGoalOrNull() }
                        .associateBy { it.date }
                    if (_dailyGoals.value != map) {
                        _dailyGoals.value = map
                    }
                }
            }
            activeListeners.add(regGoals)

            // 7. Notes listener
            val notesRef = firestore.collection("users").document(uid).collection("notes")
            val regNotes = notesRef.addSnapshotListener { snap, err ->
                if (err != null) return@addSnapshotListener
                if (snap != null) {
                    val list = snap.documents.mapNotNull { it.toDayNoteOrNull() }
                    if (_dayNotes.value != list) {
                        _dayNotes.value = list
                    }
                }
            }
            activeListeners.add(regNotes)

            // 8. Reflections listener
            val refRef = firestore.collection("users").document(uid).collection("reflections")
            val regReflections = refRef.addSnapshotListener { snap, err ->
                if (err != null) return@addSnapshotListener
                if (snap != null) {
                    val map = snap.documents.mapNotNull { it.toDayReflectionOrNull() }
                        .associateBy { it.date }
                    if (_dayReflections.value != map) {
                        _dayReflections.value = map
                    }
                }
            }
            activeListeners.add(regReflections)

        } catch (e: Exception) {
            Log.e("DaylogRepo", "Failed attaching Firestore listeners", e)
            FirestoreDebugManager.recordWriteFailure("ATTACH_LISTENERS", "users/$uid", e, 0L)
        }
    }

    // --- ACTIVITY OPERATIONS ---

    suspend fun startActivity(
        name: String,
        categoryName: String = "WORK",
        classification: ActivityClassification = ActivityClassification.PRODUCTIVE,
        date: String = DateTimeUtils.getTodayDateString()
    ): Activity {
        val now = System.currentTimeMillis()

        // If there's an active running activity, automatically complete it
        val currentRunning = _activities.value.find { it.status == ActivityStatus.RUNNING }
        if (currentRunning != null) {
            stopActivity(
                activityId = currentRunning.id,
                endedAt = now,
                qualityRating = null,
                note = null
            )
        }

        val newActivity = Activity(
            id = UUID.randomUUID().toString(),
            uid = currentUid,
            date = date,
            name = name.trim().ifBlank { "Unspecified Activity" },
            categoryName = categoryName.uppercase(),
            classification = classification,
            startedAt = now,
            endedAt = null,
            durationSeconds = 0L,
            status = ActivityStatus.RUNNING,
            source = ActivitySource.TIMER,
            createdAt = now,
            updatedAt = now
        )

        // Instant local cache update (0ms UI latency)
        val updated = _activities.value.toMutableList()
        updated.add(newActivity)
        _activities.value = updated.sortedBy { it.startedAt }

        // Sync Firestore
        val targetPath = "users/$currentUid/activities/${newActivity.id}"
        if (currentUid != "local_guest") {
            val startTime = System.currentTimeMillis()
            try {
                firestore.collection("users").document(currentUid)
                    .collection("activities").document(newActivity.id)
                    .set(newActivity.toFirestoreMap()).await()

                val duration = System.currentTimeMillis() - startTime
                FirestoreDebugManager.recordWriteSuccess("START_ACTIVITY", targetPath, duration)
            } catch (e: Exception) {
                val duration = System.currentTimeMillis() - startTime
                FirestoreDebugManager.recordWriteFailure("START_ACTIVITY", targetPath, e, duration)
            }
        } else {
            FirestoreDebugManager.recordGuestSkipped("START_ACTIVITY", targetPath)
        }
        return newActivity
    }

    suspend fun stopActivity(
        activityId: String,
        endedAt: Long = System.currentTimeMillis(),
        qualityRating: Int? = null,
        note: String? = null
    ) {
        val current = _activities.value.find { it.id == activityId } ?: return
        val calculatedDuration = ((endedAt - current.startedAt) / 1000).coerceAtLeast(0)

        val completedActivity = current.copy(
            endedAt = endedAt,
            durationSeconds = calculatedDuration,
            status = ActivityStatus.COMPLETED,
            qualityRating = qualityRating ?: current.qualityRating,
            note = note ?: current.note,
            updatedAt = System.currentTimeMillis()
        )

        // Local cache update
        _activities.value = _activities.value.map {
            if (it.id == activityId) completedActivity else it
        }.sortedBy { it.startedAt }

        // Firestore sync
        val targetPath = "users/$currentUid/activities/$activityId"
        if (currentUid != "local_guest") {
            val startTime = System.currentTimeMillis()
            try {
                firestore.collection("users").document(currentUid)
                    .collection("activities").document(activityId)
                    .set(completedActivity.toFirestoreMap(), SetOptions.merge()).await()

                val duration = System.currentTimeMillis() - startTime
                FirestoreDebugManager.recordWriteSuccess("STOP_ACTIVITY", targetPath, duration)
            } catch (e: Exception) {
                val duration = System.currentTimeMillis() - startTime
                FirestoreDebugManager.recordWriteFailure("STOP_ACTIVITY", targetPath, e, duration)
            }
        } else {
            FirestoreDebugManager.recordGuestSkipped("STOP_ACTIVITY", targetPath)
        }
    }

    suspend fun switchActivity(
        newActivityName: String,
        newCategoryName: String,
        newClassification: ActivityClassification,
        date: String = DateTimeUtils.getTodayDateString()
    ): Activity {
        val now = System.currentTimeMillis()
        val running = _activities.value.find { it.status == ActivityStatus.RUNNING }
        if (running != null) {
            stopActivity(activityId = running.id, endedAt = now)
        }
        return startActivity(
            name = newActivityName,
            categoryName = newCategoryName,
            classification = newClassification,
            date = date
        )
    }

    suspend fun pauseActivity(activityId: String) {
        val current = _activities.value.find { it.id == activityId } ?: return
        val now = System.currentTimeMillis()
        val additionalDuration = ((now - current.startedAt) / 1000).coerceAtLeast(0)
        val paused = current.copy(
            durationSeconds = current.durationSeconds + additionalDuration,
            status = ActivityStatus.PAUSED,
            updatedAt = now
        )
        _activities.value = _activities.value.map { if (it.id == activityId) paused else it }

        val targetPath = "users/$currentUid/activities/$activityId"
        if (currentUid != "local_guest") {
            val startTime = System.currentTimeMillis()
            try {
                firestore.collection("users").document(currentUid)
                    .collection("activities").document(activityId)
                    .set(paused.toFirestoreMap(), SetOptions.merge()).await()
                FirestoreDebugManager.recordWriteSuccess("PAUSE_ACTIVITY", targetPath, System.currentTimeMillis() - startTime)
            } catch (e: Exception) {
                FirestoreDebugManager.recordWriteFailure("PAUSE_ACTIVITY", targetPath, e, System.currentTimeMillis() - startTime)
            }
        }
    }

    suspend fun resumeActivity(activityId: String) {
        val current = _activities.value.find { it.id == activityId } ?: return
        val now = System.currentTimeMillis()
        val resumed = current.copy(
            startedAt = now,
            status = ActivityStatus.RUNNING,
            updatedAt = now
        )
        _activities.value = _activities.value.map { if (it.id == activityId) resumed else it }

        val targetPath = "users/$currentUid/activities/$activityId"
        if (currentUid != "local_guest") {
            val startTime = System.currentTimeMillis()
            try {
                firestore.collection("users").document(currentUid)
                    .collection("activities").document(activityId)
                    .set(resumed.toFirestoreMap(), SetOptions.merge()).await()
                FirestoreDebugManager.recordWriteSuccess("RESUME_ACTIVITY", targetPath, System.currentTimeMillis() - startTime)
            } catch (e: Exception) {
                FirestoreDebugManager.recordWriteFailure("RESUME_ACTIVITY", targetPath, e, System.currentTimeMillis() - startTime)
            }
        }
    }

    suspend fun addManualActivity(
        name: String,
        categoryName: String,
        classification: ActivityClassification,
        date: String,
        startTimeMillis: Long,
        endTimeMillis: Long,
        qualityRating: Int? = null,
        note: String? = null
    ): Activity {
        val durationSec = ((endTimeMillis - startTimeMillis) / 1000).coerceAtLeast(60)
        val activity = Activity(
            id = UUID.randomUUID().toString(),
            uid = currentUid,
            date = date,
            name = name.trim().ifBlank { "Manual Entry" },
            categoryName = categoryName.uppercase(),
            classification = classification,
            startedAt = startTimeMillis,
            endedAt = endTimeMillis,
            durationSeconds = durationSec,
            status = ActivityStatus.COMPLETED,
            source = ActivitySource.MANUAL,
            qualityRating = qualityRating,
            note = note,
            createdAt = System.currentTimeMillis(),
            updatedAt = System.currentTimeMillis()
        )

        _activities.value = (_activities.value + activity).sortedBy { it.startedAt }

        val targetPath = "users/$currentUid/activities/${activity.id}"
        if (currentUid != "local_guest") {
            val startTime = System.currentTimeMillis()
            try {
                firestore.collection("users").document(currentUid)
                    .collection("activities").document(activity.id)
                    .set(activity.toFirestoreMap()).await()
                FirestoreDebugManager.recordWriteSuccess("MANUAL_ACTIVITY", targetPath, System.currentTimeMillis() - startTime)
            } catch (e: Exception) {
                FirestoreDebugManager.recordWriteFailure("MANUAL_ACTIVITY", targetPath, e, System.currentTimeMillis() - startTime)
            }
        } else {
            FirestoreDebugManager.recordGuestSkipped("MANUAL_ACTIVITY", targetPath)
        }
        return activity
    }

    suspend fun updateActivity(activity: Activity) {
        val updated = activity.copy(updatedAt = System.currentTimeMillis())
        _activities.value = _activities.value.map {
            if (it.id == activity.id) updated else it
        }.sortedBy { it.startedAt }

        val targetPath = "users/$currentUid/activities/${activity.id}"
        if (currentUid != "local_guest") {
            val startTime = System.currentTimeMillis()
            try {
                firestore.collection("users").document(currentUid)
                    .collection("activities").document(activity.id)
                    .set(updated.toFirestoreMap(), SetOptions.merge()).await()
                FirestoreDebugManager.recordWriteSuccess("UPDATE_ACTIVITY", targetPath, System.currentTimeMillis() - startTime)
            } catch (e: Exception) {
                FirestoreDebugManager.recordWriteFailure("UPDATE_ACTIVITY", targetPath, e, System.currentTimeMillis() - startTime)
            }
        }
    }

    suspend fun deleteActivity(activityId: String) {
        _activities.value = _activities.value.filter { it.id != activityId }

        val targetPath = "users/$currentUid/activities/$activityId"
        if (currentUid != "local_guest") {
            val startTime = System.currentTimeMillis()
            try {
                firestore.collection("users").document(currentUid)
                    .collection("activities").document(activityId)
                    .delete().await()
                FirestoreDebugManager.recordWriteSuccess("DELETE_ACTIVITY", targetPath, System.currentTimeMillis() - startTime)
            } catch (e: Exception) {
                FirestoreDebugManager.recordWriteFailure("DELETE_ACTIVITY", targetPath, e, System.currentTimeMillis() - startTime)
            }
        }
    }

    // --- GOALS OPERATIONS ---

    suspend fun setDailyGoal(
        date: String,
        title: String,
        targetMinutes: Int = 360,
        description: String = ""
    ) {
        val existing = _dailyGoals.value[date]
        val goal = existing?.copy(
            title = title,
            targetMinutes = targetMinutes,
            description = description,
            updatedAt = System.currentTimeMillis()
        ) ?: DailyGoal(
            id = UUID.randomUUID().toString(),
            uid = currentUid,
            date = date,
            title = title,
            targetMinutes = targetMinutes,
            description = description
        )

        val updatedMap = _dailyGoals.value.toMutableMap()
        updatedMap[date] = goal
        _dailyGoals.value = updatedMap

        val targetPath = "users/$currentUid/goals/$date"
        if (currentUid != "local_guest") {
            val startTime = System.currentTimeMillis()
            try {
                firestore.collection("users").document(currentUid)
                    .collection("goals").document(date)
                    .set(goal.toFirestoreMap(), SetOptions.merge()).await()
                FirestoreDebugManager.recordWriteSuccess("SET_GOAL", targetPath, System.currentTimeMillis() - startTime)
            } catch (e: Exception) {
                FirestoreDebugManager.recordWriteFailure("SET_GOAL", targetPath, e, System.currentTimeMillis() - startTime)
            }
        } else {
            FirestoreDebugManager.recordGuestSkipped("SET_GOAL", targetPath)
        }
    }

    // --- HABITS & HABIT LOGS OPERATIONS ---

    suspend fun addHabit(
        name: String,
        type: HabitType = HabitType.BOOLEAN,
        targetValue: Double = 1.0,
        unit: String = "",
        frequency: HabitFrequency = HabitFrequency.DAILY,
        reminderTime: String? = null
    ): Habit {
        val habit = Habit(
            id = UUID.randomUUID().toString(),
            uid = currentUid,
            name = name.trim(),
            type = type,
            targetValue = targetValue,
            unit = unit,
            frequency = frequency,
            reminderTime = reminderTime,
            active = true
        )
        _habits.value = _habits.value + habit

        val targetPath = "users/$currentUid/habits/${habit.id}"
        if (currentUid != "local_guest") {
            val startTime = System.currentTimeMillis()
            try {
                firestore.collection("users").document(currentUid)
                    .collection("habits").document(habit.id)
                    .set(habit.toFirestoreMap()).await()
                FirestoreDebugManager.recordWriteSuccess("ADD_HABIT", targetPath, System.currentTimeMillis() - startTime)
            } catch (e: Exception) {
                FirestoreDebugManager.recordWriteFailure("ADD_HABIT", targetPath, e, System.currentTimeMillis() - startTime)
            }
        } else {
            FirestoreDebugManager.recordGuestSkipped("ADD_HABIT", targetPath)
        }
        return habit
    }

    suspend fun deleteHabit(habitId: String) {
        _habits.value = _habits.value.filter { it.id != habitId }
        _habitLogs.value = _habitLogs.value.filter { it.habitId != habitId }

        val targetPath = "users/$currentUid/habits/$habitId"
        if (currentUid != "local_guest") {
            val startTime = System.currentTimeMillis()
            try {
                firestore.collection("users").document(currentUid)
                    .collection("habits").document(habitId)
                    .delete().await()
                FirestoreDebugManager.recordWriteSuccess("DELETE_HABIT", targetPath, System.currentTimeMillis() - startTime)
            } catch (e: Exception) {
                FirestoreDebugManager.recordWriteFailure("DELETE_HABIT", targetPath, e, System.currentTimeMillis() - startTime)
            }
        }
    }

    suspend fun updateHabitReminder(habitId: String, reminderTime: String?) {
        val habit = _habits.value.find { it.id == habitId } ?: return
        val updatedHabit = habit.copy(
            reminderTime = reminderTime,
            updatedAt = System.currentTimeMillis()
        )
        _habits.value = _habits.value.map { if (it.id == habitId) updatedHabit else it }

        val targetPath = "users/$currentUid/habits/$habitId"
        if (currentUid != "local_guest") {
            val startTime = System.currentTimeMillis()
            try {
                firestore.collection("users").document(currentUid)
                    .collection("habits").document(habitId)
                    .set(updatedHabit.toFirestoreMap(), SetOptions.merge()).await()
                FirestoreDebugManager.recordWriteSuccess("UPDATE_HABIT_REMINDER", targetPath, System.currentTimeMillis() - startTime)
            } catch (e: Exception) {
                FirestoreDebugManager.recordWriteFailure("UPDATE_HABIT_REMINDER", targetPath, e, System.currentTimeMillis() - startTime)
            }
        } else {
            FirestoreDebugManager.recordGuestSkipped("UPDATE_HABIT_REMINDER", targetPath)
        }
    }

    suspend fun toggleHabitStatus(habitId: String, date: String, targetStatus: HabitStatus? = null) {
        val habit = _habits.value.find { it.id == habitId } ?: return
        val existingLog = _habitLogs.value.find { it.habitId == habitId && it.date == date }

        val newStatus = targetStatus ?: when (existingLog?.status) {
            HabitStatus.COMPLETED -> HabitStatus.NOT_COMPLETED
            HabitStatus.NOT_COMPLETED, null -> HabitStatus.COMPLETED
            HabitStatus.SKIPPED -> HabitStatus.COMPLETED
            HabitStatus.PARTIAL -> HabitStatus.COMPLETED
            HabitStatus.MISSED -> HabitStatus.COMPLETED
        }

        val newValue = if (newStatus == HabitStatus.COMPLETED) habit.targetValue else 0.0

        val logId = existingLog?.id ?: "${habitId}_$date"
        val updatedLog = (existingLog ?: HabitLog(id = logId, uid = currentUid, habitId = habitId, date = date)).copy(
            status = newStatus,
            value = newValue,
            updatedAt = System.currentTimeMillis()
        )

        val updatedLogs = _habitLogs.value.filterNot { it.habitId == habitId && it.date == date } + updatedLog
        _habitLogs.value = updatedLogs

        // Update streaks
        val streak = calculateHabitStreak(habitId, updatedLogs)
        val updatedHabit = habit.copy(
            currentStreak = streak,
            longestStreak = maxOf(habit.longestStreak, streak)
        )
        _habits.value = _habits.value.map { if (it.id == habitId) updatedHabit else it }

        if (currentUid != "local_guest") {
            val startTime = System.currentTimeMillis()
            try {
                firestore.collection("users").document(currentUid)
                    .collection("habitLogs").document(logId)
                    .set(updatedLog.toFirestoreMap(), SetOptions.merge()).await()

                firestore.collection("users").document(currentUid)
                    .collection("habits").document(habitId)
                    .set(updatedHabit.toFirestoreMap(), SetOptions.merge()).await()
                FirestoreDebugManager.recordWriteSuccess("TOGGLE_HABIT", "users/$currentUid/habitLogs/$logId", System.currentTimeMillis() - startTime)
            } catch (e: Exception) {
                FirestoreDebugManager.recordWriteFailure("TOGGLE_HABIT", "users/$currentUid/habitLogs/$logId", e, System.currentTimeMillis() - startTime)
            }
        }
    }

    private fun calculateHabitStreak(habitId: String, logs: List<HabitLog>): Int {
        val completedDates = logs.filter { it.habitId == habitId && it.status == HabitStatus.COMPLETED }
            .map { it.date }
            .toSet()
        if (completedDates.isEmpty()) return 0

        var streak = 0
        val cal = java.util.Calendar.getInstance()
        val sdf = java.text.SimpleDateFormat("yyyy-MM-dd", java.util.Locale.US)

        for (i in 0..100) {
            val dStr = sdf.format(cal.time)
            if (completedDates.contains(dStr)) {
                streak++
            } else if (i > 0) {
                break
            }
            cal.add(java.util.Calendar.DAY_OF_YEAR, -1)
        }
        return streak
    }

    // --- TASKS OPERATIONS ---

    suspend fun addTask(
        title: String,
        date: String = DateTimeUtils.getTodayDateString(),
        priority: TaskPriority = TaskPriority.MEDIUM,
        estimatedMinutes: Int = 30
    ) {
        val task = Task(
            id = UUID.randomUUID().toString(),
            uid = currentUid,
            date = date,
            title = title.trim(),
            priority = priority,
            estimatedMinutes = estimatedMinutes,
            completed = false
        )
        _tasks.value = _tasks.value + task

        val targetPath = "users/$currentUid/tasks/${task.id}"
        if (currentUid != "local_guest") {
            val startTime = System.currentTimeMillis()
            try {
                firestore.collection("users").document(currentUid)
                    .collection("tasks").document(task.id)
                    .set(task.toFirestoreMap()).await()
                FirestoreDebugManager.recordWriteSuccess("ADD_TASK", targetPath, System.currentTimeMillis() - startTime)
            } catch (e: Exception) {
                FirestoreDebugManager.recordWriteFailure("ADD_TASK", targetPath, e, System.currentTimeMillis() - startTime)
            }
        } else {
            FirestoreDebugManager.recordGuestSkipped("ADD_TASK", targetPath)
        }
    }

    suspend fun toggleTask(taskId: String, completed: Boolean) {
        val updated = _tasks.value.map {
            if (it.id == taskId) it.copy(
                completed = completed,
                completedAt = if (completed) System.currentTimeMillis() else null,
                updatedAt = System.currentTimeMillis()
            ) else it
        }
        _tasks.value = updated

        val targetPath = "users/$currentUid/tasks/$taskId"
        if (currentUid != "local_guest") {
            val target = updated.find { it.id == taskId } ?: return
            val startTime = System.currentTimeMillis()
            try {
                firestore.collection("users").document(currentUid)
                    .collection("tasks").document(taskId)
                    .set(target.toFirestoreMap(), SetOptions.merge()).await()
                FirestoreDebugManager.recordWriteSuccess("TOGGLE_TASK", targetPath, System.currentTimeMillis() - startTime)
            } catch (e: Exception) {
                FirestoreDebugManager.recordWriteFailure("TOGGLE_TASK", targetPath, e, System.currentTimeMillis() - startTime)
            }
        }
    }

    suspend fun deleteTask(taskId: String) {
        _tasks.value = _tasks.value.filter { it.id != taskId }
        val targetPath = "users/$currentUid/tasks/$taskId"
        if (currentUid != "local_guest") {
            val startTime = System.currentTimeMillis()
            try {
                firestore.collection("users").document(currentUid)
                    .collection("tasks").document(taskId)
                    .delete().await()
                FirestoreDebugManager.recordWriteSuccess("DELETE_TASK", targetPath, System.currentTimeMillis() - startTime)
            } catch (e: Exception) {
                FirestoreDebugManager.recordWriteFailure("DELETE_TASK", targetPath, e, System.currentTimeMillis() - startTime)
            }
        }
    }

    // --- NOTES & REFLECTIONS OPERATIONS ---

    suspend fun saveNote(
        date: String,
        content: String,
        title: String? = null,
        activityId: String? = null
    ) {
        val note = DayNote(
            id = UUID.randomUUID().toString(),
            uid = currentUid,
            date = date,
            title = title,
            content = content.trim(),
            activityId = activityId
        )
        _dayNotes.value = _dayNotes.value + note

        val targetPath = "users/$currentUid/notes/${note.id}"
        if (currentUid != "local_guest") {
            val startTime = System.currentTimeMillis()
            try {
                firestore.collection("users").document(currentUid)
                    .collection("notes").document(note.id)
                    .set(note.toFirestoreMap()).await()
                FirestoreDebugManager.recordWriteSuccess("SAVE_NOTE", targetPath, System.currentTimeMillis() - startTime)
            } catch (e: Exception) {
                FirestoreDebugManager.recordWriteFailure("SAVE_NOTE", targetPath, e, System.currentTimeMillis() - startTime)
            }
        } else {
            FirestoreDebugManager.recordGuestSkipped("SAVE_NOTE", targetPath)
        }
    }

    suspend fun deleteNote(noteId: String) {
        _dayNotes.value = _dayNotes.value.filter { it.id != noteId }
        val targetPath = "users/$currentUid/notes/$noteId"
        if (currentUid != "local_guest") {
            val startTime = System.currentTimeMillis()
            try {
                firestore.collection("users").document(currentUid)
                    .collection("notes").document(noteId)
                    .delete().await()
                FirestoreDebugManager.recordWriteSuccess("DELETE_NOTE", targetPath, System.currentTimeMillis() - startTime)
            } catch (e: Exception) {
                FirestoreDebugManager.recordWriteFailure("DELETE_NOTE", targetPath, e, System.currentTimeMillis() - startTime)
            }
        }
    }

    suspend fun saveDayReflection(
        date: String,
        wentWell: String,
        wentWrong: String,
        distractedBy: String,
        changeTomorrow: String,
        dayRating: Int
    ) {
        val reflection = DayReflection(
            id = date,
            uid = currentUid,
            date = date,
            wentWell = wentWell.trim(),
            wentWrong = wentWrong.trim(),
            distractedBy = distractedBy.trim(),
            changeTomorrow = changeTomorrow.trim(),
            dayRating = dayRating
        )
        val updated = _dayReflections.value.toMutableMap()
        updated[date] = reflection
        _dayReflections.value = updated

        val targetPath = "users/$currentUid/reflections/$date"
        if (currentUid != "local_guest") {
            val startTime = System.currentTimeMillis()
            try {
                firestore.collection("users").document(currentUid)
                    .collection("reflections").document(date)
                    .set(reflection.toFirestoreMap(), SetOptions.merge()).await()
                FirestoreDebugManager.recordWriteSuccess("SAVE_REFLECTION", targetPath, System.currentTimeMillis() - startTime)
            } catch (e: Exception) {
                FirestoreDebugManager.recordWriteFailure("SAVE_REFLECTION", targetPath, e, System.currentTimeMillis() - startTime)
            }
        } else {
            FirestoreDebugManager.recordGuestSkipped("SAVE_REFLECTION", targetPath)
        }
    }

    // --- USER PROFILE & PREFERENCES ---

    suspend fun updateUserProfile(profile: UserProfile) {
        _userProfile.value = profile
        val targetPath = "users/$currentUid"
        if (currentUid != "local_guest") {
            val startTime = System.currentTimeMillis()
            try {
                firestore.collection("users").document(currentUid)
                    .set(profile.toFirestoreMap(), SetOptions.merge()).await()
                FirestoreDebugManager.recordWriteSuccess("UPDATE_PROFILE", targetPath, System.currentTimeMillis() - startTime)
            } catch (e: Exception) {
                FirestoreDebugManager.recordWriteFailure("UPDATE_PROFILE", targetPath, e, System.currentTimeMillis() - startTime)
            }
        }
    }

    /**
     * Batch uploads all local in-memory activities, habits, tasks, and goals to Firestore.
     * Prevents data loss when converting a local guest user into a signed-in user.
     */
    suspend fun syncAllLocalDataToCloud(targetUid: String = currentUid): Int {
        if (targetUid.isBlank() || targetUid == "local_guest") return 0

        var count = 0
        Log.i("DaylogRepo", "Starting full synchronization of local data to Cloud Firestore for UID: $targetUid")

        try {
            // Activities
            for (act in _activities.value) {
                val actToSave = act.copy(uid = targetUid)
                firestore.collection("users").document(targetUid)
                    .collection("activities").document(actToSave.id)
                    .set(actToSave.toFirestoreMap(), SetOptions.merge()).await()
                count++
            }

            // Habits
            for (h in _habits.value) {
                val hToSave = h.copy(uid = targetUid)
                firestore.collection("users").document(targetUid)
                    .collection("habits").document(hToSave.id)
                    .set(hToSave.toFirestoreMap(), SetOptions.merge()).await()
                count++
            }

            // Tasks
            for (t in _tasks.value) {
                val tToSave = t.copy(uid = targetUid)
                firestore.collection("users").document(targetUid)
                    .collection("tasks").document(tToSave.id)
                    .set(tToSave.toFirestoreMap(), SetOptions.merge()).await()
                count++
            }

            // Goals
            for ((_, g) in _dailyGoals.value) {
                val gToSave = g.copy(uid = targetUid)
                firestore.collection("users").document(targetUid)
                    .collection("goals").document(gToSave.date)
                    .set(gToSave.toFirestoreMap(), SetOptions.merge()).await()
                count++
            }

            FirestoreDebugManager.recordWriteSuccess("BATCH_SYNC", "users/$targetUid", 0L)
            Log.i("DaylogRepo", "Successfully synced $count documents to Cloud Firestore")
        } catch (e: Exception) {
            Log.e("DaylogRepo", "Error during full local-to-cloud sync", e)
            FirestoreDebugManager.recordWriteFailure("BATCH_SYNC", "users/$targetUid", e, 0L)
        }
        return count
    }

    /**
     * Runs live diagnostic probe
     */
    suspend fun runConnectionDiagnostic(): Result<String> {
        return FirestoreDebugManager.runConnectionDiagnostic(firestore, currentUid)
    }

    private fun defaultHabits(): List<Habit> {
        return listOf(
            Habit(id = "h1", name = "Exercise", type = HabitType.BOOLEAN, currentStreak = 4, longestStreak = 12),
            Habit(id = "h2", name = "Read 30 minutes", type = HabitType.DURATION, targetValue = 30.0, unit = "mins", currentStreak = 7, longestStreak = 15),
            Habit(id = "h3", name = "Meditation", type = HabitType.BOOLEAN, currentStreak = 2, longestStreak = 6),
            Habit(id = "h4", name = "Deep work block", type = HabitType.BOOLEAN, currentStreak = 5, longestStreak = 14),
            Habit(id = "h5", name = "Sleep before 23:00", type = HabitType.BOOLEAN, currentStreak = 3, longestStreak = 8)
        )
    }

    private fun seedInitialDayData() {
        val today = DateTimeUtils.getTodayDateString()
        val cal = java.util.Calendar.getInstance()
        cal.set(java.util.Calendar.HOUR_OF_DAY, 8)
        cal.set(java.util.Calendar.MINUTE, 0)
        cal.set(java.util.Calendar.SECOND, 0)
        val t0800 = cal.timeInMillis

        cal.set(java.util.Calendar.HOUR_OF_DAY, 8)
        cal.set(java.util.Calendar.MINUTE, 25)
        val t0825 = cal.timeInMillis

        cal.set(java.util.Calendar.HOUR_OF_DAY, 9)
        cal.set(java.util.Calendar.MINUTE, 0)
        val t0900 = cal.timeInMillis

        cal.set(java.util.Calendar.HOUR_OF_DAY, 10)
        cal.set(java.util.Calendar.MINUTE, 40)
        val t1040 = cal.timeInMillis

        cal.set(java.util.Calendar.HOUR_OF_DAY, 11)
        cal.set(java.util.Calendar.MINUTE, 12)
        val t1112 = cal.timeInMillis

        cal.set(java.util.Calendar.HOUR_OF_DAY, 12)
        cal.set(java.util.Calendar.MINUTE, 0)
        val t1200 = cal.timeInMillis

        val initialActivities = listOf(
            Activity(
                id = "seed_1",
                uid = currentUid,
                date = today,
                name = "Breakfast",
                categoryName = "FOOD",
                classification = ActivityClassification.NEUTRAL,
                startedAt = t0800,
                endedAt = t0825,
                durationSeconds = 25 * 60L,
                status = ActivityStatus.COMPLETED
            ),
            Activity(
                id = "seed_2",
                uid = currentUid,
                date = today,
                name = "Commute",
                categoryName = "COMMUTE",
                classification = ActivityClassification.NEUTRAL,
                startedAt = t0825,
                endedAt = t0900,
                durationSeconds = 35 * 60L,
                status = ActivityStatus.COMPLETED
            ),
            Activity(
                id = "seed_3",
                uid = currentUid,
                date = today,
                name = "Deep Work",
                categoryName = "WORK",
                classification = ActivityClassification.PRODUCTIVE,
                startedAt = t0900,
                endedAt = t1040,
                durationSeconds = 100 * 60L,
                status = ActivityStatus.COMPLETED,
                qualityRating = 4
            ),
            Activity(
                id = "seed_4",
                uid = currentUid,
                date = today,
                name = "Instagram",
                categoryName = "SOCIAL MEDIA",
                classification = ActivityClassification.UNPRODUCTIVE,
                startedAt = t1040,
                endedAt = t1112,
                durationSeconds = 32 * 60L,
                status = ActivityStatus.COMPLETED
            ),
            Activity(
                id = "seed_5",
                uid = currentUid,
                date = today,
                name = "Deep Work",
                categoryName = "WORK",
                classification = ActivityClassification.PRODUCTIVE,
                startedAt = t1112,
                endedAt = t1200,
                durationSeconds = 48 * 60L,
                status = ActivityStatus.COMPLETED,
                qualityRating = 5
            )
        )
        _activities.value = initialActivities

        _dailyGoals.value = mapOf(
            today to DailyGoal(
                id = "g_$today",
                uid = currentUid,
                date = today,
                title = "Complete 6 focused hours of work.",
                targetMinutes = 360,
                completedMinutes = 148
            )
        )

        _habitLogs.value = listOf(
            HabitLog(id = "l1", habitId = "h1", date = today, status = HabitStatus.COMPLETED, value = 1.0),
            HabitLog(id = "l2", habitId = "h2", date = today, status = HabitStatus.COMPLETED, value = 30.0),
            HabitLog(id = "l4", habitId = "h4", date = today, status = HabitStatus.COMPLETED, value = 1.0)
        )

        _tasks.value = listOf(
            Task(id = "t1", date = today, title = "Finish API integration", priority = TaskPriority.HIGH, completed = true),
            Task(id = "t2", date = today, title = "Complete login screen", priority = TaskPriority.HIGH, completed = true),
            Task(id = "t3", date = today, title = "Write documentation", priority = TaskPriority.MEDIUM, completed = false),
            Task(id = "t4", date = today, title = "Evening workout", priority = TaskPriority.LOW, completed = false)
        )

        _dayNotes.value = listOf(
            DayNote(id = "n1", date = today, content = "Felt distracted after lunch. Refocusing on core modules.")
        )
    }
}
