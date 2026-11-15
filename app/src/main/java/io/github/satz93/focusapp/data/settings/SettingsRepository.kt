package io.github.satz93.focusapp.data.settings

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.emptyPreferences
import androidx.datastore.preferences.core.intPreferencesKey
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.map
import java.io.IOException

private val Context.settingsStore: DataStore<Preferences> by preferencesDataStore(name = "settings")

class SettingsRepository(context: Context) {

    private val store = context.applicationContext.settingsStore

    private object Keys {
        val ScreenTimeGoal = intPreferencesKey("screen_time_goal_min")
        val SessionsPerDay = intPreferencesKey("sessions_per_day")
        val SessionLength = intPreferencesKey("session_length_min")
        val LimitMode = stringPreferencesKey("limit_mode")
        val OnboardingDone = booleanPreferencesKey("onboarding_done")
    }

    val settings: Flow<UserSettings> = store.data
        .catch { e -> if (e is IOException) emit(emptyPreferences()) else throw e }
        .map { p ->
            val defaults = UserSettings()
            UserSettings(
                screenTimeGoalMin = p[Keys.ScreenTimeGoal] ?: defaults.screenTimeGoalMin,
                sessionsPerDay = p[Keys.SessionsPerDay] ?: defaults.sessionsPerDay,
                sessionLengthMin = p[Keys.SessionLength] ?: defaults.sessionLengthMin,
                limitMode = p[Keys.LimitMode]?.let { runCatching { LimitMode.valueOf(it) }.getOrNull() }
                    ?: defaults.limitMode,
                onboardingDone = p[Keys.OnboardingDone] ?: defaults.onboardingDone,
            )
        }

    suspend fun setScreenTimeGoal(minutes: Int) = store.edit { it[Keys.ScreenTimeGoal] = minutes }

    suspend fun setFocusTarget(sessionsPerDay: Int, sessionLengthMin: Int) = store.edit {
        it[Keys.SessionsPerDay] = sessionsPerDay
        it[Keys.SessionLength] = sessionLengthMin
    }

    suspend fun setLimitMode(mode: LimitMode) = store.edit { it[Keys.LimitMode] = mode.name }

    suspend fun setOnboardingDone(done: Boolean) = store.edit { it[Keys.OnboardingDone] = done }
}
