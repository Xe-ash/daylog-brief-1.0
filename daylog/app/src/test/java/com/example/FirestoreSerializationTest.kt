package com.example

import com.example.data.FirestoreDebugManager
import com.example.data.FirestoreSerialization.toFirestoreMap
import com.example.data.SyncState
import com.example.data.WriteStatus
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
import com.example.model.HabitLog
import com.example.model.HabitStatus
import com.example.model.NoteType
import com.example.model.Task
import com.example.model.TaskPriority
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [36])
class FirestoreSerializationTest {

    @Before
    fun setUp() {
        FirestoreDebugManager.clearLogs()
    }

    @Test
    fun testActivityToFirestoreMap() {
        val activity = Activity(
            id = "act_123",
            uid = "user_abc",
            date = "2026-09-23",
            name = "Deep Work",
            categoryId = "cat_1",
            categoryName = "Coding",
            classification = ActivityClassification.PRODUCTIVE,
            startedAt = 1000000L,
            endedAt = 1003600L,
            durationSeconds = 3600L,
            status = ActivityStatus.COMPLETED,
            source = ActivitySource.TIMER,
            qualityRating = 5,
            note = "Flow state",
            createdAt = 1000000L,
            updatedAt = 1003600L
        )

        val map = activity.toFirestoreMap()

        assertEquals("act_123", map["id"])
        assertEquals("user_abc", map["uid"])
        assertEquals("Deep Work", map["name"])
        assertEquals("Coding", map["categoryName"])
        assertEquals("PRODUCTIVE", map["classification"])
        assertEquals(1000000L, map["startedAt"])
        assertEquals(1003600L, map["endedAt"])
        assertEquals(3600L, map["durationSeconds"])
        assertEquals("2026-09-23", map["date"])
        assertEquals("COMPLETED", map["status"])
        assertEquals(5, map["qualityRating"])
        assertEquals("Flow state", map["note"])
    }

    @Test
    fun testDailyGoalToFirestoreMap() {
        val goal = DailyGoal(
            id = "goal_1",
            uid = "user_abc",
            date = "2026-09-23",
            type = GoalType.PRIMARY,
            title = "Focus 6 hours",
            description = "High leverage tasks",
            targetMinutes = 360,
            completedMinutes = 300,
            status = GoalStatus.IN_PROGRESS
        )

        val map = goal.toFirestoreMap()
        assertEquals("goal_1", map["id"])
        assertEquals("Focus 6 hours", map["title"])
        assertEquals("PRIMARY", map["type"])
        assertEquals("IN_PROGRESS", map["status"])
        assertEquals(360, map["targetMinutes"])
    }

    @Test
    fun testHabitAndLogToFirestoreMap() {
        val habit = Habit(
            id = "habit_1",
            uid = "user_abc",
            name = "Morning Workout",
            currentStreak = 7,
            longestStreak = 14
        )
        val habitMap = habit.toFirestoreMap()
        assertEquals("Morning Workout", habitMap["name"])
        assertEquals(7, habitMap["currentStreak"])

        val log = HabitLog(
            id = "log_1",
            uid = "user_abc",
            habitId = "habit_1",
            date = "2026-09-23",
            status = HabitStatus.COMPLETED
        )
        val logMap = log.toFirestoreMap()
        assertEquals("COMPLETED", logMap["status"])
        assertEquals("2026-09-23", logMap["date"])
    }

    @Test
    fun testTaskToFirestoreMap() {
        val task = Task(
            id = "task_1",
            uid = "user_abc",
            title = "Write tests",
            date = "2026-09-23",
            priority = TaskPriority.HIGH,
            completed = true
        )
        val map = task.toFirestoreMap()
        assertEquals("Write tests", map["title"])
        assertEquals("HIGH", map["priority"])
        assertEquals(true, map["completed"])
    }

    @Test
    fun testDayNoteAndReflectionToFirestoreMap() {
        val note = DayNote(
            id = "note_1",
            uid = "user_abc",
            date = "2026-09-23",
            content = "Great progress today",
            type = NoteType.DAILY
        )
        val noteMap = note.toFirestoreMap()
        assertEquals("Great progress today", noteMap["content"])
        assertEquals("DAILY", noteMap["type"])

        val reflection = DayReflection(
            id = "refl_1",
            uid = "user_abc",
            date = "2026-09-23",
            wentWell = "Coding was on point",
            wentWrong = "Procrastinated 15 mins",
            distractedBy = "News feed",
            changeTomorrow = "Start earlier",
            dayRating = 4
        )
        val reflMap = reflection.toFirestoreMap()
        assertEquals("Coding was on point", reflMap["wentWell"])
        assertEquals("News feed", reflMap["distractedBy"])
        assertEquals(4, reflMap["dayRating"])
    }

    @Test
    fun testFirestoreDebugManagerLogging() {
        FirestoreDebugManager.setSyncState(SyncState.Syncing)
        assertEquals(SyncState.Syncing, FirestoreDebugManager.syncState.value)

        FirestoreDebugManager.recordWriteSuccess("START_ACTIVITY", "users/u1/activities/a1", 45L)
        assertEquals(1, FirestoreDebugManager.writeLogs.value.size)
        assertEquals(WriteStatus.SUCCESS, FirestoreDebugManager.writeLogs.value.first().status)
        assertEquals(1, FirestoreDebugManager.totalSucceeded.value)
        assertEquals(SyncState.Synced, FirestoreDebugManager.syncState.value)
        assertNotNull(FirestoreDebugManager.lastSyncTimestamp.value)

        FirestoreDebugManager.recordWriteFailure("SET_GOAL", "users/u1/goals/g1", Exception("PERMISSION_DENIED"), 120L)
        assertEquals(2, FirestoreDebugManager.writeLogs.value.size)
        assertEquals(1, FirestoreDebugManager.totalFailed.value)
        assertTrue(FirestoreDebugManager.syncState.value is SyncState.Error)
    }
}
