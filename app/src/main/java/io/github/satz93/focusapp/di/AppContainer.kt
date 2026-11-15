package io.github.satz93.focusapp.di

import android.content.Context
import io.github.satz93.focusapp.data.settings.SettingsRepository

/**
 * Manual dependency injection. One instance lives on [io.github.satz93.focusapp.FocusApplication].
 * Room, usage stats and the session repositories get added here in later milestones.
 */
class AppContainer(context: Context) {
    val settingsRepository = SettingsRepository(context)
}
