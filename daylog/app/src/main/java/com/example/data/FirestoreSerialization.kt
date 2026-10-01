package com.example.data

import android.util.Log
import com.example.model.Activity
import com.example.model.ActivityClassification
import com.example.model.ActivitySource
import com.example.model.ActivityStatus
import com.example.model.DailyGoal
import com.example.model.DayNote
import com.example.model.DayReflection
import com.example.model.GoalStatus
import com.example.model.GoalType
import com.example.model.Habit
import com.example.model.HabitFrequency
import com.example.model.HabitLog
import com.example.model.HabitStatus
import com.example.model.HabitType
import com.example.model.NoteType
import com.example.model.Task
import com.example.model.TaskPriority
import com.example.model.UserProfile
import com.google.firebase.firestore.DocumentSnapshot

/**
 * Bulletproof serialization & deserialization helpers for Firestore.
 * Avoids reflection crashes, enum deserialization mismatches, and NullPointerExceptions.
 */
object FirestoreSerialization {

    // --- ACTIVITY ---
    fun Activity.toFirestoreMap(): Map<String, Any?> = mapOf(
        "id" to id,
        "uid" to uid,
        "date" to date,
        "name" to name,
        "categoryId" to categoryId,
        "categoryName" to categoryName,
        "classification" to classification.name,
        "startedAt" to startedAt,
        "endedAt" to endedAt,
        "durationSeconds" to durationSeconds,
        "status" to status.name,
        "source" to source.name,
        "qualityRating" to qualityRating,
        "noteId" to noteId,
        "note" to note,
        "createdAt" to createdAt,
        "updatedAt" to updatedAt
    )

    fun DocumentSnapshot.toActivityOrNull(): Activity? {
        return try {
            val data = this.data ?: return null
            Activity(
                id = getString("id") ?: id,
                uid = getString("uid") ?: "",
                date = getString("date") ?: "",
                name = getString("name") ?: "",
                categoryId = getString("categoryId") ?: "",
                categoryName = getString("categoryName") ?: "WORK",
                classification = ActivityClassification.fromString(getString("classification")),
                startedAt = getLong("startedAt") ?: 0L,
                endedAt = getLong("endedAt"),
                durationSeconds = getLong("durationSeconds") ?: 0L,
                status = parseEnum(getString("status"), ActivityStatus.COMPLETED),
                source = parseEnum(getString("source"), ActivitySource.TIMER),
                qualityRating = getLong("qualityRating")?.toInt(),
                noteId = getString("noteId"),
                note = getString("note"),
                createdAt = getLong("createdAt") ?: 0L,
                updatedAt = getLong("updatedAt") ?: 0L
            )
        } catch (e: Exception) {
            Log.e("FirestoreSerialization", "Error parsing Activity doc: $id", e)
            null
        }
    }

    // --- DAILY GOAL ---
    fun DailyGoal.toFirestoreMap(): Map<String, Any?> = mapOf(
        "id" to id,
        "uid" to uid,
        "date" to date,
        "type" to type.name,
        "title" to title,
        "description" to description,
        "targetMinutes" to targetMinutes,
        "completedMinutes" to completedMinutes,
        "status" to status.name,
        "createdAt" to createdAt,
        "updatedAt" to updatedAt
    )

    fun DocumentSnapshot.toDailyGoalOrNull(): DailyGoal? {
        return try {
            val data = this.data ?: return null
            DailyGoal(
                id = getString("id") ?: id,
                uid = getString("uid") ?: "",
                date = getString("date") ?: "",
                type = parseEnum(getString("type"), GoalType.PRIMARY),
                title = getString("title") ?: "",
                description = getString("description") ?: "",
                targetMinutes = getLong("targetMinutes")?.toInt() ?: 360,
                completedMinutes = getLong("completedMinutes")?.toInt() ?: 0,
                status = parseEnum(getString("status"), GoalStatus.NOT_STARTED),
                createdAt = getLong("createdAt") ?: 0L,
                updatedAt = getLong("updatedAt") ?: 0L
            )
        } catch (e: Exception) {
            Log.e("FirestoreSerialization", "Error parsing DailyGoal doc: $id", e)
            null
        }
    }

    // --- HABIT ---
    fun Habit.toFirestoreMap(): Map<String, Any?> = mapOf(
        "id" to id,
        "uid" to uid,
        "name" to name,
        "type" to type.name,
        "targetValue" to targetValue,
        "unit" to unit,
        "frequency" to frequency.name,
        "scheduledDays" to scheduledDays,
        "reminderTime" to reminderTime,
        "currentStreak" to currentStreak,
        "longestStreak" to longestStreak,
        "active" to active,
        "createdAt" to createdAt,
        "updatedAt" to updatedAt
    )

