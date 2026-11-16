package io.github.satz93.focusapp.ui.onboarding

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import io.github.satz93.focusapp.data.apps.AppInfoProvider
import io.github.satz93.focusapp.data.settings.GuardConfig
import io.github.satz93.focusapp.data.settings.SettingsRepository
import io.github.satz93.focusapp.data.usage.DayPart
import io.github.satz93.focusapp.data.usage.UsageMath
import io.github.satz93.focusapp.data.usage.UsageRepository
import io.github.satz93.focusapp.data.usage.UsageSummary
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

data class TopApp(val pkg: String, val label: String, val avgMs: Long, val share: Float)

data class UsageInsights(
    val summary: UsageSummary,
    val minutesBetweenUnlocks: Int?,
    val busiestPart: DayPart?,
    val topApps: List<TopApp>,
    val otherAppsMs: Long,
    val topShare: Float,
)

sealed interface UsageState {
    data object Idle : UsageState
    data object Loading : UsageState
    data class Ready(val insights: UsageInsights) : UsageState
}

/** Holds onboarding data and choices across steps (scoped to the onboarding nav graph). */
class OnboardingViewModel(
    private val settings: SettingsRepository,
    private val usage: UsageRepository,
    private val apps: AppInfoProvider,
) : ViewModel() {

    private val _usage = MutableStateFlow<UsageState>(UsageState.Idle)
    val usageState: StateFlow<UsageState> = _usage.asStateFlow()

    fun hasUsageAccess(): Boolean = usage.hasUsageAccess()

    fun loadUsageIfNeeded() {
        if (_usage.value != UsageState.Idle || !usage.hasUsageAccess()) return
        _usage.value = UsageState.Loading
        viewModelScope.launch {
            val summary = runCatching { usage.loadSummary() }.getOrNull()
            if (summary == null) {
                _usage.value = UsageState.Idle
                return@launch
            }
            _usage.value = UsageState.Ready(buildInsights(summary))
        }
    }

    private suspend fun buildInsights(s: UsageSummary): UsageInsights {
        val total = s.avgScreenTimeMs.coerceAtLeast(1)
        val top = withContext(Dispatchers.Default) {
            s.avgAppMs.take(3).map { (pkg, ms) -> TopApp(pkg, apps.label(pkg), ms, ms.toFloat() / total) }
        }
        val topMs = top.sumOf { it.avgMs }
        val parts = s.avgUnlocksByPart
        val busiest = if (parts.sum() == 0) null else DayPart.entries[parts.indexOf(parts.max())]
        return UsageInsights(
            summary = s,
            minutesBetweenUnlocks = UsageMath.minutesBetweenUnlocks(s.avgUnlocks, GuardConfig.WAKING_HOURS),
            busiestPart = busiest,
            topApps = top,
            otherAppsMs = (s.avgScreenTimeMs - topMs).coerceAtLeast(0),
            topShare = topMs.toFloat() / total,
        )
    }

    fun finish(onDone: () -> Unit) {
        viewModelScope.launch {
            settings.setOnboardingDone(true)
            onDone()
        }
    }
}
