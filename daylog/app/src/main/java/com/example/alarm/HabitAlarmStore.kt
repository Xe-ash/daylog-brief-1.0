package com.example.alarm

import android.content.Context
import android.content.SharedPreferences
import org.json.JSONArray
import org.json.JSONObject

data class ActiveHabitAlarm(
    val habitId: String,
    val habitName: String,
    val reminderTime: String, // "HH:mm"
    val scheduledTimeMillis: Long
)

/**
 * Lightweight local persistent store for scheduled habit alarms.
 * Ensures alarms can be rescheduled across device reboots and queried synchronously.
 */
object HabitAlarmStore {

    private const val PREFS_NAME = "daylog_habit_alarms_prefs"
    private const val KEY_ALARMS = "active_habit_alarms_json"

    private fun getPrefs(context: Context): SharedPreferences {
        return context.applicationContext.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
    }

    fun saveAlarm(
        context: Context,
        habitId: String,
        habitName: String,
        reminderTime: String,
        scheduledTimeMillis: Long
    ) {
        val alarms = getAllAlarms(context).toMutableList()
        // Remove old entry for this habit if exists
        alarms.removeAll { it.habitId == habitId }
        alarms.add(ActiveHabitAlarm(habitId, habitName, reminderTime, scheduledTimeMillis))
        saveAll(context, alarms)
    }

    fun removeAlarm(context: Context, habitId: String) {
        val alarms = getAllAlarms(context).toMutableList()
        val changed = alarms.removeAll { it.habitId == habitId }
        if (changed) {
            saveAll(context, alarms)
        }
    }

    fun getAlarm(context: Context, habitId: String): ActiveHabitAlarm? {
        return getAllAlarms(context).find { it.habitId == habitId }
    }

    fun getAllAlarms(context: Context): List<ActiveHabitAlarm> {
        val raw = getPrefs(context).getString(KEY_ALARMS, null) ?: return emptyList()
        return try {
            val jsonArray = JSONArray(raw)
            val list = mutableListOf<ActiveHabitAlarm>()
            for (i in 0 until jsonArray.length()) {
                val obj = jsonArray.getJSONObject(i)
                list.add(
                    ActiveHabitAlarm(
                        habitId = obj.getString("habitId"),
                        habitName = obj.getString("habitName"),
                        reminderTime = obj.getString("reminderTime"),
                        scheduledTimeMillis = obj.optLong("scheduledTimeMillis", 0L)
                    )
                )
            }
            list
        } catch (e: Exception) {
            emptyList()
        }
    }

    private fun saveAll(context: Context, list: List<ActiveHabitAlarm>) {
        val jsonArray = JSONArray()
        for (item in list) {
            val obj = JSONObject().apply {
                put("habitId", item.habitId)
                put("habitName", item.habitName)
                put("reminderTime", item.reminderTime)
                put("scheduledTimeMillis", item.scheduledTimeMillis)
            }
            jsonArray.put(obj)
        }
        getPrefs(context).edit().putString(KEY_ALARMS, jsonArray.toString()).apply()
    }
}
