package io.github.satz93.focusapp.data.focus

/**
 * A running (or paused) focus session. Wall-clock based so it can be saved and restored if
 * Android kills the app process mid-session.
 */
data class ActiveSession(
    val startedAt: Long,
    val plannedMin: Int,
    val sessionNumber: Int,
    val pausedTotalMs: Long = 0L,
    /** Non-null while paused. */
    val pausedAt: Long? = null,
) {
    val plannedMs: Long get() = plannedMin * 60_000L
    val isPaused: Boolean get() = pausedAt != null

    fun elapsedMs(now: Long): Long = ((pausedAt ?: now) - startedAt - pausedTotalMs).coerceIn(0L, plannedMs)
    fun remainingMs(now: Long): Long = plannedMs - elapsedMs(now)
    fun progress(now: Long): Float = if (plannedMs == 0L) 1f else elapsedMs(now).toFloat() / plannedMs

    /** Wall-clock time the session ends if it keeps running. */
    fun endsAt(now: Long): Long = now + remainingMs(now)

    fun paused(now: Long): ActiveSession = if (isPaused) this else copy(pausedAt = now)
    fun resumed(now: Long): ActiveSession =
        pausedAt?.let { copy(pausedTotalMs = pausedTotalMs + (now - it), pausedAt = null) } ?: this
}
