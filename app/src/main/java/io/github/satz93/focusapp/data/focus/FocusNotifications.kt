package io.github.satz93.focusapp.data.focus

import android.Manifest
import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import androidx.core.content.ContextCompat
import io.github.satz93.focusapp.MainActivity
import io.github.satz93.focusapp.R
import io.github.satz93.focusapp.ui.util.formatClock
import java.text.DateFormat
import java.util.Date

object FocusNotifications {
    const val CHANNEL_TIMER = "focus_timer"
    const val CHANNEL_DONE = "focus_done"
    const val ID_TIMER = 1
    const val ID_DONE = 2

    const val EXTRA_ROUTE = "route"

    fun createChannels(context: Context) {
        val nm = context.getSystemService(NotificationManager::class.java)
        nm.createNotificationChannel(
            NotificationChannel(CHANNEL_TIMER, "Focus timer", NotificationManager.IMPORTANCE_LOW).apply {
                description = "Shows the running focus session"
                setShowBadge(false)
            },
        )
        nm.createNotificationChannel(
            NotificationChannel(CHANNEL_DONE, "Session complete", NotificationManager.IMPORTANCE_HIGH).apply {
                description = "Tells you when a focus session ends"
            },
        )
    }

    private fun openApp(context: Context, route: String, requestCode: Int): PendingIntent {
        val intent = Intent(context, MainActivity::class.java)
            .addFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP or Intent.FLAG_ACTIVITY_CLEAR_TOP)
            .putExtra(EXTRA_ROUTE, route)
        return PendingIntent.getActivity(
            context, requestCode, intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE,
        )
    }

    fun timer(context: Context, session: ActiveSession?, timerRoute: String): Notification {
        val now = System.currentTimeMillis()
        val b = NotificationCompat.Builder(context, CHANNEL_TIMER)
            .setSmallIcon(R.drawable.ic_stat_focus)
            .setOngoing(true)
            .setOnlyAlertOnce(true)
            .setCategory(NotificationCompat.CATEGORY_PROGRESS)
            .setForegroundServiceBehavior(NotificationCompat.FOREGROUND_SERVICE_IMMEDIATE)
            .setContentIntent(openApp(context, timerRoute, 1))
        if (session == null) return b.setContentTitle("Focus session").build()

        b.setContentTitle("Focus session ${session.sessionNumber} · ${session.plannedMin} min")
        b.setProgress(1000, (session.progress(now) * 1000).toInt(), false)
        if (session.isPaused) {
            b.setContentText("Paused · ${formatClock(session.remainingMs(now) / 1000)} left")
            b.setShowWhen(false)
        } else {
            val end = session.endsAt(now)
            b.setContentText("Ends at ${DateFormat.getTimeInstance(DateFormat.SHORT).format(Date(end))}")
            b.setWhen(end).setShowWhen(true).setUsesChronometer(true).setChronometerCountDown(true)
        }
        return b.build()
    }

    fun notifyComplete(context: Context, sessionNumber: Int, target: Int, minutes: Int, completeRoute: String) {
        if (android.os.Build.VERSION.SDK_INT >= 33 &&
            ContextCompat.checkSelfPermission(context, Manifest.permission.POST_NOTIFICATIONS) !=
            PackageManager.PERMISSION_GRANTED
        ) return
        val progress = if (sessionNumber <= target) "Session $sessionNumber of $target done." else "Session $sessionNumber done, beyond your target."
        val n = NotificationCompat.Builder(context, CHANNEL_DONE)
            .setSmallIcon(R.drawable.ic_stat_focus)
            .setContentTitle("Nice work")
            .setContentText("$progress You stayed focused for $minutes minutes.")
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setCategory(NotificationCompat.CATEGORY_STATUS)
            .setAutoCancel(true)
            .setContentIntent(openApp(context, completeRoute, 2))
            .build()
        runCatching { NotificationManagerCompat.from(context).notify(ID_DONE, n) }
    }

    fun cancelComplete(context: Context) = NotificationManagerCompat.from(context).cancel(ID_DONE)
}