    fun DocumentSnapshot.toHabitOrNull(): Habit? {
        return try {
            val data = this.data ?: return null
            @Suppress("UNCHECKED_CAST")
            val scheduled = (get("scheduledDays") as? List<*>)?.mapNotNull { (it as? Number)?.toInt() }
                ?: listOf(1, 2, 3, 4, 5, 6, 7)
            Habit(
                id = getString("id") ?: id,
                uid = getString("uid") ?: "",
                name = getString("name") ?: "",
                type = parseEnum(getString("type"), HabitType.BOOLEAN),
                targetValue = getDouble("targetValue") ?: 1.0,
                unit = getString("unit") ?: "",
                frequency = parseEnum(getString("frequency"), HabitFrequency.DAILY),
                scheduledDays = scheduled,
                reminderTime = getString("reminderTime"),
                currentStreak = getLong("currentStreak")?.toInt() ?: 0,
                longestStreak = getLong("longestStreak")?.toInt() ?: 0,
                active = getBoolean("active") ?: true,
                createdAt = getLong("createdAt") ?: 0L,
                updatedAt = getLong("updatedAt") ?: 0L
            )
        } catch (e: Exception) {
            Log.e("FirestoreSerialization", "Error parsing Habit doc: $id", e)
            null
        }
    }

    // --- HABIT LOG ---
    fun HabitLog.toFirestoreMap(): Map<String, Any?> = mapOf(
        "id" to id,
        "uid" to uid,
        "habitId" to habitId,
        "date" to date,
        "status" to status.name,
        "value" to value,
        "note" to note,
        "createdAt" to createdAt,
        "updatedAt" to updatedAt
    )

    fun DocumentSnapshot.toHabitLogOrNull(): HabitLog? {
        return try {
            val data = this.data ?: return null
            HabitLog(
                id = getString("id") ?: id,
                uid = getString("uid") ?: "",
                habitId = getString("habitId") ?: "",
                date = getString("date") ?: "",
                status = parseEnum(getString("status"), HabitStatus.NOT_COMPLETED),
                value = getDouble("value") ?: 0.0,
                note = getString("note"),
                createdAt = getLong("createdAt") ?: 0L,
                updatedAt = getLong("updatedAt") ?: 0L
            )
        } catch (e: Exception) {
            Log.e("FirestoreSerialization", "Error parsing HabitLog doc: $id", e)
            null
        }
    }

    // --- TASK ---
    fun Task.toFirestoreMap(): Map<String, Any?> = mapOf(
        "id" to id,
        "uid" to uid,
        "date" to date,
        "title" to title,
        "description" to description,
        "priority" to priority.name,
        "estimatedMinutes" to estimatedMinutes,
        "actualMinutes" to actualMinutes,
        "completed" to completed,
        "completedAt" to completedAt,
        "goalId" to goalId,
        "createdAt" to createdAt,
        "updatedAt" to updatedAt
    )

    fun DocumentSnapshot.toTaskOrNull(): Task? {
        return try {
            val data = this.data ?: return null
            Task(
                id = getString("id") ?: id,
                uid = getString("uid") ?: "",
                date = getString("date") ?: "",
                title = getString("title") ?: "",
                description = getString("description") ?: "",
                priority = parseEnum(getString("priority"), TaskPriority.MEDIUM),
                estimatedMinutes = getLong("estimatedMinutes")?.toInt() ?: 30,
                actualMinutes = getLong("actualMinutes")?.toInt() ?: 0,
                completed = getBoolean("completed") ?: false,
                completedAt = getLong("completedAt"),
                goalId = getString("goalId"),
                createdAt = getLong("createdAt") ?: 0L,
                updatedAt = getLong("updatedAt") ?: 0L
            )
        } catch (e: Exception) {
            Log.e("FirestoreSerialization", "Error parsing Task doc: $id", e)
            null
        }
    }

    // --- DAY NOTE ---
    fun DayNote.toFirestoreMap(): Map<String, Any?> = mapOf(
        "id" to id,
        "uid" to uid,
        "date" to date,
        "title" to title,
        "content" to content,
        "type" to type.name,
        "activityId" to activityId,
        "createdAt" to createdAt,
        "updatedAt" to updatedAt
    )

