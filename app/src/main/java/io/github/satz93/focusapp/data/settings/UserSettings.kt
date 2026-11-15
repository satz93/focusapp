package io.github.satz93.focusapp.data.settings

enum class LimitMode { NUDGE, LOCK }

/** User choices from onboarding. Defaults match the "Suggested" options in the design. */
data class UserSettings(
    val screenTimeGoalMin: Int = 210, // 3h 30m
    val sessionsPerDay: Int = 3,
    val sessionLengthMin: Int = 45,
    val limitMode: LimitMode = LimitMode.NUDGE,
    val onboardingDone: Boolean = false,
)
