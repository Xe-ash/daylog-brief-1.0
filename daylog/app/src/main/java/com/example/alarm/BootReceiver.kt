package com.example.alarm

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.util.Log

class BootReceiver : BroadcastReceiver() {

    override fun onReceive(context: Context, intent: Intent) {
        val action = intent.action
        if (action == Intent.ACTION_BOOT_COMPLETED || action == "android.intent.action.QUICKBOOT_POWERON") {
            Log.d("BootReceiver", "Device reboot detected: rescheduling all active habit alarms...")
            val alarms = HabitAlarmStore.getAllAlarms(context)
            for (alarm in alarms) {
                try {
                    HabitAlarmScheduler.scheduleHabitAlarm(
                        context = context,
                        habitId = alarm.habitId,
                        habitName = alarm.habitName,
                        timeString = alarm.reminderTime
                    )
                    Log.d("BootReceiver", "Rescheduled alarm for habit ${alarm.habitName} at ${alarm.reminderTime}")
                } catch (e: Exception) {
                    Log.e("BootReceiver", "Error rescheduling habit alarm: ${e.message}", e)
                }
            }
        }
    }
}
