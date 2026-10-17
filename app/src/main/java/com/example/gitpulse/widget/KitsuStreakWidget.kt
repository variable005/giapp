package com.example.gitpulse.widget

import android.appwidget.AppWidgetManager
import android.appwidget.AppWidgetProvider
import android.content.Context

/** Small 2x2 masthead: Kitsu plus the current streak. */
class KitsuStreakWidget : AppWidgetProvider() {

    override fun onUpdate(
        context: Context,
        appWidgetManager: AppWidgetManager,
        appWidgetIds: IntArray
    ) {
        KitsuWidgetRenderer.updateStreak(context)
    }

    /** First instance added: fetch real numbers instead of the placeholder. */
    override fun onEnabled(context: Context) {
        WidgetRefreshScheduler.refreshNow(context)
    }
}
