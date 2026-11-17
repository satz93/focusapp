package io.github.satz93.focusapp.data.focus

import android.app.Service
import android.content.Context
import android.content.Intent
import android.content.pm.ServiceInfo
import android.os.IBinder
import androidx.core.app.NotificationManagerCompat
import androidx.core.app.ServiceCompat
import androidx.core.content.ContextCompat
import io.github.satz93.focusapp.FocusApplication
import io.github.satz93.focusapp.ui.navigation.Routes
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel
import kotlinx.coroutines.launch

/**
 * Keeps the app process alive during a focus session and shows the ongoing timer notification.
 * The actual end of the session is triggered by an exact alarm (see [FocusSessionManager]).
 */
class FocusTimerService : Service() {
    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.Main.immediate)
    private var started = false

    private val manager get() = (application as FocusApplication).container.focusSessionManager

    override fun onBind(intent: Intent?): IBinder? = null

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        val current = manager.active.value
        ServiceCompat.startForeground(
            this,
            FocusNotifications.ID_TIMER,
            FocusNotifications.timer(this, current, Routes.FOCUS_TIMER),
            ServiceInfo.FOREGROUND_SERVICE_TYPE_SPECIAL_USE,
        )
        if (current == null) {
            stop()
            return START_NOT_STICKY
        }
        if (!started) {
            started = true
            scope.launch {
                manager.active.collect { s ->
                    if (s == null) {
                        stop()
                    } else {
                        runCatching {
                            NotificationManagerCompat.from(this@FocusTimerService).notify(
                                FocusNotifications.ID_TIMER,
                                FocusNotifications.timer(this@FocusTimerService, s, Routes.FOCUS_TIMER),
                            )
                        }
                    }
                }
            }
        }
        return START_STICKY
    }

    private fun stop() {
        ServiceCompat.stopForeground(this, ServiceCompat.STOP_FOREGROUND_REMOVE)
        stopSelf()
    }

    override fun onDestroy() {
        scope.cancel()
        super.onDestroy()
    }

    companion object {
        fun start(context: Context) {
            runCatching { ContextCompat.startForegroundService(context, Intent(context, FocusTimerService::class.java)) }
        }
    }
}
