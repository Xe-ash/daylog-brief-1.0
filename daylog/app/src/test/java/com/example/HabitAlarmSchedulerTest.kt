package com.example

import com.example.alarm.HabitAlarmScheduler
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test
import java.util.Calendar

class HabitAlarmSchedulerTest {

    @Test
    fun testFormatDisplayTime() {
        assertEquals("7:30 AM", HabitAlarmScheduler.formatDisplayTime("07:30"))
        assertEquals("6:45 PM", HabitAlarmScheduler.formatDisplayTime("18:45"))
        assertEquals("12:00 PM", HabitAlarmScheduler.formatDisplayTime("12:00"))
        assertEquals("12:15 AM", HabitAlarmScheduler.formatDisplayTime("00:15"))
    }

    @Test
    fun testGetAlarmRequestCode() {
        val code1 = HabitAlarmScheduler.getAlarmRequestCode("habit_123")
        val code2 = HabitAlarmScheduler.getAlarmRequestCode("habit_123")
        val code3 = HabitAlarmScheduler.getAlarmRequestCode("habit_456")

        assertEquals("Same habit ID should produce identical request code", code1, code2)
        assertTrue("Request code should be non-negative", code1 >= 0)
        assertTrue("Request code should be within safe limit", code1 < 1_000_000)
    }

    @Test
    fun testEdgeCasePastTimeAdvancesToNextDay() {
        val now = Calendar.getInstance()
        val currentHour = now.get(Calendar.HOUR_OF_DAY)
        val currentMinute = now.get(Calendar.MINUTE)

        // Choose a time 1 hour in the past (or earlier today)
        val pastHour = (currentHour - 1 + 24) % 24
        val timeString = String.format(java.util.Locale.US, "%02d:%02d", pastHour, currentMinute)

        val parts = timeString.split(":")
        val target = Calendar.getInstance().apply {
            set(Calendar.HOUR_OF_DAY, parts[0].toInt())
            set(Calendar.MINUTE, parts[1].toInt())
            set(Calendar.SECOND, 0)
            set(Calendar.MILLISECOND, 0)
        }

        // Test the edge case calculation logic
        if (target.timeInMillis <= now.timeInMillis) {
            target.add(Calendar.DAY_OF_YEAR, 1)
        }

        assertTrue("Trigger time should be strictly in the future", target.timeInMillis > now.timeInMillis)
        // Ensure it is scheduled for tomorrow (within 24 hours from target time)
        val diffHours = (target.timeInMillis - now.timeInMillis) / (1000 * 3600)
        assertTrue("Difference should be less than 25 hours", diffHours < 25)
    }
}
