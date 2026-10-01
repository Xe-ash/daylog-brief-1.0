package com.example.model

data class TimeGap(
    val startTimeMillis: Long,
    val endTimeMillis: Long,
    val durationMinutes: Long,
    val date: String
)

data class DaySummary(
    val date: String,
    val productiveSeconds: Long = 0L,
    val neutralSeconds: Long = 0L,
    val unproductiveSeconds: Long = 0L,
    val unknownSeconds: Long = 0L,
    val untrackedSeconds: Long = 0L,
    val totalTrackedSeconds: Long = 0L,
    val targetProductiveMinutes: Int = 360,
    val primaryGoalCompleted: Boolean = false,
    val primaryGoalText: String = "",
    val completedHabitsCount: Int = 0,
    val totalHabitsCount: Int = 0,
    val dayScore: Int = 0, // 0 - 100
    val trackingCoveragePercent: Double = 0.0,
    val goalCompletionPercent: Double = 0.0,
    val categoryDurations: Map<String, Long> = emptyMap() // CategoryName -> Seconds
)
