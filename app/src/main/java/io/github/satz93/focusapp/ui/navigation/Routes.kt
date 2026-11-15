package io.github.satz93.focusapp.ui.navigation

object Routes {
    // Onboarding graph
    const val ONBOARDING = "onboarding"
    const val WELCOME = "onboarding/welcome"
    const val USAGE_ACCESS = "onboarding/usage_access"
    const val UNLOCKS = "onboarding/unlocks"
    const val TOP_APPS = "onboarding/top_apps"
    const val SCREEN_TIME_GOAL = "onboarding/screen_time_goal"
    const val FOCUS_TARGET = "onboarding/focus_target"
    const val DISTRACTING_APPS = "onboarding/distracting_apps"

    // Main
    const val HOME = "home"
    const val PROGRESS = "progress"
    const val FOCUS_TIMER = "focus_timer"
    const val SESSION_COMPLETE = "session_complete"

    val bottomBarRoutes = setOf(HOME, PROGRESS)
}
