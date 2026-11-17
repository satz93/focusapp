package io.github.satz93.focusapp.ui.home

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import io.github.satz93.focusapp.data.db.FocusSessionDao
import io.github.satz93.focusapp.data.focus.ActiveSession
import io.github.satz93.focusapp.data.focus.FocusSessionManager
import io.github.satz93.focusapp.data.settings.SettingsRepository
import io.github.satz93.focusapp.data.settings.UserSettings
import io.github.satz93.focusapp.data.usage.UsageRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.ExperimentalCoroutinesApi
import java.time.LocalDate
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

/** Today's numbers from usage stats. Null = not loaded or no usage access. */
data class TodayUsage(
    val screenTimeMs: Long,
    val pickups: Int,
    val avgPickups: Int?,
    val unlocksAvailable: Boolean,
)

data class HomeUiState(
    val sessionsDone: Int = 0,
    val sessionsTarget: Int = UserSettings().sessionsPerDay,
    val sessionLengthMin: Int = UserSettings().sessionLengthMin,
    val screenTimeGoalMin: Int = UserSettings().screenTimeGoalMin,
    val hasUsageAccess: Boolean = true,
    val today: TodayUsage? = null,
    val active: ActiveSession? = null,
    val focusTodayMs: Long = 0L,
)

/** Per-app limits on Home arrive with milestone 5. */
@OptIn(ExperimentalCoroutinesApi::class)
class HomeViewModel(
    settings: SettingsRepository,
    private val usage: UsageRepository,
    dao: FocusSessionDao,
    manager: FocusSessionManager,
) : ViewModel() {

    private val today = MutableStateFlow<TodayUsage?>(null)
    private val hasAccess = MutableStateFlow(true)
    private val date = MutableStateFlow(LocalDate.now())
    private var avgPickups: Int? = null

    /** A session that finished while the app was closed; Home shows its "Session complete" screen. */
    val pendingComplete: StateFlow<Long?> = manager.pendingComplete

    private val todaySessions = date.flatMapLatest { dao.observeDay(it.toString()) }

    val state: StateFlow<HomeUiState> = combine(
        settings.settings, today, hasAccess, todaySessions, manager.active,
    ) { s, t, access, sessions, active ->
        val completed = sessions.filter { it.counts }
        HomeUiState(
            sessionsDone = completed.size,
            sessionsTarget = s.sessionsPerDay,
            sessionLengthMin = s.sessionLengthMin,
            screenTimeGoalMin = s.screenTimeGoalMin,
            hasUsageAccess = access,
            today = t,
            active = active,
            focusTodayMs = completed.sumOf { it.actualMs },
        )
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), HomeUiState())

    /** Called whenever Home becomes visible. */
    fun refresh() {
        date.value = LocalDate.now() // rolls over at midnight
        val access = usage.hasUsageAccess()
        hasAccess.value = access
        if (!access) {
            today.value = null
            return
        }
        viewModelScope.launch {
            val day = runCatching { usage.loadToday() }.getOrNull() ?: return@launch
            if (avgPickups == null) {
                avgPickups = runCatching { usage.loadSummary() }.getOrNull()
                    ?.takeIf { !it.isPartialDay && it.avgUnlocks > 0 }?.avgUnlocks
            }
            today.value = TodayUsage(
                screenTimeMs = day.screenTimeMs,
                pickups = day.unlocks,
                avgPickups = avgPickups,
                unlocksAvailable = usage.unlocksAvailable,
            )
        }
    }
}
