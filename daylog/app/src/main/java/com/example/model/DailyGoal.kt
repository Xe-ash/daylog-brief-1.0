package com.example.model

enum class GoalType {
    PRIMARY,
    SECONDARY
}

enum class GoalStatus {
    NOT_STARTED,
    IN_PROGRESS,
    COMPLETED,
    FAILED,
    CANCELLED
}

data class DailyGoal(
    val id: String = "",
    val uid: String = "",
    val date: String = "", // YYYY-MM-DD
    val type: GoalType = GoalType.PRIMARY,
    val title: String = "",
    val description: String = "",
    val targetMinutes: Int = 360, // e.g. 6 hours = 360 mins
    val completedMinutes: Int = 0,
    val status: GoalStatus = GoalStatus.NOT_STARTED,
    val createdAt: Long = System.currentTimeMillis(),
    val updatedAt: Long = System.currentTimeMillis()
)
