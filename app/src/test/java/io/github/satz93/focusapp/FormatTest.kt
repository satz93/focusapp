package io.github.satz93.focusapp

import io.github.satz93.focusapp.data.settings.GuardConfig
import io.github.satz93.focusapp.ui.util.formatClock
import io.github.satz93.focusapp.ui.util.formatDuration
import org.junit.Assert.assertEquals
import org.junit.Test

class FormatTest {
    @Test fun durations() {
        assertEquals("3h 30m", formatDuration(210))
        assertEquals("4h", formatDuration(240))
        assertEquals("45m", formatDuration(45))
        assertEquals("2h 15m", formatDuration(3 * 45))
    }

    @Test fun clock() {
        assertEquals("45:00", formatClock(45 * 60))
        assertEquals("0:04", formatClock(4))
        assertEquals("0:00", formatClock(-3))
    }

    @Test fun challengeEscalation() {
        assertEquals(30, GuardConfig.waitSecondsForAttempt(0))
        assertEquals(60, GuardConfig.waitSecondsForAttempt(1))
        assertEquals(120, GuardConfig.waitSecondsForAttempt(2))
        assertEquals(120, GuardConfig.waitSecondsForAttempt(7))
    }
}
