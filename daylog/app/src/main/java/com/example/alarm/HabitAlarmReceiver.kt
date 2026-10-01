package com.example.alarm

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.media.AudioAttributes
import android.media.RingtoneManager
import android.net.Uri
import android.os.Build
import android.util.Log
import androidx.core.app.NotificationCompat
import com.example.MainActivity
import com.example.R

class HabitAlarmReceiver : BroadcastReceiver() {

    companion object {
        const val ACTION_HABIT_ALARM = "com.example.ACTION_HABIT_ALARM"
        const val EXTRA_HABIT_ID = "extra_habit_id"
        const val EXTRA_HABIT_NAME = "extra_habit_name"
        const val EXTRA_REMINDER_TIME = "extra_reminder_time"
        const val CHANNEL_ID = "daylog_habit_alarm_channel"
        private const val TAG = "HabitAlarmReceiver"
    }

    override fun onReceive(context: Context, intent: Intent) {
        val habitId = intent.getStringExtra(EXTRA_HABIT_ID) ?: return
        val habitName = intent.getStringExtra(EXTRA_HABIT_NAME) ?: "Daily Habit"
        val reminderTime = intent.getStringExtra(EXTRA_REMINDER_TIME)

        Log.d(TAG, "Habit alarm triggered for: $habitName ($habitId) at time $reminderTime")

        showNotification(context, habitId, habitName)

        // Automatically reschedule for the next day so the habit alarm repeats daily!
        if (!reminderTime.isNullOrBlank()) {
            HabitAlarmScheduler.scheduleHabitAlarm(
                context = context,
                habitId = habitId,
                habitName = habitName,
                timeString = reminderTime
            )
            Log.d(TAG, "Habit alarm automatically rescheduled for next day: $habitName at $reminderTime")
        }
    }

    private fun showNotification(context: Context, habitId: String, habitName: String) {
        val notificationManager =
            context.getSystemService(Context.NOTIFICATION_SERVICE) as? NotificationManager ?: return

        // Sound URI: Default alarm sound, fallback to notification sound
        val soundUri: Uri = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_ALARM)
            ?: RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION)

        // Create high-importance NotificationChannel on Android 8.0+ (Oreo+)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val audioAttributes = AudioAttributes.Builder()
                .setContentType(AudioAttributes.CONTENT_TYPE_SONIFICATION)
                .setUsage(AudioAttributes.USAGE_ALARM)
                .build()

            val channel = NotificationChannel(
                CHANNEL_ID,
                "Habit Reminders & Alarms",
                NotificationManager.IMPORTANCE_HIGH
            ).apply {
                description = "High-priority alarms and audio notifications for scheduled habits"
                setSound(soundUri, audioAttributes)
                enableVibration(true)
                vibrationPattern = longArrayOf(0, 500, 200, 500, 200, 500)
                enableLights(true)
                lockscreenVisibility = Notification.VISIBILITY_PUBLIC
                setShowBadge(true)
            }
            notificationManager.createNotificationChannel(channel)
        }

        // Tap action: open MainActivity
        val tapIntent = Intent(context, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP
            putExtra("navigate_tab", "HABITS")
            putExtra("highlight_habit_id", habitId)
        }

        val requestCode = HabitAlarmScheduler.getAlarmRequestCode(habitId)
        val pendingTapIntent = PendingIntent.getActivity(
            context,
            requestCode,
            tapIntent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val notification = NotificationCompat.Builder(context, CHANNEL_ID)
            .setSmallIcon(android.R.drawable.ic_lock_idle_alarm)
            .setContentTitle("⏰ Habit Alarm: $habitName")
            .setContentText("Time to complete your habit! Keep your streak alive.")
            .setStyle(
                NotificationCompat.BigTextStyle()
                    .bigText("Time to complete your habit: $habitName!\nOpen Daylog now to execute and maintain your daily streak.")
            )
            .setPriority(NotificationCompat.PRIORITY_MAX)
            .setCategory(NotificationCompat.CATEGORY_ALARM)
            .setSound(soundUri)
            .setVibrate(longArrayOf(0, 500, 200, 500, 200, 500))
            .setAutoCancel(true)
            .setContentIntent(pendingTapIntent)
            .setDefaults(NotificationCompat.DEFAULT_LIGHTS)
            .setOngoing(false)
            .build()

        try {
            notificationManager.notify(requestCode, notification)
            Log.d(TAG, "System notification dispatched for habit $habitId (id=$requestCode)")
        } catch (se: SecurityException) {
            Log.e(TAG, "Notification permission missing: ${se.message}")
        } catch (e: Exception) {
            Log.e(TAG, "Failed to display notification: ${e.message}", e)
        }
    }
}
