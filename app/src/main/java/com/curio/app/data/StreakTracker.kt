package com.curio.app.data

import android.content.Context
import java.util.Calendar
import java.util.concurrent.TimeUnit

/**
 * Manages the daily activity streak via SharedPreferences.
 *
 * Streak logic:
 * - Each call to [recordActivity] records today as active.
 * - If today is the day after [lastActiveEpochDay], the streak increments.
 * - If today is the same as [lastActiveEpochDay], the streak is unchanged (no double-count).
 * - If there's a gap > 1 day, the streak resets to 1.
 * - [getStreak] returns 0 if the last active day is older than yesterday (streak broken).
 *
 * Usage:
 *   StreakTracker.recordActivity(context)   // call on save or spin
 *   val days = StreakTracker.getStreak(context)  // read in HomeScreen
 */
object StreakTracker {

    private const val PREFS_NAME = "curio_streak"
    private const val KEY_LAST_EPOCH_DAY = "streak_last_epoch_day"
    private const val KEY_CURRENT_STREAK = "streak_current"

    /** Record that the user was active today. Safe to call multiple times per day. */
    fun recordActivity(context: Context) {
        val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        val today = todayEpochDay()
        val lastDay = prefs.getLong(KEY_LAST_EPOCH_DAY, 0L)
        val currentStreak = prefs.getInt(KEY_CURRENT_STREAK, 0)

        val newStreak = when {
            lastDay == 0L -> 1                          // first ever activity
            today == lastDay -> currentStreak           // already recorded today
            today == lastDay + 1 -> currentStreak + 1   // consecutive day!
            else -> 1                                   // gap — reset streak
        }

        prefs.edit()
            .putLong(KEY_LAST_EPOCH_DAY, today)
            .putInt(KEY_CURRENT_STREAK, newStreak)
            .apply()
    }

    /**
     * Get the current streak count.
     * Returns 0 if the streak is broken (no activity today or yesterday).
     */
    fun getStreak(context: Context): Int {
        val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        val lastDay = prefs.getLong(KEY_LAST_EPOCH_DAY, 0L)
        val streak = prefs.getInt(KEY_CURRENT_STREAK, 0)
        if (lastDay == 0L) return 0

        val today = todayEpochDay()
        return when {
            today == lastDay || today == lastDay + 1 -> streak  // active today or yesterday
            else -> 0                                            // streak broken
        }
    }

    /** Reset streak entirely (for testing / debugging). */
    fun reset(context: Context) {
        context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
            .edit().clear().apply()
    }

    /** Epoch day = days since Unix epoch (UTC). Integer division truncates. */
    private fun todayEpochDay(): Long =
        TimeUnit.MILLISECONDS.toDays(System.currentTimeMillis())
}
