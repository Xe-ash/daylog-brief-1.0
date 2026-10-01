package com.example.model

enum class HabitType {
    BOOLEAN,
    DURATION,
    COUNT,
    NUMERIC
}

enum class HabitFrequency {
    DAILY,
    WEEKLY,
    CUSTOM
}

enum class HabitStatus {
    NOT_COMPLETED,
    PARTIAL,
    COMPLETED,
    MISSED,
    SKIPPED
}

data class Habit(
    val id: String = "",
    val uid: String = "",
    val name: String = "",
    val type: HabitType = HabitType.BOOLEAN,
    val targetValue: Double = 1.0,
    val unit: String = "", // e.g. "mins", "glasses", "reps"
    val frequency: HabitFrequency = HabitFrequency.DAILY,
    val scheduledDays: List<Int> = listOf(1, 2, 3, 4, 5, 6, 7), // 1=Mon .. 7=Sun
    val reminderTime: String? = null,
    val currentStreak: Int = 0,
    val longestStreak: Int = 0,
    val active: Boolean = true,
    val createdAt: Long = System.currentTimeMillis(),
    val updatedAt: Long = System.currentTimeMillis()
)

data class HabitLog(
    val id: String = "",
    val uid: String = "",
    val habitId: String = "",
    val date: String = "", // YYYY-MM-DD
    val value: Double = 0.0,
    val status: HabitStatus = HabitStatus.NOT_COMPLETED,
    val note: String? = null,
    val createdAt: Long = System.currentTimeMillis(),
    val updatedAt: Long = System.currentTimeMillis()
)
