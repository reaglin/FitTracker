package com.fittracker.data

import android.content.Context
import androidx.core.content.edit

/**
 * Thin wrapper around SharedPreferences for user settings.
 * Stored on-device, no Room needed.
 */
class UserPreferences(context: Context) {

    private val prefs = context.getSharedPreferences("fittracker_prefs", Context.MODE_PRIVATE)

    /** Daily exercise completion goal (default 5) */
    var dailyGoal: Int
        get() = prefs.getInt(KEY_DAILY_GOAL, 5)
        set(value) = prefs.edit { putInt(KEY_DAILY_GOAL, value) }

    companion object {
        private const val KEY_DAILY_GOAL = "daily_goal"
    }
}
