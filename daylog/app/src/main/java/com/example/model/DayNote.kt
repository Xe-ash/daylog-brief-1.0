package com.example.model

enum class NoteType {
    DAILY,
    ACTIVITY,
    REFLECTION,
    WEEKLY,
    MONTHLY
}

data class DayNote(
    val id: String = "",
    val uid: String = "",
    val date: String = "", // YYYY-MM-DD
    val title: String? = null,
    val content: String = "",
    val type: NoteType = NoteType.DAILY,
    val activityId: String? = null,
    val createdAt: Long = System.currentTimeMillis(),
    val updatedAt: Long = System.currentTimeMillis()
)

data class DayReflection(
    val id: String = "",
    val uid: String = "",
    val date: String = "",
    val wentWell: String = "",
    val wentWrong: String = "",
    val distractedBy: String = "",
    val changeTomorrow: String = "",
    val dayRating: Int = 3, // 1-5
    val createdAt: Long = System.currentTimeMillis()
)
