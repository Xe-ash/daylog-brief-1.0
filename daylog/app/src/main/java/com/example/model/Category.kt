package com.example.model

data class Category(
    val id: String = "",
    val uid: String = "",
    val name: String = "",
    val defaultClassification: ActivityClassification = ActivityClassification.PRODUCTIVE,
    val isDefault: Boolean = false
) {
    companion object {
        val DEFAULT_CATEGORIES = listOf(
            Category("work", "", "WORK", ActivityClassification.PRODUCTIVE, true),
            Category("study", "", "STUDY", ActivityClassification.PRODUCTIVE, true),
            Category("deep_work", "", "DEEP WORK", ActivityClassification.PRODUCTIVE, true),
            Category("exercise", "", "EXERCISE", ActivityClassification.PRODUCTIVE, true),
            Category("reading", "", "READING", ActivityClassification.PRODUCTIVE, true),
            Category("health", "", "HEALTH", ActivityClassification.NEUTRAL, true),
            Category("sleep", "", "SLEEP", ActivityClassification.NEUTRAL, true),
            Category("food", "", "FOOD", ActivityClassification.NEUTRAL, true),
            Category("commute", "", "COMMUTE", ActivityClassification.NEUTRAL, true),
            Category("personal", "", "PERSONAL", ActivityClassification.NEUTRAL, true),
            Category("family", "", "FAMILY", ActivityClassification.NEUTRAL, true),
            Category("rest", "", "REST", ActivityClassification.NEUTRAL, true),
            Category("social_media", "", "SOCIAL MEDIA", ActivityClassification.UNPRODUCTIVE, true),
            Category("youtube", "", "YOUTUBE", ActivityClassification.UNPRODUCTIVE, true),
            Category("gaming", "", "GAMING", ActivityClassification.UNPRODUCTIVE, true),
            Category("phone", "", "PHONE", ActivityClassification.UNPRODUCTIVE, true),
            Category("entertainment", "", "ENTERTAINMENT", ActivityClassification.UNPRODUCTIVE, true),
            Category("other", "", "OTHER", ActivityClassification.UNKNOWN, true)
        )
    }
}
