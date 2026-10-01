package com.example.model

enum class TaskPriority {
    LOW,
    MEDIUM,
    HIGH
}

data class Task(
    val id: String = "",
    val uid: String = "",
    val date: String = "", // YYYY-MM-DD
    val title: String = "",
    val description: String = "",
    val priority: TaskPriority = TaskPriority.MEDIUM,
    val estimatedMinutes: Int = 30,
    val actualMinutes: Int = 0,
    val completed: Boolean = false,
    val completedAt: Long? = null,
    val goalId: String? = null,
    val createdAt: Long = System.currentTimeMillis(),
    val updatedAt: Long = System.currentTimeMillis()
)
