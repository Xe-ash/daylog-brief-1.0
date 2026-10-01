package com.example.alarm

import android.app.AlarmManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Build
import android.provider.Settings
import android.util.Log
import java.util.Calendar

object HabitAlarmScheduler {

    private const val TAG = "HabitAlarmScheduler"

    /**
     * Schedules a precise alarm for a daily habit using AlarmManager.
     * Uses [AlarmManager.setExactAndAllowWhileIdle] to ensure it triggers even
     * in Doze mode or when the application is completely killed.
     *
     * Handles edge cases:
     * If the selected time today has already passed, it automatically targets tomorrow at that time.
     *
     * @param context Android context
     * @param habitId Unique identifier for the habit
     * @param habitName Name of the habit for notifications
     * @param timeString 24-hour formatted time ("HH:mm", e.g. "07:30" or "18:00")
     * @return The scheduled trigger time in epoch milliseconds
     */
    fun scheduleHabitAlarm(
        context: Context,
        habitId: String,
        habitName: String,
        timeString: String
    ): Long {
        val appContext = context.applicationContext
        val alarmManager = appContext.getSystemService(Context.ALARM_SERVICE) as? AlarmManager
        if (alarmManager == null) {
            Log.e(TAG, "AlarmManager not available on this device")
            return 0L
        }

        val parts = timeString.split(":")
        val hour = parts.getOrNull(0)?.toIntOrNull() ?: 8
        val minute = parts.getOrNull(1)?.toIntOrNull() ?: 0

        val now = Calendar.getInstance()
        val target = Calendar.getInstance().apply {
            set(Calendar.HOUR_OF_DAY, hour)
            set(Calendar.MINUTE, minute)
            set(Calendar.SECOND, 0)
            set(Calendar.MILLISECOND, 0)
        }

        // Edge case: If selected time is earlier than or equal to current time today,
        // automatically advance to the next day!
        if (target.timeInMillis <= now.timeInMillis) {
            target.add(Calendar.DAY_OF_YEAR, 1)
            Log.d(TAG, "Selected time $timeString has passed today. Scheduling for tomorrow: ${target.time}")
        } else {
            Log.d(TAG, "Scheduling alarm for today at ${target.time}")
        }

        val triggerAtMillis = target.timeInMillis

        val intent = Intent(appContext, HabitAlarmReceiver::class.java).apply {
            action = HabitAlarmReceiver.ACTION_HABIT_ALARM
            putExtra(HabitAlarmReceiver.EXTRA_HABIT_ID, habitId)
            putExtra(HabitAlarmReceiver.EXTRA_HABIT_NAME, habitName)
            putExtra(HabitAlarmReceiver.EXTRA_REMINDER_TIME, timeString)
        }

        val requestCode = getAlarmRequestCode(habitId)
        val pendingIntent = PendingIntent.getBroadcast(
            appContext,
            requestCode,
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        try {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                if (alarmManager.canScheduleExactAlarms()) {
                    alarmManager.setExactAndAllowWhileIdle(
                        AlarmManager.RTC_WAKEUP,
                        triggerAtMillis,
                        pendingIntent
                    )
                    Log.d(TAG, "Exact alarm setAndAllowWhileIdle successful for habit $habitId at $triggerAtMillis")
                } else {
                    Log.w(TAG, "SCHEDULE_EXACT_ALARM not permitted by user; falling back to setAndAllowWhileIdle")
                    alarmManager.setAndAllowWhileIdle(
                        AlarmManager.RTC_WAKEUP,
                        triggerAtMillis,
                        pendingIntent
                    )
                }
            } else if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                alarmManager.setExactAndAllowWhileIdle(
                    AlarmManager.RTC_WAKEUP,
                    triggerAtMillis,
                    pendingIntent
                )
            } else {
                alarmManager.setExact(
                    AlarmManager.RTC_WAKEUP,
                    triggerAtMillis,
                    pendingIntent
                )
            }

            // Persist to local store for reboot recovery
            HabitAlarmStore.saveAlarm(
                context = appContext,
                habitId = habitId,
                habitName = habitName,
                reminderTime = timeString,
                scheduledTimeMillis = triggerAtMillis
            )

        } catch (se: SecurityException) {
            Log.e(TAG, "SecurityException while scheduling exact alarm: ${se.message}. Falling back to standard alarm.", se)
            try {
                alarmManager.setAndAllowWhileIdle(
                    AlarmManager.RTC_WAKEUP,
                    triggerAtMillis,
                    pendingIntent
                )
            } catch (fallbackEx: Exception) {
                Log.e(TAG, "Fallback alarm failed: ${fallbackEx.message}")
            }
        } catch (e: Exception) {
            Log.e(TAG, "Failed to schedule habit alarm: ${e.message}", e)
        }

        return triggerAtMillis
    }

    /**
     * Cancels an existing alarm for the specified habit.
     */
    fun cancelHabitAlarm(context: Context, habitId: String) {
        val appContext = context.applicationContext
        val alarmManager = appContext.getSystemService(Context.ALARM_SERVICE) as? AlarmManager ?: return

        val intent = Intent(appContext, HabitAlarmReceiver::class.java).apply {
            action = HabitAlarmReceiver.ACTION_HABIT_ALARM
            putExtra(HabitAlarmReceiver.EXTRA_HABIT_ID, habitId)
        }

        val requestCode = getAlarmRequestCode(habitId)
        val pendingIntent = PendingIntent.getBroadcast(
            appContext,
            requestCode,
            intent,
            PendingIntent.FLAG_NO_CREATE or PendingIntent.FLAG_IMMUTABLE
        )

        if (pendingIntent != null) {
            alarmManager.cancel(pendingIntent)
            pendingIntent.cancel()
            Log.d(TAG, "Cancelled alarm for habit $habitId (requestCode: $requestCode)")
        }

        HabitAlarmStore.removeAlarm(appContext, habitId)
    }

    /**
     * Checks if exact alarms can be scheduled on Android 12+ (API 31+).
     */
    fun canScheduleExactAlarms(context: Context): Boolean {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as? AlarmManager
            return alarmManager?.canScheduleExactAlarms() ?: false
        }
        return true
    }

    /**
     * Creates an intent for opening system exact alarm permission settings on Android 12+.
     */
    fun createExactAlarmSettingsIntent(context: Context): Intent {
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            Intent(
                Settings.ACTION_REQUEST_SCHEDULE_EXACT_ALARM,
                Uri.parse("package:${context.packageName}")
            )
        } else {
            Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS, Uri.parse("package:${context.packageName}"))
        }
    }

    /**
     * Returns a consistent integer request code from habitId string.
     */
    fun getAlarmRequestCode(habitId: String): Int {
        // Absolute value modulo 1,000,000 to keep within valid integer range
        return Math.abs(habitId.hashCode() % 1_000_000)
    }

    /**
     * Formats 24h "HH:mm" time string into a 12h user-friendly string (e.g. "07:30" -> "7:30 AM").
     */
    fun formatDisplayTime(timeString: String): String {
        return try {
            val parts = timeString.split(":")
            val h = parts[0].toInt()
            val m = parts[1].toInt()
            val amPm = if (h >= 12) "PM" else "AM"
            val displayHour = when {
                h == 0 -> 12
                h > 12 -> h - 12
                else -> h
            }
            String.format(java.util.Locale.US, "%d:%02d %s", displayHour, m, amPm)
        } catch (e: Exception) {
            timeString
        }
    }
}