    fun DocumentSnapshot.toDayNoteOrNull(): DayNote? {
        return try {
            val data = this.data ?: return null
            val typeStr = getString("type")
            val noteType: NoteType = typeStr?.let {
                try { NoteType.valueOf(it) } catch (e: Exception) { NoteType.DAILY }
            } ?: NoteType.DAILY
            DayNote(
                id = getString("id") ?: id,
                uid = getString("uid") ?: "",
                date = getString("date") ?: "",
                title = getString("title"),
                content = getString("content") ?: "",
                type = noteType,
                activityId = getString("activityId"),
                createdAt = getLong("createdAt") ?: 0L,
                updatedAt = getLong("updatedAt") ?: 0L
            )
        } catch (e: Exception) {
            Log.e("FirestoreSerialization", "Error parsing DayNote doc: $id", e)
            null
        }
    }

    // --- DAY REFLECTION ---
    fun DayReflection.toFirestoreMap(): Map<String, Any?> = mapOf(
        "id" to id,
        "uid" to uid,
        "date" to date,
        "wentWell" to wentWell,
        "wentWrong" to wentWrong,
        "distractedBy" to distractedBy,
        "changeTomorrow" to changeTomorrow,
        "dayRating" to dayRating,
        "createdAt" to createdAt
    )

    fun DocumentSnapshot.toDayReflectionOrNull(): DayReflection? {
        return try {
            val data = this.data ?: return null
            DayReflection(
                id = getString("id") ?: id,
                uid = getString("uid") ?: "",
                date = getString("date") ?: "",
                wentWell = getString("wentWell") ?: "",
                wentWrong = getString("wentWrong") ?: "",
                distractedBy = getString("distractedBy") ?: "",
                changeTomorrow = getString("changeTomorrow") ?: "",
                dayRating = getLong("dayRating")?.toInt() ?: 3,
                createdAt = getLong("createdAt") ?: 0L
            )
        } catch (e: Exception) {
            Log.e("FirestoreSerialization", "Error parsing DayReflection doc: $id", e)
            null
        }
    }

    // --- USER PROFILE ---
    fun UserProfile.toFirestoreMap(): Map<String, Any?> = mapOf(
        "uid" to uid,
        "email" to email,
        "displayName" to displayName,
        "photoURL" to photoURL,
        "timezone" to timezone,
        "wakeTime" to wakeTime,
        "sleepTime" to sleepTime,
        "defaultDailyGoalMinutes" to defaultDailyGoalMinutes,
        "onboardingCompleted" to onboardingCompleted,
        "brutalMode" to brutalMode,
        "appearance" to appearance,
        "remindersEnabled" to remindersEnabled,
        "gapReminderEnabled" to gapReminderEnabled,
        "habitReminderEnabled" to habitReminderEnabled,
        "goalReminderEnabled" to goalReminderEnabled,
        "endOfDayReminderEnabled" to endOfDayReminderEnabled,
        "createdAt" to createdAt,
        "updatedAt" to updatedAt
    )

    fun DocumentSnapshot.toUserProfileOrNull(): UserProfile? {
        return try {
            val data = this.data ?: return null
            UserProfile(
                uid = getString("uid") ?: id,
                email = getString("email") ?: "",
                displayName = getString("displayName") ?: "User",
                photoURL = getString("photoURL"),
                timezone = getString("timezone") ?: "UTC",
                wakeTime = getString("wakeTime") ?: "06:00",
                sleepTime = getString("sleepTime") ?: "23:00",
                defaultDailyGoalMinutes = getLong("defaultDailyGoalMinutes")?.toInt() ?: 240,
                onboardingCompleted = getBoolean("onboardingCompleted") ?: false,
                brutalMode = getBoolean("brutalMode") ?: false,
                appearance = getString("appearance") ?: "Brutalist Dark",
                remindersEnabled = getBoolean("remindersEnabled") ?: true,
                gapReminderEnabled = getBoolean("gapReminderEnabled") ?: true,
                habitReminderEnabled = getBoolean("habitReminderEnabled") ?: true,
                goalReminderEnabled = getBoolean("goalReminderEnabled") ?: true,
                endOfDayReminderEnabled = getBoolean("endOfDayReminderEnabled") ?: true,
                createdAt = getLong("createdAt") ?: 0L,
                updatedAt = getLong("updatedAt") ?: 0L
            )
        } catch (e: Exception) {
            Log.e("FirestoreSerialization", "Error parsing UserProfile doc: $id", e)
            null
        }
    }

    private inline fun <reified T : Enum<T>> parseEnum(name: String?, fallback: T): T {
        if (name.isNullOrBlank()) return fallback
        return try {
            java.lang.Enum.valueOf(T::class.java, name.trim().uppercase())
        } catch (_: Exception) {
            fallback
        }
    }
}
