package io.github.satz93.focusapp.ui.progress

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import io.github.satz93.focusapp.data.db.FocusSessionDao
import io.github.satz93.focusapp.data.db.FocusSessionEntity
import io.github.satz93.focusapp.data.settings.SettingsRepository
import io.github.satz93.focusapp.data.usage.DayUsage
import io.github.satz93.focusapp.data.usage.UsageRepository
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import java.time.DayOfWeek
import java.time.LocalDate

data class DayStats(
    val date: LocalDate,
    val sessions: List<FocusSessionEntity>,
    val completed: Int,
    val focusMs: Long,
    /** Null when the phone no longer has usage data for that day (or no usage access). */
    val screenTimeMs: Long?,
    val pickups: Int?,
    val isFuture: Boolean,
)

data class WeekState(
    val days: List<DayStats> = emptyList(),
    val target: Int = 3,
    val goalMin: Int = 210,
) {
    private val pastDays get() = days.filter { !it.isFuture }
    val totalCompleted get() = days.sumOf { it.completed }
    val totalFocusMs get() = days.sumOf { it.focusMs }
    val targetHitDays get() = pastDays.count { it.completed >= target }
    val avgScreenMs: Long? get() = pastDays.mapNotNull { it.screenTimeMs }.takeIf { it.isNotEmpty() }?.average()?.toLong()
    val screenDays get() = pastDays.mapNotNull { it.screenTimeMs }
    val underGoalDays get() = screenDays.count { it <= goalMin * 60_000L }
}

data class DayState(val stats: DayStats? = null, val target: Int = 3, val goalMin: Int = 210)

@OptIn(ExperimentalCoroutinesApi::class)
class ProgressViewModel(
    dao: FocusSessionDao,
    private val usage: UsageRepository,
    settings: SettingsRepository,
) : ViewModel() {

    private val today = MutableStateFlow(LocalDate.now())
    private val _selected = MutableStateFlow(LocalDate.now())
    val selected: StateFlow<LocalDate> = _selected.asStateFlow()
    private val usageDays = MutableStateFlow<Map<LocalDate, DayUsage>>(emptyMap())

    val week: StateFlow<WeekState> = today.flatMapLatest { t ->
        val start = t.with(DayOfWeek.MONDAY)
        val end = start.plusDays(6)
        combine(dao.observeRange(start.toString(), end.toString()), settings.settings, usageDays) { sessions, s, usageMap ->
            WeekState(
                days = (0L..6L).map { i -> stats(start.plusDays(i), t, sessions, usageMap) },
                target = s.sessionsPerDay,
                goalMin = s.screenTimeGoalMin,
            )
        }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), WeekState())

    val day: StateFlow<DayState> = _selected.flatMapLatest { d ->
        combine(dao.observeDay(d.toString()), settings.settings, usageDays, today) { sessions, s, usageMap, t ->
            DayState(stats(d, t, sessions, usageMap), s.sessionsPerDay, s.screenTimeGoalMin)
        }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), DayState())

    fun refresh() {
        val t = LocalDate.now()
        today.value = t
        if (_selected.value.isAfter(t)) _selected.value = t
        viewModelScope.launch {
            // Android keeps detailed events for about a week; older days show sessions only.
            usageDays.value = runCatching { usage.loadDays(t.minusDays(9), t) }.getOrDefault(emptyMap())
        }
    }

    fun select(date: LocalDate) {
        _selected.value = minOf(date, LocalDate.now())
    }

    fun shiftDay(by: Long) = select(_selected.value.plusDays(by))

    private fun stats(
        date: LocalDate,
        today: LocalDate,
        all: List<FocusSessionEntity>,
        usageMap: Map<LocalDate, DayUsage>,
    ): DayStats {
        val sessions = all.filter { it.day == date.toString() }
        val completed = sessions.filter { it.counts }
        val u = usageMap[date]?.takeIf { it.hadEvents }
        return DayStats(
            date = date,
            sessions = sessions,
            completed = completed.size,
            focusMs = completed.sumOf { it.actualMs },
            screenTimeMs = u?.screenTimeMs,
            pickups = u?.unlocks?.takeIf { usage.unlocksAvailable },
            isFuture = date.isAfter(today),
        )
    }
}
