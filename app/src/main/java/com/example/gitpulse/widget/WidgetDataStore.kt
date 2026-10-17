package com.example.gitpulse.widget

import android.content.Context
import com.example.gitpulse.data.HeatmapDay
import com.example.gitpulse.data.remote.GitPulseAggregateData
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

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
    private const val KEY_TOKEN = "auth_token"

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

    /**
     * Remembers the personal access token so the background worker can include
     * private contributions. It lives in the same app-private file as the
     * snapshot and is read only by the worker.
     */
    fun saveToken(context: Context, token: String?) {
        context.applicationContext
            .getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
            .edit()
            .putString(KEY_TOKEN, token)
            .apply()
    }

    fun loadToken(context: Context): String? =
        context.applicationContext
            .getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
            .getString(KEY_TOKEN, null)
}

/** Builds the widget payload from a freshly loaded aggregate. */
fun widgetSnapshotFrom(data: GitPulseAggregateData): WidgetSnapshot {
    val profile = data.profile
    return WidgetSnapshot(
        username = profile.username,
        activeStreakDays = profile.activeStreakDays,
        longestStreakDays = profile.longestStreakDays,
        totalAnnualCommits = profile.totalAnnualCommits,
        reposCount = profile.reposCount,
        heatGrid = buildHeatGrid(data.contributions)
    )
}

/**
 * Encodes eight weeks of contributions as one character per day, week-major
 * (eight columns of seven), matching [KitsuWidgetRenderer]. Days outside the
 * window are `x`; otherwise the character is the level `0`-`4`.
 *
 * The window is anchored to today because the year fetch also returns days that
 * have not happened yet.
 */
fun buildHeatGrid(contributions: List<HeatmapDay>): String {
    if (contributions.isEmpty()) return ""
    val today = SimpleDateFormat("yyyy-MM-dd", Locale.US).format(Date())
    val past = contributions.filter { it.dateString <= today }
    if (past.isEmpty()) return ""

    val latestWeek = past.maxOf { it.weekOfYear }
    val bySlot = past.associateBy { it.weekOfYear to it.dayOfWeek }

    val grid = StringBuilder(56)
    for (week in (latestWeek - 7)..latestWeek) {
        for (day in 0..6) {
            if (week < 0) {
                grid.append('x')
                continue
            }
            val entry = bySlot[week to day]
            grid.append(if (entry == null) 'x' else ('0' + entry.level.coerceIn(0, 4)))
        }
    }
    return grid.toString()
}
