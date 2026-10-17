package com.example.gitpulse.widget

import android.content.Context
import androidx.work.Constraints
import androidx.work.ExistingPeriodicWorkPolicy
import androidx.work.ExistingWorkPolicy
import androidx.work.NetworkType
import androidx.work.OneTimeWorkRequestBuilder
import androidx.work.PeriodicWorkRequestBuilder
import androidx.work.WorkManager
import java.util.concurrent.TimeUnit

/**
 * Schedules refresh work for the widgets.
 *
 * One hourly periodic job keeps the snapshot warm; a one-shot job runs when a
 * widget is first added so it shows real numbers immediately instead of
 * waiting for the next period.
 */
object WidgetRefreshScheduler {

    private const val PERIODIC_WORK = "gitpulse_widget_refresh"
    private const val ONE_SHOT_WORK = "gitpulse_widget_refresh_now"

    /** One hour keeps the widgets useful while staying well inside GitHub's
     *  unauthenticated rate limit. */
    private const val INTERVAL_HOURS = 1L

    private val networkConstraints = Constraints.Builder()
        .setRequiredNetworkType(NetworkType.CONNECTED)
        .build()

    fun ensurePeriodic(context: Context) {
        val request = PeriodicWorkRequestBuilder<WidgetRefreshWorker>(
            INTERVAL_HOURS,
            TimeUnit.HOURS
        )
            .setConstraints(networkConstraints)
            .build()

        WorkManager.getInstance(context).enqueueUniquePeriodicWork(
            PERIODIC_WORK,
            ExistingPeriodicWorkPolicy.KEEP,
            request
        )
    }

    fun refreshNow(context: Context) {
        val request = OneTimeWorkRequestBuilder<WidgetRefreshWorker>()
            .setConstraints(networkConstraints)
            .build()

        WorkManager.getInstance(context).enqueueUniqueWork(
            ONE_SHOT_WORK,
            ExistingWorkPolicy.REPLACE,
            request
        )
    }
}
