package io.github.satz93.focusapp.data.settings

/**
 * Open product questions from the brief, kept as constants for now so they are easy to tune.
 */
object GuardConfig {
    /** Wait time for the unlock challenge, escalating with each use on the same day (30s, 60s, 2 min). */
    val challengeWaitSeconds: List<Int> = listOf(30, 60, 120)

    /** Whether "End session early" asks for the same challenge as opening a blocked app. */
    const val END_EARLY_REQUIRES_CHALLENGE = true

    /** How long a distracting app stays open after the challenge is completed. */
    const val UNLOCK_MINUTES = 5

    /** "5 more minutes" on the limit sheet (Nudge mode only, once per app per day). */
    const val EXTRA_TIME_MINUTES = 5

    /** Waking hours assumed for "once every X minutes you're awake". */
    const val WAKING_HOURS = 16

    /** Notch bar turns amber at this share of the limit; home cards use 0.85. */
    const val NOTCH_WARN_FRACTION = 0.8f
    const val HOME_WARN_FRACTION = 0.85f

    fun waitSecondsForAttempt(attemptToday: Int): Int =
        challengeWaitSeconds[attemptToday.coerceIn(0, challengeWaitSeconds.lastIndex)]
}
