package io.github.satz93.focusapp.data.focus

import android.app.AlarmManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.os.Build
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.intPreferencesKey
import androidx.datastore.preferences.core.longPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.ProcessLifecycleOwner
import io.github.satz93.focusapp.data.db.FocusSessionDao
import io.github.satz93.focusapp.data.db.FocusSessionEntity
import io.github.satz93.focusapp.data.db.SessionStatus
import io.github.satz93.focusapp.data.settings.SettingsRepository
import io.github.satz93.focusapp.data.usage.UsageRepository
import io.github.satz93.focusapp.ui.navigation.Routes
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import kotlinx.coroutines.withContext
import java.time.Instant
import java.time.LocalDate
import java.time.ZoneId

private val Context.timerStore: DataStore<Preferences> by preferencesDataStore(name = "focus_timer")

/**
 * Single source of truth for the focus timer. There is no cap on sessions per day:
 * the daily target is a goal, and sessions beyond it are recorded and shown too.
 */
class FocusSessionManager(
    context: Context,
    private val dao: FocusSessionDao,
    private val usage: UsageRepository,
    private val settings: SettingsRepository,
    private val scope: CoroutineScope,
) {
    private val app = context.applicationContext
    private val store = app.timerStore
    private val mutex = Mutex()

    private val _active = MutableStateFlow<ActiveSession?>(null)
    val active: StateFlow<ActiveSession?> = _active.asStateFlow()

    /** Id of a session that finished and whose "Session complete" screen hasn't been shown yet. */
    private val _pendingComplete = MutableStateFlow<Long?>(null)
    val pendingComplete: StateFlow<Long?> = _pendingComplete.asStateFlow()

    private object Keys {
        val StartedAt = longPreferencesKey("started_at")
        val PlannedMin = intPreferencesKey("planned_min")
        val Number = intPreferencesKey("session_number")
        val PausedTotal = longPreferencesKey("paused_total_ms")
        val PausedAt = longPreferencesKey("paused_at")
    }

    /** Called from Application.onCreate: picks a session back up if the process was killed. */
    fun restore() {
        scope.launch {
            val p = store.data.first()
            val startedAt = p[Keys.StartedAt] ?: return@launch
            val restored = ActiveSession(
                startedAt = startedAt,
                plannedMin = p[Keys.PlannedMin] ?: return@launch,
                sessionNumber = p[Keys.Number] ?: 1,
                pausedTotalMs = p[Keys.PausedTotal] ?: 0L,
                pausedAt = p[Keys.PausedAt],
            )
            mutex.withLock { if (_active.value == null) _active.value = restored }
            if (restored.remainingMs(System.currentTimeMillis()) <= 0L) {
                completeIfDue()
            } else {
                if (!restored.isPaused) scheduleAlarm(restored)
                FocusTimerService.start(app)
            }
        }
    }

    suspend fun start(plannedMin: Int) {
        val session = mutex.withLock {
            if (_active.value != null) return
            val number = dao.completedCount(today()) + 1
            ActiveSession(System.currentTimeMillis(), plannedMin, number).also {
                _active.value = it
                save(it)
            }
        }
        FocusNotifications.cancelComplete(app)
        scheduleAlarm(session)
        FocusTimerService.start(app)
    }

    suspend fun pause() = mutex.withLock {
        val s = _active.value ?: return@withLock
        val paused = s.paused(System.currentTimeMillis())
        _active.value = paused
        save(paused)
        cancelAlarm()
    }

    suspend fun resume() {
        val resumed = mutex.withLock {
            val s = _active.value ?: return
            s.resumed(System.currentTimeMillis()).also {
                _active.value = it
                save(it)
            }
        }
        scheduleAlarm(resumed)
    }

    /** Ends the session without counting it toward today's target. */
    suspend fun endEarly() {
        finish(SessionStatus.ENDED_EARLY)
    }

    /** Completes the session if its time is up. Safe to call repeatedly (UI tick, alarm, restore). */
    suspend fun completeIfDue() {
        val s = _active.value ?: return
        if (s.isPaused || s.remainingMs(System.currentTimeMillis()) > 0L) return
        finish(SessionStatus.COMPLETED)
    }

    fun consumePendingComplete() {
        _pendingComplete.value = null
    }

    private suspend fun finish(status: String) {
        val now = System.currentTimeMillis()
        val s = mutex.withLock {
            val s = _active.value ?: return
            if (status == SessionStatus.COMPLETED && (s.isPaused || s.remainingMs(now) > 0L)) return
            _active.value = null
            clearSaved()
            s
        }
        cancelAlarm()
        val endedAt = if (status == SessionStatus.COMPLETED) s.startedAt + s.pausedTotalMs + s.plannedMs else now
        val pickups = withContext(Dispatchers.Default) {
            runCatching { usage.countUnlocks(s.startedAt, endedAt) }.getOrDefault(0)
        }
        val day = Instant.ofEpochMilli(s.startedAt).atZone(ZoneId.systemDefault()).toLocalDate().toString()
        val id = dao.insert(
            FocusSessionEntity(
                day = day,
                startedAt = s.startedAt,
                endedAt = endedAt,
                plannedMin = s.plannedMin,
                actualMs = s.elapsedMs(now),
                status = status,
                pickupsDuring = pickups,
            ),
        )
        if (status == SessionStatus.COMPLETED) {
            _pendingComplete.value = id
            if (!isAppInForeground()) {
                val target = settings.settings.first().sessionsPerDay
                FocusNotifications.notifyComplete(app, s.sessionNumber, target, s.plannedMin, Routes.SESSION_COMPLETE)
            }
        }
    }

    private fun isAppInForeground(): Boolean =
        ProcessLifecycleOwner.get().lifecycle.currentState.isAtLeast(Lifecycle.State.STARTED)

    private fun today(): String = LocalDate.now().toString()

    private suspend fun save(s: ActiveSession) = store.edit { p ->
        p[Keys.StartedAt] = s.startedAt
        p[Keys.PlannedMin] = s.plannedMin
        p[Keys.Number] = s.sessionNumber
        p[Keys.PausedTotal] = s.pausedTotalMs
        if (s.pausedAt != null) p[Keys.PausedAt] = s.pausedAt else p.remove(Keys.PausedAt)
    }

    private suspend fun clearSaved() = store.edit { it.clear() }

    // --- Exact alarm for the end of the session (works while the phone sleeps) ---

    private val alarmManager get() = app.getSystemService(AlarmManager::class.java)

    private fun alarmIntent(): PendingIntent = PendingIntent.getBroadcast(
        app, 0, Intent(app, FocusAlarmReceiver::class.java),
        PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE,
    )

    private fun scheduleAlarm(s: ActiveSession) {
        val at = s.endsAt(System.currentTimeMillis())
        val am = alarmManager
        val exactAllowed = Build.VERSION.SDK_INT < Build.VERSION_CODES.S || am.canScheduleExactAlarms()
        runCatching {
            if (exactAllowed) {
                am.setExactAndAllowWhileIdle(AlarmManager.RTC_WAKEUP, at, alarmIntent())
            } else {
                am.setAndAllowWhileIdle(AlarmManager.RTC_WAKEUP, at, alarmIntent())
            }
        }
    }

    private fun cancelAlarm() = alarmManager.cancel(alarmIntent())
}
