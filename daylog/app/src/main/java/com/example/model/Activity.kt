package com.example.model

enum class ActivityClassification(val displayName: String) {
    PRODUCTIVE("PRODUCTIVE"),
    NEUTRAL("NEUTRAL"),
    UNPRODUCTIVE("UNPRODUCTIVE"),
    UNKNOWN("UNKNOWN");

    companion object {
        fun fromString(value: String?): ActivityClassification {
            return entries.find { it.name.equals(value, ignoreCase = true) || it.displayName.equals(value, ignoreCase = true) } ?: UNKNOWN
        }
    }
}

enum class ActivityStatus {
    RUNNING,
    PAUSED,
    COMPLETED
}

enum class ActivitySource {
    TIMER,
    MANUAL,
    IMPORT
}

data class Activity(
    val id: String = "",
    val uid: String = "",
    val date: String = "", // YYYY-MM-DD
    val name: String = "",
    val categoryId: String = "",
    val categoryName: String = "WORK",
    val classification: ActivityClassification = ActivityClassification.PRODUCTIVE,
    val startedAt: Long = System.currentTimeMillis(), // epoch millis
    val endedAt: Long? = null,
    val durationSeconds: Long = 0L,
    val status: ActivityStatus = ActivityStatus.RUNNING,
    val source: ActivitySource = ActivitySource.TIMER,
    val qualityRating: Int? = null, // 1 to 5
    val noteId: String? = null,
    val note: String? = null,
    val createdAt: Long = System.currentTimeMillis(),
    val updatedAt: Long = System.currentTimeMillis()
)
