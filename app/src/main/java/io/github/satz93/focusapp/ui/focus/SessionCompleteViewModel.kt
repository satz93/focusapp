package io.github.satz93.focusapp.ui.focus

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import io.github.satz93.focusapp.data.db.FocusSessionDao
import io.github.satz93.focusapp.data.db.FocusSessionEntity
import io.github.satz93.focusapp.data.focus.FocusNotifications
import io.github.satz93.focusapp.data.focus.FocusSessionManager
import io.github.satz93.focusapp.data.settings.SettingsRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import java.time.LocalDate

data class CompleteUiState(
    val session: FocusSessionEntity? = null,
    val doneToday: Int = 0,
    val target: Int = 3,
    val focusTodayMs: Long = 0L,
)

class SessionCompleteViewModel(
    manager: FocusSessionManager,
    dao: FocusSessionDao,
    settings: SettingsRepository,
    context: android.content.Context,
) : ViewModel() {

    private val session = MutableStateFlow<FocusSessionEntity?>(null)

    init {
        val id = manager.pendingComplete.value
        manager.consumePendingComplete()
        FocusNotifications.cancelComplete(context.applicationContext)
        viewModelScope.launch {
            session.value = (id?.let { dao.get(it) }) ?: dao.latestCompleted()
        }
    }

    val state: StateFlow<CompleteUiState> = combine(
        session,
        dao.observeDay(LocalDate.now().toString()),
        settings.settings,
    ) { s, today, prefs ->
        val completed = today.filter { it.counts }
        CompleteUiState(
            session = s,
            doneToday = completed.size,
            target = prefs.sessionsPerDay,
            focusTodayMs = completed.sumOf { it.actualMs },
        )
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), CompleteUiState())
}
