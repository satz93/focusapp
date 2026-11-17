package io.github.satz93.focusapp.ui.focus

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import io.github.satz93.focusapp.data.db.FocusSessionDao
import io.github.satz93.focusapp.data.focus.ActiveSession
import io.github.satz93.focusapp.data.focus.FocusSessionManager
import io.github.satz93.focusapp.data.settings.SettingsRepository
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import java.time.LocalDate

data class ReadyInfo(
    val nextNumber: Int = 1,
    val target: Int = 3,
    val lengthMin: Int = 45,
)

class FocusTimerViewModel(
    private val manager: FocusSessionManager,
    settings: SettingsRepository,
    dao: FocusSessionDao,
    /** Session changes run here so leaving the screen can't cancel a save. */
    private val appScope: CoroutineScope,
) : ViewModel() {

    val active: StateFlow<ActiveSession?> = manager.active
    val pendingComplete: StateFlow<Long?> = manager.pendingComplete

    /** Length chosen on the ready screen; null = use the default from settings. */
    private val chosenLength = MutableStateFlow<Int?>(null)

    val ready: StateFlow<ReadyInfo> = combine(
        settings.settings,
        dao.observeDay(LocalDate.now().toString()),
        chosenLength,
    ) { s, today, chosen ->
        ReadyInfo(
            nextNumber = today.count { it.counts } + 1,
            target = s.sessionsPerDay,
            lengthMin = chosen ?: s.sessionLengthMin,
        )
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), ReadyInfo())

    fun chooseLength(min: Int) {
        chosenLength.value = min
    }

    fun start() {
        val minutes = ready.value.lengthMin
        appScope.launch { manager.start(minutes) }
    }

    fun pause() {
        appScope.launch { manager.pause() }
    }

    fun resume() {
        appScope.launch { manager.resume() }
    }

    fun endEarly(onDone: () -> Unit) {
        appScope.launch {
            manager.endEarly()
            viewModelScope.launch { onDone() }
        }
    }

    fun tick() {
        appScope.launch { manager.completeIfDue() }
    }
}
