package com.example.gitpulse.widget

import android.content.Context
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import com.example.gitpulse.data.remote.GitHubApiService
import java.util.Calendar

/**
 * Keeps the mascot widgets current without the user opening the app.
 *
 * It replays the last known account (username + optional token) through the
 * same loader the UI uses, rewrites the snapshot and repaints every widget.
 * Widgets themselves never touch the network.
 */
class WidgetRefreshWorker(
    context: Context,
    params: WorkerParameters
) : CoroutineWorker(context, params) {

    override suspend fun doWork(): Result {
        val context = applicationContext

        val snapshot = WidgetDataStore.load(context) ?: return Result.success()
        val username = snapshot.username
        if (username.isBlank()) return Result.success()

        val token = WidgetDataStore.loadToken(context)
        val year = Calendar.getInstance().get(Calendar.YEAR)

        val data = GitHubApiService()
            .fetchCompleteUserData(username, token, year)
            .getOrElse {
                // Transient (offline, rate limited): back off and try again, but
                // do not hammer a permanently failing account.
                return if (runAttemptCount < 3) Result.retry() else Result.success()
            }

        WidgetDataStore.save(context, widgetSnapshotFrom(data))
        KitsuWidgetRenderer.refreshAll(context)
        return Result.success()
    }
}
