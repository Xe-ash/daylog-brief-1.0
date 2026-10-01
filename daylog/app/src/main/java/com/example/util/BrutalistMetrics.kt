package com.example.util

import com.example.model.Activity
import com.example.model.ActivityClassification
import com.example.model.DailyGoal
import com.example.model.DayNote
import com.example.model.DayReflection
import com.example.model.DaySummary
import com.example.model.Habit
import com.example.model.HabitLog
import com.example.model.HabitStatus
import kotlin.math.min
import kotlin.math.roundToInt

object BrutalistMetrics {

    fun renderAsciiBar(percent: Double, totalBlocks: Int = 12): String {
        val clamped = percent.coerceIn(0.0, 100.0)
        val filledBlocks = ((clamped / 100.0) * totalBlocks).roundToInt()
        val emptyBlocks = totalBlocks - filledBlocks
        val filledStr = "█".repeat(filledBlocks.coerceAtLeast(0))
        val emptyStr = "░".repeat(emptyBlocks.coerceAtLeast(0))
        return "$filledStr$emptyStr ${clamped.roundToInt()}%"
    }

    fun calculateDaySummary(
        date: String,
        activities: List<Activity>,
        targetProductiveMinutes: Int,
        primaryGoalText: String,
        habits: List<Habit>,
        habitLogs: List<HabitLog>,
        nowMillis: Long = System.currentTimeMillis()
    ): DaySummary {
        var prodSec = 0L
        var neutralSec = 0L
        var unprodSec = 0L
        var unknownSec = 0L
        val catMap = mutableMapOf<String, Long>()

        activities.forEach { act ->
            val duration = if (act.status == com.example.model.ActivityStatus.RUNNING) {
                ((nowMillis - act.startedAt) / 1000).coerceAtLeast(0)
            } else {
                act.durationSeconds
            }

            when (act.classification) {
                ActivityClassification.PRODUCTIVE -> prodSec += duration
                ActivityClassification.NEUTRAL -> neutralSec += duration
                ActivityClassification.UNPRODUCTIVE -> unprodSec += duration
                ActivityClassification.UNKNOWN -> unknownSec += duration
            }

            val cat = act.categoryName.ifBlank { "OTHER" }
            catMap[cat] = (catMap[cat] ?: 0L) + duration
        }

        val totalTrackedSec = prodSec + neutralSec + unprodSec + unknownSec
        // [VM FIX]: Waking window is 17h (06:00 to 23:00). Untracked is elapsed time inside waking window minus tracked time.
        val wakingWindowCapacitySec = 17 * 3600L
        val cal = java.util.Calendar.getInstance()
        val todayDateStr = java.text.SimpleDateFormat("yyyy-MM-dd", java.util.Locale.US).format(cal.time)
        val elapsedWakingSec = when {
            date < todayDateStr -> wakingWindowCapacitySec
            date > todayDateStr -> 0L
            else -> {
                val currentSecondsSinceMidnight = cal.get(java.util.Calendar.HOUR_OF_DAY) * 3600L +
                        cal.get(java.util.Calendar.MINUTE) * 60L +
                        cal.get(java.util.Calendar.SECOND)
                (currentSecondsSinceMidnight - 6 * 3600L).coerceIn(0L, wakingWindowCapacitySec)
            }
        }
        val untrackedSec = (elapsedWakingSec - totalTrackedSec).coerceAtLeast(0L)

        val targetProductiveSec = (targetProductiveMinutes * 60L).coerceAtLeast(1L)
        val goalProgressPercent = (prodSec.toDouble() / targetProductiveSec.toDouble()) * 100.0

        val completedHabits = habitLogs.count { it.status == HabitStatus.COMPLETED }
        val totalHabits = habits.count { it.active }
        val habitPercent = if (totalHabits > 0) (completedHabits.toDouble() / totalHabits.toDouble()) * 100.0 else 100.0

        val coveragePercent = (totalTrackedSec.toDouble() / wakingWindowCapacitySec.toDouble()) * 100.0
        val productiveRatioPercent = if (totalTrackedSec > 0) (prodSec.toDouble() / totalTrackedSec.toDouble()) * 100.0 else 0.0

        val score = (
                min(goalProgressPercent, 100.0) * 0.40 +
                        min(productiveRatioPercent, 100.0) * 0.30 +
                        min(habitPercent, 100.0) * 0.20 +
                        min(coveragePercent, 100.0) * 0.10
                ).roundToInt().coerceIn(0, 100)

        return DaySummary(
            date = date,
            productiveSeconds = prodSec,
            neutralSeconds = neutralSec,
            unproductiveSeconds = unprodSec,
            unknownSeconds = unknownSec,
            untrackedSeconds = untrackedSec,
            totalTrackedSeconds = totalTrackedSec,
            targetProductiveMinutes = targetProductiveMinutes,
            primaryGoalCompleted = goalProgressPercent >= 100.0,
            primaryGoalText = primaryGoalText,
            completedHabitsCount = completedHabits,
            totalHabitsCount = totalHabits,
            dayScore = score,
            trackingCoveragePercent = coveragePercent,
            goalCompletionPercent = goalProgressPercent,
            categoryDurations = catMap
        )
    }

    fun calculateOpportunityCosts(unproductiveSeconds: Long): List<String> {
        val hours = unproductiveSeconds / 3600.0
        if (hours < 0.2) return listOf("No significant time leakage detected.")

        val items = mutableListOf<String>()
        val readingSessions = (unproductiveSeconds / (30 * 60)).toInt()
        val gymSessions = (unproductiveSeconds / (45 * 60)).toInt()
        val deepWorkDays = (unproductiveSeconds / (4 * 3600)).toInt()
        val codingModules = (unproductiveSeconds / (2 * 3600)).toInt()

        if (readingSessions > 0) items.add("+ $readingSessions full reading sessions (30m each)")
        if (gymSessions > 0) items.add("+ $gymSessions intense workout sessions (45m each)")
        if (deepWorkDays > 0) items.add("+ $deepWorkDays full deep-work sessions (4h blocks)")
        else if (codingModules > 0) items.add("+ $codingModules dedicated project build sprints (2h)")

        return items
    }

    fun generateDeterministicInsights(
        summaries: List<DaySummary>,
        categories: List<Pair<String, Long>>,
        leaks: List<Pair<String, Long>>
    ): List<String> {
        val insights = mutableListOf<String>()
        val activeDays = summaries.filter { it.totalTrackedSeconds > 0 }
        if (activeDays.isEmpty()) return listOf("Begin tracking your hours to generate behavioral audits.")

        val avgProd = activeDays.map { it.productiveSeconds }.average() / 3600.0
        val avgUnprod = activeDays.map { it.unproductiveSeconds }.average() / 3600.0

        if (avgProd >= 5.0) {
            insights.add("High execution velocity: averaging ${String.format(java.util.Locale.US, "%.1f", avgProd)}h productive time on tracked days.")
        } else {
            insights.add("Productive baseline is ${String.format(java.util.Locale.US, "%.1f", avgProd)}h/day. Aim to push deep work past 5h.")
        }

        if (leaks.isNotEmpty()) {
            val topLeak = leaks.first()
            val leakHrs = topLeak.second / 3600.0
            insights.add("Primary friction point: '${topLeak.first}' accounted for ${String.format(java.util.Locale.US, "%.1f", leakHrs)}h of lost focus.")
        }

        val highStreakDays = activeDays.count { it.goalCompletionPercent >= 100.0 }
        if (highStreakDays > 0) {
            insights.add("Met or exceeded 100% daily focus target on $highStreakDays days in this period.")
        }

        return insights
    }


}
