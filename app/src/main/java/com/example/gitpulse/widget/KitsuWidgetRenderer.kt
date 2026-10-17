package com.example.gitpulse.widget

import android.app.PendingIntent
import android.appwidget.AppWidgetManager
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.widget.RemoteViews
import androidx.core.content.ContextCompat
import com.example.gitpulse.MainActivity
import com.example.gitpulse.R

/**
 * Renders the mascot widgets from the persisted [WidgetSnapshot].
 *
 * All three widgets read the same snapshot and share one click target (the
 * app), so the only per-widget work is choosing values and, for the heatmap,
 * rasterising the contribution grid.
 */
object KitsuWidgetRenderer {

    /** Grid geometry, mirrored by the encoder in the view model. */
    private const val COLUMNS = 8
    private const val ROWS = 7

    fun refreshAll(context: Context) {
        updateStreak(context)
        updateStats(context)
        updateHeatmap(context)
    }

    fun updateStreak(context: Context) {
        val manager = AppWidgetManager.getInstance(context)
        val ids = manager.getAppWidgetIds(
            ComponentName(context, KitsuStreakWidget::class.java)
        )
        if (ids.isEmpty()) return

        val snapshot = WidgetDataStore.load(context)
        val value = if (snapshot?.hasData == true) {
            snapshot.activeStreakDays.toString()
        } else {
            context.getString(R.string.widget_empty_value)
        }
        val label = context.getString(R.string.widget_streak_label)

        ids.forEach { id ->
            val views = RemoteViews(context.packageName, R.layout.widget_kitsu_streak)
            views.setTextViewText(R.id.widget_value, value)
            views.setTextViewText(R.id.widget_label, label)
            views.setOnClickPendingIntent(R.id.widget_root, openApp(context))
            manager.updateAppWidget(id, views)
        }
    }

    fun updateStats(context: Context) {
        val manager = AppWidgetManager.getInstance(context)
        val ids = manager.getAppWidgetIds(
            ComponentName(context, KitsuStatsWidget::class.java)
        )
        if (ids.isEmpty()) return

        val snapshot = WidgetDataStore.load(context)
        val placeholder = context.getString(R.string.widget_empty_value)
        val hasData = snapshot?.hasData == true

        val username = if (hasData) "@${snapshot.username}" else placeholder
        val streak = if (hasData) snapshot.activeStreakDays.toString() else placeholder
        val commits = if (hasData) snapshot.totalAnnualCommits.toString() else placeholder
        val repos = if (hasData) snapshot.reposCount.toString() else placeholder

        ids.forEach { id ->
            val views = RemoteViews(context.packageName, R.layout.widget_kitsu_stats)
            views.setImageViewResource(R.id.widget_mascot, R.drawable.kitsu_companion)
            views.setTextViewText(R.id.widget_username, username)
            views.setTextViewText(R.id.widget_streak_value, streak)
            views.setTextViewText(R.id.widget_commits_value, commits)
            views.setTextViewText(R.id.widget_repos_value, repos)
            views.setOnClickPendingIntent(R.id.widget_root, openApp(context))
            manager.updateAppWidget(id, views)
        }
    }

    fun updateHeatmap(context: Context) {
        val manager = AppWidgetManager.getInstance(context)
        val ids = manager.getAppWidgetIds(
            ComponentName(context, KitsuHeatmapWidget::class.java)
        )
        if (ids.isEmpty()) return

        val snapshot = WidgetDataStore.load(context)
        val placeholder = context.getString(R.string.widget_empty_value)
        val hasData = snapshot?.hasData == true

        val username = if (hasData) "@${snapshot.username}" else placeholder
        val total = if (hasData) snapshot.totalAnnualCommits.toString() else placeholder
        val streak = if (hasData) {
            "${snapshot.activeStreakDays}d · ${snapshot.longestStreakDays}d"
        } else {
            placeholder
        }

        val grid = snapshot?.heatGrid.orEmpty()
        val bitmap = renderHeatGrid(context, grid) ?: renderEmptyGrid(context)

        ids.forEach { id ->
            val views = RemoteViews(context.packageName, R.layout.widget_kitsu_heatmap)
            views.setImageViewResource(R.id.widget_mascot, R.drawable.kitsu_companion)
            views.setTextViewText(R.id.widget_username, username)
            views.setTextViewText(R.id.widget_total, total)
            views.setTextViewText(R.id.widget_streak, streak)
            views.setImageViewBitmap(R.id.widget_heatmap, bitmap)
            views.setOnClickPendingIntent(R.id.widget_root, openApp(context))
            manager.updateAppWidget(id, views)
        }
    }

    private fun openApp(context: Context): PendingIntent {
        val intent = Intent(context, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP
        }
        return PendingIntent.getActivity(
            context,
            0,
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )
    }

    /**
     * Rasterises the week-major grid. Levels reuse the in-app ramp so the
     * widget and the dashboard read the same.
     */
    private fun renderHeatGrid(context: Context, grid: String): Bitmap? {
        if (grid.length < COLUMNS * ROWS) return null

        val density = context.resources.displayMetrics.density
        val cell = 9f * density
        val gap = 3f * density
        val width = (COLUMNS * cell + (COLUMNS - 1) * gap).toInt()
        val height = (ROWS * cell + (ROWS - 1) * gap).toInt()

        val bitmap = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888)
        val canvas = Canvas(bitmap)
        val paint = Paint(Paint.ANTI_ALIAS_FLAG)
        val accent = ContextCompat.getColor(context, R.color.widget_accent)
        val empty = ContextCompat.getColor(context, R.color.widget_heat_empty)
        val radius = cell * 0.3f

        for (index in 0 until COLUMNS * ROWS) {
            val column = index / ROWS
            val row = index % ROWS
            val token = grid[index]
            if (token == 'x') continue

            val level = token - '0'
            paint.color = when (level) {
                4 -> accent
                3 -> withAlpha(accent, 0.82f)
                2 -> withAlpha(accent, 0.58f)
                1 -> withAlpha(accent, 0.32f)
                else -> empty
            }

            val left = column * (cell + gap)
            val top = row * (cell + gap)
            canvas.drawRoundRect(left, top, left + cell, top + cell, radius, radius, paint)
        }
        return bitmap
    }

    /** Neutral grid shown before the first successful sync. */
    private fun renderEmptyGrid(context: Context): Bitmap {
        val density = context.resources.displayMetrics.density
        val cell = 9f * density
        val gap = 3f * density
        val width = (COLUMNS * cell + (COLUMNS - 1) * gap).toInt()
        val height = (ROWS * cell + (ROWS - 1) * gap).toInt()

        val bitmap = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888)
        val canvas = Canvas(bitmap)
        val paint = Paint(Paint.ANTI_ALIAS_FLAG)
        paint.color = ContextCompat.getColor(context, R.color.widget_heat_empty)
        val radius = cell * 0.3f

        for (column in 0 until COLUMNS) {
            for (row in 0 until ROWS) {
                val left = column * (cell + gap)
                val top = row * (cell + gap)
                canvas.drawRoundRect(left, top, left + cell, top + cell, radius, radius, paint)
            }
        }
        return bitmap
    }

    private fun withAlpha(color: Int, alpha: Float): Int = Color.argb(
        (alpha * 255f).toInt().coerceIn(0, 255),
        Color.red(color),
        Color.green(color),
        Color.blue(color)
    )
}
