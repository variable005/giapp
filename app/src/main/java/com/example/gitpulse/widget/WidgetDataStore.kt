package com.example.gitpulse.widget

import android.content.Context

/**
 * Flat snapshot of the last successful load, persisted so home screen widgets
 * can render without running the network stack themselves.
 *
 * [heatGrid] is a compact, week-major encoding of the contribution grid: eight
 * columns of seven days, each character a level digit `0`-`4`, or `x` for a day
 * that does not exist in the window.
 */
data class WidgetSnapshot(
    val username: String,
    val activeStreakDays: Int,
    val longestStreakDays: Int,
    val totalAnnualCommits: Int,
    val reposCount: Int,
    val heatGrid: String
) {
    val hasData: Boolean get() = username.isNotEmpty()
}

object WidgetDataStore {

    private const val PREFS_NAME = "gitpulse_widget_snapshot"
    private const val KEY_USERNAME = "username"
    private const val KEY_ACTIVE = "active_streak"
    private const val KEY_LONGEST = "longest_streak"
    private const val KEY_COMMITS = "total_commits"
    private const val KEY_REPOS = "repos"
    private const val KEY_GRID = "heat_grid"

    fun save(context: Context, snapshot: WidgetSnapshot) {
        context.applicationContext
            .getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
            .edit()
            .putString(KEY_USERNAME, snapshot.username)
            .putInt(KEY_ACTIVE, snapshot.activeStreakDays)
            .putInt(KEY_LONGEST, snapshot.longestStreakDays)
            .putInt(KEY_COMMITS, snapshot.totalAnnualCommits)
            .putInt(KEY_REPOS, snapshot.reposCount)
            .putString(KEY_GRID, snapshot.heatGrid)
            .apply()
    }

    fun load(context: Context): WidgetSnapshot? {
        val prefs = context.applicationContext
            .getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        val username = prefs.getString(KEY_USERNAME, null) ?: return null
        return WidgetSnapshot(
            username = username,
            activeStreakDays = prefs.getInt(KEY_ACTIVE, 0),
            longestStreakDays = prefs.getInt(KEY_LONGEST, 0),
            totalAnnualCommits = prefs.getInt(KEY_COMMITS, 0),
            reposCount = prefs.getInt(KEY_REPOS, 0),
            heatGrid = prefs.getString(KEY_GRID, "").orEmpty()
        )
    }
}
