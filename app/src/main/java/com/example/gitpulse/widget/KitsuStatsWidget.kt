package com.example.gitpulse.widget

import android.appwidget.AppWidgetManager
import android.appwidget.AppWidgetProvider
import android.content.Context

/** Medium 4x2 masthead: Kitsu beside streak, commits and repo count. */
class KitsuStatsWidget : AppWidgetProvider() {
    override fun onUpdate(
        context: Context,
        appWidgetManager: AppWidgetManager,
        appWidgetIds: IntArray
    ) {
        KitsuWidgetRenderer.updateStats(context)
    }
}
