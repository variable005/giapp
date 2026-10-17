package com.example.gitpulse.widget

import android.appwidget.AppWidgetManager
import android.appwidget.AppWidgetProvider
import android.content.Context

/** Large 4x3 masthead: Kitsu, the annual total and an eight-week heatmap. */
class KitsuHeatmapWidget : AppWidgetProvider() {

    override fun onUpdate(
        context: Context,
        appWidgetManager: AppWidgetManager,
        appWidgetIds: IntArray
    ) {
        KitsuWidgetRenderer.updateHeatmap(context)
    }

    /** First instance added: fetch real numbers instead of the placeholder. */
    override fun onEnabled(context: Context) {
        WidgetRefreshScheduler.refreshNow(context)
    }
}
