package com.example.veggielens.viewmodel

import com.example.veggielens.data.model.ScanHistory
import java.util.TimeZone
import java.util.Calendar

object ProfileStatsCalculator {
    fun calculate(
        history: List<ScanHistory>,
        now: Long = System.currentTimeMillis(),
        timeZone: TimeZone = TimeZone.getDefault()
    ): ProfileStats {
        val current = Calendar.getInstance(timeZone).apply { timeInMillis = now }
        val monthlyScans = history.count { itme ->
            Calendar.getInstance(timeZone).apply { timeInMillis = itme.timestamp }.let {
                it.get(Calendar.YEAR) == current.get(Calendar.YEAR) &&
                        it.get(Calendar.MONTH) == current.get(Calendar.MONTH)
            }
        }

        return ProfileStats(
            totalScans = history.size,
            uniqueVegetables = history.map { it.vegetableName.lowercase() }.distinct().size,
            monthlyScans = monthlyScans,
            streakDays = calculateStreak(history, current, timeZone)
        )
    }

    private fun calculateStreak(
        history: List<ScanHistory>,
        today: Calendar,
        timeZone: TimeZone
    ): Int {
        val scannedDays = history.mapTo(mutableSetOf()) { item ->
            Calendar.getInstance(timeZone).apply { timeInMillis = item.timestamp }.dayKey()
        }
        val cursor = (today.clone() as Calendar).apply {
            set(Calendar.HOUR_OF_DAY, 0)
            set(Calendar.MINUTE, 0)
            set(Calendar.SECOND, 0)
            set(Calendar.MILLISECOND, 0)
        }

        var streak = 0
        while (cursor.dayKey() in scannedDays) {
            streak++
            cursor.add(Calendar.DAY_OF_YEAR, -1)
        }
        return streak
    }

    private fun Calendar.dayKey(): String =
        "${get(Calendar.YEAR)}-${get(Calendar.DAY_OF_YEAR)}"
}