package io.github.satz93.focusapp.di

import android.content.Context
import io.github.satz93.focusapp.data.apps.AppInfoProvider
import io.github.satz93.focusapp.data.db.AppDatabase
import io.github.satz93.focusapp.data.focus.FocusSessionManager
import io.github.satz93.focusapp.data.settings.SettingsRepository
import io.github.satz93.focusapp.data.usage.UsageRepository
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob

/**
 * Manual dependency injection. One instance lives on [io.github.satz93.focusapp.FocusApplication].
 */
class AppContainer(context: Context) {
    /** Lives as long as the process; for work that must outlive a screen (e.g. saving a session). */
    val appScope = CoroutineScope(SupervisorJob() + Dispatchers.Default)

    val database = AppDatabase.create(context)
    val settingsRepository = SettingsRepository(context)
    val appInfoProvider = AppInfoProvider(context)
    val usageRepository = UsageRepository(context, appInfoProvider)
    val focusSessionManager = FocusSessionManager(
        context = context,
        dao = database.focusSessionDao(),
        usage = usageRepository,
        settings = settingsRepository,
        scope = appScope,
    )
}
