package com.example.model

data class UserProfile(
    val uid: String = "",
    val email: String = "",
    val displayName: String = "",
    val photoURL: String? = null,
    val timezone: String = "UTC",
    val wakeTime: String = "06:00",
    val sleepTime: String = "23:00",
    val defaultDailyGoalMinutes: Int = 240, // 4 hours
    val onboardingCompleted: Boolean = false,
    val brutalMode: Boolean = false,
    val appearance: String = "Brutalist Dark", // "Brutalist Dark", "Brutalist Light", "System"
    val remindersEnabled: Boolean = true,
    val gapReminderEnabled: Boolean = true,
    val habitReminderEnabled: Boolean = true,
    val goalReminderEnabled: Boolean = true,
    val endOfDayReminderEnabled: Boolean = true,
    val createdAt: Long = System.currentTimeMillis(),
    val updatedAt: Long = System.currentTimeMillis()
)
