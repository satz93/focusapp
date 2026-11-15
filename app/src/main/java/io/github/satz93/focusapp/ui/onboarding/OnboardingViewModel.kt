package io.github.satz93.focusapp.ui.onboarding

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import io.github.satz93.focusapp.data.settings.SettingsRepository
import kotlinx.coroutines.launch

/**
 * Holds onboarding choices across steps. Milestones 2–3 add the usage numbers,
 * goal, focus target and distracting-app selections here.
 */
class OnboardingViewModel(
    private val settings: SettingsRepository,
) : ViewModel() {

    fun finish(onDone: () -> Unit) {
        viewModelScope.launch {
            settings.setOnboardingDone(true)
            onDone()
        }
    }
}
