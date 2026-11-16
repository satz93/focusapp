package io.github.satz93.focusapp.di

import android.content.Context
import io.github.satz93.focusapp.data.apps.AppInfoProvider
import io.github.satz93.focusapp.data.settings.SettingsRepository
import io.github.satz93.focusapp.data.usage.UsageRepository

/**
 * Manual dependency injection. One instance lives on [io.github.satz93.focusapp.FocusApplication].
 */
class AppContainer(context: Context) {
    val settingsRepository = SettingsRepository(context)
    val appInfoProvider = AppInfoProvider(context)
    val usageRepository = UsageRepository(context, appInfoProvider)
}
