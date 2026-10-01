package com.example.util

import com.example.model.Activity
import com.example.model.TimeGap
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale
import java.util.TimeZone

object DateTimeUtils {

    fun getTodayDateString(timeZone: TimeZone = TimeZone.getDefault()): String {
        val sdf = SimpleDateFormat("yyyy-MM-dd", Locale.US)
        sdf.timeZone = timeZone
        return sdf.format(Date())
    }

    fun getDateStringWithOffset(dayOffset: Int, timeZone: TimeZone = TimeZone.getDefault()): String {
        val cal = Calendar.getInstance(timeZone)
        cal.add(Calendar.DAY_OF_YEAR, dayOffset)
        val sdf = SimpleDateFormat("yyyy-MM-dd", Locale.US)
        sdf.timeZone = timeZone
        return sdf.format(cal.time)
    }

    fun formatDateToHeader(dateStr: String): String {
        return try {
            val sdfInput = SimpleDateFormat("yyyy-MM-dd", Locale.US)
            val date = sdfInput.parse(dateStr) ?: Date()
            val sdfOutput = SimpleDateFormat("EEEE\ndd MMMM yyyy", Locale.US)
            sdfOutput.format(date).uppercase(Locale.US)
        } catch (e: Exception) {
            dateStr
        }
    }

    fun formatShortDate(dateStr: String): String {
        return try {
            val sdfInput = SimpleDateFormat("yyyy-MM-dd", Locale.US)
            val date = sdfInput.parse(dateStr) ?: Date()
            val sdfOutput = SimpleDateFormat("dd MMM", Locale.US)
            sdfOutput.format(date).uppercase(Locale.US)
        } catch (e: Exception) {
            dateStr
        }
    }

    fun formatTime(timestamp: Long, timeZone: TimeZone = TimeZone.getDefault()): String {
        val sdf = SimpleDateFormat("HH:mm", Locale.US)
        sdf.timeZone = timeZone
        return sdf.format(Date(timestamp))
    }

    fun formatTimeWithSeconds(timestamp: Long, timeZone: TimeZone = TimeZone.getDefault()): String {
        val sdf = SimpleDateFormat("HH:mm:ss", Locale.US)
        sdf.timeZone = timeZone
        return sdf.format(Date(timestamp))
    }

    fun formatDurationHhMmSs(seconds: Long): String {
        val hrs = seconds / 3600
        val mins = (seconds % 3600) / 60
        val secs = seconds % 60
        return String.format(Locale.US, "%02d:%02d:%02d", hrs, mins, secs)
    }

    fun formatDurationShort(seconds: Long): String {
        val hrs = seconds / 3600
        val mins = (seconds % 3600) / 60
        return if (hrs > 0) {
            "${hrs}h ${mins}m"
        } else {
            "${mins}m"
        }
    }

    fun formatDurationDetailed(seconds: Long): String {
        val hrs = seconds / 3600
        val mins = (seconds % 3600) / 60
        return if (hrs > 0) {
            "${hrs}h ${mins}m"
        } else {
            "${mins}m"
        }
    }

    fun formatDurationFromMinutes(minutes: Int): String {
        val hrs = minutes / 60
        val mins = minutes % 60
        return if (hrs > 0 && mins > 0) {
            "${hrs}h ${mins}m"
        } else if (hrs > 0) {
            "${hrs}h"
        } else {
            "${mins}m"
        }
    }

    fun parseDate(dateStr: String): Date? {
        return try {
            val sdf = SimpleDateFormat("yyyy-MM-dd", Locale.US)
            sdf.parse(dateStr)
        } catch (e: Exception) {
            null
        }
    }

    fun getStartOfDayMillis(dateStr: String, timeZone: TimeZone = TimeZone.getDefault()): Long {
        val cal = Calendar.getInstance(timeZone)
        val date = parseDate(dateStr) ?: Date()
        cal.time = date
        cal.set(Calendar.HOUR_OF_DAY, 0)
        cal.set(Calendar.MINUTE, 0)
        cal.set(Calendar.SECOND, 0)
        cal.set(Calendar.MILLISECOND, 0)
        return cal.timeInMillis
    }

    fun getEndOfDayMillis(dateStr: String, timeZone: TimeZone = TimeZone.getDefault()): Long {
        val cal = Calendar.getInstance(timeZone)
        val date = parseDate(dateStr) ?: Date()
        cal.time = date
        cal.set(Calendar.HOUR_OF_DAY, 23)
        cal.set(Calendar.MINUTE, 59)
        cal.set(Calendar.SECOND, 59)
        cal.set(Calendar.MILLISECOND, 999)
        return cal.timeInMillis
    }

    /**
     * Finds gaps between recorded activities >= 5 minutes.
     */
    fun findUntrackedGaps(
        activities: List<Activity>,
        dateStr: String,
        wakeTime: String = "06:00",
        sleepTime: String = "23:00"
    ): List<TimeGap> {
        val sorted = activities
            .filter { it.status == com.example.model.ActivityStatus.COMPLETED && it.endedAt != null && it.endedAt > it.startedAt }
            .sortedBy { it.startedAt }

        if (sorted.isEmpty()) return emptyList()

        val gaps = mutableListOf<TimeGap>()
        for (i in 0 until sorted.size - 1) {
            val currentEnd = sorted[i].endedAt ?: continue
            val nextStart = sorted[i + 1].startedAt
            val diffMillis = nextStart - currentEnd
            val diffMins = diffMillis / (1000 * 60)
            if (diffMins >= 5) {
                gaps.add(
                    TimeGap(
                        startTimeMillis = currentEnd,
                        endTimeMillis = nextStart,
                        durationMinutes = diffMins,
                        date = dateStr
                    )
                )
            }
        }
        return gaps
    }

    fun isOverlapping(startA: Long, endA: Long, startB: Long, endB: Long): Boolean {
        return startA < endB && endA > startB
    }
}
