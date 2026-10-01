package com.example.util

import android.content.Context
import android.content.SharedPreferences
import java.util.Calendar

object NightlyReviewManager {

    private const val PREFS_NAME = "daylog_nightly_review_prefs"
    private const val KEY_LAST_DISMISSED_DATE = "last_dismissed_night_date"

    private fun getPrefs(context: Context): SharedPreferences {
        return context.applicationContext.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
    }

    /**
     * Checks if current local hour falls in the night window (8:00 PM / 20:00 to 4:00 AM).
     */
    fun isNightTime(): Boolean {
        val currentHour = Calendar.getInstance().get(Calendar.HOUR_OF_DAY)
        return currentHour >= 20 || currentHour < 4
    }

    /**
     * Checks whether the user has already acknowledged or dismissed the nightly note for this date.
     */
    fun isDismissedForDate(context: Context, date: String): Boolean {
        val lastDismissed = getPrefs(context).getString(KEY_LAST_DISMISSED_DATE, null)
        return lastDismissed == date
    }

    /**
     * Records that the user dismissed the nightly note for this date.
     */
    fun dismissForDate(context: Context, date: String) {
        getPrefs(context).edit().putString(KEY_LAST_DISMISSED_DATE, date).apply()
    }

    /**
     * Resets dismissal for testing/manual triggering.
     */
    fun resetDismissal(context: Context) {
        getPrefs(context).edit().remove(KEY_LAST_DISMISSED_DATE).apply()
    }
}
