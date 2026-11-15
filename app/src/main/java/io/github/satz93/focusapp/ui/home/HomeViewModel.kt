package io.github.satz93.focusapp.ui.home

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import io.github.satz93.focusapp.data.settings.SettingsRepository
import io.github.satz93.focusapp.data.settings.UserSettings
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn

data class HomeUiState(
    val sessionsDone: Int = 0,
    val sessionsTarget: Int = UserSettings().sessionsPerDay,
    val sessionLengthMin: Int = UserSettings().sessionLengthMin,
    val screenTimeGoalMin: Int = UserSettings().screenTimeGoalMin,
)

/** Milestone 4 adds today's sessions, screen time, pickups and per-app usage. */
class HomeViewModel(settings: SettingsRepository) : ViewModel() {
    val state: StateFlow<HomeUiState> = settings.settings
        .map {
            HomeUiState(
                sessionsTarget = it.sessionsPerDay,
                sessionLengthMin = it.sessionLengthMin,
                screenTimeGoalMin = it.screenTimeGoalMin,
            )
        }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), HomeUiState())
}
